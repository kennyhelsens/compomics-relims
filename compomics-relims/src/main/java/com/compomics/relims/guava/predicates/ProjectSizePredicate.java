package com.compomics.relims.guava.predicates;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.relims.model.mslims.MsLimsProvider;
import com.google.common.base.Predicate;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;

/**
 * This class is a
 */
public class ProjectSizePredicate implements Predicate<Project> {
    private static Logger logger = Logger.getLogger(ProjectSizePredicate.class);
    private final int iMinimumNumberOfSpectra;
    private final int iMinimumNumberOfPeptides;

    public ProjectSizePredicate(int aMinimumNumberOfSpectra, int aMinimumNumberOfPeptides) {
        iMinimumNumberOfSpectra = aMinimumNumberOfSpectra;
        iMinimumNumberOfPeptides = aMinimumNumberOfPeptides;
    }

    public boolean apply(@Nullable Project aProject) {

        long lProjectid = aProject.getProjectid();
        long lSpectraForProject = MsLimsProvider.getInstance().getNumberOfSpectraForProject(lProjectid);
        if(lSpectraForProject < iMinimumNumberOfSpectra){
            logger.debug("spectrum and peptide counts for project " + lProjectid + " are NOT OK");
            return false;
        }

        long lPeptidesForProject = MsLimsProvider.getInstance().getNumberOfPeptidesForProject(lProjectid);
        if(lPeptidesForProject < iMinimumNumberOfPeptides){
            logger.debug("spectrum and peptide counts for project " + lProjectid + " are NOT OK");
            return false;
        }

        logger.debug("spectrum and peptide counts for project " + lProjectid + " is OK");
        return true;
    }
}
