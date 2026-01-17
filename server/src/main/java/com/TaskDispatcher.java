package com;

import com.WorkerStorage.WorkerInfo;
import com.messageprotocol.ClientTask;
import org.slf4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TaskDispatcher implements Runnable {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TaskDispatcher.class);
    private final TaskEnvironment taskEnvironment;
    private final WorkerSelector workerSelector;
    private static final TaskDispatcher INSTANCE = new TaskDispatcher();
    private final WorkerStorage workerStorage;

    private TaskDispatcher(){
       taskEnvironment = new TaskEnvironment();
       workerStorage = WorkerStorage.getInstance();
       workerSelector = new WorkerSelector(workerStorage);
    }

    public void addTask(ClientTask task){
        taskEnvironment.addTask(task);
    }

    public static TaskDispatcher getInstance(){
        return INSTANCE;
    }


    public void process() {
        BlockingQueue<ClientTask> taskQueue = taskEnvironment.getTaskQueue();
        WorkerInfo freeWorker = workerSelector.selectWorker();
        
        while (freeWorker == null) {
            LOGGER.warn("No available workers. Waiting to select a free worker...");
            try {
                Thread.sleep(1000); 
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("Interrupted while waiting for available workers: {}", e.getMessage());
            }
            freeWorker = workerSelector.selectWorker();
        }

        LOGGER.info("TasksProcessor started processing tasks worker {}", freeWorker.getAddress());
        while (true) {
            try {
                ClientTask task = taskQueue.take();
                LOGGER.debug("Add task: {} for worker {}", task, freeWorker.getAddress());
                workerStorage.addWorkerTask(new WorkerStorage.WorkerTask(freeWorker, task));
                freeWorker = workerSelector.selectWorker();
                LOGGER.info("Assigned task to worker {}", freeWorker.getAddress());
            } catch (InterruptedException e) {
                e.printStackTrace();
                LOGGER.error("TasksProcessor interrupted while processing tasks: {}", e.getMessage());
            }
        }


    }

    @Override
    public void run() {
        process();
    }


    public static class TaskEnvironment {
        private final BlockingQueue<ClientTask> taskQueue;


        public TaskEnvironment() {
            taskQueue = new ArrayBlockingQueue<>(System.getenv("MAX_TASKS_QUEUE_SIZE") != null ? Integer.parseInt(System.getenv("MAX_TASKS_QUEUE_SIZE")) : 1000);
        }


        public BlockingQueue<ClientTask> getTaskQueue() {
            return taskQueue;
        }

        public void addTask(ClientTask task) {
            taskQueue.add(task);
        }


    }
}
