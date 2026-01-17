package com.messageprotocol;

import com.TaskRunner;

import java.util.Arrays;
import java.util.Map;

public class ClientTask implements TaskRunner.Task {
    private final String pathToClass;
    private final byte[] classData;
    private final Map<String, String> params;

    public ClientTask(String pathToClass, byte[] classData, Map<String, String> params) {
        this.pathToClass = pathToClass;
        this.classData = classData;
        this.params = params;
    }


    public String getPathToClass() {
        return pathToClass;
    }
    public byte[] getClassData() {
        return classData;
    }

    @Override
    public String getClassPath() {
        return pathToClass;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public byte[] getData() {
        return classData;
    }

    @Override
    public String toString() {
        return "ClientTask{" +
                "classData=" + Arrays.toString(classData) +
                ", pathToClass='" + pathToClass + '\'' +
                ", params=" + params +
                '}';
    }

}
