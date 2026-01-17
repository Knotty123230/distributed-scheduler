package com;

import javax.sound.midi.SoundbankResource;
import java.util.Map;

public class Test implements TaskApi{
    @Override
    public void run(Map<String, String> params) {
        System.out.println("Hello from Test Task!");
        System.out.println("Parameters:");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Emulating CPU load...");
        emulateCpuLoad();
    }

    public void emulateCpuLoad() {
        while (true) {
            double x = Math.random() * Math.random();
        }
    }
}
