package com;

import java.nio.ByteBuffer;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;

public class MonitoringService {
    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(MonitoringService.class);
    private volatile long[] previousCpuTicks = null;
    private final SystemInfo systemInfo;
    private final CentralProcessor processor;

    private MonitoringService() {
        this.systemInfo = new SystemInfo();
        this.processor = systemInfo.getHardware().getProcessor();
        this.previousCpuTicks = processor.getSystemCpuLoadTicks();
        checkTicks();
    }

    private void checkTicks() {
        // Краще використовувати ScheduledExecutorService замість Timer (більш сучасний підхід)
        java.util.concurrent.ScheduledExecutorService scheduler = 
            java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            });

        scheduler.scheduleAtFixedRate(() -> {
            previousCpuTicks = processor.getSystemCpuLoadTicks();
        }, 3, 3, java.util.concurrent.TimeUnit.SECONDS);
    }

    public static MonitoringService getInstance() {
        return MonitoringServiceHolder.INSTANCE;
    }

    private static class MonitoringServiceHolder {
        private static final MonitoringService INSTANCE = new MonitoringService();
    }

    

    public MonitorInfo startMonitoring() {
        double cpuLoad = processor.getSystemCpuLoadBetweenTicks(previousCpuTicks);
        
        var hardware = systemInfo.getHardware();
        var memory = hardware.getMemory();
        
        long totalMemory = memory.getTotal();
        long freeMemory = memory.getAvailable();
        long usedMemory = totalMemory - freeMemory;

        return new MonitorInfo(
                cpuLoad,
                usedMemory,
                freeMemory,
                totalMemory,
                processor.getLogicalProcessorCount()
        );
    }


    public record MonitorInfo(
            // CPU load as a value between 0.0 and 1.0 1.0 == 100%
            double cpuLoad,
            long usedMemory,
            long freeMemory,
            long totalMemory,
            int availableProcessors
    ) {

        public byte[] toBytes() {
            ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES + Long.BYTES * 3 + Integer.BYTES);
            buffer.putDouble(cpuLoad);
            buffer.putLong(usedMemory);
            buffer.putLong(freeMemory);
            buffer.putLong(totalMemory);
            buffer.putInt(availableProcessors);
            return buffer.array();
        }

        public static MonitorInfo fromBytes(byte[] bytes) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            double cpuLoad = buffer.getDouble();
            long usedMemory = buffer.getLong();
            long freeMemory = buffer.getLong();
            long totalMemory = buffer.getLong();
            int availableProcessors = buffer.getInt();
            return new MonitorInfo(cpuLoad, usedMemory, freeMemory, totalMemory, availableProcessors);
        }
    }

}
