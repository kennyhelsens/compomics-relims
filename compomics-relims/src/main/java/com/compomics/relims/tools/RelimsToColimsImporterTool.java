package com.compomics.relims.tools;

import com.compomics.colims.core.exception.MappingException;
import com.compomics.colims.core.exception.PeptideShakerIOException;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.colimsmanager.ColimsImporter;
import com.compomics.relims.model.provider.ColimsConnectionProvider;
import com.compomics.relims.modes.networking.worker.general.ResourceManager;
import com.compomics.util.experiment.identification.SearchParameters;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
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
            logger.debug(String.format("identified mgf file %s", lMGFFile.getName()));

            File lFastaFile = null;
            lFastaFile = getFastaFile(lRelimsResultPath);
            logger.debug(String.format("identified fasta file %s", lFastaFile.getName()));

            String lTitle = String.format("taskid-%d-projectid-%d", ResourceManager.getTaskID(), ResourceManager.getProjectID());
            ColimsImporter.getInstance().transferToColims(lCPSFile, lFastaFile, lMGFFile, lTitle);


        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (MappingException e) {
            logger.error(e.getMessage(), e);
        } catch (PeptideShakerIOException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
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

    private File getFastaFile(File aRelimsResultPath) throws IOException, ClassNotFoundException {
        File lSearchParameterFile = new File(aRelimsResultPath, "SearchGUI.parameters");
        SearchParameters lSearchParameters = SearchParameters.getIdentificationParameters(lSearchParameterFile);
        File lFastaFile = lSearchParameters.getFastaFile();

        if(lFastaFile.exists()){
            logger.debug(String.format("identified fasta file %s", lFastaFile.getName()));
            return(lFastaFile);
        }else{

           // hard coded hack from analysis run by Kenneth on Volume V, different on localhost @kenny
          lFastaFile = new File("/Users/kennyhelsens/tmp/sp_local/SP_concatenated_target_decoy.fasta");
          if(lFastaFile.exists()){
              return lFastaFile;
          }

           // if this doesn't work either, abort.
          abort(String.format("Failed to locate fasta file in %s", lFastaFile.getName()));
        }
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

        if(lMGFFiles.length != 1){
            abort(String.format("Failed to locate single mgf file in %s", lMGFFolder.getName()));
            // should never get here.
            return null;
        }else if(lMGFFiles[0].exists()){
                logger.debug(String.format("identified mgf file %s", lMGFFiles[0].getName()));
                return(lMGFFiles[0]);
        }else{
          return null;
        }

    }

    private File getPeptideShakerFile(File aRelimsResultPath) {
        File[] lCPSFiles = aRelimsResultPath.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getAbsolutePath().endsWith(".cps");
            }
        });
        if(lCPSFiles.length != 1){
            abort(String.format("Failed to locate single cps result file in %s", aRelimsResultPath.getName()));
            // should never get here.
            return null;
        }else if(lCPSFiles[0].exists()){
                logger.debug(String.format("identified cps file %s", lCPSFiles[0].getName()));
                return(lCPSFiles[0]);
        }else{
          return null;
        }
    }


    public static void main(String[] args) throws SQLException {
        RelimsProperties.initialize(false);
        String lRelimsResult = null;
        if(args.length == 1){
            lRelimsResult = args[0];
        }else{
            lRelimsResult = "/Users/kennyhelsens/tmp/11954_pride_30042013_123103";
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
