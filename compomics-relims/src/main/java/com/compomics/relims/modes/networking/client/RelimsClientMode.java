/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.client;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.client.GUI.MainClientGUI;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class RelimsClientMode {

    private final static Logger logger = Logger.getLogger(MainClientGUI.class);

    public static void main(String args[]) {
        RelimsProperties.initialize();
        if (!RelimsProperties.getDebugMode()) {
            Logger.getRootLogger().setLevel(Level.ERROR);
        }
//        ClientLoginGUI cLGui = new ClientLoginGUI();
        String intermediateClient = RelimsProperties.getUserID();
        MainClientGUI gui = new MainClientGUI(intermediateClient);
        gui.setVisible(true);

    }
}
