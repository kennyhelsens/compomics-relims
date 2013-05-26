package com.compomics.relims.concurrent;

import com.compomics.acromics.process.CommandThread;
import com.compomics.relims.manager.processmanager.processguard.CommandExceptionGuard;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import java.io.*;
import org.apache.log4j.Logger;

public class Command {
// ------------------------------ FIELDS ------------------------------

    /**
     * Plain logger.
     */
    private static Logger logger = Logger.getLogger(CommandThread.class);
    /**
     * The folder where the jar is located that should be run
     */
    private static File workFolder;
    /**
     * A ProgressManager to store the state of the project and monitor it
     */
    private static ProgressManager progressManager = ProgressManager.getInstance();
    /**
     * The process exit value differs from 0 if there is an error
     */
    private static int processExitValue = 0;
    /**
     * An errorGuard : reads and analyzes the output of the called process (if
     * it is an external jar). It looks for keywords and sets up a time out.
     * When something goes wrong, it kills the search engines... TODO : DOES
     * THIS WORK FOR LINUX?
     *
     */
    private static CommandExceptionGuard errorGuard;
    /**
     * Not sure what this does...kept it anyway TODO figure it out !
     */
    private static File lScriptFile;

// -------------------------- STATIC METHODS --------------------------
    public static int call(String aCommand) {
        try {
            try {
                long startTime = System.currentTimeMillis();
                logger.debug("Running process:\t" + aCommand);
                Process processus = Runtime.getRuntime().exec(aCommand, null, workFolder);
                errorGuard = new CommandExceptionGuard(processus);
                boolean errorless = (Boolean) errorGuard.call();
                if (errorless) {
                    processus.waitFor();
                    processExitValue = processus.exitValue();
                    if (processExitValue > 0) {
                        processExitValue = 1;
                    }
                } else {
                    processExitValue = 1;
                }
                processus.destroy();
                //System.out.println("Process exitValue: " + processExitValue);
            } catch (Throwable e) {
                logger.error(e.getMessage());
                logger.error(e.getCause());
                System.out.println(e.getMessage());
                System.out.println("Consult the log file for more detailed information on the error");
                progressManager.setState(Checkpoint.FAILED);
            } finally {
                errorGuard.release();
                errorGuard = null;
            }
        } catch (Throwable ex) {
            progressManager.setState(Checkpoint.FAILED, ex);
        }
        return processExitValue;
    }

    /**
     * Create a qsub script file for the given command.
     *
     * @param aCommand String with start command.
     */
    public static void runqsub(String aCommand) throws IOException {
        try {
            File lScriptFileFolder = new File((System.getProperty("user.home")) + "/tmp/");
            if (!lScriptFileFolder.exists()) {
                lScriptFileFolder.mkdir();
            }

            File lScriptFile = new File(lScriptFileFolder, "" + System.currentTimeMillis() + ".sh");

            if (!lScriptFile.exists()) {
                lScriptFile.createNewFile();
            }
        } catch (Exception e) {
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(lScriptFile));
        String lQsubHeader = getQSubHeader(lScriptFile.getName() + "_");
        bw.write(lQsubHeader);
        bw.write(aCommand);
        bw.flush();
        bw.close();

        try {
            String lScriptFileName = lScriptFile.getAbsolutePath();
            Runtime.getRuntime().exec("qsub " + lScriptFileName);
        } catch (Exception e) {
            e.printStackTrace();
            progressManager.setState(Checkpoint.FAILED, e);;
            Thread.currentThread().interrupt();
            return;
        }
    }

    private static String getQSubHeader(String aName) {
        StringBuilder sb = new StringBuilder();

        sb.append("#!/bin/sh\n");
        sb.append("\n");
        sb.append("#PBS -N processName\n");
        sb.append("#PBS -o /user/home/gent/vsc401/vsc40161/tmp/" + aName + "output.txt\n");
        sb.append("#PBS -e /user/home/gent/vsc401/vsc40161/tmp/" + aName + "error.txt\n");
        sb.append("#PBS -l walltime=00:30:00\n");
        sb.append("\n");

        return sb.toString();
    }

    /*    private static String getQSubHeader() {
     return getQSubHeader("");
     }*/
    public static void setWorkFolder(File aWorkFolder) {
        workFolder = aWorkFolder;
    }
}