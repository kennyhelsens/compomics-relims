package com.compomics.relims.conf;

import com.compomics.relims.manager.variablemanager.RelimsVariableManager;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.pride_asa_pipeline.config.PropertiesConfigurationHolder;
import com.compomics.relims.concurrent.Command;
import com.compomics.relims.modes.gui.util.Properties;
import com.compomics.relims.model.guava.functions.SpeciesFinderFunction;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.relims.manager.processmanager.gearbox.enums.PriorityLevel;
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
import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * This class contains the Relims properties.
 *
 * @author Kenny Helsens
 */
public class RelimsProperties {

    /**
     * Plain logger
     */
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

    public static PriorityLevel getPriority() {

        String level = config.getString("process.priority");
        if (level == null) {
            return PriorityLevel.BELOW_NORMAL;
        } else {
            return PriorityLevel.valueOf(level);
        }
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
    // -------------------------- STATIC BLOCKS --------------------------

    static {
        try {
            File lResource;
            int lOperatingSystem = Utilities.getOperatingSystem();
            String jarFilePath = new Properties().getJarFilePath() + folderSeparator;
            if (jarFilePath.startsWith(".")) {
                jarFilePath = "";
            }

            File locatorFile = new File(".");
            String path = locatorFile.getAbsolutePath().toString();
            path = path.substring(0, path.length() - 1);

            if (lOperatingSystem == Utilities.OS_MAC) {
                path = path + "configuration" + folderSeparator + "relims-mac.properties";
            } else if (lOperatingSystem == Utilities.OS_WIN_OTHER) {
                path = path + "configuration" + folderSeparator + "relims-windows.properties";
            } else {
                path = path + "configuration" + folderSeparator + "relims.properties";
            }
            lResource = new File(path);
            if (lResource.exists()) {
                logger.debug("Found relimsproperties");
            }
            config = new PropertiesConfiguration(lResource);

            // Set the workspace for all future Commands to the SearchGUI  --> this is overkill...

            Command.setWorkFolder(new File(getSearchGuiFolder() + folderSeparator));

            // Override Pride-Asap properties
            PropertiesConfigurationHolder lAsapProperties = PropertiesConfigurationHolder.getInstance();
            lAsapProperties.setProperty("spectrum.limit", config.getBoolean("relims.asap.spectrum.limit"));
            lAsapProperties.setProperty("spectrum.limit.size", config.getInt("relims.asap.spectrum.limit.size"));
            lAsapProperties.setProperty("spectrum_peaks_cache.maximum_cache_size", config.getInt("spectrum_peaks_cache.maximum_cache_size"));
            lAsapProperties.setProperty("spectrumannotator.annotate_modified_identifications_only", true);
            lAsapProperties.setProperty("results_path_tmp_max", config.getInt("relims.results_path_tmp_max"));
            lAsapProperties.setProperty("results_path", config.getString("relims.asap.results"));
            lAsapProperties.setProperty("results_path_tmp", config.getString("relims.asap.results.tmp"));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            progressManager.setState(Checkpoint.FAILED, e);;
            //TODO set default values?
        }
    }

    public static String getRepositoryPath() {
        return config.getString("remote.relims.repository");
    }

    public static String getJavaExec() {
        return config.getString("java.home");
    }

    public static String getPeptideShakerFolder() {
        return config.getString("peptideshaker.directory");
    }

    public static String getPeptideShakerArchive() {
        return config.getString("peptideshaker.jar");
    }

    public static String getPeptideShakerMemory() {
        return config.getString("peptideshaker.heap.memory");
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
        //            iWorkSpace = Files.createTempDir();
        //todo ake a nicer format for the analysis...Milliseconds = not telling much
        // iWorkSpace = new File(getWorkSpacePath(), String.valueOf(System.currentTimeMillis()));
        StringBuilder fileName = new StringBuilder();
        Calendar date = Calendar.getInstance();
        SimpleDateFormat dateformatter = new SimpleDateFormat("ddMMyyyy_hhmmss");
        fileName.append("").append(projectID).append("_");
        fileName.append(projectSource).append("_");
        fileName.append(dateformatter.format(date.getTime()));
        workSpace = new File(getWorkSpacePath(), fileName.toString());
        workSpace.mkdir();
        RelimsVariableManager.setSearchResultFolder(workSpace.getAbsolutePath().toString());
        return workSpace;
    }

    public static File createWorkSpace() {
        workSpace = getWorkSpacePath();
        return workSpace;
    }

    public static File getWorkSpace() {
        return workSpace;
    }

    public static File getTmpFile(String aID) throws IOException {
        if (workSpace == null) {
            workSpace = getWorkSpace();
        }
        File lFile = new File(workSpace, aID + ".tmp");
        lFile.createNewFile();
        return lFile;
    }

    public static File getTmpFile(String aID, boolean aTimeStamp) throws IOException {
        if (workSpace == null) {
            workSpace = getWorkSpace();
        }
        File lFile = new File(workSpace, aID + "_" + System.currentTimeMillis() + ".tmp");
        lFile.createNewFile();
        return lFile;
    }

    public static File getSearchGuiUserModFile() {
        return new File(getSearchGuiConfFolder(), config.getString("searchgui.usermods"));
    }

    private static PTMFactory loadOMSSAPTMFactory() {

        File lModFile = new File(getSearchGuiConfFolder(), config.getString("searchgui.mods"));
        File lUserModFile = new File(getSearchGuiConfFolder(), config.getString("searchgui.usermods.default"));

        try {
            if (ptmFactory == null) {
                ptmFactory = PTMFactory.getInstance();
            }

            ptmFactory.clearFactory();
            ptmFactory = PTMFactory.getInstance();

            ptmFactory.importModifications(lModFile, false);
            ptmFactory.importModifications(lUserModFile, true);

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

    public static PropertiesConfiguration getDefaultSearchGuiConfiguration() {
        if (searchGUIPropertiesConfiguration == null) {

            try {
                File lPropertiesFile = new File(getSearchGuiFolder(),
                        folderSeparator + "resources"
                        + folderSeparator + "conf"
                        + folderSeparator + "default_SearchGUI.properties");

                searchGUIPropertiesConfiguration = new PropertiesConfiguration(lPropertiesFile);
                return searchGUIPropertiesConfiguration;

            } catch (ConfigurationException e) {
                logger.error(e.getMessage(), e);
                progressManager.setState(Checkpoint.FAILED, e);;
                Thread.currentThread().interrupt();
            }
        }

        return searchGUIPropertiesConfiguration;
    }

    public static String getSearchGuiArchivePath() {
        return getSearchGuiFolder() + folderSeparator + getSearchGuiArchive();
    }

    public static String getPeptideShakerArchivePath() {
        return getPeptideShakerFolder() + folderSeparator + getPeptideShakerArchive();
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

    public static void logSettings() {
        createWorkSpace();
        try {
            logger.debug("using omssa:" + String.valueOf(useOmssa()));
            logger.debug("using tandem:" + String.valueOf(useTandem()));
            logger.debug("workspace:" + getWorkSpace().getCanonicalPath());
            logger.debug("searchgui:" + getSearchGuiFolder());
            logger.debug("relims mods:" + Joiner.on(",").join(Lists.transform(getRelimsMods(), new Function<UserMod, Object>() {
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

    public static int getMaxSucces() {
        return config.getInt("program.param.max.succes");
    }

    public static int getRandomProjectAttempts() {
        return config.getInt("program.param.attempt.count");
    }

    public static boolean hasSpectrumLimit() {
        return config.getBoolean("program.param.spectrum.limit.boolean");
    }

    public static int getSpectrumLimitCount() {
        return config.getInt("program.param.spectrum.limit.count");
    }

    public static String getDefaultSearchDatabase() {
        return config.getString("searchgui.fasta.default");
    }

    public static PropertiesConfiguration getConfig() {
        return config;
    }

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

    public static String getControllerIP() {
        return config.getString("remote.relims.controller.ip");
    }

    public static int getControllerPort() {
        return config.getInt("remote.relims.controller.port");
    }

    public static String getDatabaseFilename(String aDbVarID) {
        return config.getString("relims.db." + aDbVarID + ".file");
    }

    public static String getDatabaseTitle(String aDbVarID) {
        return config.getString("relims.db." + aDbVarID + ".name");
    }

    public static String[] getDatabaseVarIDs() {
        return config.getStringArray("relims.db.ids");
    }

    public static String[] getRelimsClassList() {
        return config.getStringArray("relims.strategy.ids");
    }

    public static String[] getRelimsSourceList() {
        return config.getStringArray("relims.source.ids");
    }

    public static Class getRelimsSearchStrategyClass(String aStrategyID) throws ClassNotFoundException {
        String lClassname = config.getString("relims.strategy.class." + aStrategyID);
        return Class.forName(lClassname);
    }

    public static Class getRelimsSourceClass(String aSourceID) throws ClassNotFoundException {
        String lClassname = config.getString("relims.source.class." + aSourceID);
        return Class.forName(lClassname);
    }

    public static List<Long> getPredifinedProjects() {
        String[] lProjectStrings = config.getStringArray("relims.projects.list");
        List<Long> lProjectIds = Lists.newArrayList();

        for (String lProjectString : lProjectStrings) {
            lProjectIds.add(Long.parseLong(lProjectString));
        }
        return lProjectIds;
    }

    public static Integer getMissedCleavages() {
        return config.getInt("searchgui.missed.cleavages");
    }

    public static Boolean appendPrideAsapAutomatic() {
        return config.getBoolean("relims.asap.automatic.append");
    }

    public static String[] getAllowedInstruments() {
        return config.getStringArray("predicate.project.instrument");
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

    public static boolean useProjectListFromRedis() {
        return config.getBoolean("relims.project.redis");
    }

    public static String getRedisServer() {
        return config.getString("relims.project.redis.server");
    }

    public static String getRedisProjectKey() {
        return config.getString("relims.project.redis.key");
    }

    public static String getPeptideShakerResultsFolder() {
        return config.getString("peptideshaker.export");
    }

    public static double getFDR() {
        return config.getDouble("peptideshaker.export.fdr");
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

    public static void setPrideDataSource(boolean b) {
        config.setProperty("relims.asap.datasource.xml", b);
    }
}
