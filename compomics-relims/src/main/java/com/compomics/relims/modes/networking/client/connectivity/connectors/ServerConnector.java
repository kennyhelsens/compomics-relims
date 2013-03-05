/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.client.connectivity.connectors;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.client.GUI.MainClientGUI;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.TaskContainer;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;



/**
 *
 * @author Kenneth
 */
public class ServerConnector {

    private static org.apache.log4j.Logger logger = MainClientGUI.getLogger();
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

    public boolean createOnServer(TaskContainer taskMap) throws IOException {

        boolean isAvailable = false;
        logger.debug("Creating socket to '" + controllerIP + "' on port " + controllerPort);
        boolean loading = true;
        Map<Long, String> myCurrentProjects = null;
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
                output.writeObject(taskMap);
                output.flush();
                loading = false;
            } catch (IOException IOExc) {
                IOExc.printStackTrace();
                logger.debug("Could not send results...");
                loading = false;
            }
            try {
                isAvailable = (boolean) input.readBoolean();
                loading = false;
            } catch (IOException IOExc) {
                loading = false;
            }
            if (!loading) {
                socketConnection.close();
                break;
            }
        }

        return isAvailable;
    }

    public List<String[]> getFromServer(TaskContainer taskMap) throws IOException {
        boolean loading = true;
        List<String[]> myCurrentProjects = new LinkedList<String[]>();
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

            try {
                output.writeInt(0);
                output.writeObject(taskMap);
                output.flush();

            } catch (IOException IOExc) {
                IOExc.printStackTrace();
                logger.debug("Could not send results...");
                loading = false;
            }
            try {
                try {
                    myCurrentProjects = (List<String[]>) input.readObject();
                } catch (ClassNotFoundException ex) {
                    logger.error(ex);
                }
            } catch (IOException IOExc) {
                loading = false;
            }
            if (myCurrentProjects != null) {
                loading = false;
                logger.debug("Jobs retrieved from online database.");
                socketConnection.close();

            }
            if (!loading || taskMap.getInstructionMap() != null) {
                socketConnection.close();
                break;
            }
        }

        return myCurrentProjects;
    }

    public HashMap<String, Object> getSpecificFromServer(TaskContainer taskMap) throws IOException {
        boolean loading = true;
        HashMap<String, Object> myProjectInfo = new HashMap<String, Object>();
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

            try {
                output.writeInt(0);
                output.writeObject(taskMap);
                output.flush();

            } catch (IOException IOExc) {
                IOExc.printStackTrace();
                logger.debug("Could not send results...");
                loading = false;
            }
            try {
                try {
                    myProjectInfo = (HashMap<String, Object>) input.readObject();
                } catch (ClassNotFoundException ex) {
                    logger.error(ex);
                }
            } catch (IOException IOExc) {
                loading = false;
            }
            if (myProjectInfo != null) {
                loading = false;
                logger.debug("Task was retrieved from the server.");
                socketConnection.close();
            }
            if (!loading || taskMap.getInstructionMap() != null) {
                socketConnection.close();
                break;
            }
        }
        return myProjectInfo;
    }

    public Map<Long, Long> SendToServer(TaskContainer taskMap) throws IOException {


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
                output.writeObject(taskMap);
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
            if (!loading || taskMap.getInstructionMap() != null) {
                socketConnection.close();
                break;
            }
        }

        return generatedTaskIDs;
    }

    public boolean loginToServer(TaskContainer taskMap) throws IOException {

        logger.debug("Creating socket to '" + controllerIP + "' on port " + controllerPort);
        boolean loading = true;
        boolean loggedIn = false;
        while (loading) {
            socketConnection = new Socket(controllerIP, controllerPort);

            output = new ObjectOutputStream(socketConnection.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socketConnection.getInputStream());

            logger.debug("Input- and outputstreams are ready...");

            try {
                output.writeInt(0);
                output.flush();
                output.writeObject(taskMap);
                output.flush();

            } catch (IOException IOExc) {
                loading = false;
                IOExc.printStackTrace();
                logger.debug("Could not send results...");
            }
            try {
                while (loading) {
                    loggedIn = input.readBoolean();
                    loading = false;
                }
            } catch (IOException IOExc) {
                IOExc.printStackTrace();
                loading = false;
            }
            if (loggedIn != false) {
                loading = false;
                logger.debug("Logged in to the service...");
            }
            if (!loading) {
                input.close();
                output.close();
                socketConnection.close();
                break;
            }
        }
        return loggedIn;
    }

    private static void setupStreams() throws IOException {

        output = new ObjectOutputStream(socketConnection.getOutputStream());
        output.flush();

        input = new ObjectInputStream(socketConnection.getInputStream());
        logger.debug("Input- and outputstreams are ready...");
    }

    public Socket stop() throws IOException {
        if (socketConnection != null) {
            socketConnection.close();
        }
        return socketConnection;
    }
}
