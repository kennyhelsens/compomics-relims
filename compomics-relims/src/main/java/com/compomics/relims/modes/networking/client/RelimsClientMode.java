/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.client;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.predicatemanager.MetaDataCollector;
import com.compomics.relims.modes.networking.client.GUI.projectmanager.NewProjectDialog;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class RelimsClientMode {
    
    private final static Logger logger = Logger.getLogger(NewProjectDialog.class);
    
    public static void main(String args[]) {
        RelimsProperties.setNetworkingMode(RelimsProperties.NetworkMode.CLIENT);
        RelimsProperties.initialize(false);
        if (!RelimsProperties.getDebugMode()) {
            Logger.getRootLogger().setLevel(Level.ERROR);
        }
//        ClientLoginGUI cLGui = new ClientLoginGUI();
        String intermediateClient = RelimsProperties.getUserID();
        try {
            MetaDataCollector.main(args);
        } catch (Exception ex) {
            logger.error("Could not build pride-metadata table. Filtering might be an issue !");
        }
        NewProjectDialog gui = new NewProjectDialog(intermediateClient, RelimsProperties.getControllerIP(), RelimsProperties.getControllerPort(), "pride", "varmod");
        gui.setVisible(true);
    }
}
