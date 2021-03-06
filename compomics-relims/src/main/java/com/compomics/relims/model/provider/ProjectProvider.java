package com.compomics.relims.model.provider;

import com.compomics.relims.model.ModificationResolverImpl;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.processmanager.processguard.RelimsException;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.interfaces.ModificationResolver;
import com.compomics.relims.model.interfaces.ProjectListProvider;
import com.compomics.relims.model.provider.projectlist.ProjectListProviderLocal;
import com.compomics.relims.model.provider.projectlist.ProjectListProviderRedis;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * This class is a
 */
public abstract class ProjectProvider {

    protected final static Logger logger = Logger.getLogger(ProjectProvider.class);
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


    public ProjectListProvider getPreDefinedProjects() {

        ProjectListProvider result = null;
        if(RelimsProperties.useProjectListFromRedis()){
            result = new ProjectListProviderRedis();
        }else{
            result = new ProjectListProviderLocal();
        }
        return result;
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
