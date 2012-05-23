package com.compomics.relims.concurrent;

import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.Closable;
import com.compomics.relims.model.interfaces.ProjectRunner;
import com.compomics.relims.model.provider.ProjectProvider;
import com.compomics.relims.model.provider.mslims.MsLimsDataProvider;
import com.compomics.relims.model.provider.mslims.MsLimsProjectProvider;
import com.compomics.relims.observer.ResultObserver;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is a
 */
public class RelimsJob implements Callable, Closable {
    static private ExecutorService iService = Executors.newSingleThreadExecutor();

    private ResultObserver iResultObserver;
    private static Logger logger = Logger.getLogger(RelimsJob.class);
    private final Class iProjectRunnerClass;


    public RelimsJob(Class aProjectRunner) {
        iProjectRunnerClass = aProjectRunner;

        try {
            iResultObserver = new ResultObserver();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }


    public Object call() {
        try {
            ProjectProvider lProjectProvider = new MsLimsProjectProvider();
            MsLimsDataProvider lMsLimsProjectProvider = new MsLimsDataProvider();
            lProjectProvider.setDataProvider(lMsLimsProjectProvider);

            List<Long> lPreDefinedProjects = lProjectProvider.getPreDefinedProjects();
            List<Future> lFutures = Lists.newArrayList();

            for (Long lProject : lPreDefinedProjects) {
                RelimsProjectBean lRelimsProjectBean = lProjectProvider.getProject(lProject);

                Object o = iProjectRunnerClass.newInstance();

                ProjectRunner lProjectRunner = (ProjectRunner) o;
                lProjectRunner.setProject(lRelimsProjectBean);

                Observable lObservable = (Observable) o;
                lObservable.addObserver(iResultObserver);

                lFutures.add(iService.submit(lProjectRunner));
            }

        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    public void close() {
        List<Runnable> lRunnables = iService.shutdownNow();
        for (Runnable lRunnable : lRunnables) {
            logger.debug("shutting down " + lRunnable.toString());
        }
        iService = Executors.newSingleThreadExecutor();
    }
}
