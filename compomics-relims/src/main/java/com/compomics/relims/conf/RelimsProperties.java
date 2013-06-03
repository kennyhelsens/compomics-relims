package com.compomics.relims.conf;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.relims.manager.processmanager.gearbox.enums.PriorityLevel;
import com.compomics.relims.manager.processmanager.processguard.RelimsException;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.relims.model.guava.functions.SpeciesFinderFunction;
import com.compomics.relims.modes.gui.util.Properties;
import com.compomics.util.experiment.biology.PTMFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static com.google.common.base.Preconditions.checkNotNull;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.management.ManagementFactory;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * This class contains the Relims properties.
 *
 * @author Kenny Helsens and Kenneth Verheggen
 */
public class RelimsProperties {

    private static File tempFolder;
    private static File localworkSpace;

    public static String getDbPrefix() {
        if (getTaskDatabaseDriver().toLowerCase().contains("derby")) {
            return getTaskDatabaseName() + ".";
        } else {
            return "";
        }
    }

    public static int getAllowedRAM() {
        return (int) (0.9 * (((com.sun.management.OperatingSystemMXBean) ManagementFactory
                .getOperatingSystemMXBean()).getTotalPhysicalMemorySize()) / 1024 / 1024);
    }

    public static void setFasta(String fasta) {
        config.setProperty("searchgui.fasta.default", fasta);
    }

    public static List<String> getPossibleFasta() {
        File fastaRepository = getFastaRepository();
        List<String> possibleFastaNames = new ArrayList<String>();
        for (File aFile : fastaRepository.listFiles()) {
            if (aFile.getAbsolutePath().endsWith(".fasta")) {
                possibleFastaNames.add(aFile.getName());
            }
        }
        return possibleFastaNames;
    }

    private static File getFastaRepository() {
        return new File(config.getString("relims.db.repository"));
    }

    public static File getFastaFromRepository(String fastaname) {
        File fastaRepository = getFastaRepository();
        for (File aFile : fastaRepository.listFiles()) {
            System.out.println(aFile.getName());
            System.out.println(fastaname);
            if (aFile.getName().toLowerCase().equals(fastaname.toLowerCase())) {
                return aFile;
            }
        }
        return null;
    }
    /**
     * the results will all be placed in a user-specific folder. Therefor, all
     * "normal" relims projects that are not run via the automatic setup, will
     * be placed in a folder called "default"
     */
    public String userID = "default";
    /**
     * A ProgressManager to store the state of the project and monitor it
     */
    private static ProgressManager progressManager = ProgressManager.getInstance();
    private static Logger logger = Logger.getLogger(RelimsProperties.class);
    /**
     * config stores the configurations from the file
     */
    private static PropertiesConfiguration config;
    //TODO : not sure if these searchguiproperties should stay...they are called in a different manner now
    private static PropertiesConfiguration searchGUIPropertiesConfiguration;
    /**
     * The workspace is project dependent and will be generated at the start of
     * a project
     */
    private static File workSpace = null;
    /**
     * Resolver/container for the PTM's ---> TODO : NEED TO BE STORED IN THE
     * SEARCHPARAMETER, NEXT TO PRIDE-ASA-SUGGESTIONS?
     */
    private static PTMFactory ptmFactory;
    /**
     * the folderSeparator is system-dependent...
     */
    public static final String folderSeparator = System.getProperty("file.separator");
    /**
     * These are the logging files
     */
    private static File searchguiLogFile;
    private static File peptideshakerLogFile;
    private static File logFolder;
    private static File relimsLoggingFile;
    /**
     * Standard networking mode is local
     */
    private static NetworkMode networkingMode = NetworkMode.LOCAL;

    //PROJECT SPECIFIC METHODS
    //RELIMS METHODS
    public static void initialize(boolean test) {

        if (config == null) {
            try {
                File lResource;
                int lOperatingSystem = Utilities.getOperatingSystem();

                String rootPath = new Properties().getRootFolder() + folderSeparator;
                if (rootPath.startsWith(".")) {
                    rootPath = "";
                }
                String path;
                if (!test) {
                    path = rootPath + "resources" + folderSeparator + "conf" + folderSeparator;
                } else {
                    path = "src" + folderSeparator + "test" + folderSeparator + "resources" + folderSeparator + "conf" + folderSeparator;
                }
                if (lOperatingSystem == Utilities.OS_MAC) {
                    path += "relims-mac.properties";
                } else if (lOperatingSystem == Utilities.OS_WIN_OTHER) {
                    path += "relims-windows.properties";
                } else {
                    path += "relims.properties";
                }

                lResource = new File(path);
                if (lResource.exists()) {
                    logger.debug("Found relimsproperties");
                    config = new PropertiesConfiguration(lResource);
                    configFolder = new File(lResource.getParent());
                } else {
                    throw new RelimsException(String.format("Could not find properties file %s", path));
                }
                // Override Pride-Asap properties
                PropertiesConfigurationHolder lAsapProperties = PropertiesConfigurationHolder.getInstance();
                lAsapProperties.setProperty("spectrum.limit", config.getBoolean("relims.asap.spectrum.limit"));
                lAsapProperties.setProperty("spectrum.limit.size", config.getInt("relims.asap.spectrum.limit.size"));
                lAsapProperties.setProperty("spectrum_peaks_cache.maximum_cache_size", config.getInt("spectrum_peaks_cache.maximum_cache_size"));
                lAsapProperties.setProperty("spectrumannotator.annotate_modified_identifications_only", true);
                lAsapProperties.setProperty("results_path_tmp_max", config.getInt("relims.results_path_tmp_max"));
                lAsapProperties.setProperty("results_path", config.getString("relims.asap.results") + "/" + System.currentTimeMillis());
                lAsapProperties.setProperty("results_path_tmp", lAsapProperties.getProperty("results_path") + "/mgf/tmp");
                //make the needed temp folders PER project ---> otherwise the temp folder gets cleared for ALL running projects...
                File tempFolder = new File(lAsapProperties.getProperty("results_path_tmp").toString());
                tempFolder.mkdirs();
                //reroute the log4J if the program runs in workermode
                try {
                    File logForJDestination = new File(rootPath + "/resources/conf/log4j.properties");
                    PropertiesConfiguration log4JConfig = new PropertiesConfiguration(logForJDestination.getAbsolutePath());
                    System.out.println("");
                } catch (ConfigurationException ex) {
                    logger.error(ex);
                    logger.error("Could not load relims Log4J properties");
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e.getMessage(), e);
                //TODO set default values?
            }
        }
    }

    public static File getWorkSpacePath() {
        try {
            //make the directory if ot does not exist yet 
            File tempFile = new File(config.getString("workspace.file") + "/" + config.getString("workspace.userID"));
            tempFile.mkdir();
            return tempFile;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getUserID() {
        return config.getString("workspace.userID");
    }

    public static void setUserID(String userID) {
        if (userID.isEmpty()) {
            config.setProperty("workspace.userID", "default");
        }
        config.setProperty("workspace.userID", userID);
    }

    public static File createWorkSpace(long projectID, String projectSource) {
        System.out.println("Creating workspace for project " + projectID);
        StringBuilder fileName = new StringBuilder();
        Calendar date = Calendar.getInstance();
        SimpleDateFormat dateformatter = new SimpleDateFormat("ddMMyyyy_hhmmss");
        fileName.append("").append(projectID).append("_");
        fileName.append(projectSource).append("_");
        fileName.append(dateformatter.format(date.getTime()));
        workSpace = new File(getWorkSpacePath(), fileName.toString());
        workSpace.mkdir();
        logFolder = new File(workSpace.getAbsolutePath() + "/processinfo/");
        logFolder.mkdir();
        config.setProperty("relims.log.folder", logFolder.getAbsolutePath());
        config.setProperty("relims.resultFolder", workSpace.getAbsolutePath());
        return workSpace;
    }

    public static File createWorkSpace() {
        workSpace = getWorkSpacePath();
        return workSpace;
    }

    public static File getWorkSpace() {
        return workSpace;
    }

    public static File getLogFolder() {
        return logFolder;
    }

    public static boolean getDebugMode() {
        return config.getBoolean("relims.debugmode");
    }

    //SUBPROCESS METHODS
    //-----------------------searchgui
    public static void setSearchGUIFolder(String searchGUIDir) {
        config.setProperty("searchgui.directory", searchGUIDir);
        searchguiLogFile = new File(getSearchGuiFolder() + "/resources/SearchGUI.log");
    }

    public static File getUserModsFile() {
        return new File(getSearchGuiUserModFile().getAbsolutePath());
    }

    public static String getSearchGuiFolder() {
        return config.getString("searchgui.directory");
    }

    public static String getSearchGuiArchive() {
        return config.getString("searchgui.jar");
    }

    public static String getSearchGuiConfFolder() {
        return config.getString("searchgui.directory") + folderSeparator + "resources" + folderSeparator + "conf";
    }

    public static String getSearchGuiArchivePath() {
        return getSearchGuiFolder() + folderSeparator + getSearchGuiArchive();
    }

    public static File getSearchGuiUserModFile() {
        return new File(getSearchGuiConfFolder(), config.getString("searchgui.usermods"));
    }

    private static PTMFactory loadOMSSAPTMFactory() {

        File lModFile = new File(getSearchGuiConfFolder(), config.getString("searchgui.mods"));
        // File lUserModFile = new File(getSearchGuiConfFolder(), config.getString("searchgui.usermods.default"));

        try {
            if (ptmFactory == null) {
                ptmFactory = PTMFactory.getInstance();
            }

            ptmFactory.clearFactory();
            ptmFactory = PTMFactory.getInstance();

            ptmFactory.importModifications(lModFile, false);
            //       ptmFactory.importModifications(lUserModFile, true);

            logger.debug("loaded PTMFactory (size: " + ptmFactory.getPTMs().size() + " mods)");

        } catch (IOException e) {
            logger.error("error initializing OMSSA mods", e);
            logger.error(e.getMessage(), e);
            progressManager.setState(Checkpoint.FAILED, e);;
            Thread.currentThread().interrupt();
        } catch (XmlPullParserException e) {
            logger.error("error initializing OMSSA mods", e);
            logger.error(e.getMessage(), e);
            progressManager.setState(Checkpoint.FAILED, e);;
            Thread.currentThread().interrupt();
        }

        return PTMFactory.getInstance();
    }

    public static PTMFactory getPTMFactory(boolean aReload) {
        if (ptmFactory == null || aReload) {
            loadOMSSAPTMFactory();
        }
        return ptmFactory;
    }

    public static ArrayList<UserMod> getRelimsMods() {
        String[] lRelimsModIds = config.getStringArray("relims.mod.ids");
        checkNotNull(lRelimsModIds);

        ArrayList<UserMod> lRelimsMods = new ArrayList<UserMod>();

        for (String lRelimsModId : lRelimsModIds) {
            UserMod lRelimsMod = new UserMod();
            String lBase = "relims.mod." + lRelimsModId + ".";

            int lLocationTypeId = config.getInt(lBase + "locationtype");
            double lMass = config.getDouble(lBase + "mass");
            String lLocation = config.getString(lBase + "location");
            boolean isFixed = config.getBoolean(lBase + "fixed");

            lRelimsMod.setLocationTypeByOMSSAID(lLocationTypeId);
            lRelimsMod.setMass(lMass);
            lRelimsMod.setLocation(lLocation);
            lRelimsMod.setFixed(isFixed);
            lRelimsMod.setModificationName(lRelimsModId);

            lRelimsMods.add(lRelimsMod);
        }

        return lRelimsMods;

    }

    public static boolean useTandem() {
        return config.getBoolean("searchgui.engine.tandem");
    }

    public static boolean useOmssa() {
        return config.getBoolean("searchgui.engine.omssa");
    }

    public static String getDefaultSearchDatabase() {
        return config.getString("searchgui.fasta.default");
    }

    public static Integer getMissedCleavages() {
        return config.getInt("searchgui.missed.cleavages");
    }
    //-----------------------peptideshaker

    public static String getPeptideShakerFolder() {
        return config.getString("peptideshaker.directory");
    }

    public static void setPeptideShakerFolder(String peptideshakerDir) {
        config.setProperty("peptideshaker.directory", peptideshakerDir);
        peptideshakerLogFile = new File(getPeptideShakerFolder() + "/resources/PeptideShaker.log");
    }

    public static String getPeptideShakerArchive() {
        return config.getString("peptideshaker.jar");
    }

    public static void setPeptideShakerArchive(String peptideshakerJar) {
        config.setProperty("peptideshaker.jar", peptideshakerJar);
    }

    public static String getPeptideShakerArchivePath() {
        return getPeptideShakerFolder() + folderSeparator + getPeptideShakerArchive();
    }

    public static double getFDR() {
        return config.getDouble("peptideshaker.export.fdr");
    }

    public static boolean getPeptideShakerCPSOutput() {
        return config.getBoolean("peptideshaker.export.cps");
    }

    public static boolean getPeptideShakerTSVOutput() {
        return config.getBoolean("peptideshaker.export.tsv");
    }

    public static boolean getPeptideShakerUniprotOutput() {
        return config.getBoolean("peptideshaker.export.uniprot");
    }
    //-----------------------pride-asa

    public static Boolean appendPrideAsapAutomatic() {
        return config.getBoolean("relims.asap.automatic.append");
    }

    public static String getPrideMGFSource() {
        return config.getString("pride.MGF.source");
    }

    public static String getLocalPrideXMLRepository() {
        return config.getString("pride.prideXML.repository");
    }

    public static void setAppendPrideAsapAutomatic(boolean b) {
        config.setProperty("relims.asap.automatic.append", b);
    }

    public static boolean getPrideDataSource() {
        return config.getBoolean("relims.asap.datasource.xml");
    }

    public static void setPrideDataSource(boolean useXML) {
        config.setProperty("relims.asap.datasource.xml", useXML);
    }

    public static String getRepositoryPath() {
        return config.getString("remote.relims.repository");
    }

    public static String getParameterStorageMode() {
        return config.getString("remote.relims.repository.mode");
    }

    //-----------------------general
    public static PriorityLevel getPriority() {

        String level = config.getString("process.priority");
        if (level == null) {
            return PriorityLevel.BELOW_NORMAL;
        } else {
            return PriorityLevel.valueOf(level);
        }
    }

    //DATASOURCE METHODS
    public static String getDbUserName() {
        return config.getString("db.user");
    }

    public static String getDbPass() {
        return config.getString("db.pass");
    }

    public static String getDbDatabaseName() {
        return config.getString("db.name");
    }

    public static String getDbAdress() {
        return config.getString("db.ip");
    }
    //NETWORKING METHODS

    public static int getControllerPort() {
        try {
            return Integer.parseInt(config.getString("relims.networking.controller.port"));
        } catch (NumberFormatException ex) {
            return 6789;
        }
    }

    public static int getWorkerPort() {
        try {
            return Integer.parseInt(config.getString("relims.networking.worker.port"));
        } catch (NumberFormatException ex) {
            return 11554;
        }
    }

    public static String getControllerIP() {
        return config.getString("relims.networking.controller.IP");
    }

    public static void setNetworkingMode(NetworkMode mode) {
        networkingMode = mode;
    }

    public static NetworkMode getNetworkingMode() {
        return networkingMode;
    }

    public static int getBackupInterval() {
        return config.getInt("relims.networking.db.backupInterval");
    }

    public static int getMaxBackups() {
        return config.getInt("relims.networking.db.maxBackups");
    }

    public static String getTaskDatabaseFramework() {
        return config.getString("relims.networking.db.framework");
    }

    public static String getPassword() {
        return config.getString("workspace.password");
    }

    public static String getJavaExec() {
        return config.getString("java.home");
    }

    public static File getPrideMetaDataFile() {
        return new File(config.getString("pride.metadata.file"));
    }

    public static File getRelimsTempFolder() {
        if (tempFolder == null) {
            try {
                File locator = File.createTempFile("relimsTemp", Long.toString(System.nanoTime()));
                locator.delete();
                locator.mkdirs();
                tempFolder = new File(locator.getParent() + "/relimsTemp");
                tempFolder.deleteOnExit();
            } catch (IOException ex) {
                ex.printStackTrace();
                logger.error("Could not create tempfolder!");
            }
        }
        return tempFolder;
    }

    public static int getMaxJobHours() {
        return config.getInt("max.job.time.hours");
    }

    public static int getMaxJobMinutes() {
        return config.getInt("max.job.time.minutes");
    }

    public static int getPollingTime() {
        return config.getInt("max.job.time.polling.seconds");
    }

    public static PropertiesConfiguration getConfig() {
        return config;
    }

    //GENERAL CONFIGURATION
    public static File getConfigFolder() {
        if (configFolder == null) {
            RelimsProperties.configFolder = new File(".");
        }
        return RelimsProperties.configFolder;
    }

    public static void setConfigFolder(String location) {
        RelimsProperties.configFolder = new File(location);
    }

    //PREDICATES METHODS
    public static int getMinimumNumberOfSpectra() {
        return config.getInt("predicate.project.spectrum.min");
    }

    public static int getMinimumNumberOfPeptides() {
        return config.getInt("predicate.project.peptide.min");
    }

    public static int getAllowedSpeciesTestSize() {
        return config.getInt("predicate.project.species.size");
    }

    public static SpeciesFinderFunction.SPECIES getAllowedSpecies() {

        String lSpecies = config.getString("predicate.project.species.type");

        if (lSpecies.equals("drosphila")) {
            return SpeciesFinderFunction.SPECIES.DROSOPHILA;

        } else if (lSpecies.equals("human")) {
            return SpeciesFinderFunction.SPECIES.HUMAN;

        } else if (lSpecies.equals("yeast")) {
            return SpeciesFinderFunction.SPECIES.YEAST;

        } else if (lSpecies.equals("mouse")) {
            return SpeciesFinderFunction.SPECIES.MOUSE;

        } else if (lSpecies.equals("rat")) {
            return SpeciesFinderFunction.SPECIES.RAT;

        } else if (lSpecies.equals("mixture")) {
            return SpeciesFinderFunction.SPECIES.MIX;

        } else {
            return SpeciesFinderFunction.SPECIES.NA;
        }
    }

    public static String[] getAllowedInstruments() {
        return config.getStringArray("predicate.project.instrument");
    }

    //TaskDatabase and WORKERPOOL Methods
    public static String getTaskDatabaseName() {
        return config.getString("relims.networking.db.name");
    }

    public static String getTaskDatabasePassword() {
        return config.getString("relims.networking.db.password");
    }

    public static File getTaskDatabaseLocation() {
        String location = config.getString("relims.networking.db.location");
        return new File(location);
    }

    public static String getTaskDatabaseProtocol() {
        return config.getString("relims.networking.db.protocol");
    }

    public static String getTaskDatabaseDriver() {
        return config.getString("relims.networking.db.driver");
    }

    public static int getTaskDatabaseMaxClients() {
        try {
            return Integer.parseInt(config.getString("relims.networking.db.maxWaitingClients"));
        } catch (NumberFormatException ex) {
            return 100;
        }
    }

    //LOGGING
    public static void saveLoggedFiles() {
        try {
            if (networkingMode.equals(NetworkMode.WORKER)) {
                config.setProperty("used.workspace", getWorkSpace().getAbsolutePath());
                config.save(new File(getLogFolder().getAbsolutePath() + "/relimsproperties.properties"));
                bundleLogs();
                resetProcessLoggers();
            }
        } catch (ConfigurationException ex) {
            logger.error("Could not save relimsproperties \n" + ex);
        }
    }

    public static void resetProcessLoggers() {
        searchguiLogFile = new File(getSearchGuiFolder() + "/resources/SearchGUI.log");
        peptideshakerLogFile = new File(getPeptideShakerFolder() + "/resources/PeptideShaker.log");
        clearLog(searchguiLogFile);
        clearLog(peptideshakerLogFile);
        clearLog(relimsLoggingFile);
    }

    private static void bundleLogs() {
        try {
            FileUtils.copyFile(searchguiLogFile, new File(getLogFolder().getAbsolutePath() + "/SearchGUI.log"), true);
        } catch (Exception ex) {
            logger.error("Could not save SearchGUI log file");
        }
        try {
            FileUtils.copyFile(peptideshakerLogFile, new File(getLogFolder().getAbsolutePath() + "/PeptideShaker.log"), true);
        } catch (Exception ex) {
            logger.error("Could not save PeptideShaker log file");
        }
        try {
            FileUtils.copyFile(relimsLoggingFile, new File(getLogFolder().getAbsolutePath() + "/Relims.log"), true);
        } catch (Exception ex) {
            logger.error("Could not save PeptideShaker log file");
        }

    }

    private static void clearLog(File loggingFile) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(loggingFile.getAbsolutePath()))) {
            bw.write("");
            bw.flush();
        } catch (IOException ioe) {
            logger.error("Could not clear " + loggingFile.getAbsolutePath());
            ioe.printStackTrace();
        }
    }
    //MARKED FOR DEPRECATION

    public static String[] getRelimsClassList() {
        return config.getStringArray("relims.strategy.ids");
    }

    public static String[] getRelimsSourceList() {
        return config.getStringArray("relims.source.ids");
    }
    private static File configFolder;

    public static void logSettings() {
        createWorkSpace();
        try {
            logger.debug("using omssa:" + String.valueOf(useOmssa()));
            logger.debug("using tandem:" + String.valueOf(useTandem()));
            logger.debug("workspace:" + getWorkSpace().getCanonicalPath());
            logger.debug("searchgui:" + getSearchGuiFolder());
            logger.debug("relims mods:" + Joiner.on(",").join(Lists.transform(getRelimsMods(), new Function<UserMod, Object>() {
                @Override
                public Object apply(@Nullable UserMod input) {
                    return input.getModificationName();
                }
            })));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            progressManager.setState(Checkpoint.FAILED, e);;
            Thread.currentThread().interrupt();
        }
    }

    //PREDICATES
    public static int getMaxMS1Count() {
        return config.getInt("project.predicates.maxMs1Count");
    }

    //HELPER ENUMS
    public enum NetworkMode {

        LOCAL, CONTROLLER, WORKER, CLIENT;
    }

    public static String getColimsDbServer() {
        return config.getString("relims.colims.db.server");

    }

    public static String getColimsDbSchema() {
        return config.getString("relims.colims.db.schema");

    }

    public static int getColimsDbPort() {
        return Integer.parseInt(config.getString("relims.colims.db.port"));

    }

    public static String getColimsDbUser() {
        return config.getString("relims.colims.db.user");

    }

    public static String getColimsDbPassword() {
        return config.getString("relims.colims.db.password");

    }
}
