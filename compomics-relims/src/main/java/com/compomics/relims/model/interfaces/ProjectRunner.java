package com.compomics.relims.model.interfaces;

import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.guava.predicates.PredicateManager;

import java.util.concurrent.Callable;

/**
 * This class is a
 */
public interface ProjectRunner extends Callable<String> {
    public String call();

    public void setProject(RelimsProjectBean aRelimsProjectBean);

    public void setPredicateManager(PredicateManager aPredicateManager);

    public void setSearchStrategy(SearchStrategy aSearchStrategy);

    public void setDataProvider(DataProvider aDataProvider);

    public void setModificationResolver(ModificationResolver aModificationResolver);
}
