package com.compomics.relims.modes.gui.model;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.processmanager.processguard.RelimsException;
import com.compomics.relims.modes.gui.listener.RelimsPropertiesTableModelListener;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import javax.swing.table.DefaultTableModel;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

/**
 * This class is a
 */
public class RelimsPropertiesTableModel extends DefaultTableModel {

    private static Logger logger = Logger.getLogger(RelimsPropertiesTableModel.class);
    private PropertiesConfiguration config;
    private int iNumberOfKeys = 0;
    private ArrayList<String> iKeyList = Lists.newArrayList();

    public RelimsPropertiesTableModel() {
        config = RelimsProperties.getConfig();

        // Initiate table content.
        Iterator lKeys = config.getKeys();
        while (lKeys.hasNext()) {
            iNumberOfKeys++;
            String lKey = lKeys.next().toString();
            iKeyList.add(lKey);
        }
        Collections.sort(iKeyList);

        // Define update listeners
        RelimsPropertiesTableModelListener lTableModelListener = new RelimsPropertiesTableModelListener(this);
        addTableModelListener(lTableModelListener);


    }

    private void setColumnNames() {
        Vector<String> lColumnNames = new Vector<String>(2);
        lColumnNames.addElement("Property");
        lColumnNames.addElement("Value");

        setColumnIdentifiers(lColumnNames);
    }

    public int getRowCount() {
        return iNumberOfKeys;
    }

    public int getColumnCount() {
        return 2;
    }

    public Object getValueAt(int aRowIndex, int aColumnIndex) {
        switch (aColumnIndex) {
            case 0:
                return iKeyList.get(aRowIndex);

            case 1:
                String lKey = iKeyList.get(aRowIndex);
                return config.getProperty(lKey);

            default:
                throw new RelimsException("Invalid column index in properties model!!");
        }
    }

    @Override
    public void setValueAt(Object o, int aRowIndex, int aColumnIndex) {
        String lKey = iKeyList.get(aRowIndex);
        String lValue = o.toString();

        // Update the config instance!
        config.setProperty(lKey, lValue);
        File configFile = config.getFile();
        if (configFile.exists()) {
            try {
                config.save();
                logger.debug(String.format("updated <%s> \t key:<%s> \t value:<%s>", configFile.getName(), lKey, lValue));
            } catch (ConfigurationException e) {
                logger.error(e.getMessage(), e);
                ProgressManager.setState(Checkpoint.FAILED,e);;
                Thread.currentThread().interrupt();
            }
        }

    }

    @Override
    public boolean isCellEditable(int aRowIndex, int aColumnIndex) {
        switch (aColumnIndex) {
            case 0:
                return false;
            case 1:
                return true;
            default:
                throw new RelimsException("Invalid column index in properties model!!");
        }
    }
}
