package com.compomics.relims.model.guava.predicates;

import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.guava.functions.ModificationListConverterFunction;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * This class is a
 */
public class ModificationSetPredicate implements Predicate<RelimsProjectBean>{

    private static Logger logger = Logger.getLogger(ModificationSetPredicate.class);

    public boolean apply(RelimsProjectBean aProjectSetupBean) {
        Collection<String> lTransformedModifications = Collections2.transform(aProjectSetupBean.getModificationLists(), new ModificationListConverterFunction());
        boolean isFirst = true;
        String lPreviousModificationString = null;
        for (String lModificationString : lTransformedModifications) {
            if(isFirst){
                isFirst = false;
                // do nothing
            }else{
                if(lPreviousModificationString.equals(lModificationString)){
                    // OK!
                }else{
                    logger.debug("inconsistent modification sets!!");
                    logger.debug("A:" + lPreviousModificationString);
                    logger.debug("B:" + lModificationString);
                    return false;
                }
            }
            lPreviousModificationString = lModificationString;
        }
        logger.debug("consistent modification sets in " + lTransformedModifications.size() + " result files");
        return true;
    }
}
