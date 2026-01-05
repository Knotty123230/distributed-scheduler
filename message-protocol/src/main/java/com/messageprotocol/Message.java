package com.messageprotocol;

public class Message {
    private  byte[] content;
    private int contentLength;
    private int messageType;

    public Message(byte[] content, int contentLength, int messageType) {
        this.content = content;
        this.contentLength = contentLength;
        this.messageType = messageType;
    }

    public Message() {
    }


    public void setMessageType(MessageType messageType) {
        this.messageType = messageType.getValue();
    }

    public MessageType getMessageType() {
        return MessageType.values()[this.messageType];
    }
    public byte[] getContent() {
        return content;
    }
    public void setContent(byte[] content) {
        this.content = content;
    }

    public static Message contentFromString(String contentStr, MessageType messageType) {
        Message message = new Message();
        message.setMessageType(messageType);
        message.setContentLength(contentStr.length());
        message.setContent(contentStr.getBytes());
        return message;
    }

    public int getContentLength() {
        return contentLength;
    }
    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }
}
