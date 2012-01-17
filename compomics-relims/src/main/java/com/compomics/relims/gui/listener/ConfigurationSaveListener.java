package com.compomics.relims.gui.listener;

import com.compomics.relims.conf.RelimsProperties;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
        }
    }
}
