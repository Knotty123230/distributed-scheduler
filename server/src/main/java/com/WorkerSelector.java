package com;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;

import com.WorkerStorage.WorkerInfo;

public class WorkerSelector {
    private final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(WorkerSelector.class);
    private final WorkerStorage workerStorage;

    public WorkerSelector( WorkerStorage workerStorage) {
        this.workerStorage = workerStorage;
    }




    public  WorkerInfo selectWorker() {
        List<WorkerInfo> workers = workerStorage.getWorkers();
        if (workers.isEmpty()) {
            LOGGER.error("No available workers to select from.");
            return null;
        }
        int size = workers.size();
        if (size < 2) {
            LOGGER.warn("Not enough workers available to select from. Available workers: {}", size);
            return workers.get(0);
        }
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomFirst = random.nextInt(size);
        int randomSecond = random.nextInt(size);

        WorkerInfo workerFirst = workers.get(randomFirst);
        WorkerInfo workerSecond = workers.get(randomSecond);
        LOGGER.info("Comparing workers {} (load: {}) and {} (load: {})", workerFirst.getAddress(), workerFirst.getCurrentLoad(), workerSecond.getAddress(), workerSecond.getCurrentLoad());
        if (workerFirst.getCurrentLoad() <= workerSecond.getCurrentLoad()) {
            return workerFirst;
        } else {
            return workerSecond;
        }
    }
}
