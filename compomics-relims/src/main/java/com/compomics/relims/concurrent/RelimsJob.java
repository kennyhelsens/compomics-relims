package com.compomics.relims.concurrent;

import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.exception.RelimsExceptionHandler;
import com.compomics.relims.conf.RelimsVariableManager;
import com.compomics.relims.filemanager.RepositoryManager;
import com.compomics.relims.model.guava.predicates.PredicateManager;
import com.compomics.relims.model.interfaces.*;
import com.compomics.relims.model.provider.ProjectProvider;
import com.compomics.relims.observer.Checkpoint;
import com.compomics.relims.observer.ProgressManager;
import com.compomics.util.experiment.identification.SearchParameters;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/*-1l
 * This class is a
 */
public class RelimsJob implements Callable, Closable {

    /**
     * Plain logger
     */
    private static Logger logger = Logger.getLogger(RelimsJob.class);
    /**
     * ExecutorService is used to gather and launch future jobs. //TODO is this
     * still required, --> perhaps for hanging jobs?
     */
    private ExecutorService executorService = null;
    /**
     * applicationContext detects how the process was started
     */
    private ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
    /**
     * the projectProvider is the class that handles the usage local project
     * lists to run them. This is used in the classic relims...TODO:
     * re-implement this?
     */
    protected ProjectProvider projectProvider;
    /**
     * Predicates are requirements that need to be forfilled before runtime.
     */
    protected PredicateManager predicateManager;
    /**
     * the accession number of the project
     */
    private long projectID = -1L;
    /**
     * Custom exceptionhandler for all relims-errors
     */
    private RelimsExceptionHandler workingServerErrorHandler;
    /**
     * A ProgressManager to store the state of the project and monitor it
     */
    private ProgressManager progressManager = ProgressManager.getInstance();

    public RelimsJob(String aSearchStrategyID, String aProjectProviderID) {
        //Initialize the progressmanager
        ProgressManager.setUp();

        try {
            Class lSourceClass = RelimsProperties.getRelimsSourceClass(aProjectProviderID);
            projectProvider = (ProjectProvider) lSourceClass.newInstance();
            DataProvider lDataProvider = projectProvider.getDataProvider();
            predicateManager = new PredicateManager(lDataProvider);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }

    public RelimsJob(String aSearchStrategyID, String aProjectProviderID, long aProjectID, long aTaskID, int aworkerPort, SearchParameters searchParameters, Boolean usePrideAsa) {
        Thread.setDefaultUncaughtExceptionHandler(new RelimsExceptionHandler());
        progressManager.setUp();
        // needed to notify the controller about the task...
        //Check if the repository contains the MGF/parameters...
        try {
            //   RelimsVariableManager.setTaskID(aTaskID);
            // RelimsVariableManager.setWorkerPort(aworkerPort);
            RelimsVariableManager.setResultsFolder(RelimsProperties.createWorkSpace(aProjectID, aProjectProviderID.toUpperCase()).getAbsolutePath().toString());
            // TODO ONLY USE THE SEARCHPARAMETERS FROM MSLIMS/PRIDE FOR NOW?
            if (usePrideAsa == null) {
                usePrideAsa = false;
            }
            if (!usePrideAsa) {
                RelimsProperties.setAppendPrideAsapAutomatic(false);
                RelimsVariableManager.setSearchParameters(searchParameters);
            }
//            iSearchStrategyID = aSearchStrategyID;
            this.projectID = aProjectID;
            progressManager.setState(Checkpoint.LOADINGPROVIDERS);
            Class lSourceClass = RelimsProperties.getRelimsSourceClass(aProjectProviderID);
            projectProvider = (ProjectProvider) lSourceClass.newInstance();
            DataProvider lDataProvider = projectProvider.getDataProvider();
            predicateManager = new PredicateManager(lDataProvider);
        } catch (RelimsException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            progressManager.setState(Checkpoint.FAILED, e);;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
            progressManager.setState(Checkpoint.FAILED);;
        }
    }

    @Override
    public Object call() {

        initThreadExecutor();
        RepositoryManager.initializeRepository();
        Thread.setDefaultUncaughtExceptionHandler(new RelimsExceptionHandler());
        progressManager.setUp();
        //Collection<Long> lProjectIDList = iProjectProvider.getAllProjects();
        List<Future> lFutures = Lists.newArrayList();
        Object[] relimsResultObjects = new Object[]{Checkpoint.FAILED, RelimsVariableManager.getSearchParameters()};
        if (this.projectID != -1) {
            Checkpoint endState;
            try {
                progressManager.setState(Checkpoint.RUNRELIMS);
                endState = runRelims(projectID);
                relimsResultObjects = new Object[]{endState, RelimsVariableManager.getSearchParameters(), RelimsVariableManager.getConversionErrorList()};
            } catch (Exception ex) {
                relimsResultObjects = new Object[]{Checkpoint.FAILED, RelimsVariableManager.getSearchParameters(), RelimsVariableManager.getConversionErrorList()};
            } finally {
                return relimsResultObjects;
            }
        } else {
            runClassicRelims();
            return lFutures;
        }


    }

    private void runClassicRelims() {
        //Run relims as it used to be ran ...
        Thread.setDefaultUncaughtExceptionHandler(new RelimsExceptionHandler());
        progressManager.setUp();
        Long lProjectID;
        ProjectListProvider lPreDefinedProjects = projectProvider.getPreDefinedProjects();
        RelimsVariableManager.setSearchResultFolder(RelimsProperties.getWorkSpace().getAbsolutePath().toString());
        while ((lProjectID = lPreDefinedProjects.nextProjectID()) != -1) {
            try {
                // Class searchStrategyClass = RelimsProperties.getRelimsSearchStrategyClass(iSearchStrategyID);
                // SearchStrategy searchStrategy = (SearchStrategy) searchStrategyClass.newInstance();

                ProjectRunner lProjectRunner = new ProjectRunnerImpl();

                lProjectRunner.setProjectID(lProjectID);
                lProjectRunner.setProjectProvider(projectProvider);
                lProjectRunner.setPredicateManager(predicateManager);
                //  lProjectRunner.setSearchStrategy(searchStrategy);

                Observable lObservable = (Observable) lProjectRunner;
                Future lFuture = executorService.submit(lProjectRunner);

                while (lFuture.isCancelled() == false && lFuture.isDone() == false) {
                    // Do nothing.
                }

                if (lFuture.isCancelled()) {
                    logger.debug(String.format("Actively cancelled analysis of project %s. Continuing to next project.", lProjectID));
                    initThreadExecutor();

                } else if (lFuture.isDone()) {
                    logger.debug(String.format("Finished analysis of project %s.", lProjectID));
                }
                // Clean MGF resources after project success
                PrideSpectrumAnnotator lSpectrumAnnotator;
                lSpectrumAnnotator = (PrideSpectrumAnnotator) applicationContext.getBean("prideSpectrumAnnotator");
                lSpectrumAnnotator.clearTmpResources();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private Checkpoint runRelims(long lProjectID) {
        Thread.setDefaultUncaughtExceptionHandler(workingServerErrorHandler);
        try {
            ProjectRunner lProjectRunner = new ProjectRunnerImpl();
            lProjectRunner.setProjectID(lProjectID);
            lProjectRunner.setProjectProvider(projectProvider);
            lProjectRunner.setPredicateManager(predicateManager);
            try {
                Observable lObservable = (Observable) lProjectRunner;
                Future lFuture = executorService.submit(lProjectRunner);
                while (lFuture.isCancelled() == false && lFuture.isDone() == false) {
                    // Do nothing.
                }

                if (lFuture.isCancelled()) {
                    logger.debug(String.format("Actively cancelled analysis of project %s. Continuing to next project.", lProjectID));
                    initThreadExecutor();
                    progressManager.setState(Checkpoint.FAILED);;
                } else if (lFuture.isDone()) {
                    if (progressManager.getState() == Checkpoint.FAILED) {
                        logger.debug(String.format("Failed analysis of project %s.", lProjectID));
                    } else {
                        logger.debug(String.format("Finished analysis of project %s.", lProjectID));
                        progressManager.setState(Checkpoint.FINISHED);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                progressManager.setState(Checkpoint.FAILED, e);;
            }
        } catch (/*ClassNotFoundException | InstantiationException | IllegalAccessException*/Exception e) {
            logger.error(e);
            e.printStackTrace();
            progressManager.setState(Checkpoint.FAILED, e);;
        } finally {
            // Clean MGF resources after project success
            // in finally block because it can't fail anymore...if it fails, then it's the cleanup...
            try {
                PrideSpectrumAnnotator lSpectrumAnnotator;
                lSpectrumAnnotator = (PrideSpectrumAnnotator) applicationContext.getBean("prideSpectrumAnnotator");
                lSpectrumAnnotator.clearTmpResources();
            } catch (Exception e) {
                logger.error("An error occurred during cleanup...");
                logger.error(e);
                e.printStackTrace();
            }
            return progressManager.getEndState();
        }

    }

    private void initThreadExecutor() {
        close();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public void close() {
        if (executorService != null) {
            List<Runnable> lRunnables = executorService.shutdownNow();
            for (Runnable lRunnable : lRunnables) {
                logger.debug("shutting down " + lRunnable.toString());
            }
        }
        /*
         // Remove remaining resources
         applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();

         PrideSpectrumAnnotator lSpectrumAnnotator;
         lSpectrumAnnotator = (PrideSpectrumAnnotator) applicationContext.getBean("prideSpectrumAnnotator");
         lSpectrumAnnotator.clearTmpResources();
         */
    }
}
