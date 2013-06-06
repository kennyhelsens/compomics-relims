/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.connectivity.handlers;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.taskobjects.Task;
import com.compomics.relims.manager.resourcemanager.ResourceManager;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.relims.modes.networking.worker.connectivity.listener.PortListener;
import com.compomics.relims.modes.networking.worker.taskreciever.TaskRunner;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class TaskHandler implements Runnable {

    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private final static Logger logger = Logger.getLogger(TaskHandler.class);

    public TaskHandler(Socket sock) throws IOException {
        this.sock = sock;
        //       sock.setSoTimeout(30000);
        sockOutput = new ObjectOutputStream(sock.getOutputStream());
        sockInput = new ObjectInputStream(sock.getInputStream());
        ResourceManager.setFinishState(null);
        logger.debug("New handler created.");
    }

    @Override
    public void run() {
        Task newTask;
        TaskRunner taskRunner;
        boolean finished = false;
        logger.debug("Processing a task-request.");
        while (!finished) {
            try {
                Thread.sleep(10000);
                //ONLY READ THIS STREAM IF THE TASKRECIEVER IS NOT BUSY ! = prevent error floods....
                if (!PortListener.locked) {
                    newTask = (Task) sockInput.readObject();
                    if (newTask != null) {
                        PortListener.locked = true;
                        //Unlocked when the task is done...
                        logger.debug("Setting up worker to run for project " + newTask.getProjectID());
                        ProcessVariableManager.setProjectID(Long.parseLong(newTask.getProjectID()));
                        ResourceManager.setTaskID(newTask.getTaskID());
                        RelimsProperties.setUserID(newTask.getUserID());
                        taskRunner = new TaskRunner(newTask);
                        taskRunner.launch();
                    }
                }
            } catch (EOFException eof) {
                logger.error(eof);
            } catch (IOException ex) {
                logger.error(ex);
            } catch (ClassNotFoundException ex) {
                if (!ex.toString().contains("Connection reset")) {
                } else {
                    //TODO HANDLE SOCKET RESET EXCEPTIONS
                    // switch to local mode perhaps?
                }
            } catch (InterruptedException e) {
                logger.error(e);
            } finally {
                finished = true;
                closeConnection();
            }
        }
    }

    private void closeConnection() {
        if (!sock.isClosed()) {
            try {
                logger.debug("Closing connection");
                sock.close();
            } catch (Exception e) {
                if (sock != null) {
                    sock = null;
                }
                logger.error("Exception while closing socket, e=" + e);
                logger.error("Forced shutdown on socket");
            }
        }
    }
}