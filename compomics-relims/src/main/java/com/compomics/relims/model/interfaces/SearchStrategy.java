package com.compomics.relims.model.interfaces;

import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;

import java.io.File;

/**
 * This class is a
 */
public interface SearchStrategy {
    public void fill(SearchList<SearchCommandGenerator> aSearchList, RelimsProjectBean iRelimsProjectBean);
    public void addSpectrumFile(File aSpectrumFile);

    public String getName();
    public String getDescription();
}
