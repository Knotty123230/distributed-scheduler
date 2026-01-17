package com.messageprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import org.slf4j.Logger;

import java.util.List;

public class ClientTaskEncoder extends MessageToMessageEncoder<ClientTask> {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(ClientTaskEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, ClientTask msg, List<Object> out) throws Exception {
        LOGGER.info("sending ClientTask to: {} from {}", ctx.channel().remoteAddress(), ctx.channel().localAddress());

        ByteBuf buffer = Unpooled.buffer();
        try {
            byte[] classPathBytes = msg.getPathToClass().getBytes();
            buffer.writeInt(classPathBytes.length);
            buffer.writeBytes(classPathBytes);

            byte[] payload = msg.getClassData();
            buffer.writeInt(payload.length);
            buffer.writeBytes(payload);

            if (msg.getParams() == null || msg.getParams().isEmpty()) {
                buffer.writeInt(0);
            } else {
                byte[] paramsBytes = MapToBytesConverter.convert(msg.getParams());
                buffer.writeInt(paramsBytes.length);
                buffer.writeBytes(paramsBytes);
            }

            byte[] content = new byte[buffer.readableBytes()];
            buffer.readBytes(content);

            Message message = new Message(content, content.length, MessageType.TASK.getValue());
            out.add(message);
        } finally {
            buffer.release();
        }
    }
}
