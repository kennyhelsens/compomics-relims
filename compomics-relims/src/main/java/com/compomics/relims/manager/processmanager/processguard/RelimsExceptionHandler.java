/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.processguard;

import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
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
        logger.error(e);
    }

    public void throwException(Thread t, Throwable e) {
        logger.error("Relims has thrown an exception. This task needs to be reset !");
        logger.error(e.fillInStackTrace());
        progressManager.setState(Checkpoint.FAILED);;
    }
}
