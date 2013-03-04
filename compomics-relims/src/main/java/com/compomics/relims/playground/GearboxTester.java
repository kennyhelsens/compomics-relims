/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.playground;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.processmanager.gearbox.MainProcessManager;
import com.compomics.relims.manager.processmanager.gearbox.enums.PriorityLevel;
import com.compomics.relims.manager.processmanager.gearbox.interfaces.ProcessManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class GearboxTester {

    private static ProcessManager manager = MainProcessManager.getPriorityManager();
    private static PriorityLevel priority;
    private final static Logger logger = Logger.getLogger(GearboxTester.class);

    public static void main(String[] args) {
        try {
            priority = RelimsProperties.getPriority();
            priority = PriorityLevel.REAL_TIME;
            boolean found = false;
            logger.debug("Waiting for Omssa");
            while (!isProcessRunning("omssacl.exe")) {
                Thread.sleep(500);
            }
            logger.debug("Omssa is running !");
            manager.addProcess("omssacl.exe");
            manager.setPriority(priority);
            manager.removeProcess("omssacl.exe");
            logger.debug("Waiting for xTandem");

            while (!isProcessRunning("tandem.exe")) {
                Thread.sleep(500);
            }
            logger.debug("xTandem is running !");
            manager.addProcess("tandem.exe");
            manager.setPriority(priority);
            manager.removeProcess("xTandem.exe");
            System.exit(0);
        } catch (Exception ex) {
            logger.error(ex);
        }
    }

    public static boolean isProcessRunning(String serviceName) throws Exception {

        Process p = Runtime.getRuntime().exec("TASKLIST");
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(serviceName)) {
                return true;
            }
        }
        return false;

    }

    //listen for tandem !
    public static void setPriority() {
        manager.addProcesses(new String[]{"omssacl.exe", "tandem.exe"});
        manager.setPriority(priority);
    }
}
