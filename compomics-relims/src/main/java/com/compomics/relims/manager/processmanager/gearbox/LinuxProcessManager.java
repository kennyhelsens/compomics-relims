/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.gearbox;

import com.compomics.relims.manager.processmanager.gearbox.enums.PriorityLevel;
import com.compomics.relims.manager.processmanager.gearbox.interfaces.ProcessManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kenneth
 */
public class LinuxProcessManager extends MainProcessManager implements ProcessManager {

    public LinuxProcessManager() {
    }

    @Override
    public boolean setPriority(PriorityLevel priority) {

        List<String> processList = new ArrayList<String>();
        processList.add("notepad.exe");

        for (String processName : processList) {
            try {
                try {
                    int PID = Integer.parseInt(processName);
                    Runtime.getRuntime().exec("renice " + priority.getLinuxCode() + " " + PID);
                    logger.debug("Changed process with " + PID + " to priority : " + priority);
                } catch (NumberFormatException NFE) {
                    Runtime.getRuntime().exec("renice " + priority.getLinuxCode() + "  $(pidof process_name)");
                    logger.debug("Changed " + processName + " to priority : " + priority);
                }
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
}
