package com.compomics.relims.guava.predicates;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.relims.model.mslims.MsLimsProvider;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a
 */
public class InstrumentPredicate implements Predicate<Project> {
    private static Logger logger = Logger.getLogger(InstrumentPredicate.class);
    private Set<Integer> iAllowedInstrumentIDs = null;

    public InstrumentPredicate(Set<Integer> aInstrumentIDs) {
        iAllowedInstrumentIDs = aInstrumentIDs;
    }

    public boolean apply(@Nullable Project aProject) {
        long lProjectid = aProject.getProjectid();

        HashSet<Integer> lInstrumentsForProject = MsLimsProvider.getInstance().getInstrumentsForProject(lProjectid);
        Sets.SetView<Integer> lUnion = Sets.union(lInstrumentsForProject, iAllowedInstrumentIDs);
        if (lUnion.size() > iAllowedInstrumentIDs.size()) {
            logger.debug("project " + lProjectid + " has non-allowed instruments (" + Joiner.on(",").join(lInstrumentsForProject) + ")");
            logger.debug("instruments for project " + lProjectid + " are NOT OK");
            return false;
        }

        logger.debug("instruments for project " + lProjectid + " are OK");
        return true;
    }
}
