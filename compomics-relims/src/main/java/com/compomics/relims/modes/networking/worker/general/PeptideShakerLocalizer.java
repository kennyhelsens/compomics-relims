/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.general;

import com.compomics.relims.conf.RelimsProperties;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class PeptideShakerLocalizer {

    private static File relimsPeptideShakerTemp = new File(RelimsProperties.getRelimsTempFolder().getAbsolutePath() + "/peptideshaker");
    private static final Logger logger = Logger.getLogger(PeptideShakerLocalizer.class);

    public synchronized static void cleanCopy() {
        try {
            logger.debug("Making a peptideshaker temp folder in " + relimsPeptideShakerTemp.getAbsolutePath());
            File peptideShakerFolder = new File(RelimsProperties.getPeptideShakerFolder());
            //clear the relimsPeptideShakerTemp folder
            if (relimsPeptideShakerTemp.exists()) {
                FileUtils.cleanDirectory(relimsPeptideShakerTemp);
            } else {
                relimsPeptideShakerTemp.mkdirs();
            }
            // copy the entire director
            FileUtils.copyDirectory(peptideShakerFolder, relimsPeptideShakerTemp);
            //set relims properties
            RelimsProperties.setPeptideShakerFolder(relimsPeptideShakerTemp.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e);
        } finally {
            relimsPeptideShakerTemp.deleteOnExit();
        }

    }
}
