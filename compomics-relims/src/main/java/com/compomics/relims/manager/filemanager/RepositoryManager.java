/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.filemanager;

import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.relims.conf.RelimsProperties;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.logging.Level;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class RepositoryManager {

    /**
     * a plain logger
     */
    private final static Logger logger = Logger.getLogger(RepositoryManager.class);
    /**
     * The path to the general-repository (local)
     */
    private static File repositoryMain;
    private static File repositoryMSLIMS;
    private static File repositoryPRIDE;

    public static void initializeRepository() {

        String MSLimsRepositoryString = RelimsProperties.getRepositoryPath() + "/MSLIMS/";
        String PrideRepositoryString = RelimsProperties.getRepositoryPath() + "/PRIDE/";
        String commonRepositoryString = RelimsProperties.getRepositoryPath();
        repositoryMain = new File(commonRepositoryString);
        repositoryMSLIMS = new File(MSLimsRepositoryString);
        repositoryPRIDE = new File(PrideRepositoryString);

        //check if the repository is there and exists
        logger.debug("Repositories at : ");
        logger.debug(commonRepositoryString);
        logger.debug(MSLimsRepositoryString);
        logger.debug(PrideRepositoryString);


        if (!repositoryMain.exists()) {

            try {
                repositoryMSLIMS.mkdirs();
                repositoryPRIDE.mkdir();
                logger.info("Repository was built");
            } catch (Exception e) {
                logger.info("Repository could not be initiallized");
                return;
            }
        }
        logger.info("Repository was located");
    }

    public static void storeFlagInRepository(String Flag, String provider, File resultsFolder, long ProjectID) {
        File repositoryDirectory;
        if (provider.toString().equalsIgnoreCase("mslims")) {
            repositoryDirectory = repositoryMSLIMS;
        } else {
            repositoryDirectory = repositoryPRIDE;
        }
        File flagFile = new File(repositoryDirectory.getAbsolutePath() + "/" + ProjectID, Flag);
        if (flagFile.exists()) {
            System.out.println("Created flagfile in the repositoryfolder...");
        }
    }

    public static void copyToRepository(String provider, File resultsFolder, long ProjectID) {

        File repositoryDirectory;

        File[] mgffiles = resultsFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".mgf");
            }
        });
        File mgfFile = mgffiles[0];

        File[] parameterfiles = resultsFolder.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".parameters");
            }
        });
        File parametersFile = parameterfiles[0];

        //store the files    

        if (provider.toString().equalsIgnoreCase("mslims")) {
            repositoryDirectory = repositoryMSLIMS;
        } else {
            repositoryDirectory = repositoryPRIDE;
        }
        //make the project folder if it doesn't exist yet...

        File projectSpecificRepository = new File(repositoryDirectory.getAbsolutePath() + "/" + ProjectID);
        if (!projectSpecificRepository.exists()) {
            projectSpecificRepository.mkdir();
            try {
                //store the FileArray
                storeToRepository(mgfFile, parametersFile, projectSpecificRepository, ProjectID);
                logger.info("Project was saved into local repository");
            } catch (IOException ex) {
                logger.error("Could not store files into the repository");
            }
        }


    }

    public static void storeToRepository(File MGFFile, File parametersFile, File storageLocation, long projectID) throws IOException {

        File mgfRepositoryFile = new File(storageLocation.getAbsolutePath() + "/" + projectID + ".mgf");
        File parametersRepositoryFile = new File(storageLocation.getAbsolutePath() + "/SearchGUI.parameters");

        FileUtils.copyFile(MGFFile, mgfRepositoryFile, true);
        FileUtils.copyFile(parametersFile, parametersRepositoryFile, true);

    }

    public static boolean hasBeenRun(String provider, long projectID) {
        File repositoryDirectory;

        try {
            File resultFolder = new File(ProcessVariableManager.getResultsFolder());

            if (provider.contains("mslims")) {
                repositoryDirectory = repositoryMSLIMS;
            } else {
                repositoryDirectory = repositoryPRIDE;
            }
            File projectRepositoryDirectory = new File(repositoryDirectory.getAbsolutePath() + "/" + projectID);

            if (projectRepositoryDirectory.exists()) {
                try {
                    //copy the files into the running project's repository
                    FileUtils.copyDirectory(projectRepositoryDirectory, resultFolder, true);
                    return true;
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(RepositoryManager.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            } else {
                return false;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
