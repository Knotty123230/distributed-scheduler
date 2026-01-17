# Distributed Scheduler

A lightweight distributed task execution system implemented in Java, utilizing **Netty** for high-performance networking. This project demonstrates a custom binary protocol for communication between a central server and multiple worker clients.

> **⚠️ Note:** This project is a prototype/experimental implementation. It utilizes Java 25 preview features and is currently under active development. See `NOTES.md` for known issues and architectural review.

## Project Structure

The project is organized into several Maven modules:

*   **`server`**: The central coordinator. It accepts connections from workers (clients) and dispatches tasks to them.
*   **`client`**: The worker node. It connects to the server, receives task bytecodes, loads them dynamically, and executes them.
*   **`message-protocol`**: Defines the custom binary protocol used for communication (Netty encoders/decoders, message types).
*   **`task-api`**: Contains the `TaskApi` interface that all executable tasks must implement.
*   **`java-task-runner`**: Handles the dynamic loading and execution of task classes sent over the network.

## Prerequisites

*   **Java 25** (Preview features enabled)
*   **Maven** 3.8+

## Building the Project

To build all modules and install dependencies:

```bash
mvn clean install
```

## Running the System

Since the project uses Java 25 preview features (Instance Main Methods), ensure your IDE or runtime is configured correctly (e.g., `--enable-preview`).

### 1. Start the Server (Coordinator)

The server listens on two ports:
*   **8080**: For Task Submitters (Clients sending tasks).
*   **9001**: For Workers (Nodes executing tasks).

To run:
*   **Class:** `com.Main`
*   **Module:** `server`

### 2. Start a Worker (Client)

The `client` module acts as a **Worker Node**. It connects to the Server's worker port (default `9001`). You can run multiple instances to simulate a cluster.

To run:
*   **Class:** `com.Main`
*   **Module:** `client`

### 3. Submit a Task

To execute a task on the distributed system, you need to send a `ClientTask` object containing the bytecode and parameters to port `8080`.

See `server/src/main/java/com/TestClient.java` for a reference implementation of a task submitter.

## Architecture Overview

1.  **Worker Registration**: Worker nodes (`client` module) connect to the Server on port `9001`.
2.  **Task Submission**: A user connects to the Server on port `8080` and sends a task (bytecode + parameters).
3.  **Dispatch**: The Server's `TaskDispatcher` selects an available Worker.
4.  **Execution**:
    *   The Server forwards the task to the selected Worker.
    *   The Worker receives the bytecode.
    *   `java-task-runner` dynamically loads the class using `ByteClassLoader`.
    *   The task (implementing `com.TaskApi`) is executed.

## Key Technologies

*   **Java 25**: Leveraging latest language features.
*   **Netty 4.2.9**: Asynchronous event-driven network application framework.
*   **OSHI**: (In Client) For system monitoring (CPU/Memory usage).
