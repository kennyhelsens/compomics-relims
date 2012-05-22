package com.compomics.relims.model.guava.predicates;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.relims.model.MsLimsDataProvider;
import com.google.common.base.Predicate;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;

/**
 * This class is a
 */
public class SearchSetSizePredicate implements Predicate<Project> {
    private static Logger logger = Logger.getLogger(SearchSetSizePredicate.class);
    private int iMaximumNumberOfSearches = 1;

    public SearchSetSizePredicate() {

    }

    public void setMinimumNumberOfPeptides(int aMinimumNumberOfPeptides) {
        iMaximumNumberOfSearches = aMinimumNumberOfPeptides;
    }

    public boolean apply(@Nullable Project aProject) {

        long lProjectid = aProject.getProjectid();
        long lSearchesPerProject = MsLimsDataProvider.getInstance().getNumberOfSearchesForProject(lProjectid);
        if(lSearchesPerProject <= iMaximumNumberOfSearches){
            logger.debug("search count (" + lSearchesPerProject + ") for project " + lProjectid + " is NOT OK");
            return false;
        }

        logger.debug("search count per project " + lProjectid + " is OK");
        return true;
    }
}
