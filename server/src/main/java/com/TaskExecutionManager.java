package com;

import com.WorkerStorage.WorkerTask;

public class TaskExecutionManager implements Runnable{
    private final WorkerStorage workerStorage;


    public TaskExecutionManager(){
        workerStorage = WorkerStorage.getInstance();
    }


    @Override
    public void run() {
        WorkerTask pollWorkerTask = workerStorage.pollWorkerTask();
        while (pollWorkerTask != null) {
            // Process the worker task here
            System.out.println("Processing task for worker: " + pollWorkerTask.worker().getAddress());
            pollWorkerTask.worker().sendTaskToWorker(pollWorkerTask.task());
            // Fetch the next task
            pollWorkerTask = workerStorage.pollWorkerTask();

        }
    }

}
