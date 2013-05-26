/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.handlers;

import com.compomics.relims.modes.networking.controller.workerpool.WorkerRunner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class MotherHandler implements Runnable {
    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */

    private final static Logger logger = Logger.getLogger(MotherHandler.class);
    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private Thread myThread = null;
    private WorkerRunner worker;
    private boolean registered = false;

    public MotherHandler(Socket sock) throws IOException {
        this.sock = sock;
        sockOutput = new ObjectOutputStream(sock.getOutputStream());
        sockInput = new ObjectInputStream(sock.getInputStream());
        this.myThread = new Thread(this);
    }

    @Override
    public void run() {
        while (true) {
            try {
                int handlerRequired = (Integer) sockInput.readInt();
                if (handlerRequired == 0) {
                    logger.debug("Client connected");
                    ClientHandler cHandler = new ClientHandler(sock, sockOutput, sockInput);
                    cHandler.run();
                    break;
                } else if (handlerRequired == 1) {
                    logger.debug("Worker connected to store results");
                    ResultHandler rHandler = new ResultHandler(sock, sockOutput, sockInput);
                    rHandler.run();
                    break;
                } else if (handlerRequired == 2) {
                    WorkerHandler wHandler = new WorkerHandler(sock, sockOutput, sockInput);
                    wHandler.run();
                    break;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
