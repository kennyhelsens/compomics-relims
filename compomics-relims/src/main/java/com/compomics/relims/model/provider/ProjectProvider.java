package com.compomics.relims.model.provider;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.interfaces.ModificationResolver;
import com.compomics.relims.model.provider.mslims.ModificationResolverImpl;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * This class is a
 */
public abstract class ProjectProvider {

    protected static Logger logger = Logger.getLogger(ProjectProvider.class);
    protected DataProvider iDataProvider = null;
    protected ModificationResolver iModificationResolver = null;

    public abstract Collection<Long> getAllProjects();
    public abstract Collection<Long> getRandomProjects(int lSize);

    protected ProjectProvider() {
        iModificationResolver = new ModificationResolverImpl();
    }

    public ModificationResolver getModificationResolver(){
        return iModificationResolver;
    };


    public Collection<Long> getPreDefinedProjects() {
        return RelimsProperties.getPredifinedProjects();
    }

    public RelimsProjectBean getProject(long aProjectid) {
        return iDataProvider.buildProjectBean(aProjectid);
    }

    public DataProvider getDataProvider(){
        if(iDataProvider == null){
            throw new RelimsException("NOT YET CREATED BY PROJECTPROVIDER IMPLEMENTATION");
        }
        return iDataProvider;
    }
}
