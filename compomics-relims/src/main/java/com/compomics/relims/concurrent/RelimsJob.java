package com.compomics.relims.concurrent;

import com.compomics.relims.conf.RelimsProperties;
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

    private ResultObserver iResultObserver;
    private static Logger logger = Logger.getLogger(RelimsJob.class);

    private ExecutorService iService = null;

    protected ProjectProvider iProjectProvider;
    protected PredicateManager iPredicateManager;
    private final String iSearchStrategyID;


    public RelimsJob(String aSearchStrategyID, String aProjectProviderID) {
        iSearchStrategyID = aSearchStrategyID;
        try {

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

        initThreadExecutor();

        Collection<Long> lProjectIDList = iProjectProvider.getPreDefinedProjects();
//        Collection<Long> lProjectIDList = iProjectProvider.getAllProjects();
        List<Future> lFutures = Lists.newArrayList();

        for (Long lProjectID : lProjectIDList) {

            try {

                Class searchStrategyClass = RelimsProperties.getRelimsSearchStrategyClass(iSearchStrategyID);
                SearchStrategy searchStrategy = (SearchStrategy) searchStrategyClass.newInstance();

                ProjectRunner lProjectRunner = new ProjectRunnerImpl();

                lProjectRunner.setProjectID(lProjectID);
                lProjectRunner.setProjectProvider(iProjectProvider);
                lProjectRunner.setPredicateManager(iPredicateManager);
                lProjectRunner.setSearchStrategy(searchStrategy);

                Observable lObservable = (Observable) lProjectRunner;
                lObservable.addObserver(iResultObserver);

                Future lFuture = iService.submit(lProjectRunner);
                iResultObserver.setActiveFuture(lFuture);

                while (lFuture.isCancelled() == false && lFuture.isDone() == false) {
                    // Do nothing.
                }

                if (lFuture.isCancelled()) {
                    logger.info(String.format("Actively cancelled analysis of project %s. Continuing to next project.", lProjectID));
                    initThreadExecutor();
                } else if (lFuture.isDone()) {
                    logger.info(String.format("Finished analysis of project %s.", lProjectID));
                }

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

    private void initThreadExecutor() {
        close();
        iService = Executors.newSingleThreadExecutor();
    }


    public void close() {
        if (iService != null) {
            List<Runnable> lRunnables = iService.shutdownNow();
            for (Runnable lRunnable : lRunnables) {
                logger.debug("shutting down " + lRunnable.toString());
            }
        }
    }
}
