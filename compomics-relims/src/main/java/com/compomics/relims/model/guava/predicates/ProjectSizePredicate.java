package com.compomics.relims.model.guava.predicates;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.google.common.base.Predicate;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;

/**
 * This class is a
 */
public class ProjectSizePredicate implements Predicate<RelimsProjectBean> {
    private static Logger logger = Logger.getLogger(ProjectSizePredicate.class);
    private int iMinimumNumberOfSpectra;
    private int iMinimumNumberOfPeptides;
    private DataProvider iDataProvider = null;

    public ProjectSizePredicate(DataProvider aDataProvider) {
        iDataProvider = aDataProvider;
        iMinimumNumberOfSpectra = RelimsProperties.getMinimumNumberOfSpectra();
        iMinimumNumberOfPeptides = RelimsProperties.getMinimumNumberOfPeptides();
    }


    public boolean apply(@Nullable RelimsProjectBean aRelimsProjectBean) {

        long lProjectid = aRelimsProjectBean.getProjectID();

        long lSpectraForProject = iDataProvider.getNumberOfSpectraForProject(lProjectid);
        if(lSpectraForProject < iMinimumNumberOfSpectra){
            logger.debug("spectrum and peptide counts for project " + lProjectid + " are NOT OK");
            return false;
        }

        long lPeptidesForProject = iDataProvider.getNumberOfPeptidesForProject(lProjectid);
        if(lPeptidesForProject < iMinimumNumberOfPeptides){
            logger.debug("spectrum and peptide counts for project " + lProjectid + " are NOT OK");
            return false;
        }

        logger.debug("spectrum and peptide counts for project " + lProjectid + " is OK");
        return true;
    }
}
