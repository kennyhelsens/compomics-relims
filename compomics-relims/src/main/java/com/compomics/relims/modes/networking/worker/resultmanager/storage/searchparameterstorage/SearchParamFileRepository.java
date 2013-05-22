/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.resultmanager.storage.searchparameterstorage;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.util.experiment.identification.SearchParameters;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class SearchParamFileRepository implements SearchParamStorage {

    private final File repository;
    private String provider = "pride";
    private File projectRepositoryDirectory;
    private static final Logger logger = Logger.getLogger(SearchParamFileRepository.class);

    public SearchParamFileRepository(File repository, String provider) {
        this.repository = repository;
        this.provider = provider;
    }

    @Override
    public SearchParameters retrieveParameters(String projectID) throws IOException {
        File storageLocation = new File(projectRepositoryDirectory.getAbsolutePath() + "/SearchGUI.parameters");
        try {
            return SearchParameters.getIdentificationParameters(storageLocation);
        } catch (FileNotFoundException | ClassNotFoundException ex) {
            logger.error(ex);
            return null;
        }
    }

    @Override
    public boolean storeParameters(String projectID, SearchParameters searchParameters) throws IOException {
        if (!projectRepositoryDirectory.exists()) {
            projectRepositoryDirectory.mkdirs();
        }
        File storageLocation = new File(projectRepositoryDirectory.getAbsolutePath() + "/SearchGUI.parameters");
        try {
            SearchParameters.saveIdentificationParameters(searchParameters, storageLocation);
            return true;
        } catch (FileNotFoundException | ClassNotFoundException ex) {
            logger.error(ex);
            return false;
        }
    }

    @Override
    public boolean hasBeenRun(String projectID) {
        File repositoryDirectory;
        try {
            File resultFolder = RelimsProperties.getWorkSpace();
            if (provider.contains("mslims")) {
                repositoryDirectory = new File(repository.getAbsolutePath() + "/mslims/");
            } else {
                repositoryDirectory = new File(repository.getAbsolutePath() + "/pride/");
            }
            projectRepositoryDirectory = new File(repositoryDirectory.getAbsolutePath() + "/" + projectID);

            if (projectRepositoryDirectory.exists()) {
                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
