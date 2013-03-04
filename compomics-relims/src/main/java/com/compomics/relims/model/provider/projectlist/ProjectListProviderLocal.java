package com.compomics.relims.model.provider.projectlist;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.interfaces.ProjectListProvider;

import java.util.Iterator;
import java.util.List;

/**
 * This class is a
 */
public class ProjectListProviderLocal implements ProjectListProvider {


    protected final Iterator<Long> iProjectIterator;

    public ProjectListProviderLocal() {
        List<Long> lPredifinedProjects = RelimsProperties.getPredifinedProjects();
        iProjectIterator = lPredifinedProjects.iterator();
    }

    /**
     * Returns the next projectid from the list.
     * Returns -1 if no more projects left.
     *
     * @return
     */
    public long nextProjectID() {
        if (iProjectIterator.hasNext()) {
            return iProjectIterator.next();
        } else {
            return -1;
        }
    }
}
