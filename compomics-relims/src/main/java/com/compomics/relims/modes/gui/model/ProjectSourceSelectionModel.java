package com.compomics.relims.modes.gui.model;

import com.compomics.relims.conf.RelimsProperties;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * This class is a
 */
public class ProjectSourceSelectionModel implements ComboBoxModel {

    String[] iSourceIds = RelimsProperties.getRelimsSourceList();
    Object iSelectedItem = iSourceIds[0];

    public void setSelectedItem(Object o) {
        iSelectedItem = o;
    }

    public Object getSelectedItem() {
        return iSelectedItem;
    }

    public int getSize() {
        return iSourceIds.length;
    }

    public Object getElementAt(int i) {
        return iSourceIds[i];
    }

    public void addListDataListener(ListDataListener aListDataListener) {
        // Not implemented.
    }

    public void removeListDataListener(ListDataListener aListDataListener) {
        // Not implemented.

    }
}
