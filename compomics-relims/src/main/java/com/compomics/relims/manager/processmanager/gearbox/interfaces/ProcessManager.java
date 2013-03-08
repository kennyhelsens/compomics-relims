/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.gearbox.interfaces;

import com.compomics.relims.manager.processmanager.gearbox.enums.PriorityLevel;



/**
 *
 * @author Kenneth
 */
public interface ProcessManager {

    boolean setPriority(PriorityLevel priority);

    boolean setPriorityFromPID(int PID, PriorityLevel priority);

    void removeProcess(String processName);

    void removeProcesses(String[] processNames);

    void addProcess(String processName);

    void addProcesses(String[] processNames);

    void killProcess(String processName);

    void killProcesses(String[] processNames);
    
    boolean isProcessRunning(String processName);
    
    boolean isOmssaRunning();
    
    boolean isXTandemRunning();
    
    
}
