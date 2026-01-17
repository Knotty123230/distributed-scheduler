package com;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final TaskDispatcher tasksProcessor = TaskDispatcher.getInstance();
    static void main() {
        try {
            new NettyServer(8080).run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
