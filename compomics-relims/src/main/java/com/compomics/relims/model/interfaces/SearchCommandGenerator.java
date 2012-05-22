package com.compomics.relims.model.interfaces;

import org.apache.commons.configuration.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is a
 */
public interface SearchCommandGenerator {
    public String generateCommand() throws IOException, ConfigurationException;

    public String getName();

    public ArrayList<File> getSpectrumFiles();

    public int getProjectId();

    public File getSearchResultFolder();

    public void setSearchResultFolder(File aSearchResultFolder);
}
