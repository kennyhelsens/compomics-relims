package com.compomics.relims.concurrent;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.guava.predicates.PredicateManager;
import com.compomics.relims.model.interfaces.Closable;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.interfaces.ProjectRunner;
import com.compomics.relims.model.interfaces.SearchStrategy;
import com.compomics.relims.model.provider.ProjectProvider;
import com.compomics.relims.observer.ResultObserver;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.*;

/**
 * This class is a
 */
public class RelimsJob implements Callable, Closable {

    static private ExecutorService iService = Executors.newFixedThreadPool(1);

    private ResultObserver iResultObserver;
    private static Logger logger = Logger.getLogger(RelimsJob.class);

    protected ProjectRunner iProjectRunner;
    protected ProjectProvider iProjectProvider;
    protected PredicateManager iPredicateManager;
    private final String iSearchStrategyID;


    public RelimsJob(String aSearchStrategyID, String aProjectProviderID) {
        iSearchStrategyID = aSearchStrategyID;
        try {
            iProjectRunner = new ProjectRunnerImpl();

            Class lSourceClass = RelimsProperties.getRelimsSourceClass(aProjectProviderID);
            iProjectProvider = (ProjectProvider) lSourceClass.newInstance();

            DataProvider lDataProvider = iProjectProvider.getDataProvider();
            iPredicateManager = new PredicateManager(lDataProvider);

            iResultObserver = new ResultObserver();

        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public Object call() {

        Collection<Long> lProjectIDList = iProjectProvider.getPreDefinedProjects();
//        Collection<Long> lProjectIDList = iProjectProvider.getAllProjects();
        List<Future> lFutures = Lists.newArrayList();

        for (Long lProjectID : lProjectIDList) {

            try {

                Class searchStrategyClass = RelimsProperties.getRelimsSearchStrategyClass(iSearchStrategyID);
                SearchStrategy searchStrategy = (SearchStrategy) searchStrategyClass.newInstance();

                RelimsProjectBean lRelimsProjectBean = iProjectProvider.getProject(lProjectID);
                iProjectRunner.setProject(lRelimsProjectBean);
                iProjectRunner.setPredicateManager(iPredicateManager);
                iProjectRunner.setDataProvider(iProjectProvider.getDataProvider());
                iProjectRunner.setModificationResolver(iProjectProvider.getModificationResolver());
                iProjectRunner.setSearchStrategy(searchStrategy);

                Observable lObservable = (Observable) iProjectRunner;
                lObservable.addObserver(iResultObserver);

                Future<String> lFuture = iService.submit(iProjectRunner);

                String s = lFuture.get();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                logger.error(e.getMessage(), e);
            } catch (ClassNotFoundException e) {
                logger.error(e.getMessage(), e);
            } catch (InstantiationException e) {
                logger.error(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return lFutures;
    }


    public void close() {
        List<Runnable> lRunnables = iService.shutdownNow();
        for (Runnable lRunnable : lRunnables) {
            logger.debug("shutting down " + lRunnable.toString());
        }
        iService = Executors.newSingleThreadExecutor();
    }
}
