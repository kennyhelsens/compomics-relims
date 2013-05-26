/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.resultmanager.storage.spectrumstorage;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Kenneth
 */
public interface SpectrumStorage {

    File retrieveMGF(String projectID) throws IOException;

    boolean storeMGF(String projectID, File MGF) throws IOException;
   
    boolean hasBeenRun(String projectId);
    
}
