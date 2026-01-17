package com;

import org.slf4j.Logger;

public class TaskRunner implements ITaskRunner {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TaskRunner.class);

    private TaskRunner() {
    }


    private void runTask(Runnable task) {
        new Thread(task).start();
    }

    public void runTask(Task task) {
        ClassLoader byteClassLoader = new ByteClassLoader(task.getData());
        Class<?> loadedClass;
        try {
            loadedClass = byteClassLoader.loadClass(task.getClassPath());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            LOGGER.error("ClassNotFoundException while loading class {}: {}", task.getClassPath(), e.getMessage());
            return;
        }

        if (loadedClass == null) {
            LOGGER.error("Failed to load class: {}", task.getClassPath());
            return;
        }

        if (TaskApi.class.isAssignableFrom(loadedClass)) {
            TaskApi taskInstance;
            try {
                taskInstance = (TaskApi) loadedClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error("Exception while creating instance of class {}: {}", task.getClassPath(), e.getMessage());
                return;
            }
            Runnable runnableTask = () -> {
                try {
                    taskInstance.run(task.getParams());
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.error("Exception while executing task {}: {}", task.getClassPath(), e.getMessage());
                }
            };
            runTask(runnableTask);
        } else {
            LOGGER.error("Loaded class {} does not implement TaskApi interface", task.getClassPath());
            return;
        }
        LOGGER.info("Task {} has been started", task.getClassPath());
    }


    public static class TaskRunnerProvider {
        private static final TaskRunner INSTANCE = new TaskRunner();

        private TaskRunnerProvider() {
        }

        public static TaskRunner getInstance() {
            return INSTANCE;
        }
    }
}
