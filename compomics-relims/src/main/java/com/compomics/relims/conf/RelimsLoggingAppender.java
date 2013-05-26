/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.conf;

import com.compomics.relims.manager.resourcemanager.ResourceManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Kenneth
 */
public class RelimsLoggingAppender extends AppenderSkeleton {

    private File loggingFile = null;
    private BufferedWriter logWriter = null;
    private Boolean debugMode = false;

    public RelimsLoggingAppender() {
        try {
            this.loggingFile = File.createTempFile(ResourceManager.getProjectID() + "_relims", ".log");
            System.out.println(loggingFile.getAbsolutePath() + " has been created");
        } catch (IOException ex) {
        }
        this.debugMode = RelimsProperties.getDebugMode();
    }

    @Override
    protected void append(LoggingEvent le) {
//Exclude the unmarshaller...This clogs the debugger


        if (!le.getMessage().toString().contains("Unmarshaller Initialized")
                && !le.getMessage().toString().contains("Generating peptide modification holder")
                && !le.getMessage().toString().contains("Finding modifications for percursor")) {
            try {
                logWriter = new BufferedWriter(new FileWriter(loggingFile, true));
                if (!debugMode && !le.getLevel().isGreaterOrEqual(Level.INFO)) {
                    //don't append anything that is smaller than INFO!
                } else {

                    logWriter.write(new Timestamp(new Date().getTime()) + " " + le.getLevel().toString() + " : " + le.getMessage() + System.lineSeparator()); //writes to file
                    logWriter.flush();
                }
            } catch (IOException ex) {
                System.err.println("Could not write to log...");
                ex.printStackTrace();
            } finally {
                try {
                    logWriter.close();
                } catch (Exception ex) {
                    logWriter = null;
                }
            }
        }
    }

    public File getLoggingFile() {
        return loggingFile;
    }

    public void export() {
        try {
            FileUtils.copyFile(loggingFile, new File(RelimsProperties.getLogFolder().getAbsolutePath() + "/relims.log"));
            System.out.println("Saved loggingfile in " + RelimsProperties.getLogFolder());
        } catch (IOException ex) {
            System.out.println("Logger was incorrectly terminated");
        }
    }

    @Override
    public void close() {
        System.out.print("Cleaning appender");
        //TODO does more need to be done here?
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
