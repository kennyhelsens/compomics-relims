/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.client.GUI;

import java.io.File;

/**
 *
 * @author Kenneth
 */
public class TextFilter extends javax.swing.filechooser.FileFilter {

    @Override
    public boolean accept(File file) {
        //Convert to lower case before checking extension  
        return (file.getName().toLowerCase().endsWith(".txt")
                || file.isDirectory());
    }

    @Override
    public String getDescription() {
        return "Text File (*.txt)";
    }
}
