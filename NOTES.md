# Code Review: Distributed Scheduler

## Executive Summary
The project implements a basic distributed task scheduler using Netty for communication and custom class loading for dynamic task execution. While the core networking structure is in place, the system currently suffers from **critical security vulnerabilities** (Remote Code Execution, Unsafe Deserialization), **broken architectural features** (Load Balancing is non-functional), and **resource management issues** (unbounded thread creation).

## 1. Security Concerns (Critical)

### 1.1 Remote Code Execution (RCE) via Deserialization
**File:** `message-protocol/.../BytesToMapConverter.java`
- **Issue:** The `convert` method uses `ObjectInputStream` to deserialize data from the network.
- **Risk:** This is a textbook vulnerability. An attacker can craft a malicious byte stream to execute arbitrary code on the server or client during deserialization (Gadget Chain attack).
- **Fix:** Replace `ObjectInputStream` with a safe serialization format like JSON (Jackson/Gson) or Protocol Buffers.

### 1.2 Unsandboxed Arbitrary Code Execution
**File:** `java-task-runner/.../TaskRunner.java`
- **Issue:** The system allows the server to send arbitrary bytecode (`classData`) to be loaded and executed by the client.
- **Risk:** There is no security sandbox. A malicious server (or Man-in-the-Middle) can execute `System.exit()`, delete files, or install malware on worker machines.
- **Fix:** Implementing a robust Java SecurityManager is difficult and deprecated. A better approach is running tasks in isolated containers (Docker) or restricting the worker to a very limited set of permissions.

### 1.3 Denial of Service (DoS)
**File:** `message-protocol/.../MessageDecoderNetty.java`
- **Issue:** `byte[] payload = new byte[payloadLength];` allocates memory based on a user-provided integer before validating usage.
- **Risk:** An attacker can send a message claiming a 2GB payload, causing an `OutOfMemoryError` and crashing the server/client.
- **Fix:** Implement a `TooLongFrameException` check or strict maximum message size limit before allocation.

## 2. Architecture & Logic Bugs

### 2.1 Broken Load Balancing
**Files:** `client/.../MonitoringService.java`, `server/.../WorkerStorage.java`
- **Issue:** `MonitoringService` exists in the client but is **never used**. The client never sends CPU/Memory stats to the server.
- **Impact:** The server's `WorkerInfo` objects always have default load values. `WorkerSelector` (using Power of Two Choices) effectively selects workers randomly, defeating the purpose of load balancing.
- **Fix:** The client must periodically send a `HEARTBEAT` message containing `MonitorInfo`. The server must update `WorkerInfo` with these stats.

### 2.2 Task Dispatch Crashes
**Files:** `server/.../WorkerSelector.java`, `TaskDispatcher.java`
- **Issue:** `WorkerSelector.selectWorker()` assumes `workers.size() > 0`.
- **Impact:** If no workers are connected, `random.nextInt(size)` throws `IllegalArgumentException`, crashing the `TaskDispatcher` thread.
- **Fix:** Check for empty worker list. If empty, queue the task or reject it until workers connect.

### 2.3 One-Way Task Execution
**File:** `client/.../ClientHandler.java`
- **Issue:** The client executes the task but **never reports the result** (success/failure/output) back to the server.
- **Impact:** The server assumes the task is "assigned" but never knows if it finished.
- **Fix:** Introduce `TASK_RESULT` message type. Update `ClientHandler` to send results after execution.

## 3. Performance & Resource Management

### 3.1 Unbounded Thread Creation
**File:** `java-task-runner/.../TaskRunner.java`
- **Issue:** `new Thread(task).start()` is called for every task.
- **Impact:** High task volume will exhaust system threads/memory (Thread starvation).
- **Fix:** Use a `ExecutorService` (e.g., `Executors.newFixedThreadPool`) to manage a worker pool.

### 3.2 String Encoding Bug
**File:** `message-protocol/.../Message.java`
- **Issue:** `message.setContentLength(contentStr.length());` vs `contentStr.getBytes()`.
- **Impact:** `String.length()` returns character count, not byte count. For multi-byte characters (UTF-8), the length will be wrong, causing the decoder to read incorrect frame sizes and corrupting the stream.
- **Fix:** Use `byte[] bytes = contentStr.getBytes(StandardCharsets.UTF_8);` and `bytes.length`.

### 3.3 Efficient Byte Decoding
**File:** `message-protocol/.../MessageDecoderNetty.java`
- **Suggestion:** `ReplayingDecoder` is slower than `ByteToMessageDecoder` due to exception handling overhead. Standard Netty length-field based decoders should be preferred.

## 4. Maintainability & Standards

- **Enum Ordinals:** `MessageType` relies on `ordinal()`. Changing the order of enums breaks protocol compatibility. Use explicit `int` IDs.
- **Logging:** Inconsistent use of `System.out.println` vs `SLF4J`. All code should strictly use SLF4J.
- **Hardcoded Constants:** Ports and timeouts are often hardcoded or scattered. Consolidate into a configuration class or file.
- **Timer Usage:** `WorkerStorage` uses `java.util.Timer` (legacy). Prefer `ScheduledExecutorService`.

## Recommended Next Steps

1.  **Fix Critical Crashes:** Add checks for empty worker lists in `WorkerSelector`.
2.  **Fix Protocol Bug:** Fix the string length calculation in `Message.java`.
3.  **Implement Load Balancing:** Wire up `MonitoringService` to send stats in heartbeats.
4.  **Replace Serialization:** Switch from `ObjectInputStream` to JSON.
5.  **Thread Pool:** Replace `new Thread()` with a thread pool in `TaskRunner`.
