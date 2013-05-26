/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.resultmanager.storage.searchparameterstorage;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.IOException;

/**
 *
 * @author Kenneth
 */
public interface SearchParamStorage {

    SearchParameters retrieveParameters(String projectID) throws IOException;

    boolean storeParameters(String projectID, SearchParameters searchParameters) throws IOException;
   
    boolean hasBeenRun(String projectId);
    
}
