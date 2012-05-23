package com.compomics.relims.model.interfaces;

import org.apache.commons.configuration.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * This class is a
 */
public interface SearchCommandGenerator {
    public String generateCommand() throws IOException, ConfigurationException;

    public String getName();

    public List<File> getSpectrumFiles();

    public long getProjectId();

    public File getSearchResultFolder();

    public void setSearchResultFolder(File aSearchResultFolder);
}
