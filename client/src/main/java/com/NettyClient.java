package com;

import com.messageprotocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

public class NettyClient {

    private final ITaskRunner taskRunner;
    private final MonitoringService monitoringService;
    private int serverPort = TcpConstants.WORKER_PORT;
    private final String serverHost;

    public NettyClient(int serverPort, String serverHost) {
        monitoringService = MonitoringService.getInstance();
        this.serverPort = serverPort;
        this.serverHost = serverHost;
        taskRunner = TaskRunner.TaskRunnerProvider.getInstance();
    }

    public NettyClient(String serverHost) {
        this.serverHost = serverHost;
        taskRunner = TaskRunner.TaskRunnerProvider.getInstance();
        monitoringService = MonitoringService.getInstance();
    }

    public void run() throws Exception {

        MultiThreadIoEventLoopGroup eventExecutors = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(eventExecutors)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000 * 120)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new IdleStateHandler(0, 5, 0), new MessageDecoderNetty(), new ClientTaskDecoder(), new MessageEncoderEntity(), new ClientTaskEncoder(), new ClientHandler(taskRunner, monitoringService));
                        }
                    });
            ChannelFuture future = bootstrap.connect(serverHost, serverPort).sync();
            future.channel().closeFuture().sync();
        } finally {
            eventExecutors.shutdownGracefully();
        }
    }


}
