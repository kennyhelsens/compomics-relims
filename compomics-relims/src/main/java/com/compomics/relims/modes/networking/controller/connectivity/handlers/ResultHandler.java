/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.handlers;

/**
 *
 * @author Kenneth
 */

import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.connectivity.workerpool.WorkerPool;
import com.compomics.relims.modes.networking.controller.connectivity.workerpool.WorkerRunner;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

public class ResultHandler implements Runnable {

    private final static DatabaseService dds = DatabaseService.getInstance();
    private final static Logger logger = Logger.getLogger(ResultHandler.class);
    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private Thread myThread = null;

    public ResultHandler(Socket sock, ObjectOutputStream sockOutput, ObjectInputStream sockInput) throws IOException {
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
        logger.debug("Handler is processing...");

        //   while (true) {
        try {
            try {
                HashMap<String, Object> resultMap = (HashMap<String, Object>) sockInput.readObject();

                long taskID = (Long) resultMap.get("taskID");
                int workerPort = (Integer) resultMap.get("workerPort");
                String finishState = (String) resultMap.get("finishState");
                String projectID = dds.getProjectID(taskID);
                try {
                    projectID = dds.getProjectID(taskID);
                   // dds.storeErrorList((List<ConversionError>) resultMap.get("PrideXMLErrorList"), projectID);
                } catch (Exception e) {
                    projectID = "Unknown projectID";
                }

                if (finishState == null || finishState.equalsIgnoreCase("FAILED")) {
                    // set task to finished and worker back to IDLE
                    dds.updateTask(taskID, Checkpoint.FAILED.toString());
                    logger.debug("Task " + taskID + " was set to failed and re-entered the qeue for project : " + projectID);
                    dds.storeStatistics(resultMap, sock.getInetAddress().getHostName());
                    dds.storeErrorList((List<ConversionError>) resultMap.get("PrideXMLErrorList"), projectID);
                    WorkerRunner runner = new WorkerRunner(sock.getInetAddress().getHostName(), workerPort);
                    WorkerPool.setWorkerState(runner, Checkpoint.FAILED);
                }

                if (finishState != null && finishState.equalsIgnoreCase(Checkpoint.FINISHED.toString())) {
                    // set task to finished and worker back to IDLE
                    dds.updateTask(taskID, Checkpoint.FINISHED.toString());
                    WorkerRunner runner = new WorkerRunner(sock.getInetAddress().getHostName(), workerPort);
                    logger.debug("Task " + taskID + " : attempting to store data for project : " + projectID);
                    //store statistics !                  
                    dds.storeStatistics(resultMap, sock.getInetAddress().getHostName());
                    dds.storeErrorList((List<ConversionError>) resultMap.get("PrideXMLErrorList"), projectID);
                    WorkerPool.setWorkerState(runner, Checkpoint.IDLE);
                }

                if (finishState != null && finishState.equalsIgnoreCase(Checkpoint.PRIDEFAILURE.toString())) {
                    // set task to PrideFailure and worker back to IDLE
                    dds.updateTask(taskID, Checkpoint.PRIDEFAILURE.toString());
                    WorkerRunner runner = new WorkerRunner(sock.getInetAddress().getHostName(), workerPort);
                    logger.debug("Task " + taskID + " : could not be run. Pride-asa-pipeline could not provide workable files for project : " + projectID);
                    //store statistics !        
                    dds.storeStatistics(resultMap, projectID);
                    dds.storeErrorList((List<ConversionError>) resultMap.get("PrideXMLErrorList"), projectID);
                    //delete the worker from the active database
                    dds.deleteWorker(runner.getHost(), runner.getPort());
                    WorkerPool.setWorkerState(runner, Checkpoint.IDLE);
                }

                logger.debug("Handled task");

            } catch (Exception ex) {
                logger.error("Could not reset task and worker...");
                ex.printStackTrace();
            }
        } finally {

            try {
                sock.close();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
        }
        //  }
    }
}
