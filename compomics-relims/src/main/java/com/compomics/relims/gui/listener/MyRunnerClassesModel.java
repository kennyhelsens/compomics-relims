package com.compomics.relims.gui.listener;

import com.compomics.relims.conf.RelimsProperties;

import javax.swing.*;
import javax.swing.event.ListDataListener;

/**
 * This class is a
 */
public class MyRunnerClassesModel implements ComboBoxModel {

    String[] iRunnerIds = RelimsProperties.getRelimsClassList();
    Object iSelectedItem = iRunnerIds[1];

    public void setSelectedItem(Object o) {
        iSelectedItem = o;
    }

    public Object getSelectedItem() {
        return iSelectedItem;
    }

    public int getSize() {
        return iRunnerIds.length;
    }

    public Object getElementAt(int i) {
        return iRunnerIds[i];
    }

    public void addListDataListener(ListDataListener aListDataListener) {
        // Not implemented.
    }

    public void removeListDataListener(ListDataListener aListDataListener) {
        // Not implemented.

    }
}
