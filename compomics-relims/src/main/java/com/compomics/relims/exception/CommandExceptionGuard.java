/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.exception;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.observer.Checkpoint;
import com.compomics.relims.observer.ProgressManager;
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
    private static final List<String> keyWords = new ArrayList<>();
    /**
     * Timeout values (is in relimsproperties file)
     */
    int jobTimeOutHours = RelimsProperties.getMaxJobHours();
    int jobTimeOutMin = RelimsProperties.getMaxJobMinutes();
    int maxTimeOutMinutes = jobTimeOutHours * 60 + jobTimeOutMin;
    long timeOut = (long) System.currentTimeMillis() + (long) maxTimeOutMinutes * 60L * 1000L;

    public CommandExceptionGuard(Process processus) {
        this.process = processus;
        this.ois = processus.getInputStream();
        this.eis = processus.getErrorStream();
        this.type = "ERROR";
        keyWords.add("error");
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
        System.out.println("An errorguard was hooked to the process. Timeout = " + maxTimeOutMinutes + " minutes.");
        while (errorless && !this.isInterrupted()) {
            try {
                while ((line = processOutputStream.readLine()) != null) {
                    if (!line.isEmpty() || !line.equals("")) {
                        System.out.println(line);
                    }
                    for (String aKeyword : keyWords) {
                        if (line.contains(aKeyword) || isTimeUp()) {
                            progressManager.setState(Checkpoint.SEARCHGUIFAILURE);
                            process.destroy();
                            killSearchEngines();
                            return false;
                        }
                    }
                }
            } catch (IOException ex) {
                progressManager.setState(Checkpoint.SEARCHGUIFAILURE, ex);
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
            System.out.println("The search has timed out after " + maxTimeOutMinutes + " minutes");
            logger.error("The search has timed out after " + maxTimeOutMinutes + " minutes");
            progressManager.setEndState(Checkpoint.TIMEOUTFAILURE);
            return true;
        }
    }

    //kill omssa and xtandem if running...
    public void killSearchEngines() throws IOException {
        Runtime rt = Runtime.getRuntime();
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
            rt.exec("taskkill omssacl.exe");
            rt.exec("taskkill tandem.exe");
        } else {
            //  rt.exec("kill -9 ");
        }
    }
}