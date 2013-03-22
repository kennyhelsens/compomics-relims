/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.tests;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.simulator.Simulator;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.preferences.ModificationProfile;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class SourceFileTest extends TestCase {

    String superFolder;// = "src/test/resources/Testing_Files_for_Verification/";
    private final static Logger logger = Logger.getLogger(SourceFileTest.class);
    private final static double MAX_MGF_INTENSITY = 58177.0547;
    private final static double MAX_MGF_MZ = 1198.1465;
    private final static double N_MGF_SPECTRA = 1958;
    private final static int N_PSMS = 33;
    private final static int N_PEPTIDE = 25;
    private final static int N_PROTEIN = 22;

    public SourceFileTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        superFolder = Simulator.getResultFolder().getAbsolutePath() + "/";
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    public void testMGF() {
        logger.debug("TESTING MGF RESULTFILE");
        SpectrumFactory sf = SpectrumFactory.getInstance();
        boolean correctMGF = false;
        File MGF_FILE = new File(superFolder + "12088.mgf");
        try {
            //load file in 
            sf.addSpectra(MGF_FILE);
            double maxIntensity = sf.getMaxIntensity();
            double maxMz = sf.getMaxMz();
            int nSpectra = sf.getNSpectra();
            if (maxIntensity == MAX_MGF_INTENSITY && maxMz == MAX_MGF_MZ && nSpectra == N_MGF_SPECTRA) {
                correctMGF = true;
            } else {
                correctMGF = false;
            }
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        } catch (ClassNotFoundException ex) {
            logger.error(ex);
        }
        assertEquals(true, correctMGF);
    }

    public void testSearchParameters() {
        logger.debug("TESTING SEARCHPARAMETERS RESULTFILE");
        try {//TODO ADD ALL PARAMETERS !!!!
            RelimsProperties.initializeForTesting();
            File SEARCHPARAM_FILE = new File(superFolder + "SearchGUI.parameters");
            SearchParameters parameters = SearchParameters.getIdentificationParameters(SEARCHPARAM_FILE);
            List<String> erronousParameterList = new ArrayList<String>();
            Enzyme enzyme = parameters.getEnzyme();
            File usedFasta = parameters.getFastaFile();
            Charge searchedCharge = parameters.getMaxChargeSearched();
            double fragmentIonAccuracy = parameters.getFragmentIonAccuracy();
            double precursorAccuracy = parameters.getPrecursorAccuracy();

            //problems with fasta due to makeblast db, had to override them during test!

            if (!enzyme.getName().toLowerCase().contains("trypsin")) {
                erronousParameterList.add("Enzyme");
            }

            if (!searchedCharge.getChargeAsFormattedString().equals("++++")) {
                erronousParameterList.add("Charge");
            }

            if (fragmentIonAccuracy != 1.0) {
                erronousParameterList.add("fragmentIonAccuracy");
            }

            if (precursorAccuracy != 1.0) {
                erronousParameterList.add("precursorAccuracy");
            }

            if (!erronousParameterList.isEmpty()) {
                logger.debug("The following parameters are faulty/missing : ");
                for (String aParameter : erronousParameterList) {
                    logger.debug(aParameter + " ,");
                }
                ModificationProfile modProfile = parameters.getModificationProfile();
                List<String> PTMs = modProfile.getAllModifications();
                List<String> verifiedPTMs = new ArrayList<String>();
                verifiedPTMs.add("Oxidation");
                verifiedPTMs.add("Deamination");
                verifiedPTMs.add("Carbamidomethyl");
                verifiedPTMs.add("Acetylation");
                if (!PTMs.containsAll(verifiedPTMs)) {
                    erronousParameterList.add("PTMS");
                    if (PTMs.isEmpty()) {
                        logger.debug("No PTMS in searchparameters modProfile !");
                    } else {
                        logger.debug("Incorrect PTMS !");
                    }

                }

            } else {
                logger.debug("All parameters were validated");
            }
            assertEquals(true, erronousParameterList.isEmpty());
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException | ClassNotFoundException ex) {
            logger.error(ex);
        }
    }

    public void testPsmsFileLength() {
        logger.debug("TESTING PSMS RESULTFILE");
        File PSMS_FILE = new File(superFolder + "PeptideShaker_12088_AutoReprocessed_1_psms.txt");
        int psmCounter = 0;
        try {
            FileInputStream fin = new FileInputStream(PSMS_FILE);
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
        } catch (Exception e) {//Catch exception if any
            logger.error(e);
        } finally {
            assertEquals(N_PSMS, psmCounter);
        }
    }

    public void testProteinsFileLength() {
        logger.debug("TESTING PROTEIN RESULTFILE");
        int proteinCounter = 0;
        File PROTEIN_FILE = new File(superFolder + "PeptideShaker_12088_AutoReprocessed_1_proteins.txt");
        try {
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
        } catch (Exception e) {//Catch exception if any
            logger.error(e);
        } finally {
            assertEquals(N_PROTEIN, proteinCounter);
        }
    }

    public void testPeptideFileLength() {
        logger.debug("TESTING PEPTIDE RESULTFILE");
        int peptideCounter = 0;
        File PEPTIDE_FILE = new File(superFolder + "PeptideShaker_12088_AutoReprocessed_1_peptides.txt");
        try {
            FileInputStream fin = new FileInputStream(PEPTIDE_FILE);
            DataInputStream in = new DataInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                if (!strLine.startsWith("Protein")) {
                    peptideCounter++;
                }
            }
            logger.debug("Peptides in peptidefile : " + peptideCounter);
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            logger.error(e);
        } finally {
            assertEquals(N_PEPTIDE, peptideCounter);
        }
    }

    public void testCheckCPSSize() {
        logger.debug("TESTING CPS RESULTFILE");
        File CPS_FILE = new File(superFolder + "12088.cps");
        long fileSize = CPS_FILE.length();
        System.out.println(fileSize);
        assertEquals(2529280, fileSize);
    }
}
