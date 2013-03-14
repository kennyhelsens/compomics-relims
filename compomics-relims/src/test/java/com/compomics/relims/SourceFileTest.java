/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author Kenneth
 */
public class SourceFileTest extends TestCase {

    String superFolder = "src/test/resources/Testing_Files_for_Verification/";
    File MGF_FILE = new File(superFolder + "3.mgf");
    File SEARCHPARAM_FILE = new File(superFolder + "SearchGUI.parameters");
    File PSMS_FILE = new File(superFolder + "PeptideShaker_3_AutoReprocessed_1_psms.txt");
    File PROTEIN_FILE = new File(superFolder + "PeptideShaker_3_AutoReprocessed_1_proteins.txt");
    File PEPTIDE_FILE = new File(superFolder + "PeptideShaker_3_AutoReprocessed_1_peptides.txt");
    File CPS_FILE = new File(superFolder + "3.cps");

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
        SpectrumFactory sf = SpectrumFactory.getInstance();
        boolean correctMGF = false;
        try {
            //load file in 
            sf.addSpectra(MGF_FILE);
            double maxIntensity = sf.getMaxIntensity();
            double maxMz = sf.getMaxMz();
            int nSpectra = sf.getNSpectra();
            if (maxIntensity == 58177.0547 && maxMz == 1198.1465 && nSpectra == 1958) {
                correctMGF = true;
            } else {
                correctMGF = false;
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SourceFileTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SourceFileTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SourceFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        assertEquals(true, correctMGF);
    }

    public void testSearchParameters() {
        try {//TODO ADD ALL PARAMETERS !!!!
            RelimsProperties.initializeForTesting();
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
                System.out.println("The following parameters are faulty/missing : ");
                for (String aParameter : erronousParameterList) {
                    System.out.print(aParameter + " ,");
                }
            } else {
                System.out.println("All parameters were validated");
            }
            assertEquals(true, erronousParameterList.isEmpty());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SourceFileTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(SourceFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void testPsmsFileLength() {
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
            System.out.println("PSMS : " + psmCounter);
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        } finally {
            assertEquals(33, psmCounter);
        }
    }

    public void testProteinsFileLength() {
        int proteinCounter = 0;
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
            System.out.println("PSMS : " + proteinCounter);
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        } finally {
            assertEquals(22, proteinCounter);
        }
    }

    public void testPeptideFileLength() {
        int proteinCounter = 0;
        try {
            FileInputStream fin = new FileInputStream(PEPTIDE_FILE);
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
            System.out.println("PSMS : " + proteinCounter);
            //Close the input stream
            in.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        } finally {
            assertEquals(25, proteinCounter);
        }
    }

    public void testCheckCPSSize() {
        long fileSize = CPS_FILE.length();
        System.out.println(fileSize);
        assertEquals(2529280, fileSize);
    }
}
