package com.compomics.relims.manager.processmanager.processguard;

/**
 * This class is a
 */
public class RelimsException extends RuntimeException{

    public RelimsException() {
    }

    public RelimsException(String s) {
        super(s);
    }

    public RelimsException(String s, Throwable aThrowable) {
        super(s, aThrowable);
    }

    public RelimsException(Throwable aThrowable) {
        super(aThrowable);
    }
}
