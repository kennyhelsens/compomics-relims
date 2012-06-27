package com.compomics.relims.model.interfaces;

import com.compomics.relims.model.beans.RelimsProjectBean;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * This class is a
 */
public interface DataProvider {

    public long getNumberOfSpectraForProject(long aProjectID);

    public Set<Integer> getInstrumentsForProject(long aProjectID);

    public Set<String> getProteinAccessionsForProject(long aProjectID);

    public long getNumberOfPeptidesForProject(long aProjectID);

    public File getSpectraForProject(long aProjectID) throws IOException;

    public long getNumberOfSearchesForProject(long aProjectid);

    public RelimsProjectBean buildProjectBean(long aProjectid);
}
