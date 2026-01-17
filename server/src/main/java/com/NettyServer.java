package com;

import com.messageprotocol.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyServer {

    private final int serverPort;
    private final ITaskRunner taskRunner;
    private int workerPort = TcpConstants.WORKER_PORT;
    private final MonitoringService monitoringService;


    public NettyServer(int serverPort) {
        TaskDispatcher tasksProcessor = TaskDispatcher.getInstance();
        new Thread(tasksProcessor).start();
        TaskExecutionManager taskProcessor = new TaskExecutionManager();
        new Thread(taskProcessor).start();
        this.taskRunner = TaskRunner.TaskRunnerProvider.getInstance();
        this.serverPort = serverPort;
        this.monitoringService = MonitoringService.getInstance();
    }

    public NettyServer(int serverPort, int workerPort) {
        this.serverPort = serverPort;
        this.workerPort = workerPort;
        this.taskRunner = TaskRunner.TaskRunnerProvider.getInstance();
        this.monitoringService = MonitoringService.getInstance();
    }

    public void run() throws Exception {
        new Thread(() -> {
            try {
                runServerForWorkers();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
        new Thread(() -> {
            try {
                runServerForClients();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void runServerForClients() throws InterruptedException {
        MultiThreadIoEventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        MultiThreadIoEventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new MessageDecoderNetty(), new ClientTaskDecoder(), new MessageEncoderEntity(), new ClientTaskEncoder(), new ClientHandler(taskRunner, monitoringService));
                        }
                    });
            ChannelFuture future = bootstrap.bind(serverPort).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private void runServerForWorkers() throws InterruptedException {
        MultiThreadIoEventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        MultiThreadIoEventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new MessageDecoderNetty(), new ClientTaskDecoder(), new MessageEncoderEntity(), new ClientTaskEncoder(), new IdleStateHandler(0, 5, 0), new ServerHandler());
                        }
                    });
            ChannelFuture future = bootstrap.bind(workerPort).sync();
            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


}
