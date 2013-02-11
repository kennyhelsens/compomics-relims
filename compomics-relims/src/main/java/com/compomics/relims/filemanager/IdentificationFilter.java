/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.filemanager;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.*;

/**
 *
 * @author Kenneth
 */
public class IdentificationFilter implements FilenameFilter {

    public IdentificationFilter() {
    }

    @Override
    public boolean accept(File dir, String name) {
        return Pattern.matches(".*\\.(omx|t.xml)", name);
    }
}
