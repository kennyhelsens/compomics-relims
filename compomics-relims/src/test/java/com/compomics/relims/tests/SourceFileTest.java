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

    private static String superFolder;// = "src/test/resources/Testing_Files_for_Verification/";
    private final static Logger logger = Logger.getLogger(SourceFileTest.class);
    private static double MAX_MGF_INTENSITY = 58177.0547;
    private static double MAX_MGF_MZ = 1198.1465;
    private static double N_MGF_SPECTRA = 1958;
    private static long cpsFileSize = 0;
    private static int N_PSMS = 49;
    private static int N_PEPTIDE = 45;
    private static int N_PROTEIN = 29;
    private static long projectID = 0L;

    public static void setCpsFileSize(long cpsFileSize) {
        SourceFileTest.cpsFileSize = cpsFileSize;
    }

    public static void setSuperFolder(String superFolder) {
        SourceFileTest.superFolder = superFolder;
    }

    public static void setProjectID(long projectID) {
        SourceFileTest.projectID = projectID;
    }

    public static void setMAX_MGF_INTENSITY(double MAX_MGF_INTENSITY) {
        SourceFileTest.MAX_MGF_INTENSITY = MAX_MGF_INTENSITY;
    }

    public static void setMAX_MGF_MZ(double MAX_MGF_MZ) {
        SourceFileTest.MAX_MGF_MZ = MAX_MGF_MZ;
    }

    public static void setN_MGF_SPECTRA(double N_MGF_SPECTRA) {
        SourceFileTest.N_MGF_SPECTRA = N_MGF_SPECTRA;
    }

    public static void setN_PSMS(int N_PSMS) {
        SourceFileTest.N_PSMS = N_PSMS;
    }

    public static void setN_PEPTIDE(int N_PEPTIDE) {
        SourceFileTest.N_PEPTIDE = N_PEPTIDE;
    }

    public static void setN_PROTEIN(int N_PROTEIN) {
        SourceFileTest.N_PROTEIN = N_PROTEIN;
    }

    public SourceFileTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
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
        File MGF_FILE = new File(Simulator.getResultFolder().getAbsolutePath() + "/" + projectID + ".mgf");
        try {
            //load file in 
            sf.addSpectra(MGF_FILE);
            double maxIntensity = sf.getMaxIntensity();
            double maxMz = sf.getMaxMz();
            int nSpectra = sf.getNSpectra();
            logger.debug(maxIntensity);
            logger.debug(maxMz);
            logger.debug(nSpectra);
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
            RelimsProperties.initialize(true);
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


            ModificationProfile modProfile = parameters.getModificationProfile();
            List<String> PTMs = modProfile.getAllModifications();
            List<String> verifiedPTMs = new ArrayList<String>();
            verifiedPTMs.add("oxidation of m");
            verifiedPTMs.add("pyro-glutamation of e");
            verifiedPTMs.add("terminal acetylation");
            verifiedPTMs.add("carboxyamidomethylation");
            if (!PTMs.containsAll(verifiedPTMs)) {
                erronousParameterList.add("PTMS");
                if (PTMs.isEmpty()) {
                    logger.debug("No PTMS in searchparameters modProfile !");
                } else {
                    logger.debug("Incorrect PTMS !");
                }

            }
            if (!erronousParameterList.isEmpty()) {
                System.out.println("The following parameters are faulty/missing : ");
                for (String aParameter : erronousParameterList) {
                    System.out.println(aParameter + " ,");
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
        File PSMS_FILE = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/PeptideShaker_" + this.projectID + "_AutoReprocessed_1_psms.txt");
        logger.debug(PSMS_FILE.getAbsolutePath());
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
        File PROTEIN_FILE = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/PeptideShaker_" + projectID + "_AutoReprocessed_1_proteins.txt");
        logger.debug(PROTEIN_FILE.getAbsolutePath());
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
        File PEPTIDE_FILE = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/PeptideShaker_" + projectID + "_AutoReprocessed_1_peptides.txt");
        logger.debug(PEPTIDE_FILE.getAbsolutePath());
        try {
            FileInputStream fin = new FileInputStream(PEPTIDE_FILE);
            DataInputStream in = new DataInputStream(fin);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                if (!strLine.toLowerCase().contains("protein")) {
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
        File CPS_FILE = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/" + projectID + ".cps");
        long fileSize = CPS_FILE.length();
        System.out.println(fileSize);
        assertEquals(cpsFileSize, fileSize);
    }
}
