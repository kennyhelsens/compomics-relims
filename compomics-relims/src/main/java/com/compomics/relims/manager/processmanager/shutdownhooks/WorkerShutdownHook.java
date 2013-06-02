/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.shutdownhooks;

import com.compomics.relims.concurrent.Command;
import com.compomics.relims.modes.networking.worker.feedbackproviders.HeartbeatGenerator;
import com.compomics.relims.modes.networking.worker.feedbackproviders.ResultNotifier;
import com.compomics.relims.modes.networking.worker.general.ProcessRelocalizer;
import com.compomics.relims.modes.networking.worker.taskreciever.TaskReciever;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class WorkerShutdownHook extends RespinShutdownHook {

    private static final Logger LOGGER = Logger.getLogger(WorkerShutdownHook.class);

    @Override
    protected void handleOpenConnections() {
        //close listeners and handlers?
        TaskReciever.shutdown();
        LOGGER.info("Shutting down taskreciever");
        //shut down heartbeats and resultnotifier
        HeartbeatGenerator.shutdown();
        LOGGER.info("Shutting down heartbeatgenerator");
        //TODO Notify controller that this server is out ?
        ResultNotifier.shutdown();
        LOGGER.info("Shutting down resultnotifier");
    }

    @Override
    protected void handleRunningThreads() {
        //find a way to gracefully cancel a command --> send ctr+c ?
        //how to kill subprocesses then?
        Command.cancel();

    }

    @Override
    protected void handleJunk() {
        //empty the resultfolder and tempfolder
        ProcessRelocalizer.getLocalMGFFolder().delete();
        ProcessRelocalizer.getLocalParametersFolder().delete();
        ProcessRelocalizer.getLocalPrideTempFolder().delete();
        ProcessRelocalizer.getLocalResultFolder().delete();
        LOGGER.info("Shutting down processrelocalizer");
    }
}
