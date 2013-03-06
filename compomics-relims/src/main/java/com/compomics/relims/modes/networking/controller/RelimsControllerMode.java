/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.security.BackupService;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.connectivity.listeners.PortListener;
import com.compomics.relims.modes.networking.controller.connectivity.workerpool.WorkerPool;
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
    private static Logger classLogger;
    private static ExecutorService bootingService;

    public static void main(String[] args) {
        Logger.getRootLogger().setLevel(Level.ERROR);
        RelimsProperties.initialize();
        dds = DatabaseService.getInstance();
        bs = BackupService.getInstance();
        classLogger = Logger.getLogger(RelimsControllerMode.class);
        bootingService = Executors.newSingleThreadExecutor();
        try {
            //set logger


            /*        if (args[1].equalsIgnoreCase("-nogui")) {
             } else {
             if (args.length <= 0) {
             MAINGUI maingui = new MAINGUI();
             }
             }*/

            System.out.println("-----------------------------------");
            System.out.println("------------RemoteRelims-----------");
            System.out.println("-----------------------------------");
            System.out.println("");
            System.out.println("-----------------------------------");
            System.out.println("---------STARTUP PROCEDURE---------");
            System.out.println("-----------------------------------");
            System.out.println("");

            System.out.println("Validating properties file...");
            //if driver in Config file is missing ---> remake using default settings !

            //++++++++++++++++++++BOOTING THE DATABASE
//making a connection

            Future future = bootingService.submit(new Runnable() {
                public void run() {
                    dds.launch();
                    System.out.println("Booted database...");
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
//creating root user
            future = bootingService.submit(new Runnable() {
                public void run() {
                    dds.createRootUser();
                }
            });
            while (future.get() != null) {
                // wait for the future   
            }
            System.out.println("Loaded Database...");
            System.out.println("Checking for incorrectly shut down tasks...");
            future = bootingService.submit(new Runnable() {
                public void run() {
                    dds.startupSweep();
                }
            });
            while (future.get() != null) {
                // wait for the future   
            }
            System.out.println("Booting connection service...");
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

            System.out.println("Loading job manager...");
            future = bootingService.submit(new Runnable() {
                @Override
                public void run() {
                    WorkerPool workerPool = WorkerPool.getInstance();
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
}
