/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.resultmanager.cleanup;

import com.compomics.relims.conf.RelimsProperties;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class CleanupManager {

    private static File workingFolder;
    private static final Logger logger = Logger.getLogger(CleanupManager.class);

    public static void cleanResultFolder() {
        //get current workingFolder
        workingFolder = RelimsProperties.getWorkSpace();
        logger.debug("Cleaning up resultfiles at : " + workingFolder.getAbsolutePath());
        //delete MGF-folder
        logger.debug("Deleting " + workingFolder.getAbsolutePath() + "/mgf");
        try {
            FileUtils.deleteDirectory(new File(workingFolder.getAbsolutePath() + "/mgf"));
        } catch (IOException ex) {
            logger.error("Could not delete MGF-folder : " + ex.getMessage());
        }
        //delete searchEngine-Files
        File[] fileList = workingFolder.listFiles();
        logger.debug("Deleting search engine results");
        for (File aFile : fileList) {
            if (!aFile.isDirectory()
                    && (aFile.getAbsolutePath().toUpperCase().endsWith(".OMX")
                    || aFile.getAbsolutePath().toUpperCase().endsWith(".T.XML"))) {
                logger.error("Deleting " + aFile.getAbsolutePath());
                FileUtils.deleteQuietly(aFile);
            }
        }
        //delete searchGui-Files
        fileList = workingFolder.listFiles();
        logger.debug("Deleting searchGUI files");
        for (File aFile : fileList) {
            if (!aFile.isDirectory()
                    && (aFile.getAbsolutePath().toUpperCase().endsWith("MODS.XML")
                    || aFile.getAbsolutePath().toUpperCase().endsWith(".T.XML"))
                    || aFile.getAbsolutePath().toUpperCase().contains("SEARCHGUI_INPUT")
                    || aFile.getAbsolutePath().toUpperCase().contains("SEARCHGUI_REPORT")) {
                logger.error("Deleting " + aFile.getAbsolutePath());
                FileUtils.deleteQuietly(aFile);
            }
        }
    }
}
