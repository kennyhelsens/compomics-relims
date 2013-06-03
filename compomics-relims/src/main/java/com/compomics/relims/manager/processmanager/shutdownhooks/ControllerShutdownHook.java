/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.shutdownhooks;

import com.compomics.relims.concurrent.Command;
import com.compomics.relims.manager.usernotificationmanager.MailEngine;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.connectivity.listeners.PortListener;
import com.compomics.relims.modes.networking.controller.workerpool.WorkerPool;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class ControllerShutdownHook extends RespinShutdownHook {

    private static final Logger LOGGER = Logger.getLogger(ControllerShutdownHook.class);
    private DatabaseService dbs = DatabaseService.getInstance();

    @Override
    protected void handleOpenConnections() {
        try {
            //Notify via mail 
            MailEngine.sendMail(new String[]{}, "The controller has commenced shutting down !", "Controller was shut down", null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        //shutdown workerpool
        WorkerPool.shutdownPool();
        LOGGER.info("Shutting down workerpool");
        //shutdown databases and carefully make backup
        dbs.shutdownDatabase();
        LOGGER.info("Shutting down databases");
    }

    @Override
    protected void handleRunningThreads() {
        PortListener.shutdown();
        LOGGER.info("Shutting down portlistener");
        Command.cancel();
        LOGGER.info("Shutting down running commands");
    }

    @Override
    protected void handleJunk() {
        System.out.println("Handling leftovers is not yet implemented !");
    }
}
