/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.taskreciever;

import com.compomics.relims.manager.resourcemanager.ResourceManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class TaskReciever implements Runnable {

    private static ServerSocket recievingSocket;
    private static Socket sock;
    public static boolean locked = false;
    private final static Logger logger = Logger.getLogger(TaskReciever.class);
    private int port = 0;
    private static boolean shutdownsignal = false;

    public TaskReciever() {

        boolean settingUp = true;
        while (settingUp) {
            port = ResourceManager.getWorkerPort();
            try {
                if (port != 0) {
                    recievingSocket = new ServerSocket(port);
                    settingUp = false;
                }
            } catch (IOException ex) {
            }
        }
    }

    public void waitForConnections() {
        while (!shutdownsignal) {
            if (shutdownsignal) {
                break;
            }
            try {
                sock = recievingSocket.accept();
                sock.setSoTimeout(0);
                TaskHandler handler = new TaskHandler(sock);
                handler.start();
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
        }
    }

    public static void shutdown() {
        shutdownsignal = true;
        try {
            sock.close();
            recievingSocket.close();
        } catch (IOException ex) {
            if (sock != null) {
                sock = null;
            }
            if (recievingSocket != null) {
                recievingSocket = null;
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (!locked) {
                    Thread.sleep(500);
                }
                waitForConnections();
            } catch (InterruptedException ex) {
            }
        }
    }
}
