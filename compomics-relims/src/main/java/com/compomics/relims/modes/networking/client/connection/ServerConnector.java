/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.client.connection;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.taskobjects.TaskContainer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class ServerConnector {

    private static final Logger logger = Logger.getLogger(ServerConnector.class);
    private static ObjectOutputStream output;//flows from this server to the server that is idle
    private static ObjectInputStream input;//flows from client to this server
    private static Socket socketConnection; //socket = a connection, rename later
    private String controllerIP = RelimsProperties.getControllerIP();
    private int controllerPort = RelimsProperties.getControllerPort();

    public ServerConnector() {
    }

    public void setConnectionParameters(String IP, int ports) {
        this.controllerIP = IP;
        this.controllerPort = ports;
    }

    public String getIP() {
        return controllerIP;
    }

    public int getPort() {
        return controllerPort;
    }

    public void resetConnectionParameters() {
        this.controllerIP = RelimsProperties.getControllerIP();
        this.controllerPort = RelimsProperties.getControllerPort();
    }

    public Map<Long, Long> SendToServer(TaskContainer container) throws IOException {

        logger.debug("Creating socket to '" + controllerIP + "' on port " + controllerPort);
        boolean loading = true;
        Map<Long, Long> generatedTaskIDs = null;
        while (loading) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException ex) {
                logger.error(ex);
            }
            socketConnection = new Socket(controllerIP, controllerPort);
            output = new ObjectOutputStream(socketConnection.getOutputStream());
            output.flush();

            input = new ObjectInputStream(socketConnection.getInputStream());
            logger.debug("Input- and outputstreams are ready...");

            try {
                output.writeInt(0);
                output.writeObject(container);
                output.flush();
            } catch (IOException IOExc) {
                IOExc.printStackTrace();
                logger.debug("Could not send results...");
                loading = false;
            }
            try {
                try {
                    generatedTaskIDs = (Map<Long, Long>) input.readObject();
                } catch (ClassNotFoundException ex) {
                    logger.error(ex);
                }
            } catch (IOException IOExc) {
                loading = false;
            }
            if (generatedTaskIDs != null) {
                loading = false;
                logger.debug("Jobs submitted to online database.");
            }
            if (!loading) {
                socketConnection.close();
                break;
            }
        }
        return generatedTaskIDs;
    }

    public Socket stop() throws IOException {
        if (socketConnection != null) {
            socketConnection.close();
        }
        return socketConnection;
    }
}
