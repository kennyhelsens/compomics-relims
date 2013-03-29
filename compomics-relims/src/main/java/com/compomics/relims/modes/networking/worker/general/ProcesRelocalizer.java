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
public class ProcesRelocalizer {

    private static File relimsTemp = RelimsProperties.getRelimsTempFolder();
    private static File relimsPepTemp = new File(relimsTemp.getAbsolutePath() + "/peptideshaker/"+RelimsProperties.getPeptideShakerArchive().replace(".jar",""));
    private static File relimsSearchGuiTemp = new File(relimsTemp.getAbsolutePath() + "/searchgui");
    private static final Logger logger = Logger.getLogger(ProcesRelocalizer.class);

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
        } catch (Exception e) {
            logger.error(e);
        } finally {
            relimsTemp.deleteOnExit();
        }

    }
}
