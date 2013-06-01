/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.resultmanager.storage.spectrumstorage;

import com.compomics.relims.conf.RelimsProperties;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class SpectrumFileRepository implements SpectrumStorage {

    private final File repository;
    private String provider = "pride";
    private File projectRepositoryDirectory;
    private static final Logger logger = Logger.getLogger(SpectrumFileRepository.class);

    public SpectrumFileRepository(File repository, String provider) {
        this.repository = repository;
        this.provider = provider;
        this.projectRepositoryDirectory = new File(repository.getAbsolutePath() + "/" + provider + "/");
    }

    @Override
    public File retrieveMGF(String projectID) throws IOException {
        return new File(projectRepositoryDirectory.getAbsolutePath() + "/" + projectID + "/" + projectID + ".mgf");
    }

    @Override
    public boolean storeMGF(String projectID, File MGF) throws IOException {
        if (!projectRepositoryDirectory.exists()) {
            projectRepositoryDirectory.mkdirs();
        }
        File storageLocation = new File(projectRepositoryDirectory.getAbsolutePath() + "/" + projectID + "/" + projectID + ".mgf");

        try {
            FileUtils.copyFile(MGF, storageLocation, true);
            return true;
        } catch (Exception e) {
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
