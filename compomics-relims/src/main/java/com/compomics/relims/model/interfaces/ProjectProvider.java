package com.compomics.relims.model.interfaces;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This class is a
 */
public abstract class ProjectProvider {

    protected static Logger logger = Logger.getLogger(ProjectProvider.class);

    protected DataProvider iDataProvider = null;

    public abstract List<Long> getAllProjects();

    public abstract List<Long> getRandomProjects(int lSize);

    public List<Long> getPreDefinedProjects() {
        return RelimsProperties.getPredifinedProjects();
    }

    public RelimsProjectBean getProject(long aProjectid) {
        return iDataProvider.buildProjectBean(aProjectid);
    }

    public void setDataProvider(DataProvider aDataProvider) {
        iDataProvider = aDataProvider;
    }

}
