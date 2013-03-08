/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.worker.feedbackproviders.HeartbeatGenerator;
import com.compomics.relims.modes.networking.worker.general.ResourceManager;
import com.compomics.relims.modes.networking.worker.taskreciever.TaskReciever;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class RelimsWorkerMode {

    private final static Logger logger = Logger.getLogger(RelimsWorkerMode.class);
    private static int workerPort;
    public static boolean connected = false;
    public static Map<String, Object> cliArgumentMap = new HashMap<String, Object>();

    public static void main(String[] args) {

        RelimsProperties.initialize();
        if (!RelimsProperties.getDebugMode()) {
            Logger.getRootLogger().setLevel(Level.ERROR);
        }
        try {
            workerPort = RelimsProperties.getWorkerPort();
            for (int i = 0; i < args.length; i++) {
                if (args[i].equalsIgnoreCase("-workerport")) {
                    String workerportString = args[i + 1];
                    logger.debug("Attempting to start on port : " + workerportString);
                    workerPort = Integer.parseInt(workerportString);
                }
            }
            ResourceManager.setWorkerPort(workerPort);
            launchWorkerCLI();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            logger.error("Not a valid commandline.");
            logger.error("Commandline has following input : ");
            System.out.println("");
            System.out.println("java -jar remoterelimsworker.jar -workerport x");
            System.out.println("x = the portnumber you want to run this software on");
            System.out.println("");
        }

    }

    public static void launchListeners() {
        logger.debug("Port was accepted...");
        //Start the registrator
        workerPort = ResourceManager.getWorkerPort();
        ResourceManager.setWorkerPort(workerPort);
        Thread registrator = new Thread(new HeartbeatGenerator());
        registrator.start();
        Thread reciever = new Thread(new TaskReciever());
        reciever.start();
        logger.debug("Started taskreciever-service...");
    }

    public static void launchWorkerCLI() {
        int port = ResourceManager.getWorkerPort();
        ArrayList<String> errors = new ArrayList<String>();
        int workerPort = 0;
        try {
            workerPort = port;
            if (workerPort > 65535 || workerPort < 1000) {
                logger.error("Number is not in range [1000-65535]...");
            }
        } catch (ClassCastException cce) {
            logger.error("Please specify a correct portnumber...");
            errors.add(cce.getMessage());
        } catch (NumberFormatException NFE) {
            logger.error("Please specify a correct portnumber...");
            errors.add(NFE.getMessage());
        } catch (NullPointerException NPE) {
            NPE.printStackTrace();
            logger.error("Please specify a correct portnumber...");
            errors.add(NPE.getMessage());
        } finally {
            if (errors.isEmpty()) {
                RelimsWorkerMode.connected = true;
                ResourceManager.setWorkerPort(workerPort);
                launchListeners();
            } else {
                RelimsWorkerMode.connected = false;
            }
        }

    }
}
