/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.taskreciever;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.Task;
import com.compomics.relims.modes.networking.worker.general.ResourceManager;
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
class TaskHandler implements Runnable {

    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private Thread myThread = null;
    private final static Logger logger = Logger.getLogger(TaskHandler.class);

    public TaskHandler(Socket sock) throws IOException {
        this.sock = sock;
        //       sock.setSoTimeout(30000);
        sockOutput = new ObjectOutputStream(sock.getOutputStream());
        sockInput = new ObjectInputStream(sock.getInputStream());
        ResourceManager.setFinishState(null);
        this.myThread = new Thread(this);
        logger.debug("New handler created.");
    }

    public void start() {
        myThread.start();
    }

    @Override
    public void run() {
        Task newTask = null;
        logger.debug("Processing a task-request.");
        while (true) {
            try {
                //ONLY READ THIS STREAM IF THE TASKRECIEVER IS NOT BUSY ! = prevent error floods....
                if (!TaskReciever.locked) {
                    newTask = (Task) sockInput.readObject();
                    if (newTask != null) {
                        try {
                            TaskReciever.locked = true;
                            //Unlocked when the task is done...
                            System.out.println("Setting up worker to run for project " + newTask.getProjectID());
                            ResourceManager.setTaskID(newTask.getTaskID());
                            RelimsProperties.setUserID(newTask.getUserID());
                            TaskRunner taskRunner = new TaskRunner(newTask);
                            taskRunner.launch();
                        } catch (Exception e) {
                            TaskReciever.locked = false;
                        } finally {
                            //close input?
                        }
                    }
                }
            } catch (EOFException eof) {
                logger.debug("Error : " + eof);
                eof.printStackTrace();
            } catch (IOException ex) {
                logger.debug("Error : " + ex);
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                if (!ex.toString().contains("Connection reset")) {
                    ex.printStackTrace();
                } else {
                    //TODO HANDLE SOCKET RESET EXCEPTIONS
                    // switch to local mode perhaps?
                }
            } finally {
                if (sock.isClosed()) {
                    try {
                        logger.debug("Closing connection");
                        sock.close();
                    } catch (Exception e) {
                        logger.error("Exception while closing socket, e=" + e);
                        e.printStackTrace(System.err);
                    }
                }
            }
        }
    }
}
