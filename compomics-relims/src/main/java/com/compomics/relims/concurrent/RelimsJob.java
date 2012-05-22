package com.compomics.relims.concurrent;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.MsLimsDataProvider;
import com.compomics.relims.model.MsLimsProjectProvider;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.ProjectProvider;
import com.compomics.relims.model.interfaces.ProjectRunner;
import com.compomics.relims.observer.ResultObserver;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is a
 */
public class RelimsJob implements Callable, Observer {
    static private ExecutorService iService = Executors.newSingleThreadExecutor();

    private ResultObserver iResultObserver;
    private static Logger logger = Logger.getLogger(RelimsJob.class);
    private final Class iProjectRunnerClass;

    private int iProjectCounter = 0;


    public RelimsJob(Class aProjectRunner) {
        iProjectRunnerClass = aProjectRunner;

        try {
            iResultObserver = new ResultObserver();
            addObserver(iResultObserver);
            addObserver(this);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void addObserver(Observer observer) {
        Observable observable = (Observable) iProjectRunnerClass;
        observable.addObserver(observer);
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

                lFutures.add(iService.submit(lProjectRunner));
            }

        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void update(Observable aObservable, Object o) {
        synchronized (this) {
            iProjectCounter++;
            logger.debug("PROJECT SUCCES COUNT " + iProjectCounter + "(" + o.toString() + ").");
        }

        if (iProjectCounter >= RelimsProperties.getMaxSucces()) {
            shutdown();
        }
    }

    public void shutdown() {
        List<Runnable> lRunnables = iService.shutdownNow();
        for (Runnable lRunnable : lRunnables) {
            logger.debug("shutting down " + lRunnable.toString());
        }
        iService = Executors.newSingleThreadExecutor();
    }
}
