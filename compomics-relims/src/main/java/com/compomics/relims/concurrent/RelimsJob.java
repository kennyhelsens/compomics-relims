package com.compomics.relims.concurrent;

import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.relims.manager.processmanager.processguard.RelimsException;
import com.compomics.relims.manager.processmanager.processguard.RelimsExceptionHandler;
import com.compomics.relims.manager.filemanager.RepositoryManager;
import com.compomics.relims.model.guava.predicates.PredicateManager;
import com.compomics.relims.model.interfaces.*;
import com.compomics.relims.model.provider.ProjectProvider;
import com.compomics.relims.model.provider.mslims.MsLimsProjectProvider;
import com.compomics.relims.model.provider.pride.PrideProjectProvider;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
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
    private RelimsExceptionHandler relimsErrorHandler;
    /**
     * A ProgressManager to store the state of the project and monitor it
     */
    private ProgressManager progressManager = ProgressManager.getInstance();

    public RelimsJob(String aSearchStrategyID, String aProjectProviderID) {
        //Initialize the progressmanager
        ProgressManager.setUp();
        if (aProjectProviderID.toUpperCase().contains("PRIDE")) {
            projectProvider = new PrideProjectProvider();
        } else {
            projectProvider = new MsLimsProjectProvider();
        }
        DataProvider lDataProvider = projectProvider.getDataProvider();
        predicateManager = new PredicateManager(lDataProvider);
    }

    public RelimsJob(String aSearchStrategyID, String aProjectProviderID, long aProjectID, long aTaskID, int aworkerPort, SearchParameters searchParameters, Boolean usePrideAsap) {
        Thread.setDefaultUncaughtExceptionHandler(new RelimsExceptionHandler());
        progressManager.setUp();

        if (aProjectProviderID.toUpperCase().contains("PRIDE")) {
            projectProvider = new PrideProjectProvider();
        } else {
            projectProvider = new MsLimsProjectProvider();
        }
        //Check if the repository contains the MGF/parameters...
        try {
            if (usePrideAsap == null) {
                usePrideAsap = false;
            }
            if (!usePrideAsap) {
                RelimsProperties.setAppendPrideAsapAutomatic(false);
                ProcessVariableManager.setSearchParameters(searchParameters);
            }
            this.projectID = aProjectID;
            progressManager.setState(Checkpoint.LOADINGPROVIDERS);
            DataProvider lDataProvider = projectProvider.getDataProvider();
            predicateManager = new PredicateManager(lDataProvider);
        } catch (RelimsException e) {
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
        List<Future> lFutures = Lists.newArrayList();
        Object[] relimsResultObjects = new Object[]{Checkpoint.FAILED, ProcessVariableManager.getSearchParameters()};
        if (this.projectID != -1) {
            Checkpoint endState;
            try {
                progressManager.setState(Checkpoint.RUNRELIMS);
                endState = runRelims(projectID);
                relimsResultObjects = new Object[]{endState, ProcessVariableManager.getSearchParameters(), ProcessVariableManager.getConversionErrorList()};
            } catch (Exception ex) {
                relimsResultObjects = new Object[]{Checkpoint.FAILED, ProcessVariableManager.getSearchParameters(), ProcessVariableManager.getConversionErrorList()};
            } finally {
                return relimsResultObjects;
            }
        } else {
            ProcessVariableManager.setClassicMode(true);
            runClassicRelims();
            return lFutures;
        }
    }

    private void runClassicRelims() {
        //Run relims as it used to be run ...
        Thread.setDefaultUncaughtExceptionHandler(new RelimsExceptionHandler());
        ProcessVariableManager.setClassicMode(true);
        progressManager.setUp();
        Long lProjectID;
        ProjectListProvider lPreDefinedProjects = projectProvider.getPreDefinedProjects();
        ProcessVariableManager.setResultsFolder(" ");
        while ((lProjectID = lPreDefinedProjects.nextProjectID()) != -1) {
            try {
                // Class searchStrategyClass = RelimsProperties.getRelimsSearchStrategyClass(iSearchStrategyID);
                // SearchStrategy searchStrategy = (SearchStrategy) searchStrategyClass.newInstance();
                ProjectRunner lProjectRunner = new RelimsJobController();
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

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private Checkpoint runRelims(long lProjectID) {
        //   ProcessVariableManager.setResultsFolder(RelimsProperties.createWorkSpace().getAbsolutePath().toString());
        Thread.setDefaultUncaughtExceptionHandler(relimsErrorHandler);
        ProcessVariableManager.setClassicMode(false);
        ProcessVariableManager.setResultsFolder(" ");
        try {
            ProjectRunner lProjectRunner = new RelimsJobController();
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
                    if (progressManager.getState() == Checkpoint.FAILED
                            || progressManager.getState() == Checkpoint.PRIDEFAILURE
                            || progressManager.getState() == Checkpoint.PROCESSFAILURE
                            || progressManager.getState() == Checkpoint.PEPTIDESHAKERFAILURE
                            || progressManager.getState() == Checkpoint.TIMEOUTFAILURE) {
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
                //cleanup pride
                PrideSpectrumAnnotator lSpectrumAnnotator;
                lSpectrumAnnotator = (PrideSpectrumAnnotator) applicationContext.getBean("prideSpectrumAnnotator");
                lSpectrumAnnotator.clearTmpResources();
                //TODO cleanup others
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
        //TODO : CLEANUP HERE? Remove unnecessary files from repository/results folder
        //Analyze results perhaps?
    }
}
