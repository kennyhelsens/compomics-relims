package com.compomics.relims.tools;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.provider.ColimsConnectionProvider;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * This class holds everything to initiate the latest
 * compomics-colims database
 */
public class RelimsToColimsImporterTool {

    private static Logger logger = Logger.getLogger(RelimsToColimsImporterTool.class);
    private Connection iConn;

    public RelimsToColimsImporterTool(String aRelimsResultFolder) {
        iConn = ColimsConnectionProvider.getConnection();
        try {
            File lRelimsResultPath = new File(aRelimsResultFolder);


            File lCPSFile = null;
            lCPSFile = getPeptideShakerFile(lRelimsResultPath);

            File lMGFFile = null;
            lMGFFile = getMGFFile(lRelimsResultPath);

            File lFastaFile = null;
            lFastaFile = getFastaFile(lRelimsResultPath);

            lCPSFile.getName()


        } finally{
            try {
                logger.debug("closing database connection");
                iConn.close();
                logger.debug("exiting now");
                System.exit(0);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private File getFastaFile(File aRelimsResultPath) {
        return null;
    }

    private File getMGFFile(File aRelimsResultPath) {
        File lMGFFolder = new File(aRelimsResultPath, "mgf");
        File[] lMGFFiles = lMGFFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getAbsolutePath().endsWith(".mgf");
            }
        });

        if(lMGFFiles.length != 0){
            abort(String.format("Failed to locate single mgf file in %s", lMGFFolder.getName()));
            // should never get here.
            return null;
        }else{
            return(lMGFFiles[0]);
        }
    }

    private File getPeptideShakerFile(File aRelimsResultPath) {
        File[] lCPSFiles = aRelimsResultPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getAbsolutePath().endsWith(".cps");
            }
        });
        if(lCPSFiles.length != 0){
            abort(String.format("Failed to locate single cps result file in %s", aRelimsResultPath.getName()));
            // should never get here.
            return null;
        }else{
            return(lCPSFiles[0]);
        }
    }


    public static void main(String[] args) throws SQLException {
        RelimsProperties.initialize(false);
        String lRelimsResult = null;
        if(args.length == 1){
            lRelimsResult = args[0];
        }else{
            lRelimsResult = "/Users/kennyhelsens/tmp/1852_pride_22042013_044724";
        }
        new RelimsToColimsImporterTool(lRelimsResult);

    }

    private static void abort(String aMessage) {
        logger.debug("aborting colims importer:");
        logger.debug(aMessage);
        logger.debug("exiting now");
        System.exit(0);
    }
}
