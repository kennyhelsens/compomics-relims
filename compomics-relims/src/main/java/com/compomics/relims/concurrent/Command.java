package com.compomics.relims.concurrent;

import com.compomics.acromics.process.CommandThread;
import org.apache.log4j.Logger;

import java.io.*;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class Command {
// ------------------------------ FIELDS ------------------------------


    private static Logger logger = Logger.getLogger(CommandThread.class);
    private static File iWorkFolder;

// -------------------------- STATIC METHODS --------------------------


    public static void run(String aCommand) {
        BufferedReader out = null;
        try {
            long startTime = System.currentTimeMillis();
            String strOutputline;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date(System.currentTimeMillis());
            logger.debug("Running process:\t" + aCommand);
            Process processus = Runtime.getRuntime().exec(aCommand, null, iWorkFolder);
//            out = new BufferedReader(new InputStreamReader(processus.getErrorStream()));
            out = new BufferedReader(new InputStreamReader(processus.getInputStream()));
            while ((strOutputline = out.readLine()) != null) {
                now.setTime(System.currentTimeMillis());
                logger.debug(sdf.format(now) + " " + strOutputline);
            }
            logger.debug("RESULT : " + processus.waitFor());
            out.close();
            processus.destroy();
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Duration : " + duration);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Create a qsub script file for the given command.
     *
     * @param aCommand String with start command.
     */
    public static void runqsub(String aCommand) throws IOException {
        File lScriptFileFolder = new File((System.getProperty("user.home")) + "/tmp/");
        if (!lScriptFileFolder.exists()) {
            lScriptFileFolder.mkdir();
        }

        File lScriptFile = new File(lScriptFileFolder, "" + System.currentTimeMillis() + ".sh");
        if (!lScriptFile.exists()) {
            lScriptFile.createNewFile();
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

    private static String getQSubHeader() {
        return getQSubHeader("");
    }

    public static void setWorkFolder(File aWorkFolder) {
        iWorkFolder = aWorkFolder;
    }
}