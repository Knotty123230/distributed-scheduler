package com;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import javax.sound.midi.SoundbankResource;
import java.net.InetSocketAddress;


public class ServerHandler extends ChannelInboundHandlerAdapter {
    private final ServerContext serverContext = ServerContext.ServerContextHolder.getInstance();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            ctx.write(msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Channel channel = ctx.channel();
        InetSocketAddress socketAddress = (InetSocketAddress) channel.remoteAddress();
        System.out.println("Client connected from: " + socketAddress);
        String clientId = socketAddress.getHostString();

        serverContext.addClient(clientId, new ServerContext.ClientInfo(System.currentTimeMillis(), System.currentTimeMillis()));
        super.channelActive(ctx);
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
