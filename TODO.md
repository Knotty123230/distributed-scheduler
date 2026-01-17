# Project Roadmap & TODO

## 1. Persistence Layer (RocksDB Integration) 🪨
Currently, all state is in-memory. We need to persist data so the server can survive restarts.
- [ ] **Add RocksDB Dependency**: Integrate `org.rocksdb:rocksdbjni`.
- [ ] **Persistent Task Queue**: When a task is submitted, save it to RocksDB immediately. Only remove it after a worker confirms completion.
- [ ] **Result Storage**: Store execution results (logs, status, return values) keyed by `TaskId`.
- [ ] **Crash Recovery**: On server startup, read pending tasks from RocksDB and re-queue them.

## 2. Reliability & Fault Tolerance 🛡️
- [ ] **Task Retries**: If a task fails (or worker disconnects), automatically re-queue it (up to `MAX_RETRIES`).
- [ ] **Dead Letter Queue (DLQ)**: Move tasks that fail consistently to a separate storage for analysis.
- [ ] **Worker Health Check**: If a worker misses 3 heartbeats, mark it as "Dead" and re-assign its active tasks to other workers.
- [ ] **Ack Mechanism**: Workers must send an explicit `ACK` when a task is *finished*, not just received.

## 3. Advanced Features (The "Cool Stuff") 🚀
- [ ] **Task Workflows (DAGs)**: Support task dependencies (e.g., "Run Task B only after Task A succeeds").
- [ ] **Scheduled Tasks (Cron)**: Allow submitting tasks with a cron expression or delay (e.g., "Run every 5 mins").
- [ ] **Resource-Aware Scheduling**: Use the CPU/RAM stats from the client to send heavy tasks only to idle workers.

## 4. Observability & UI 📊
- [ ] **Embedded Web Dashboard**: Add an HTTP handler to the Netty Server to serve a JSON API or simple HTML page showing:
    - Connected Workers (and their load).
    - Queued vs. Running Tasks.
    - Throughput (Tasks/sec).
- [ ] **Distributed Tracing**: Add a correlation ID to trace a task from API -> Queue -> Worker -> Result.

## 5. Security & Protocol Improvements 🔒
- [ ] **Secure Protocol**: Replace Java Serialization (`ObjectInputStream`) with JSON or Protobuf to prevent RCE attacks.
- [ ] **Authentication**: Simple token-based auth so unauthorized clients cannot connect.
- [ ] **TLS/SSL**: Encrypt traffic between Server and Workers.
