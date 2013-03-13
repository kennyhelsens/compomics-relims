/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.client.connectivity.connectors.ServerConnector;
import com.compomics.relims.modes.networking.controller.RelimsControllerMode;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.TaskContainer;
import com.compomics.relims.modes.networking.worker.RelimsWorkerMode;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author Kenneth
 */
public class ProcessSimulatorTest extends TestCase {

    private static String[] Controllerargs = new String[]{""};
    private static String[] Workerargs = new String[]{"-workerport", "15557"};
    private String[] resultFolderFilenames = new String[]{"3.cps",
        "3.mgf",
        "3.omx",
        "3.t.xml",
        "PeptideShaker_3_AutoReprocessed_1_peptides.txt",
        "PeptideShaker_3_AutoReprocessed_1_proteins.txt",
        "PeptideShaker_3_AutoReprocessed_1_psms.txt",
        "SearchGUI.parameters",};
    private SearchParameters loadedSearchParameters;

    public ProcessSimulatorTest(String testName) {
        super(testName);

    }

    @Override
    protected void setUp() throws Exception {
        cleanUp();
        super.setUp();

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}

    public void testAAProcess() {
        overrideSearchGUI();
        sleep(3000);
        InitializeController();
        sleep(3000);
        InitializeWorker();
        sleep(3000);
        simulateClientInput();
        //wait to finish this up !
        File results = new File("src/test/resources/results");
        //add a timer?
        while (results.listFiles().length < 1) {
            //wait to test this untill the result folder is made ! (multithreaded bottleneck)
        }
        File subdirectory = results.listFiles()[0];
        while (subdirectory.listFiles().length <= 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(ProcessSimulatorTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        assertEquals(true, results.listFiles().length > 9);
    }

    private void overrideSearchGUI() {
        RelimsProperties.initializeForTesting();
        PropertiesConfiguration config = RelimsProperties.getConfig();
        File searchGUIDefaultFasta = new File(RelimsProperties.getDefaultSearchDatabase());
        config.setProperty("searchgui.fasta.default", searchGUIDefaultFasta.getAbsolutePath());
        config.setProperty("relims.db.DB_OR.file", searchGUIDefaultFasta.getAbsolutePath());
        System.out.println("Overriden searchgui's default database location");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProcessSimulatorTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void cleanUp() {
        File databaseLocation = new File("src/test/resources/databases");
        File results = new File("src/test/resources/results");
        File repository = new File("src/test/resources/repository");
        File fastawin = new File("src/test/resources/sourcefiles/Fasta/windows");
        File fastamac = new File("src/test/resources/sourcefiles/Fasta/mac");
        File[] filesFolder = databaseLocation.listFiles();
        for (File aFolder : filesFolder) {
            FileUtils.deleteQuietly(aFolder);
            System.out.println("Removed " + aFolder.getName());
        }
        filesFolder = results.listFiles();
        for (File aFolder : filesFolder) {
            try {
                FileUtils.deleteDirectory(aFolder);
                System.out.println("Removed " + aFolder.getName());
            } catch (IOException ex) {
                System.err.println("COULD NOT REMOVE" + aFolder.getName());
            }
        }
        filesFolder = repository.listFiles();
        for (File aFolder : filesFolder) {
            try {
                FileUtils.deleteDirectory(aFolder);
                System.out.println("Removed " + aFolder.getName());
            } catch (IOException ex) {
                System.err.println("COULD NOT REMOVE" + aFolder.getName());
            }
        }

        filesFolder = fastawin.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getAbsolutePath().toLowerCase().endsWith(".fasta");
            }
        });
        for (File aFolder : filesFolder) {
            FileUtils.deleteQuietly(aFolder);
            System.out.println("Removed " + aFolder.getName());
        }

        filesFolder = fastamac.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getAbsolutePath().toLowerCase().endsWith(".fasta");
            }
        });
        for (File aFolder : filesFolder) {
            FileUtils.deleteQuietly(aFolder);
            System.out.println("Removed " + aFolder.getName());
        }

    }

    public void InitializeController() {
        RelimsControllerMode.main(Controllerargs);
    }

    public void InitializeWorker() {
        RelimsWorkerMode.main(Workerargs);
    }

    public void simulateClientInput() {

        Map<String, String> currentUserMap = new HashMap<String, String>();
        currentUserMap.put("username", "admin");
        currentUserMap.put("password", "admin");
        //setting up TaskObject
        TaskContainer tasksForServer = new TaskContainer();
        tasksForServer.setInstructionMap(currentUserMap);
        tasksForServer.updateInstruction("instruction", "doTasks");
        tasksForServer.setStrategyID("rdbVarMOD1");
        tasksForServer.setSourceID("pride");
        //read the parameters used
        try {
            File currentParameters = new File(RelimsProperties.getConfigFolder().getAbsolutePath() + "/default_parameters.parameters");
            loadedSearchParameters = SearchParameters.getIdentificationParameters(currentParameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        tasksForServer.setSearchParameters(loadedSearchParameters);
        tasksForServer.updateInstruction("runpipeline", "allow");
        tasksForServer.addJob("3", "TestingProject");
        ServerConnector connector = new ServerConnector();
        connector.setConnectionParameters(RelimsProperties.getControllerIP(), RelimsProperties.getControllerPort());
        try {
            Map<Long, Long> generatedTaskIDs = connector.SendToServer(tasksForServer);
            connector.resetConnectionParameters();
        } catch (IOException ex) {
            ex.printStackTrace();
            connector.resetConnectionParameters();
        }
    }

    public void testAllOutput() {
        //TEST THIS WITH A WHILE FILES ARE NOT THERE ?
        //Work with timeout counter?

        File results = new File("src/test/resources/results");


        File[] filesFolder = results.listFiles();
        filesFolder = results.listFiles();
        boolean accountedFor = false;
        for (File aFolder : filesFolder) {
            if (aFolder.isDirectory()) {
                for (String aFilename : resultFolderFilenames) {
                    if (new File(aFolder.getAbsolutePath() + "/" + aFilename).exists()) {
                        accountedFor = true;
                    } else {
                        System.out.println(aFilename + " was not found !");
                        accountedFor = false;
                    }
                }
            }
        }
        assertEquals(true, accountedFor);
    }

    public void testCleanupOnExit() {
        //move this in a test suite later on...(not sure how to do this atm)
        cleanUp();
        sleep(3000);
    }
}
