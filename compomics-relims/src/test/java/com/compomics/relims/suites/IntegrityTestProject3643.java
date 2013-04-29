/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.suites;

import com.compomics.relims.tests.SourceFileTest;
import com.compomics.relims.simulator.Simulator;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Kenneth
 */
public class IntegrityTestProject3643 extends TestCase {

    public IntegrityTestProject3643(String testName) {
        super(testName);
    }

    public static Test suite() {
        Simulator.setProjectID(3643);
        SourceFileTest.setProjectID(3643);
        SourceFileTest.setN_PSMS(1233);
        SourceFileTest.setN_PROTEIN(96);
        SourceFileTest.setN_PEPTIDE(689);
        SourceFileTest.setN_MGF_SPECTRA(517);
        SourceFileTest.setMAX_MGF_MZ(1321.146);
        SourceFileTest.setMAX_MGF_INTENSITY(1.0);

        TestSuite suite = new TestSuite("SourceFileTests");
        suite.addTest(new Simulator("testSimulateProcess"));
        suite.addTest(new SourceFileTest("testMGF"));
        suite.addTest(new SourceFileTest("testSearchParameters"));
        suite.addTest(new SourceFileTest("testPsmsFileLength"));
        suite.addTest(new SourceFileTest("testProteinsFileLength"));
        suite.addTest(new SourceFileTest("testPeptideFileLength"));
        //   suite.addTest(new SourceFileTest("testCheckCPSSize"));
        suite.addTest(new Simulator("testFinalCleanUp"));
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
