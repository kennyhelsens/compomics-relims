/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.processguard;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.relims.manager.processmanager.gearbox.MainProcessManager;
import com.compomics.relims.manager.processmanager.gearbox.enums.PriorityLevel;
import com.compomics.relims.manager.processmanager.gearbox.interfaces.ProcessManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class CommandExceptionGuard extends Thread implements Callable {

    /**
     * a plain logger
     */
    Logger logger = Logger.getLogger(CommandExceptionGuard.class);
    /**
     * progressManager to notify relims that the monitored process has failed...
     */
    private static ProgressManager progressManager = ProgressManager.getInstance();
    /**
     * process regular inputstream = process outputstream
     */
    private InputStream ois;
    /**
     * process error-related inputstream = process outputstream
     */
    private InputStream eis;
    /**
     * Type of the detected problem
     */
    private String type;
    /**
     * The process that has been hooked by this CommandExceptionGuard
     */
    private Process process;
    /**
     * Keywords that need to be monitored
     */
    private static final List<String> keyWords = new ArrayList<String>();
    /**
     * Timeout values (is in relimsproperties file)
     */
    PriorityLevel priority = RelimsProperties.getPriority();
    int jobTimeOutHours = RelimsProperties.getMaxJobHours();
    int jobTimeOutMin = RelimsProperties.getMaxJobMinutes();
    int maxTimeOutMinutes = jobTimeOutHours * 60 + jobTimeOutMin;
    long timeOut = (long) System.currentTimeMillis() + (long) maxTimeOutMinutes * 60L * 1000L;
    //returns a system dependent manager for the process...
    private ProcessManager manager = MainProcessManager.getPriorityManager();

    public CommandExceptionGuard(Process processus) {
        this.process = processus;
        this.ois = processus.getInputStream();
        this.eis = processus.getErrorStream();
        this.type = "ERROR";
        keyWords.add("error");
        keyWords.add("fatal");
        keyWords.add("please contact the developers");
        keyWords.add("Exception");
        keyWords.add("exception");

    }

    @Override
    public Object call() {
        boolean errorless = true;
        InputStream mergedInputStream = new SequenceInputStream(ois, eis);
        BufferedReader processOutputStream = new BufferedReader(new InputStreamReader(mergedInputStream));
        String line;
        logger.debug("An errorguard was hooked to the process. Timeout = " + maxTimeOutMinutes + " minutes.");
        while (errorless && !this.isInterrupted()) {
            Thread priorityThread = new Thread(new PrioritySetter());
            priorityThread.start();

            try {
                while ((line = processOutputStream.readLine()) != null) {
                    if (!line.isEmpty() || !line.equals("")) {
                        System.out.println(line);
                        //reset the timer if the process returned output 
                        resetTimer();
                    }
                    for (String aKeyword : keyWords) {
                        if (line.contains(aKeyword) || isTimeUp()) {
                            progressManager.setState(Checkpoint.PROCESSFAILURE);
                            process.destroy();
                            killSearchEngines();
                            return false;
                        }
                    }
                    //check if omssa / xtandem got stuck...     
                    //     checkProcesses();
                }
            } catch (IOException ex) {
                progressManager.setState(Checkpoint.PROCESSFAILURE, ex);
                errorless = false;
                process.destroy();
                break;
            }
            this.interrupt();
            return errorless;
        }
        return errorless;

    }

    public void release() {
        this.interrupt();
    }

    private boolean isTimeUp() {
        if (System.currentTimeMillis() < timeOut) {
            return false;
        } else {
            process.destroy();
            logger.error("The search has timed out after " + maxTimeOutMinutes + " minutes");
            progressManager.setEndState(Checkpoint.TIMEOUTFAILURE);
            return true;
        }
    }

    private void resetTimer() {
        timeOut = (long) System.currentTimeMillis() + (long) maxTimeOutMinutes * 60L * 1000L;
    }

    //kill omssa and xtandem if running...
    public void killSearchEngines() throws IOException {
        manager.killProcesses(new String[]{"omssacl.exe", "tandem.exe"});
    }

    private class PrioritySetter implements Runnable {

        @Override
        public void run() {
            try {
                priority = RelimsProperties.getPriority();
                while (!manager.isOmssaRunning()) {
                    Thread.sleep(500);
                }
                manager.setPriority(priority);
                while (!manager.isXTandemRunning()) {
                    Thread.sleep(500);
                }
                manager.setPriority(priority);
            } catch (Exception ex) {
                if (ex instanceof NullPointerException) {
                    //if OMSSA or XTandem isn't running, it returns a nullpointer...
                } else {
                    ex.printStackTrace();
                    logger.error(ex);
                }

            }
        }
    }
}
