/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.listeners;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.handlers.MotherHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class PortListener extends Thread implements Runnable {
    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */

    /**
     *
     * @author Kenneth
     */
    private int serverPort = 0;
    private static ServerSocket serverSock = null;
    private Socket sock = null;
    private final static Logger logger = Logger.getLogger(PortListener.class);

    public PortListener() {
    }

    public PortListener(int serverPort) throws IOException {
        this.serverPort = serverPort;
        try {
            serverSock = new ServerSocket(serverPort);
        } catch (java.net.BindException e) {
            logger.error("Another instance of the controller seems to be running on port " + serverPort);
            System.exit(0);
        }
    }

    public void waitForConnections() {
        while (true) {
            try {
                sock = serverSock.accept();
                MotherHandler handler = new MotherHandler(sock);
                handler.run();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public static void launch() {
        int port;
        try {
            port = RelimsProperties.getControllerPort();
        } catch (NullPointerException ex) {
            port = 6789;
            ex.printStackTrace();
        }
        PortListener server = null;
        try {
            server = new PortListener(port);
            logger.debug("Now Listening on port " + port);
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
        server.waitForConnections();
    }

    @Override
    public void run() {
        this.launch();
    }
}
