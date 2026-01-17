package com.messageprotocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;

public class MapToBytesConverter {

    public static byte[] convert(Map<String, String> map) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(map);
            objectOutputStream.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();

            throw new RuntimeException("Failed to convert map to bytes", e);
        }


    }
}
