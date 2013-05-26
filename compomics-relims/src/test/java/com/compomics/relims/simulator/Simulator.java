/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.simulator;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.gui.util.Properties;
import com.compomics.relims.modes.networking.client.connection.ServerConnector;
import com.compomics.relims.modes.networking.controller.RelimsControllerMode;
import com.compomics.relims.modes.networking.controller.taskobjects.TaskContainer;
import com.compomics.relims.modes.networking.worker.RelimsWorkerMode;
import com.compomics.util.experiment.identification.SearchParameters;
import junit.framework.TestCase;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Kenneth
 */
public class Simulator extends TestCase {

    private static String[] Controllerargs = new String[]{""};
    private static String[] Workerargs = new String[]{"-workerport", "15557"};
    private static long TIME_OUT = Long.MAX_VALUE;
    private static final Logger logger = Logger.getLogger(Simulator.class);
    private static int projectId;
    private String[] resultFolderFilenames = new String[]{"3.cps",
        "3.mgf",
        "3.omx",
        "3.t.xml",
        "PeptideShaker_3_AutoReprocessed_1_peptides.txt",
        "PeptideShaker_3_AutoReprocessed_1_proteins.txt",
        "PeptideShaker_3_AutoReprocessed_1_psms.txt",
        "SearchGUI.parameters",};
    private static SearchParameters loadedSearchParameters;

    public Simulator(String testName) {
        super(testName);
    }

    public static void setProjectID(int projectId) {
        Simulator.projectId = projectId;
    }

    public static void testSimulateProcess() {

        cleanUp();
        overrideSearchGUI();
        sleep(3000);
        initializeController();
        sleep(3000);
        initializeWorker();
        sleep(3000);
        simulateClientInput(projectId);
        //wait to finish this up !
        File results = new File("src/test/resources/results/" + RelimsProperties.getUserID() + "/");
        long timeout = 0;
        while (!results.exists()) {
            sleep(1000);
            //wait untill result folder is actually there...
        }
        timeout = 0;
        while (results.listFiles().length == 0) {
            sleep(1000);
            //wait for the timestamped map to be there
        }
        timeout = 0;
        File relimsPropertiesFile = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/relimsproperties.properties");
        while (!relimsPropertiesFile.exists() && timeout < TIME_OUT) {
            sleep(1000);
            timeout++;
            //wait untill all the files are processed and put in the resultfolder or timeout happens...
            if (timeout >= TIME_OUT) {
                assertFalse("Search timed out...No results were created in time...", false);
            }
        }
        assertTrue("Simulation completed...", true);
    }

    private static void overrideSearchGUI() {
        RelimsProperties.initialize(true);
        PropertiesConfiguration config = RelimsProperties.getConfig();
        File searchGUIDefaultFasta = new File(RelimsProperties.getDefaultSearchDatabase());
        config.setProperty("searchgui.fasta.default", searchGUIDefaultFasta.getAbsolutePath());
        config.setProperty("relims.db.DB_OR.file", searchGUIDefaultFasta.getAbsolutePath());
        logger.info("Overriden searchgui's default database location");
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            logger.error(ex);
        }
    }

    //HELPERMETHODS
    public static void cleanUp() {
        String lRootFolder = new Properties().getRootFolder();

        File databaseLocation = new File("src/test/resources/databases");
        File results = new File(lRootFolder, "src/test/resources/results");
        File repository = new File(lRootFolder, "src/test/resources/repository");
        File fastawin = new File(lRootFolder, "src/test/resources/sourcefiles/Fasta/windows");
        File fastamac = new File(lRootFolder, "src/test/resources/sourcefiles/Fasta/mac");

        //CLEANUP DATABASE
        if (databaseLocation.exists()) {
            File[] filesFolder = databaseLocation.listFiles();
            for (File aFolder : filesFolder) {
                FileUtils.deleteQuietly(aFolder);
                System.out.println("Removed " + aFolder.getName());
            }
        }

        //CLEAN UP RESULTS
        if (results.exists()) {
            File[] filesFolder = results.listFiles();
            for (File aFolder : filesFolder) {
                try {
                    FileUtils.deleteDirectory(aFolder);
                    System.out.println("Removed " + aFolder.getName());
                } catch (IOException ex) {
                    System.err.println("COULD NOT REMOVE" + aFolder.getName());
                }
            }
        }

        if (repository.exists()) {
            //CLEAN UP REPOSITORY
            File[] filesFolder = repository.listFiles();
            for (File aFolder : filesFolder) {
                try {
                    FileUtils.deleteDirectory(aFolder);
                    System.out.println("Removed " + aFolder.getName());
                } catch (IOException ex) {
                    System.err.println("COULD NOT REMOVE" + aFolder.getName());
                }
            }
        }
        File[] filesFolder = new File[0];

        if (fastawin.exists()) {
            //CLEAN UP FASTAFILES
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

        }

        //CLEAN UP REPOSITORY
        filesFolder = repository.listFiles();
        if (filesFolder != null) {
            for (File aFolder : filesFolder) {
                FileUtils.deleteQuietly(aFolder);
                logger.info("Removed " + aFolder.getName());
            }
        }

        //CLEAN UP FASTAFILES
        filesFolder = fastawin.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getAbsolutePath().toLowerCase().endsWith(".fasta");
            }
        });

        if (filesFolder != null) {
            for (File aFolder : filesFolder) {
                FileUtils.deleteQuietly(aFolder);
                logger.info("Removed " + aFolder.getName());
            }
        }

        filesFolder = fastamac.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return !pathname.getAbsolutePath().toLowerCase().endsWith(".fasta");
            }
        });

        if (filesFolder != null) {
            for (File aFolder : filesFolder) {
                FileUtils.deleteQuietly(aFolder);
                logger.info("Removed " + aFolder.getName());
            }
        }
    }

    public static File getResultFolder() {
        File results = new File("src/test/resources/results/" + RelimsProperties.getUserID());
        return results.listFiles()[0];
    }

    public static void initializeController() {
        RelimsProperties.setNetworkingMode(RelimsProperties.NetworkMode.CONTROLLER);
        RelimsControllerMode.main(Controllerargs);
    }

    public static void initializeWorker() {
        RelimsProperties.setNetworkingMode(RelimsProperties.NetworkMode.WORKER);
        RelimsWorkerMode.main(Workerargs);
    }

    public static void endSimulation() {
        RelimsWorkerMode.stopWorker();
        RelimsControllerMode.stopController();
        logger.info("END OF SIMULATION");
    }

    public static void simulateClientInput(int projectId) {

       //setting up TaskObject
        TaskContainer tasksForServer = new TaskContainer();
        tasksForServer.setStrategyID("rdbVarMOD1");
        tasksForServer.setSourceID("pride");
        tasksForServer.setName("TestingContainer");
        tasksForServer.enablePipeline();
        tasksForServer.addJob("" + projectId, "TestingProject");
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

    public void testFinalCleanUp() {
        Simulator.endSimulation();
        cleanUp();
        assertTrue(true);
        //TODO ---> CHECK IF THE FILES ARE ACTUALLY DELETED IN THE FUTURE !
    }
}
