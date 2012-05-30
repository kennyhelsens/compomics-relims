package com.compomics.relims.model.provider;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.interfaces.ModificationResolver;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This class is a
 */
public abstract class ProjectProvider {

    protected static Logger logger = Logger.getLogger(ProjectProvider.class);
    protected DataProvider iDataProvider = null;
    protected ModificationResolver iModificationResolver = null;

    public abstract List<Long> getAllProjects();

    public ModificationResolver getModificationResolver(){
        if(iModificationResolver == null){
            throw new RelimsException("NOT YET IMPLEMENTED BY PROJECTPROVIDER");
        }
        return iModificationResolver;
    };

    public abstract List<Long> getRandomProjects(int lSize);

    public List<Long> getPreDefinedProjects() {
        return RelimsProperties.getPredifinedProjects();
    }

    public RelimsProjectBean getProject(long aProjectid) {
        return iDataProvider.buildProjectBean(aProjectid);
    }

    public DataProvider getDataProvider(){
        if(iDataProvider == null){
            throw new RelimsException("NOT YET IMPLEMENTED BY PROJECTPROVIDER");
        }
        return iDataProvider;
    }
}
