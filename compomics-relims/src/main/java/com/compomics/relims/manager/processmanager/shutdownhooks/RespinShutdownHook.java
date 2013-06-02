/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.shutdownhooks;

/**
 *
 * @author Kenneth
 */
public abstract class RespinShutdownHook extends Thread {

    @Override
    public void run() {
        handleOpenConnections();
        handleRunningThreads();
        handleJunk();
        System.out.println("Bye");
    }

    protected void handleOpenConnections() {
        System.out.println("Handling open connections should be done by an implementation of this class");
    }

    protected void handleRunningThreads() {
        System.out.println("Handling running threads should be done by an implementation of this class");
    }

    protected void handleJunk() {
        System.out.println("Handling leftovers should be done by an implementation of this class");
    }
}
