package com;


import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ServerContext {
    private final Map<String, ClientInfo> clients = new ConcurrentHashMap<>();

    public static class ServerContextHolder {
        private static final ServerContext INSTANCE = new ServerContext();

        public static ServerContext getInstance() {
            return INSTANCE;
        }
    }

    public void addClient(String clientId, ClientInfo clientInfo) {
        clients.put(clientId, clientInfo);
    }

    public void removeClient(String clientId) {
        clients.remove(clientId);
    }
    public ClientInfo getClient(String clientId) {
        return clients.get(clientId);
    }

    public static  class ClientInfo {
        private  final long connectedAt;
        private  long lastActiveAt;

        public ClientInfo(long connectedAt, long lastActiveAt) {
            this.connectedAt = connectedAt;
            this.lastActiveAt = lastActiveAt;
        }

        public long connectedAt() {
            return connectedAt;
        }

        public long lastActiveAt() {
            return lastActiveAt;
        }

        public void setLastActiveAt(long lastActiveAt) {
            this.lastActiveAt = lastActiveAt;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            var that = (ClientInfo) obj;
            return this.connectedAt == that.connectedAt &&
                    this.lastActiveAt == that.lastActiveAt;
        }

        @Override
        public int hashCode() {
            return Objects.hash(connectedAt, lastActiveAt);
        }

        @Override
        public String toString() {
            return "ClientInfo[" +
                    "connectedAt=" + connectedAt + ", " +
                    "lastActiveAt=" + lastActiveAt + ']';
        }

        }

}
