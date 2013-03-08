/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.gearbox;

import com.compomics.relims.manager.processmanager.gearbox.enums.PriorityLevel;
import com.compomics.relims.manager.processmanager.gearbox.interfaces.ProcessManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kenneth
 */
public class LinuxProcessManager extends MainProcessManager implements ProcessManager {

    public LinuxProcessManager() {
        processList.add("omssacl");
        processList.add("tandem");
    }

    @Override
    public boolean setPriority(PriorityLevel priority) {

        for (String processName : processList) {
            try {
                Runtime.getRuntime().exec("renice " + priority.getLinuxCode() + "  $(pidof " + processName + ")");
                logger.debug("Changed " + processName + " to priority : " + priority);
            } catch (IOException ex) {
                logger.error("Could not change priority for process with name : " + processName);
            }

        }
        return false;
    }

    public boolean setPriorityFromPID(int PID, PriorityLevel priority) {
        try {
            Runtime.getRuntime().exec("renice " + priority.getLinuxCode() + " " + PID);
            logger.debug("Changed process with " + PID + " to priority : " + priority);
        } catch (IOException ex) {
            logger.error("Could not change priority for process with PID : " + PID);
        }
        return false;
    }

    @Override
    public void killProcess(String processName) {
        try {
            Runtime.getRuntime().exec("kill -9 " + processName);
        } catch (IOException ex) {
            logger.error("Could not kill for process with name : " + processName);
        }
    }

    @Override
    public void killProcesses(String[] processNames) {
        for (String aProcessName : processNames) {
            killProcess(aProcessName);
        }
    }

    @Override
    public boolean isProcessRunning(String serviceName) {
        try {
            //in other words, is the PID in the tasklist ! 
            Process process = Runtime.getRuntime().exec("$(pidof " + serviceName + ")");
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String ps = br.readLine();
            try {
                int pid = Integer.valueOf(ps);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        } catch (IOException ex) {
            logger.error(ex);
            return false;
        }
    }
    
       @Override
    public boolean isOmssaRunning() {
        return isProcessRunning("omssacl");
    }

    @Override
    public boolean isXTandemRunning() {
        return isProcessRunning("xTandem");
    }
    
}
