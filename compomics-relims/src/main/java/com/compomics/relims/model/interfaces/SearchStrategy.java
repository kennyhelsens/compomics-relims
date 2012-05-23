package com.compomics.relims.model.interfaces;

import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;

import java.io.File;
import java.util.List;

/**
 * This class is a
 */
public interface SearchStrategy {
    public void fill(SearchList<SearchCommandGenerator> aSearchList);
    public void setSpectrumFiles(List<File> aSpectrumFiles);
    public void setRelimsProjectBean(RelimsProjectBean aRelimsProjectBean);

    public String getName();
    public String getDescription();
}
