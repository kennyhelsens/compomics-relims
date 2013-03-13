/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.RelimsControllerMode;
import java.io.File;
import junit.framework.TestCase;

/**
 *
 * @author Kenneth
 */
public class ProcessSimulatorTest extends TestCase {

    private static String[] Controllerargs = new String[]{""};

    public ProcessSimulatorTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {

        RelimsProperties.initializeForTesting();

        super.setUp();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    public void testProcess() {
        InitializeController();
    }

    public void InitializeController() {

        File databaseLocation = new File("src/test/resources/databases");
        if (databaseLocation.exists()) {
            System.err.println("TESTDATABASE FILE ALREADY EXISTS, REMOVING");
            databaseLocation.delete();
        }
        RelimsControllerMode.main(Controllerargs);

    }
}
