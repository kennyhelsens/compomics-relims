/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.general;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.resultmanager.cleanup.CleanupManager;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class ProcessRelocalizer {

    private static File relimsTemp = RelimsProperties.getRelimsTempFolder();
    private static File relimsPepTemp = new File(relimsTemp.getAbsolutePath() + "/peptideshaker");
    private static File relimsSearchGuiTemp = new File(relimsTemp.getAbsolutePath() + "/searchgui");
    private static final Logger logger = Logger.getLogger(ProcessRelocalizer.class);
    private static File fastaFolder;
    private static File MGFFolder;
    private static File parameterFolder;
    private static File resultFolder;
    private static File prideAsaTemp;
    private static File prideAsaTempFolder;
    private static File loggingFile;

    public synchronized static void cleanCopy() {
        try {
            logger.debug("Making a process temp folder in " + relimsTemp.getAbsolutePath());
            File peptideShakerFolder = new File(RelimsProperties.getPeptideShakerFolder());
            File searchGuiFolder = new File(RelimsProperties.getSearchGuiFolder());
            //redirect relims to these folders
            RelimsProperties.setPeptideShakerFolder(relimsPepTemp.getAbsolutePath());
            RelimsProperties.setSearchGUIFolder(relimsSearchGuiTemp.getAbsolutePath());
            //clear the relimsTemp folder
            if (relimsTemp.exists()) {
                FileUtils.cleanDirectory(relimsTemp);
            } else {
                relimsTemp.mkdirs();
            }
            // copy the entire director
            FileUtils.copyDirectory(peptideShakerFolder, relimsPepTemp);
            FileUtils.copyDirectory(searchGuiFolder, relimsSearchGuiTemp);
            makeFastaFolder();
            makeMGFFolder();
            makeParametersFolder();
            makePrideAsapTempFolder();
            makeResultFolder();
        } catch (Exception e) {
            logger.error(e);
        } finally {
            relimsTemp.deleteOnExit();
        }
    }

    private static void makeFastaFolder() {
        fastaFolder = new File(relimsTemp + "/fasta/");
        if (!fastaFolder.exists()) {
            fastaFolder.mkdir();
        }
    }

    private static void makeMGFFolder() {
        MGFFolder = new File(relimsTemp + "/MGF/");
        if (!MGFFolder.exists()) {
            MGFFolder.mkdir();
        }
    }

    private static void makeParametersFolder() {
        parameterFolder = new File(relimsTemp + "/parameters/");
        if (!parameterFolder.exists()) {
            parameterFolder.mkdir();
        }
    }

    private static void makePrideAsapTempFolder() {
        prideAsaTempFolder = new File(relimsTemp + "/prideTemp/");
        if (!prideAsaTempFolder.exists()) {
            prideAsaTempFolder.mkdir();
        }
    }

    private static void makeResultFolder() {
        resultFolder = new File(relimsTemp + "/results/");
        if (!resultFolder.exists()) {
            resultFolder.mkdir();
        }
    }

    public static File getLocalResultFolder() {
        return resultFolder;
    }

    public static File getLocalFastaFolder() {
        return fastaFolder;
    }

    public static File getLocalMGFFolder() {
        return MGFFolder;
    }

    public static File getLocalParametersFolder() {
        return parameterFolder;
    }

    public static File localizeFasta(File fasta) throws IOException {
// Look in local folder if the fasta is already there
        for (File aFasta : fastaFolder.listFiles()) {
            if (fasta.getName().toLowerCase().equals(aFasta.getName().toLowerCase())) {
                return aFasta;
            }
        }
        // Reaching this point = no fasta with that name was found locally...
        File newLocalFasta = new File(fastaFolder.getAbsolutePath() + "/" + fasta.getName());
        FileUtils.copyFile(fasta, newLocalFasta);
        logger.info("Copied fasta to " + newLocalFasta.getAbsolutePath());
        return newLocalFasta;
    }

    public static File localizeMGF(File MGF) throws IOException {
// Look in local folder if the MGF is already there
        for (File aMGF : MGFFolder.listFiles()) {
            if (MGF.getName().toLowerCase().equals(aMGF.getName().toLowerCase())) {
                return aMGF;
            }
        }
        // Reaching this point = no MGF with that name was found locally...
        File newLocalMGF = new File(MGFFolder.getAbsolutePath() + "/" + MGF.getName());
        FileUtils.copyFile(MGF, newLocalMGF);
        logger.info("Copied MGF to " + newLocalMGF.getAbsolutePath());
        return newLocalMGF;
    }

    public static File localizeSearchParameters(File searchparameters) throws IOException {
        // Reaching this point = no searchparameters with that name was found locally...
        File newLocalParameters = new File(parameterFolder.getAbsolutePath() + "/" + searchparameters.getName());
        FileUtils.copyFile(searchparameters, newLocalParameters);
        logger.info("Copied searchparameters to " + newLocalParameters.getAbsolutePath());
        return newLocalParameters;
    }

    public static void uploadResults(String providerName) {
        while (true) {
            try {
                File remoteFolder = RelimsProperties.createWorkSpace(ProcessVariableManager.getProjectId(), providerName);
                //clean up resultfolder...
                CleanupManager.cleanResultFolder();
                FileUtils.copyDirectory(resultFolder, remoteFolder);
                break;
            } catch (IOException ex) {
                logger.error("Failed to upload results...Retrying");
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ex1) {
                }
            }
        }
        logger.info("Finished uploading results");
        logger.info("Clearing local result folders");
//clear resultfolder
        FileUtils.deleteQuietly(resultFolder);
        resultFolder.mkdir();
//clear MGF folder
        FileUtils.deleteQuietly(MGFFolder);
        MGFFolder.mkdir();
//clear ParametersFolder
        FileUtils.deleteQuietly(parameterFolder);
        parameterFolder.mkdir();
    }

    public static File getLocalPrideTempFolder() {
        return prideAsaTempFolder;
    }

    public static void setLocalLoggingFile(File loggingFile) {
        ProcessRelocalizer.loggingFile = loggingFile;
    }

    public static File getLocalLoggingFile() {
        return loggingFile;
    }
}
