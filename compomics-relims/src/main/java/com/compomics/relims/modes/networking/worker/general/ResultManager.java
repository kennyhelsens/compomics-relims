/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.general;

import com.compomics.colims.core.exception.PeptideShakerIOException;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.colimsmanager.ColimsImporter;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.preferences.ModificationProfile;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class ResultManager {

    private static ResultManager resultManager;
    private static final Logger logger = Logger.getLogger(ResultManager.class);
    private static HashMap<String, Object> resultMap;

    public ResultManager() {
        this.resultMap = new HashMap<String, Object>();
    }

    public HashMap<String, Object> buildResultMap() {
        logger.info("Gathering results...");
        includeSearchParameters();
        resultMap.put("psms", getPsms());
        resultMap.put("peptides", getPeptides());
        resultMap.put("proteins", getProteins());
        for (String aKey : resultMap.keySet()) {
            logger.debug("Set " + aKey + " to " + resultMap.get(aKey));
        }
        return resultMap;
    }

    private static long getPsms() {
        File PSM_FILE = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/PeptideShaker_" + ResourceManager.getProjectID() + "_AutoReprocessed_1_psms.txt");

        int psmCounter = 0;
        try {
            if (PSM_FILE.exists()) {
                logger.debug("Getting peptides from " + PSM_FILE.getAbsolutePath());
                FileInputStream fin = new FileInputStream(PSM_FILE);
                DataInputStream in = new DataInputStream(fin);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                //Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    // Print the content on the console
                    if (!strLine.startsWith("Protein")) {
                        psmCounter++;
                    }
                }
                logger.debug("PSMS in PSMS-file : " + psmCounter);
                //Close the input stream
                in.close();
            } else {
                logger.debug("No peptides were found : " + PSM_FILE.getAbsolutePath() + " does not exist");
            }
        } catch (Exception e) {//Catch exception if any
            logger.error(e);
        } finally {
            return psmCounter;
        }
    }

    public static void transferToColims() {
        File cpsFile = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/" + ResourceManager.getProjectID() + ".cps");
        if (cpsFile.exists()) {
            try {
                ColimsImporter.transferToColims(cpsFile);
            } catch (PeptideShakerIOException ex) {
                logger.error("Could not store in Colims ");
                logger.error(ex);
            }
        } else {
            logger.warn("There was no cps-file to be found");
        }
    }

    private static long getPeptides() {
        File PEP_FILE = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/PeptideShaker_" + ResourceManager.getProjectID() + "_AutoReprocessed_1_peptides.txt");
        int pepCounter = 0;
        try {
            if (PEP_FILE.exists()) {
                logger.debug("Getting peptides from " + PEP_FILE.getAbsolutePath());
                FileInputStream fin = new FileInputStream(PEP_FILE);
                DataInputStream in = new DataInputStream(fin);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                //Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    // Print the content on the console
                    if (!strLine.toLowerCase().contains("protein")) {
                        pepCounter++;
                    }
                }
                logger.debug("Peptides in Peptide-file : " + pepCounter);
                //Close the input stream
                in.close();
            } else {
                logger.debug("No peptides were found : " + PEP_FILE.getAbsolutePath() + " does not exist");
            }
        } catch (Exception e) {//Catch exception if any
            logger.error(e);
        } finally {
            return pepCounter;
        }
    }

    private long getProteins() {
        int proteinCounter = 0;
        File PROTEIN_FILE = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/PeptideShaker_" + ResourceManager.getProjectID() + "_AutoReprocessed_1_proteins.txt");
        logger.debug("Getting proteins from " + PROTEIN_FILE.getAbsolutePath());
        try {
            if (PROTEIN_FILE.exists()) {
                FileInputStream fin = new FileInputStream(PROTEIN_FILE);
                DataInputStream in = new DataInputStream(fin);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                //Read File Line By Line
                while ((strLine = br.readLine()) != null) {
                    // Print the content on the console
                    if (!strLine.startsWith("Protein")) {
                        proteinCounter++;
                    }
                }
                logger.debug("Proteins in protein-file  : " + proteinCounter);
                //Close the input stream
                in.close();
            } else {
                logger.debug("No proteins were found : " + PROTEIN_FILE.getAbsolutePath() + " does not exist");
            }
        } catch (Exception e) {//Catch exception if any
            logger.error(e);
        } finally {
            return proteinCounter;
        }
    }

    private void includeSearchParameters() {
        try {//TODO ADD ALL PARAMETERS !!!!
            File SEARCHPARAM_FILE = new File(RelimsProperties.getWorkSpace() + "/SearchGUI.parameters");
            logger.debug("Getting searchparameters from " + SEARCHPARAM_FILE.getAbsolutePath());
            SearchParameters parameters = SearchParameters.getIdentificationParameters(SEARCHPARAM_FILE);
            Enzyme enzyme = parameters.getEnzyme();
            resultMap.put("enzyme", enzyme.getName());
            File usedFasta = parameters.getFastaFile();
            resultMap.put("fasta", usedFasta.getAbsolutePath());
            Charge searchedCharge = parameters.getMaxChargeSearched();
            resultMap.put("charge", searchedCharge);
            double fragmentIonAccuracy = parameters.getFragmentIonAccuracy();
            resultMap.put("fragmentIonAccuracy", fragmentIonAccuracy);
            double precursorAccuracy = parameters.getPrecursorAccuracy();
            resultMap.put("precursorAccuracy", precursorAccuracy);
            ModificationProfile modProfile = parameters.getModificationProfile();
            List<String> varMods = modProfile.getVariableModifications();
            List<String> fixMods = modProfile.getFixedModifications();
            resultMap.put("varmods", varMods);
            resultMap.put("fixmods", fixMods);
            logger.debug("Collected results for storage");
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException | ClassNotFoundException ex) {
            logger.error(ex);
        }
    }

    public static void removeJunk() {
        //omx
        logger.debug("Deleting working files to reduce foldersize ");
        File[] allFilesInResults = RelimsProperties.getWorkSpace().listFiles();
        String[] deleteMarkers = new String[]{".mgf", ".xml", "omx", "SearchGUI_input"};
        String absolutePath;
        for (File aFile : allFilesInResults) {
            absolutePath = aFile.getAbsolutePath();
            //always delete these extensions
            if (absolutePath.endsWith(".cui")) {
                aFile.delete();
                logger.debug("Deleted " + aFile.getName());
            } else if (absolutePath.contains(".mgf") || absolutePath.contains(".omx") || absolutePath.contains(".xml")) {
                aFile.delete();
                logger.debug("Deleted " + aFile.getName());
            } else if (absolutePath.contains("SearchGUI_input")) {
                aFile.delete();
                logger.debug("Deleted " + aFile.getName());
            }
            //only delete these extensions when the switch is on TODO

        }

        logger.info("Removed searchengine result files to reduce foldersize");
    }
}
