package com;

import com.messageprotocol.ClientTask;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final TaskDispatcher tasksProcessor = TaskDispatcher.getInstance();
    private final ITaskRunner taskRunner;
    private final MonitoringService monitoringService;

    public ClientHandler(ITaskRunner taskRunner, MonitoringService monitoringService) {
        this.taskRunner = taskRunner;
        this.monitoringService = monitoringService;
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (!(msg instanceof ClientTask clientTask)) {
            System.out.println("Received unknown message type");
            return;
        }

        System.out.println("Received ClientTask: " + clientTask);

        tasksProcessor.addTask(clientTask);
    }
}
