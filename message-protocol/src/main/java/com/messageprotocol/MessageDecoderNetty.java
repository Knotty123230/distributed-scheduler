package com.messageprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class MessageDecoderNetty extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        int messageTypeValue = byteBuf.readInt();
        int payloadLength = byteBuf.readInt();
        byte[] payload = new byte[payloadLength];
        byteBuf.readBytes(payload);

        Message message = new Message(payload,payloadLength, messageTypeValue);
        list.add(message);

    }
}
