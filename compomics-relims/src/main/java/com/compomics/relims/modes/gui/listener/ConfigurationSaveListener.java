package com.compomics.relims.modes.gui.listener;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

/**
 * This class is a
 */
public class ConfigurationSaveListener implements ActionListener {

    private static Logger logger = Logger.getLogger(ConfigurationSaveListener.class);

    public void actionPerformed(ActionEvent aActionEvent) {
        try {
            RelimsProperties.getConfig().save();
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
        }
    }
}
