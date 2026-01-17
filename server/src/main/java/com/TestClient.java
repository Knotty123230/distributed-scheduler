package com;

import com.messageprotocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class TestClient {
    static void main(String[] args) throws Exception {
        String host = "localhost";
        int port = 8080; // Порт твого Майстра

        // Шлях до скомпільованого класу, який ми хочемо виконати
        Path classPath = Paths.get("server/target/classes/com/Test.class");
        byte[] bytecode = Files.readAllBytes(classPath);

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new MessageDecoderNetty(), new ClientTaskDecoder(), new MessageEncoderEntity(), new ClientTaskEncoder());
                        }
                    });

            ChannelFuture f = b.connect(host, port).sync();

            // Відправляємо задачу
            ClientTask request = new ClientTask(
                    "com.Test",
                    bytecode,
                    new HashMap<>()
            );

            System.out.println("Sending task to Master...");
            f.channel().writeAndFlush(request).sync();

            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
