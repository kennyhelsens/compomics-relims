/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.workerpool;

import java.util.Objects;

/**
 *
 * @author Kenneth
 */
public class WorkerRunner {

    private final String host;
    private final int port;

    public WorkerRunner(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.port;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final WorkerRunner other = (WorkerRunner) obj;
           
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (this.port != other.port) {
            return false;
        }
        return true;
    }
    
    
}
