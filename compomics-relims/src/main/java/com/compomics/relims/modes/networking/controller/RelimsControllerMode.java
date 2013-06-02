/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.security.BackupService;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.connectivity.listeners.PortListener;
import com.compomics.relims.modes.networking.controller.workerpool.WorkerPool;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class RelimsControllerMode {

    private static DatabaseService dds;
    private static BackupService bs;
    private static Logger logger = Logger.getLogger(RelimsControllerMode.class);
    private static ExecutorService bootingService;

    public static void main(String[] args) {
        RelimsProperties.setNetworkingMode(RelimsProperties.NetworkMode.CONTROLLER);
        RelimsProperties.initialize(false);
        if (!RelimsProperties.getDebugMode()) {
            Logger.getRootLogger().setLevel(Level.INFO);
        }
        dds = DatabaseService.getInstance();
        bs = BackupService.getInstance();
        logger = Logger.getLogger(RelimsControllerMode.class);
        bootingService = Executors.newSingleThreadExecutor();
        try {

            System.out.println("-----------------------------------");
            System.out.println("------------RemoteRelims-----------");
            System.out.println("-----------------------------------");
            System.out.println("");
            System.out.println("-----------------------------------");
            System.out.println("---------STARTUP PROCEDURE---------");
            System.out.println("-----------------------------------");
            System.out.println("");

            logger.info("Validating properties file...");
            //++++++++++++++++++++BOOTING THE DATABASE
//making a connection

            Future future = bootingService.submit(new Runnable() {
                public void run() {
                    dds.launch();
                    logger.info("Booted database...");
                }
            });
            while (future.get() != null) {
                // wait for the future   
            }
//creating tables
            future = bootingService.submit(new Runnable() {
                public void run() {
                    dds.createTables();
                }
            });
            while (future.get() != null) {
                // wait for the future   
            }

            logger.info("Loaded Database...");
            logger.info("Checking for incorrectly shut down tasks...");
            future = bootingService.submit(new Runnable() {
                public void run() {
                    dds.startupSweep();
                }
            });
            while (future.get() != null) {
                // wait for the future   
            }
            logger.info("Booting connection service...");
            future = bootingService.submit(new Runnable() {
                @Override
                public void run() {
                    PortListener cListener = new PortListener();
                    cListener.start();
                }
            });
            while (future.get() != null) {
                // wait for the future   
            }

            logger.info("Loading job manager...");
            future = bootingService.submit(new Runnable() {
                @Override
                public void run() {
                    WorkerPool workerPool = WorkerPool.getInstance();
                    workerPool.updateFromDb();
                }
            });

            //start backupservice
            future = bootingService.submit(new Runnable() {
                @Override
                public void run() {
                    Thread databaseBackup = new Thread(bs);
                    databaseBackup.start();
                }
            });
            while (future.get() != null) {
                // wait for the future   
            }
            Thread.sleep(2000);
            System.out.println(" ");
            System.out.println("-----------------------------------");
            System.out.println("---------RUNNING PROCEDURE---------");
            System.out.println("-----------------------------------");
            System.out.println("");
        } catch (Exception ex) {
            ex.printStackTrace();

        }
    }

    public static void stopController() {
        bootingService.shutdownNow();
        logger.info("Controller interrupted");
    }
}
