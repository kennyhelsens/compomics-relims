package com.compomics.relims.model.interfaces;

import com.compomics.relims.model.guava.predicates.PredicateManager;
import com.compomics.relims.model.provider.ProjectProvider;

import java.util.concurrent.Callable;

/**
 * This class is a
 */
public interface ProjectRunner extends Callable<String> {
    public String call();

    public void setProjectProvider(ProjectProvider aProjectProvider);

    public void setPredicateManager(PredicateManager aPredicateManager);

    public void setSearchStrategy(SearchStrategy aSearchStrategy);

    public void setProjectID(long aProjectID);
}
