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
public class SearchGUILocalizer {

    private static File relimsTemp = RelimsProperties.getRelimsTempFolder();
    private static final Logger logger = Logger.getLogger(SearchGUILocalizer.class);

    public synchronized static void cleanCopy() {
        try {
            logger.debug("Making a searchGUI temp folder in " + relimsTemp.getAbsolutePath());
            File searchGUIFolder = new File(RelimsProperties.getSearchGuiFolder());
            //clear the relimsTemp folder
            if (relimsTemp.exists()) {
                FileUtils.cleanDirectory(relimsTemp);
            } else {
                relimsTemp.mkdirs();
            }
            // copy the entire director
            FileUtils.copyDirectory(searchGUIFolder, relimsTemp);
            //set relims properties
            RelimsProperties.setSearchGUIFolder(relimsTemp.getAbsolutePath());
        } catch (Exception e) {
            logger.error(e);
        } finally {
            relimsTemp.deleteOnExit();
        }

    }
}
