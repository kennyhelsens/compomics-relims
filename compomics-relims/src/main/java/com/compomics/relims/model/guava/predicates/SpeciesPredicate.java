package com.compomics.relims.model.guava.predicates;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.guava.functions.SpeciesFinderFunction;
import com.compomics.relims.model.provider.mslims.MsLimsDataProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * This class is a
 */
public class SpeciesPredicate implements Predicate<RelimsProjectBean> {
    private static Logger logger = Logger.getLogger(SpeciesPredicate.class);
    private final SpeciesFinderFunction.SPECIES iAllowedSpecies;
    private final int iNumberToTest;

    public SpeciesPredicate(SpeciesFinderFunction.SPECIES aAllowedSpecies, int aNumberToTest) {
        iAllowedSpecies = aAllowedSpecies;
        iNumberToTest = aNumberToTest;
    }

    public SpeciesPredicate() {
        iAllowedSpecies = RelimsProperties.getAllowedSpecies();
        iNumberToTest = RelimsProperties.getAllowedSpeciesTestSize();
    }

    public boolean apply(@Nullable RelimsProjectBean aProjectBean) {

        long lProjectid = aProjectBean.getProjectID();

        Iterable<String> lAccessionsForProject = MsLimsDataProvider.getInstance().getProteinAccessionsForProject(lProjectid);
        Set<String> lReducedSet = Sets.newHashSet();

        int lCounter = 0;
        for (String lAccession : lAccessionsForProject) {
            lReducedSet.add(lAccession);
            if(lCounter++ > iNumberToTest){
                break;
            }
        }

        SpeciesFinderFunction lSpeciesFinderFunction = new SpeciesFinderFunction();
        SpeciesFinderFunction.SPECIES lResolvedSpecies = lSpeciesFinderFunction.apply(lReducedSet);

        if(lResolvedSpecies == iAllowedSpecies){
            logger.debug("successfully matched project accessions to the allowed species ");
            return true;
        }else{
            logger.debug("failed to match project accessions to the allowed species");
            return false;
        }
    }
}
