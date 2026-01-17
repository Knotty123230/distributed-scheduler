package com;

public class Main {
    static void main() {
        try {
            new NettyClient("localhost").run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
