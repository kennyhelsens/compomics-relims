/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.taskreciever;

import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import com.compomics.relims.concurrent.RelimsJob;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.processmanager.processguard.RelimsException;
import com.compomics.relims.manager.processmanager.processguard.RelimsExceptionHandler;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.modes.networking.controller.taskobjects.Task;
import com.compomics.relims.modes.networking.worker.feedbackproviders.ResultNotifier;
import com.compomics.relims.manager.resourcemanager.ResourceManager;
import com.compomics.relims.manager.resultmanager.ResultManager;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class TaskRunner {

    private static final Logger logger = Logger.getLogger(TaskRunner.class);
    private Task task;

    public TaskRunner(Task task) {
        this.task = task;
    }
    Thread runner = new Thread(new RelimsJobStarter());
    Thread jobStarter = null;

    public void launch() {

        Thread.setDefaultUncaughtExceptionHandler(new RelimsExceptionHandler());

        try {
            logger.info("Preparing the search...This might take a long period of time.");
            jobStarter = new Thread(new RelimsJobStarter());
            jobStarter.start();
            //use checkpoints to determine action
        } catch (RelimsException RE) {
            logger.fatal("Relims has caused an exception. Shutting down runner and resetting task...");
            if (jobStarter != null) {
                jobStarter.interrupt();
            }
        }
    }

    public boolean isRunning() {
        if (jobStarter == null) {
            return false;
        } else if (jobStarter.isAlive()) {
            return true;
        }
        return false;
    }

    private class RelimsJobStarter implements Callable, Runnable {

        private Thread updateThread;
        private RelimsJob iRelimsJob;
        private ResultNotifier resultNotifier = new ResultNotifier();
        private Checkpoint endState;
        private long lTaskID;
        private long lProjectID;
        private String lProjectProviderID;
        private String lSearchStrategyID;
        private SearchParameters lSearchParameters;
        private Boolean usePrideAsa;
        private List<ConversionError> conversionErrorList;

        @Override
        public Checkpoint call() {
            try {
                ResourceManager.setTaskTime(System.currentTimeMillis());
                lSearchParameters = task.getSearchParameters();
                lSearchStrategyID = task.getStrategyID();
                lProjectProviderID = task.getSourceID();
                usePrideAsa = task.getAllowPridePipeline();
                lProjectID = Long.parseLong(task.getProjectID());
                lTaskID = task.getTaskID();
                int lWorkerPort = ResourceManager.getWorkerPort();
                File fastaFile = RelimsProperties.getFastaFromRepository(task.getFasta());
                iRelimsJob = new RelimsJob(lSearchStrategyID, lProjectProviderID, lProjectID, lTaskID, lWorkerPort, lSearchParameters, usePrideAsa, fastaFile);
                ResourceManager.setUserID(task.getUserID());
                ResourceManager.setProjectID(lProjectID);
                //get correct fasta


                Object returnValue = iRelimsJob.call();
                if (returnValue instanceof Checkpoint) {
                    endState = (Checkpoint) returnValue;
                } else if (returnValue instanceof Object[]) {
                    Object[] endList = (Object[]) returnValue;
                    endState = (Checkpoint) endList[0];
                    lSearchParameters = (SearchParameters) endList[1];
                    conversionErrorList = (List<ConversionError>) endList[2];
                    if (lSearchParameters == null) {
                        lSearchParameters = new SearchParameters();
                    }
                    ResourceManager.setSearchParameters(lSearchParameters);
                    ResourceManager.setConversionErrors(conversionErrorList);
                }
                ResourceManager.setFinishState(endState);
            } catch (Throwable e) {
                e.printStackTrace();
                ResourceManager.setTaskTime(System.currentTimeMillis() - ResourceManager.getTaskTime());
                endState = Checkpoint.FAILED;
                this.stop();
            } finally {
                ResourceManager.setTaskTime(System.currentTimeMillis() - ResourceManager.getTaskTime());
                if (endState == Checkpoint.FINISHED) {
                    logger.debug("Finished task :" + lTaskID
                            + "(projectID : " + lProjectID
                            + "project provider : " + lProjectProviderID
                            + "search strategy : " + lSearchStrategyID
                            + " )");
                    ResourceManager.setFinishState(Checkpoint.FINISHED);
                    System.out.println(" ");
                    System.out.println("Finished task :" + lTaskID
                            + "(projectID : " + lProjectID
                            + "project provider : " + lProjectProviderID
                            + "search strategy : " + lSearchStrategyID
                            + " )");
                    System.out.println(" ");
                } else {
                    if (endState == Checkpoint.FAILED || endState == null) {
                        logger.debug("Failed task :" + lTaskID
                                + "(projectID : " + lProjectID
                                + "project provider : " + lProjectProviderID
                                + "search strategy : " + lSearchStrategyID
                                + " )");
                        ResourceManager.setFinishState(Checkpoint.FAILED);
                        System.out.println(" ");
                        System.out.println("Could not run task :" + lTaskID
                                + "(projectID : " + lProjectID
                                + "project provider : " + lProjectProviderID
                                + "search strategy : " + lSearchStrategyID
                                + " )");
                        System.out.println(" ");
                        ResultManager.removeJunk();
                    } else {
                        if (endState == Checkpoint.PRIDEFAILURE || endState == null) {
                            logger.debug("Failed task :" + lTaskID
                                    + "(projectID : " + lProjectID
                                    + "project provider : " + lProjectProviderID
                                    + "search strategy : " + lSearchStrategyID
                                    + " )");
                            ResourceManager.setFinishState(Checkpoint.PRIDEFAILURE);
                            System.out.println("Reason : PRIDE-ASA did not provide workable data output");
                            System.out.println(" ");
                            ResultManager.removeJunk();
                        }
                    }
                }
                Checkpoint returnValue = Checkpoint.valueOf(ResourceManager.getFinishState());
                if (returnValue == Checkpoint.FAILED || returnValue == null) {
                    return Checkpoint.FAILED;
                } else {
                    if (returnValue == Checkpoint.PRIDEFAILURE || returnValue == null) {
                        return Checkpoint.PRIDEFAILURE;
                    } else {
                        return Checkpoint.valueOf(ResourceManager.getFinishState());
                    }

                }
            }
        }

        public void start() {
            if (updateThread == null) {
                updateThread = new Thread(this);
                updateThread.start();
            }
        }

        public void stop() {
            if (updateThread != null) {
                updateThread.interrupt();
                updateThread = null;
            }
        }

        @Override
        public void run() {
            int failcounter = 0;
            try {
                Checkpoint finishedState = this.call();
                ResourceManager.setFinishState(finishedState);
            } catch (Exception e) {
                ResourceManager.setFinishState(Checkpoint.FAILED);
                this.stop();
            } finally {
                boolean finished = false;
                while (!finished) {
                    try {
                        logger.info("Sending feedback to server...");
                        finished = resultNotifier.sendResults(Checkpoint.valueOf(ResourceManager.getFinishState()));
                        //TODO re-activate later
                        //    ResultManager.importToColims();
                        TaskReciever.locked = false;
                        Thread.sleep(30000);
                        System.out.println("");
                    } catch (Exception ex) {
                        //catch a general exception to make sure the results are sent...
                        failcounter++;
                        if (failcounter == 10) {
                            logger.error("Server could not be contacted succesfully. Retrying...");
                            logger.error(ex);
                            failcounter = 0;
                            finished = true;
                        }
                    }
                }
                logger.debug("Waiting for new task");
            }
        }
    }
}
