/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.handlers;

import com.compomics.relims.modes.networking.controller.connectivity.workerpool.WorkerPool;
import com.compomics.relims.modes.networking.controller.connectivity.workerpool.WorkerRunner;

/**
 *
 * @author Kenneth
 */
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import org.apache.log4j.Logger;

public class WorkerHandler implements Runnable {

    private final static Logger logger = Logger.getLogger(WorkerHandler.class);
    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private Thread myThread = null;
    private WorkerRunner worker;
    private boolean registered = false;

    public WorkerHandler(Socket sock, ObjectOutputStream sockOutput, ObjectInputStream sockInput) throws IOException {
        this.sock = sock;
        this.sockOutput = sockOutput;
        this.sockInput = sockInput;
        this.myThread = new Thread(this);

    }

    public void start() {
        try {
            myThread.join();
        } catch (InterruptedException ex) {
        }
        myThread.start();
    }

    @Override
    public void run() {

        try {
            WorkerPool workerpool = WorkerPool.getInstance();
            try {
                int workerPortNumber = (Integer) sockInput.readInt();
                worker = new WorkerRunner(sock.getInetAddress().getHostName(), workerPortNumber);
                registered = workerpool.isRegistered(worker);
            } catch (IOException ex) {
                ex.printStackTrace();
                registered = false;
            }
            if (!registered) {
                workerpool.register(worker);
                logger.debug("Worker was registrated : " + worker.getHost() + " ( " + worker.getPort() + ")");
                registered = true;
                workerpool.setOnline(worker);
            } else {
                logger.debug(worker.getHost() + " sent heartbeat...");
                workerpool.setOnline(worker);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                sock.close();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }
}
