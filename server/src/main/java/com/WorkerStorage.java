package com;

import com.MonitoringService.MonitorInfo;
import com.messageprotocol.ClientTask;
import io.netty.channel.Channel;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkerStorage {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(WorkerStorage.class);

    private final Map<InetSocketAddress, WorkerInfo> allWorkersMap;

    private BlockingQueue<WorkerTask> workerTasksQueue = new LinkedBlockingQueue<>();

    private static final WorkerStorage INSTANCE = new WorkerStorage();


    public WorkerTask pollWorkerTask() {
        try {
            return workerTasksQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }


    public List<WorkerInfo> getWorkers() {
        return new ArrayList<>(allWorkersMap.values());
    }


    public void addWorkerTask(WorkerTask workerTask) {
        workerTasksQueue.add(workerTask);
    }

    private WorkerStorage() {
        allWorkersMap = new TreeMap<>(Comparator.comparing(InetSocketAddress::getHostString));
        scheduleWorkerCheck(10000, 5000);
    }

    public void scheduleWorkerCheck(long heartbeatTimeoutMillis, long checkIntervalMillis) {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                checkHeartbeats(heartbeatTimeoutMillis);
            }
        }, checkIntervalMillis, checkIntervalMillis);
    }

    public void checkHeartbeats(long heartbeatTimeoutMillis) {
        long currentTime = System.currentTimeMillis();
        List<InetSocketAddress> toRemove = new ArrayList<>();
        for (Map.Entry<InetSocketAddress, WorkerInfo> entry : allWorkersMap.entrySet()) {
            WorkerInfo worker = entry.getValue();
            if (currentTime - worker.getLastHeartbeatTime() > heartbeatTimeoutMillis) {
                LOGGER.warn("Worker at address {} timed out due to missing heartbeat.", worker.getAddress());
                toRemove.add(entry.getKey());
            }
        }
        LOGGER.info("Completed heartbeat check. Removing {} timed-out workers.", toRemove.size());
        for (InetSocketAddress address : toRemove) {
            allWorkersMap.remove(address);
        }
    }



    public void registerWorker(Channel channel, InetSocketAddress socketAddress) {
        LOGGER.info("Registering new worker from address: {}", socketAddress.getAddress().getHostAddress());
        WorkerInfo worker = new WorkerInfo(WorkerStatus.FREE, channel, socketAddress, System.currentTimeMillis());
        allWorkersMap.put(socketAddress, worker);
    }

    public void updateHeartbeat(InetSocketAddress address) {
        WorkerInfo worker = allWorkersMap.get(address);
        if (worker != null) {
            worker.setLastHeartbeatTime(System.currentTimeMillis());
            LOGGER.info("Updated heartbeat time for worker at address: {}", address);
        } else {
            LOGGER.warn("Worker not found for address: {}", address);
        }
    }


    public void updateWorkerMonitoringInfo(InetSocketAddress address,MonitorInfo monitorInfo) {
        WorkerInfo worker = allWorkersMap.get(address);
        if (worker != null) {
            WorkerInfo updatedWorker = new WorkerInfo(
                    worker.getAddress(),
                    worker.getStatus(),
                    worker.getChannel(),
                    worker.getLastHeartbeatTime(),
                    monitorInfo.usedMemory(),
                    monitorInfo.cpuLoad()
            );
            allWorkersMap.put(address, updatedWorker);
            LOGGER.info("Updated monitoring info for worker at address: {}", address);
        } else {
            LOGGER.warn("Worker not found for address: {}", address);
        }
    }



    public void unregisterWorker(InetSocketAddress address) {
        LOGGER.info("Unregistering worker with address: {}", address);
        allWorkersMap.remove(address);
    }


    public static WorkerStorage getInstance() {
        return INSTANCE;
    }

    public static class WorkerInfo {
        private final InetSocketAddress address;
        private WorkerStatus status;
        private final Channel channel;
        private Long lastHeartbeatTime;
        private long memoryUsage;
        private double cpuLoad;




        public WorkerInfo(WorkerStatus status, Channel channel, InetSocketAddress address, Long lastHeartbeatTime) {
            this.status = status;
            this.channel = channel;
            this.address = address;
            this.lastHeartbeatTime = lastHeartbeatTime;
        }


        public long getMemoryUsage() {
            return memoryUsage;
        }

        public double getCpuLoad() {
            return cpuLoad;
        }

        public WorkerInfo(InetSocketAddress address, WorkerStatus status, Channel channel, Long lastHeartbeatTime,
                long memoryUsage, double cpuLoad) {
            this.address = address;
            this.status = status;
            this.channel = channel;
            this.lastHeartbeatTime = lastHeartbeatTime;
            this.memoryUsage = memoryUsage;
            this.cpuLoad = cpuLoad;
        }



        public Long getLastHeartbeatTime() {
            return lastHeartbeatTime;
        }

        public void setLastHeartbeatTime(Long lastHeartbeatTime) {
            this.lastHeartbeatTime = lastHeartbeatTime;
        }

        public WorkerStatus getStatus() {
            return status;
        }

        public InetSocketAddress getAddress() {
            return address;
        }



        public Channel getChannel() {
            return channel;
        }

        public void sendTaskToWorker(ClientTask task) {
            this.status = WorkerStatus.BUSY;
            channel.writeAndFlush(task);
            LOGGER.info("Sent task to worker at address: {} and set status to {}", address, status);
        }


        public int getCurrentLoad() {
            return (int) (cpuLoad * 100 + memoryUsage / (1024 * 1024)); 
        }
    }

    public enum WorkerStatus {
        FREE,
        BUSY
    }


    public record WorkerTask(
            WorkerInfo worker,
            ClientTask task
    ) {
    }


}
