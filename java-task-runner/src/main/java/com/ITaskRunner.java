package com;

import java.util.Map;

public interface ITaskRunner {
     void runTask(TaskRunner.Task task);

    interface Task {
        String getClassPath();

        Map<String, String> getParams();

        byte[] getData();

    }
}
