package com.messageprotocol;

public enum MessageType {
    PING,
    PONG,
    TASK, 
    MONITOR_INFO;


    public int getValue() {
        return this.ordinal();
    }

    public MessageType fromValue(int value) {
        return MessageType.values()[value];
    }

}
