package com.compomics.relims.guava.predicates;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.guava.functions.SpeciesFinderFunction;
import com.compomics.relims.model.mslims.MsLimsProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;

/**
 * This class is a
 */
public class SpeciesPredicate implements Predicate<Project> {
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

    public boolean apply(@Nullable Project aProject) {

        long lProjectid = aProject.getProjectid();
        HashSet<String> lAccessionsForProject = MsLimsProvider.getInstance().getProteinAccessionsForProject(lProjectid);
        HashSet<String> lReducedSet = Sets.newHashSet();

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
