package com.compomics.relims.model.interfaces;

import com.compomics.relims.model.beans.RelimsProjectBean;

import java.util.concurrent.Callable;

/**
 * This class is a
 */
public interface ProjectRunner extends Callable<String> {
    public String call();

    public void setProject(RelimsProjectBean aRelimsProjectBean);

    public void setDataProvider(DataProvider aDataProvider);
}
