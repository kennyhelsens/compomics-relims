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

/**
 *
 * @author Kenneth
 */
public class WindowsProcessManager extends MainProcessManager implements ProcessManager {

    public WindowsProcessManager() {
        processList.add("omssacl.exe");
        processList.add("tandem.exe");
    }

    @Override
    public boolean setPriority(PriorityLevel priority) {

        for (String processName : processList) {
            try {
                //in case off ints (=PID)
                Runtime.getRuntime().exec("wmic process where name='" + processName + "' CALL setpriority " + priority.getWindowsCode());
                logger.debug("Changed " + processName + " to priority : " + priority);
            } catch (IOException ex) {
                logger.error("Could not change priority for process with name : " + processName);
            } finally {
                try {
                    Runtime.getRuntime().exec("wmic process where name='WMIC.exe' call terminate");
                    Runtime.getRuntime().exec("wmic process where name='conhost.exe' call terminate");
                } catch (IOException ex) {
                    logger.error("Could not close the windows WMIC.EXE");
                }
            }

        }
        return false;
    }

    public boolean setPriorityFromPID(int PID, PriorityLevel priority) {
        try {
            Runtime.getRuntime().exec("wmic process where processid='" + PID + "' CALL setpriority " + priority.getWindowsCode());
            logger.debug("Changed process with " + PID + " to priority : " + priority);
        } catch (IOException ex) {
            logger.error("Could not change priority for process with PID : " + PID);
        }
        return false;
    }

    @Override
    public void killProcess(String processName) {
        try {
            Runtime.getRuntime().exec("taskkill /PID " + processName);
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
            Process process = Runtime.getRuntime().exec("wmic process where name='" + serviceName + "' get /handle");
            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            try {
                String ps = br.readLine();
                if (ps.equalsIgnoreCase("handle")) {
                    ps = br.readLine();
                    int pid = Integer.valueOf(ps);
                    return true;
                } else {
                    return false;
                }
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
        return isProcessRunning("omssacl.exe");
    }

    @Override
    public boolean isXTandemRunning() {
        return isProcessRunning("xTandem.exe");
    }
}
