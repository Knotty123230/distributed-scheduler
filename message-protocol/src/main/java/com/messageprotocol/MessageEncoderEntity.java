package com.messageprotocol;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageEncoderEntity extends MessageToByteEncoder<Message> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Message message, ByteBuf byteBuf) throws Exception {
        byteBuf.writeInt(message.getMessageType().getValue());
        byteBuf.writeInt(message.getContentLength());
        byteBuf.writeBytes(message.getContent());
    }
}
