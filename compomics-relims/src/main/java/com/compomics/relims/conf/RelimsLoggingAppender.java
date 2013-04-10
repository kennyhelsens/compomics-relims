/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.conf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Kenneth
 */
public class RelimsLoggingAppender extends AppenderSkeleton {

    private final File loggingFile;
    private BufferedWriter logWriter = null;

    public RelimsLoggingAppender() {
        this.loggingFile = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/processinfo/relims.log");
    }

    @Override
    protected void append(LoggingEvent le) {
//Exclude the unmarshaller...This clogs the debugger
        if (!le.getMessage().toString().contains("DEBUG : Unmarshaller Initialized")
                && !le.getMessage().toString().contains("Generating peptide modification holder")
                && !le.getMessage().toString().contains("Finding modifications for percursor")) {
            try {
                logWriter = new BufferedWriter(new FileWriter(loggingFile, true));
                logWriter.write(new Timestamp(new Date().getTime()) + " " + le.getLevel().toString() + " : " + le.getMessage() + System.lineSeparator()); //writes to file
                logWriter.flush();
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

    @Override
    public void close() {
        System.out.println("Logger was terminated");
    }

    @Override
    public boolean requiresLayout() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
