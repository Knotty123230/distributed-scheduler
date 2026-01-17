package com;

import com.messageprotocol.Message;
import com.messageprotocol.MessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;

import java.lang.management.MonitorInfo;
import java.net.InetSocketAddress;


public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final WorkerStorage workerStorage = WorkerStorage.getInstance();
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ServerHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if(msg instanceof Message message) {
            if(message.getMessageType() == MessageType.PING) {
                LOGGER.info("Received ping from client {}", ctx.channel().remoteAddress());
                workerStorage.updateHeartbeat((InetSocketAddress) ctx.channel().remoteAddress());
                ctx.writeAndFlush(Message.contentFromString("pong", MessageType.PONG));
            }else if(message.getMessageType() == MessageType.MONITOR_INFO) {
                LOGGER.info("Received monitoring info from client {}", ctx.channel().remoteAddress());
                byte[] contentBytes = message.getContent();
                com.MonitoringService.MonitorInfo monitorInfo = com.MonitoringService.MonitorInfo.fromBytes(contentBytes);
                workerStorage.updateWorkerMonitoringInfo((InetSocketAddress)ctx.channel().remoteAddress(), monitorInfo);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        LOGGER.info("Registering new worker from address: {}", socketAddress.getAddress().getHostAddress());
        workerStorage.registerWorker(ctx.channel(), socketAddress);

        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Channel channel = ctx.channel();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        workerStorage.unregisterWorker(socketAddress);
        System.out.println("Client disconnected from: " + socketAddress);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (((IdleStateEvent) evt).state() == IdleState.WRITER_IDLE) {
                Message pingMessage = Message.contentFromString("ping", MessageType.PING);
                ctx.writeAndFlush(pingMessage);
            } else if (((IdleStateEvent) evt).state() == IdleState.READER_IDLE) {
                Channel channel = ctx.channel();
                InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
                System.out.println("No response from client, closing connection: " + socketAddress);
                workerStorage.unregisterWorker(socketAddress);
                channel.close();
            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
