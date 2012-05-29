package com.compomics.relims.model.guava.predicates;

import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.provider.mslims.MsLimsDataProvider;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * This class is a
 */
public class InstrumentPredicate implements Predicate<RelimsProjectBean> {
    private static Logger logger = Logger.getLogger(InstrumentPredicate.class);
    private Set<Integer> iAllowedInstrumentIDs = null;

    public InstrumentPredicate(Set<Integer> aInstrumentIDs) {
        iAllowedInstrumentIDs = aInstrumentIDs;
    }

    public boolean apply(@Nullable RelimsProjectBean aProjectBean) {
        long lProjectid = 0;
        if (aProjectBean != null) {
            lProjectid = aProjectBean.getProjectID();
        }

        Set<Integer> lInstrumentsForProject = MsLimsDataProvider.getInstance().getInstrumentsForProject(lProjectid);
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
