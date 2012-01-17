package com.compomics.relims.gui.listener;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.exception.RelimsException;
import org.apache.commons.configuration.PropertiesConfiguration;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * This class is a
 */
public class RelimsPropertiesTableModelListener implements TableModelListener {

    private PropertiesConfiguration config;
    private TableModel model;


    public RelimsPropertiesTableModelListener(TableModel aModel) {
        config = RelimsProperties.getConfig();
        this.model = aModel;
    }

    public void tableChanged(TableModelEvent aTableModelEvent) {
        // Update event?
        System.out.println("lala");
        if (aTableModelEvent.getType() == TableModelEvent.UPDATE) {
            int lFirstRow = aTableModelEvent.getFirstRow();
            int lSecondRow = aTableModelEvent.getLastRow();
            if(lFirstRow != lSecondRow){
                throw new RelimsException("Only one property can be updated at a time!!");
            }
            int lColumn = aTableModelEvent.getColumn();
            String lKey = model.getValueAt(lFirstRow, 0).toString();
            String lValue = model.getValueAt(lFirstRow, 1).toString();

            // Update the config instance!
            config.setProperty(lKey, lValue);
        }

    }
}
