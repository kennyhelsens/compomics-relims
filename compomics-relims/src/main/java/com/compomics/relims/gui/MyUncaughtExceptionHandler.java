package com.compomics.relims.gui;

import org.apache.log4j.Logger;

/**
 * This class is a
 */
public class MyUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static Logger logger = Logger.getLogger(MyUncaughtExceptionHandler.class);

    public void uncaughtException(Thread t, Throwable e) {
        if(e.getStackTrace()[0].getClassName().indexOf("BasicTextUI") > 0){
            logger.debug("ignoring AWT TextUI error");
            // do nothing!
        }else{
            logger.error("Uncaught exception in thread " + t.getName() + ": " + e.getMessage(), e);
        }
    }
}
