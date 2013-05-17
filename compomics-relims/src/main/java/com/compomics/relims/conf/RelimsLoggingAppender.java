/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.conf;

import com.compomics.relims.modes.networking.worker.general.ResourceManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
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
            // this.loggingFile = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/processinfo/relims.log");
            this.loggingFile = File.createTempFile(ResourceManager.getProjectID() + "_relims", ".log");
            System.out.println(loggingFile.getAbsolutePath() + " has been created");
        } catch (IOException ex) {
            Logger.getLogger(RelimsLoggingAppender.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
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
                if (!debugMode && !le.getLevel().isGreaterOrEqual(Level.ERROR)) {
                    //don't append anything that is smaller than ERROR!
                } else {
                    logWriter = new BufferedWriter(new FileWriter(loggingFile, true));
                    logWriter.write(new Timestamp(new Date().getTime()) + " " + le.getLevel().toString() + " : " + le.getMessage() + System.lineSeparator()); //writes to file
                    logWriter.flush();
                }
            } catch (IOException ex) {
                System.err.println("Could not write to log...");
            } finally {
                try {
                    logWriter.close();
                } catch (IOException ex) {
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
        System.out.print("Cleaning appender"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
