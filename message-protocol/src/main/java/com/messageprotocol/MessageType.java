package com.messageprotocol;

public enum MessageType {
    PING,
    TASK;


    public int getValue() {
        return this.ordinal();
    }

    public MessageType fromValue(int value) {
        return MessageType.values()[value];
    }

}
