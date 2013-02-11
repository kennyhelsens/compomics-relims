/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.exception;

import com.compomics.relims.observer.Checkpoint;
import com.compomics.relims.observer.ProgressManager;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class RelimsExceptionHandler implements Thread.UncaughtExceptionHandler {

    Logger logger = Logger.getLogger(RelimsExceptionHandler.class);
    private static ProgressManager progressManager = ProgressManager.getInstance();

    public RelimsExceptionHandler(long taskID) {
    }

    public RelimsExceptionHandler() {
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        System.out.println("Throwable: " + e.getMessage());
        e.printStackTrace();
        System.out.println(t.toString());
    }

    public void throwException(Thread t, Throwable e) {
        logger.error("Relims has thrown an exception. This task needs to be reset !");
        logger.error(e.fillInStackTrace());
        progressManager.setState(Checkpoint.FAILED);;
    }
}
