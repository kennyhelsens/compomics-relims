package com.compomics.relims.interfaces;

import org.apache.commons.configuration.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is a
 */
public interface SearchCommandGenerator {
    String generateCommand() throws IOException, ConfigurationException;

    String getName();

    ArrayList<File> getSpectrumFiles();

    int getProjectId();

    File getSearchResultFolder();

    void setSearchResultFolder(File aSearchResultFolder);
}
