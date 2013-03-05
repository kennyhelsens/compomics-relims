/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.taskreciever;

import com.compomics.relims.modes.networking.worker.general.ResourceManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.apache.log4j.Logger;



/**
 *
 * @author Kenneth
 */
public class TaskReciever implements Runnable {

    private ServerSocket recievingSocket;
    private Socket sock;
    public static boolean locked = false;
    private final static Logger logger = Logger.getLogger(TaskReciever.class);
    private int port = 0;

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
        while (true) {
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
