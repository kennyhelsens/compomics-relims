package com.compomics.relims.manager.progressmanager;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.usernotificationmanager.MailEngine;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.relims.modes.networking.worker.general.ProcessRelocalizer;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.log4j.Logger;

public class ProgressManager {

    private final static Logger logger = Logger.getLogger(ProgressManager.class);
    public final static boolean remoteMode = true;
    private static Checkpoint state;
    private static Checkpoint endState;
    private static MyProgress newProgress = new MyProgress();
    private static ProgressManager progressManager;
    private static final ArrayList<Checkpoint> failingList = new ArrayList();

    private ProgressManager() {
    }

    public static ProgressManager getInstance() {
        if (failingList.isEmpty()) {
            failingList.add(Checkpoint.MODFAILURE);
            failingList.add(Checkpoint.FAILED);
            failingList.add(Checkpoint.PROCESSFAILURE);
            failingList.add(Checkpoint.PRIDEFAILURE);
            failingList.add(Checkpoint.TIMEOUTFAILURE);
        }
        return ProgressManager.progressManager;
    }

    public static Checkpoint getEndState() {
        return endState;
    }

    public static void setEndState(Checkpoint checkpoint) {
        endState = checkpoint;
    }

    public static void setState(Checkpoint checkpoint, Throwable e) {
        logger.error(e);
        setState(checkpoint);
        //append to manual verification file
        logForValidation(Arrays.toString(e.getStackTrace()));
        try {
            String directory = RelimsProperties.getRepositoryPath();
            FileOutputStream fos;
            fos = new FileOutputStream(new File(directory + "projects4manual.txt"));
            PrintStream ps = new PrintStream(fos);
            e.printStackTrace(ps);
        } catch (FileNotFoundException ex) {
            logger.error("Could not log to file...");
        }
    }

    public static void setState(Checkpoint checkpoint, Exception e) {
        logger.error(e.getMessage());
        logger.error(e.getCause());
        System.out.println(e.getMessage());
        setState(checkpoint);
        //append to manual verification file
        logForValidation(Arrays.toString(e.getStackTrace()));
        try {
            String directory = RelimsProperties.getRepositoryPath();
            FileOutputStream fos;
            fos = new FileOutputStream(new File(directory + "projects4manual.txt"));
            PrintStream ps = new PrintStream(fos);
            e.printStackTrace(ps);
        } catch (FileNotFoundException ex) {
            logger.error("Could not log to file...");
        }

    }

    private static void logForValidation(String stackTrace) {
        try {
            String directory = RelimsProperties.getRepositoryPath();
            File outputFile = new File(directory + "projects4manual.txt");
            FileWriter fstream = new FileWriter(outputFile, true);
            BufferedWriter out = new BufferedWriter(fstream);
            try {
                out.write("=========================================================");
                out.newLine();
                out.newLine();
                out.write("Project ID : " + ProcessVariableManager.getProjectId());
                out.newLine();
                out.write("Error stacktrace : ");
                out.newLine();
                out.write(stackTrace);
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            } finally {
                out.close();
            }
        } catch (IOException e) {
        }
        // System.out.println("Project was flagged for manual validation. Please refer to the projects4manual.txt ");
    }

    public static void setUp() {
        ProgressManager.progressManager = new ProgressManager();
        newProgress.setState(Checkpoint.STARTING);
    }

    public static void setState(Checkpoint state) {
        newProgress.setState(state);

        if (state == Checkpoint.FINISHED || state == Checkpoint.FAILED || state == Checkpoint.PRIDEFAILURE || state == Checkpoint.PROCESSFAILURE || state == Checkpoint.PEPTIDESHAKERFAILURE || state == Checkpoint.TIMEOUTFAILURE) {
            if (endState != Checkpoint.FAILED && endState != Checkpoint.PRIDEFAILURE && endState != Checkpoint.PROCESSFAILURE) {
                endState = state;
            }
        }
    }

    public static Checkpoint getState() {
        return ProgressManager.endState;
    }

    private static class MyProgress implements PropertyChangeListener, Serializable {

        private Checkpoint state;
        private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

        public MyProgress() {

            pcs.addPropertyChangeListener(this);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (remoteMode) {
                Checkpoint previousState = (Checkpoint) evt.getOldValue();
                if (previousState == Checkpoint.FAILED) {
                    this.state = Checkpoint.FAILED;
                    ProgressManager.endState = Checkpoint.FAILED;
                    logger.debug("Task has failed :");
                    logger.debug(evt.getSource().getClass());
                } else {
                    logger.debug("Task passed a checkpoint : " + evt.getNewValue());
                    this.state = (Checkpoint) evt.getNewValue();
                    if (failingList.contains(state)) {
                        try {
                            File logfile = ProcessRelocalizer.getLocalLoggingFile();

                            if (!logfile.exists()) {
                                logger.error(logfile.getAbsolutePath() + " doesn't exist");
                                logfile = null;
                            }
                            MailEngine.sendMail("Project " + ProcessVariableManager.getProjectId() + " has failed !", "See attached logfile for further details", logfile);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            logger.error("Could not send logfile...");
                        }
                    }
                    if (state == Checkpoint.FINISHED && endState != Checkpoint.FAILED) {
                        ProgressManager.endState = Checkpoint.FINISHED;
                    }
                }

            }
        }

        public Checkpoint getState() {
            return state;
        }

        public void setState(Checkpoint state) {
            //    try {
            Checkpoint oldValue = this.state;
            pcs.firePropertyChange("state", oldValue, state);
            //         throw new Exception();
            //     } catch (Exception e) {
            //         e.printStackTrace();
            //       }
        }
    }
}
