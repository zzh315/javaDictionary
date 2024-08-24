package com.zzh.server.pool;

import com.zzh.server.ServerGUI;

import javax.swing.*;
import java.util.concurrent.BlockingQueue;


/**
 * The WorkerThread class represents a worker thread in the thread pool. It continuously
 * listens for tasks from a blocking queue and executes them. The tasks are typically
 * instances of Runnable that handle client connections.
 *
 * <p>
 * Created by Zhonghe Zheng, Student ID: 825612.
 * </p>
 */
class WorkerThread extends Thread {
    private final BlockingQueue<Runnable> boundedBlockingQueue;
    private int workerNum;
    private ServerGUI serverGUI;
    private boolean firstRun = true;


    /**
     * Constructs a WorkerThread with the specified task queue, worker number, and ServerGUI for logging.
     *
     * @param boundedBlockingQueue the blocking queue from which tasks are fetched
     * @param workerNum            the number identifying this worker thread
     * @param serverGUI            the ServerGUI instance for logging purposes
     */
    public WorkerThread(BlockingQueue<Runnable> boundedBlockingQueue, int workerNum, ServerGUI serverGUI) {
        this.boundedBlockingQueue = boundedBlockingQueue;
        this.workerNum = workerNum;
        this.serverGUI = serverGUI;
    }


    /**
     * The main run loop of the worker thread. This method continuously waits for tasks
     * in the blocking queue, executes them, and logs the activity through the ServerGUI.
     */
    @Override
    public void run() {
        //Consumer
        while (true) {
            try {
                //Prevent All Workers Logging the Blocking Queue At the Start
                if (!(firstRun && workerNum != 0)) {
                    serverGUI.appendLogs("Request(s) In Queue: " + boundedBlockingQueue.size() + "\n");
                }

                // Blocking until Socket in queue then Consume a Runnable Socket from the queue and run it
                Runnable socketRunner = boundedBlockingQueue.take();
                serverGUI.appendLogs(
                        "Worker " + (workerNum + 1) + " running new request\n");
                socketRunner.run();//This is blocking, the loop will only continue when socketRunner connection is finished.
            } catch (InterruptedException e) {
                // Thread was interrupted, possibly client disconnected
                JOptionPane.showMessageDialog(null, "A Client Connection Was Interrupted", "Warning",
                                              JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}