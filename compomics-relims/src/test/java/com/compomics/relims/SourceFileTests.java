/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims;

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
        suite.addTest(new SourceFileTest("testMGF"));
        return suite;
    }
    
    @Override
    protected void setUp() throws Exception {
        Simulator.simulateProcess();
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        Simulator.endSimulation();
        super.tearDown();
    }
}
