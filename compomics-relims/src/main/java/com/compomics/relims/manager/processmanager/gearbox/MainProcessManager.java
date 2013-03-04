/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.gearbox;


import com.compomics.relims.manager.processmanager.gearbox.interfaces.ProcessManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public abstract class MainProcessManager {

    static String OPERATING_SYSTEM = System.getProperty("os.name");
    static List<String> processList = new ArrayList<String>();
    MainProcessManager priorityManager;
    protected final static Logger logger = Logger.getLogger(MainProcessManager.class);

    public static ProcessManager getPriorityManager() {
        if (OPERATING_SYSTEM.toLowerCase().contains("windows")) {
            return new WindowsProcessManager();
        } else if (OPERATING_SYSTEM.toLowerCase().contains("linux")) {
            return new LinuxProcessManager();
        } else if (OPERATING_SYSTEM.toLowerCase().contains("mac")) {
            return new MacProcessManager();
        }
        return null;
    }

    public void setPriority() {
    }

    public void addProcess(String processName) {
        processList.add(processName);
    }

    public void addProcesses(String[] processNames) {
        processList.addAll(Arrays.asList(processNames));
    }

    public void removeProcess(String processName) {
        processList.remove(processName);
    }

    public void removeProcesses(String[] processNames) {
        processList.removeAll(Arrays.asList(processNames));
    }
}