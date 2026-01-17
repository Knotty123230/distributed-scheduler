package com;

import com.MonitoringService.MonitorInfo;
import com.messageprotocol.ClientTask;
import com.messageprotocol.Message;
import com.messageprotocol.MessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;

import java.net.InetSocketAddress;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final ITaskRunner taskRunner;
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ClientHandler.class);
    private final MonitoringService monitoringService;

    public ClientHandler(ITaskRunner taskRunner, MonitoringService monitoringService) {
        this.monitoringService = monitoringService;
        this.taskRunner = taskRunner;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof Message message) {
            if(message.getMessageType() == MessageType.PING) {
                LOGGER.info("Received ping from server: {}", ctx.channel().remoteAddress());
                ctx.writeAndFlush(Message.contentFromString("pong", MessageType.PONG));
            }
        }

        if (msg instanceof ClientTask clientTask) {
            LOGGER.info("Received ClientTask on client: {}", clientTask);
            taskRunner.runTask(clientTask);
        }

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("Connected to server: {}", ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();

        ctx.close();
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {

                MonitorInfo monitorInfo = monitoringService.startMonitoring();
                LOGGER.info("Current Monitor Info: {} for {}", monitorInfo, ctx.channel().remoteAddress());
                Message monitorMessage = Message.contentFromBytes(monitorInfo.toBytes(), MessageType.MONITOR_INFO);
                Message pingMessage = Message.contentFromString("ping", MessageType.PING);
                ctx.writeAndFlush(pingMessage);
                ctx.writeAndFlush(monitorMessage);
            } else if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
                LOGGER.info("No response from server, closing connection: {}", socketAddress);
                channel.close();
            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
