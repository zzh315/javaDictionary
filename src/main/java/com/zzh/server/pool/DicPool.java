package com.zzh.server.pool;

import com.zzh.server.ServerGUI;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * A simple implementation of thread pool for managing a fixed number of worker threads
 * to handle incoming client requests. It uses a blocking queue to manage tasks,
 * ensuring that no more than a specified number of clients are queued at any time.
 *
 * <p>
 * Created by Zhonghe Zheng, Student ID: 825612.
 * </p>
 */
public class DicPool {
    private final BlockingQueue<Runnable> boundedBlockingQueue;
    private final WorkerThread[] workers;
    private final ServerGUI serverGUI;


    /**
     * Constructs a DicPool with the specified number of worker threads and maximum client queue size.
     * Initializes the worker threads and starts them immediately.
     *
     * @param numberOfThreads   the number of worker threads in the pool
     * @param maxClientsInQueue the maximum number of clients that can be queued
     * @param serverGUI         the ServerGUI instance for logging and display purposes
     */
    public DicPool(int numberOfThreads, int maxClientsInQueue, ServerGUI serverGUI) {
        this.boundedBlockingQueue = new ArrayBlockingQueue<>(maxClientsInQueue);
        this.workers = new WorkerThread[numberOfThreads];
        this.serverGUI = serverGUI;

        for (int i = 0; i < numberOfThreads; i++) {
            workers[i] = new WorkerThread(boundedBlockingQueue, i, serverGUI);
            workers[i].start();
        }
    }


    /**
     * Submits a new task to the thread pool. If the task queue is full, an IllegalStateException
     * is thrown to reject the new task.
     *
     * @param socketRunner the task to be executed, typically a Runnable handling a client socket
     * @throws IllegalStateException if the queue is full and the task cannot be accepted
     */
    public void execute(Runnable socketRunner) throws IllegalStateException {
        //Main Thread Blocked until there is space in the blockingQueue
        //When lots of client request, the OS's accept queue might be overwhelmed and client side will fail to connect
        //boundedBlockingQueue.put(socketRunner); (not used)

        //New Socket connections exceeding the blockingQueue size will throw runtime IllegalStateException Error(aka rejected)
        boundedBlockingQueue.add(socketRunner); //Producer
    }
}
