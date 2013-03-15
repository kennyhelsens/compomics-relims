/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.tests;

import java.io.File;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class ControllerTest extends TestCase {

    private final static File testDBLocation = new File("src/test/resources/databases");
    private final static Logger logger = Logger.getLogger(ControllerTest.class);

    public ControllerTest(String testName) {
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

    public void testTaskDB() {
        logger.debug("TESTING TASKDATABASE CREATION");
        boolean testDatabaseExists = false;
        File[] filesInDirectory = testDBLocation.listFiles();
        for (File aFile : filesInDirectory) {
            if (!aFile.isDirectory() && aFile.getAbsolutePath().contains("TestTaskDatabase")) {
                logger.debug("A databasefile was found !");
                testDatabaseExists = true;
            }
        }
        assertTrue(testDatabaseExists);
    }

    public void testTaskDBBackup() {
        logger.debug("TESTING TASKDATABASE BACKUP");
        boolean testBackupDb = false;
        File backupLocation = new File(testDBLocation.getAbsolutePath() + "/backups");
        File[] filesInDirectory = backupLocation.listFiles();
        for (File aFile : filesInDirectory) {
            if (!aFile.isDirectory() && aFile.getAbsolutePath().contains("TestTaskDatabase")) {
                logger.debug("A backup has been created !");
                testBackupDb = true;
            }
        }
        assertTrue(testBackupDb);
    }
}
