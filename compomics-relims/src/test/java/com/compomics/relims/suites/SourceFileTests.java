/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.suites;

import com.compomics.relims.tests.SourceFileTest;
import com.compomics.relims.simulator.Simulator;
import com.compomics.relims.tests.ControllerTest;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author Kenneth
 */

public class SourceFileTests extends TestCase {

    public SourceFileTests(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("SourceFileTests");
        suite.addTest(new Simulator("testSimulateProcess"));
        suite.addTest(new ControllerTest("testTaskDB"));
        suite.addTest(new ControllerTest("testTaskDBBackup"));
        suite.addTest(new SourceFileTest("testMGF"));
        suite.addTest(new SourceFileTest("testSearchParameters"));
        suite.addTest(new SourceFileTest("testPsmsFileLength"));
        suite.addTest(new SourceFileTest("testProteinsFileLength"));
        suite.addTest(new SourceFileTest("testPeptideFileLength"));
        suite.addTest(new SourceFileTest("testCheckCPSSize"));
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
        Simulator.endSimulation();
    }
}
