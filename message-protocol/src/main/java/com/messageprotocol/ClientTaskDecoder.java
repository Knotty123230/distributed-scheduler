package com.messageprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientTaskDecoder extends MessageToMessageDecoder<Message> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ClientTaskDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, Message msg, List<Object> out) throws Exception {
        if (msg.getMessageType() == MessageType.TASK) {
            LOGGER.info("ClientTaskDecoder received TASK from: {} to {}", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            
            ByteBuf in = Unpooled.wrappedBuffer(msg.getContent());
            try {
                if (in.readableBytes() < 4) return;
                int classPathLen = in.readInt();

                byte[] classPathBytes = new byte[classPathLen];
                in.readBytes(classPathBytes);
                String classPath = new String(classPathBytes);

                int payloadLength = in.readInt();
                byte[] payload = new byte[payloadLength];
                in.readBytes(payload);

                if (in.readableBytes() == 0 || in.getInt(in.readerIndex()) == 0) {
                    // Consume the 0 if it exists
                    if (in.readableBytes() >= 4 && in.getInt(in.readerIndex()) == 0) {
                        in.readInt();
                    }
                    ClientTask clientTask = new ClientTask(classPath, payload, new HashMap<>());
                    out.add(clientTask);
                    return;
                }

                int paramsLen = in.readInt();
                byte[] paramsBytes = new byte[paramsLen];
                in.readBytes(paramsBytes);

                Map<String, String> map = BytesToMapConverter.convert(paramsBytes);

                ClientTask clientTask = new ClientTask(classPath, payload, map);
                out.add(clientTask);
            } finally {
                in.release();
            }
        } else {
            out.add(msg);
        }
    }
}
