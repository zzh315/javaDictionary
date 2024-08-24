# Multi-Threaded Dictionary Server

**COMP90015: Distributed Systems – Assignment 1**  

## Problem Context

The dictionary server system needs to handle multiple clients concurrently, allowing them to perform various operations on the dictionary, such as searching for word meanings, adding new words, deleting existing words, and modifying word meanings. The main challenge is ensuring the system's reliability and maintaining data integrity when accessed by multiple clients simultaneously.

## Approach to the Problem

The server is responsible for handling client requests and managing dictionary operations, ensuring that many clients can connect and perform actions concurrently. My approach included:

1. **Client-Server Architecture**: Implemented a client-server model with a multi-threaded server to handle multiple client requests simultaneously, allowing concurrent requests from each client.
2. **TCP Sockets**: Used TCP sockets for communication between clients and the server to guarantee reliable message delivery, simplifying error handling and ensuring data consistency.
3. **Custom Thread Pool**: Developed a custom thread pool to manage concurrency efficiently, allowing the server to handle numerous client requests without creating a new thread for each request, thus optimizing resource allocation.
4. **Workers Consume Requests**: Designed worker threads in the thread pool to stay alive and compete to handle client requests from a shared queue, dynamically allocating resources where needed most to optimize response times and balance the load across available worker threads.
5. **Communication in JSON**: Used JSON as the communication format between clients and the server to ensure structured data exchange, enhancing debugging and maintenance.
6. **Graphical User Interfaces (GUI)**: Developed GUIs for both client and server applications. The client GUI enables easy interaction with the dictionary, while the server GUI facilitates server management and monitoring.

## System Components

### Server Components

- **ServerGUI Class**: Provides a management interface for the server, allowing the administrator to start/stop the server, specify the number of workers and queue size, and view server logs.

- **DicPool Class**: A fixed thread implementation of a thread pool that manages a set number of worker threads. The `execute` method "produces" `SocketRunner` instances by adding incoming requests to a blocking queue, enabling efficient handling of multiple clients concurrently.

- **WorkerThread Class**: Represents individual threads in the thread pool. Each thread monitors the blocking queue for new tasks (`SocketRunner`) and processes them as soon as they become available, maintaining efficiency without constantly recreating threads.

- **SocketRunner Class**: Implements the `Runnable` interface and manages communication between the server and a single client. It handles client requests, interacts with the `Dictionary` class, and sends responses back to the client in JSON format.

- **Dictionary Class**: Manages all dictionary-related operations, including adding, querying, updating, deleting, and appending word meanings. Operations are synchronized to ensure thread safety and prevent data corruption during concurrent access.

### Client Components

- **ClientGUI Class**: Provides a user-friendly interface for clients to interact with the dictionary, featuring input validation and error handling to ensure smooth user interactions.

- **ClientSocket Class**: Manages communication between the client application and the server, establishing TCP socket connections, sending requests, and processing server responses.


## Critical Analysis and Conclusions

### Critical Analysis

The system successfully meets the assignment requirements, providing a reliable, scalable, and concurrent client-server application. The design choices, such as implementing a custom thread pool, using TCP sockets, and synchronized dictionary operations, ensure efficient handling of multiple clients while maintaining data integrity.

**Strengths:**

- **Resource Efficiency**: The custom thread pool allows the server to handle many client connections simultaneously without needing to create new threads for each request, optimizing resource use.
- **Concurrent Access Safety**: The `Dictionary` class uses synchronized methods to ensure data consistency during concurrent access.
- **Error Handling**: Comprehensive error handling with clear user feedback in both client and server applications enhances user experience and system usability.
- **Structured Data Transmission**: JSON format for data exchange ensures clear, manageable communication between client and server.

**Weaknesses:**

- **Idle Workers**: Workers may remain idle when no requests are in the queue. This could be improved by implementing an idle timer for workers.
- **Fixed Worker Number**: A fixed number of worker threads may lead to delays during high request volumes. An elastic worker creation mechanism could improve performance.
- **Single Point of Failure**: The system relies on a single server, which could cause service disruption if it fails. Implementing a failover mechanism or load balancing could improve reliability.
- **Error Recovery**: Requests in the queue are lost if the server fails. A backup system could enhance error recovery.

### Conclusion

The Multithreaded Dictionary Server implementation meets key requirements by providing a reliable and efficient client-server application. The modular design, thread safety, and effective thread pool management ensure smooth concurrent operations. While there is room for improvement, such as adding elastic workers and better handling of single points of failure, the current system provides a solid foundation.

## Excellence Elements

1. **Custom Thread Pool Implementation**: Developed a custom thread pool for better resource allocation and management, enhancing the server's ability to handle concurrent client connections.
2. **Error Handling**: Comprehensive error handling across client and server components ensures reliable operation and user-friendliness, displaying intuitive messages rather than technical error codes.
3. **Structured Data Exchange with JSON**: Utilized JSON for all client-server communication, simplifying data handling and enhancing maintainability and debugging.
4. **CLI Parameter Validation**: The system includes validation of command-line arguments to ensure correct server and client initialization, providing clear feedback for incorrect inputs.

## Creativity Elements

1. **Custom Thread Pool**: Developed a custom thread pool instead of using a pre-existing solution, demonstrating a deep understanding of concurrency and resource management.
2. **Dynamic Server and Client Management GUI**: The `ServerGUI` allows for efficient server administration, while the `ClientGUI` features dynamic labels and input fields to prevent incorrect data entry, improving user experience and system reliability.
