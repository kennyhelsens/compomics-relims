package com.compomics.relims.conf;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.concurrent.Command;
import com.compomics.util.experiment.biology.PTMFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static com.google.common.base.Preconditions.*;

/**
 * This class is a
 */
public class RelimsProperties {

    private static Logger logger = Logger.getLogger(RelimsProperties.class);

    private static PropertiesConfiguration config;
    private static File iWorkSpace = null;
    private static PTMFactory ptmFactory;
    public static PropertiesConfiguration iSearchGUIPropertiesConfiguration;
    public static String iFolderSeparator = System.getProperty("file.separator");

    // -------------------------- STATIC BLOCKS --------------------------

    static {
        try {
            URL lResource = Resources.getResource("config" + iFolderSeparator + "relims.properties");
            config = new PropertiesConfiguration(lResource);

            // Set the workspace for all future Commands to the SearchGUI folder
            Command.setWorkFolder(new File(getSearchGuiFolder() + iFolderSeparator));
        } catch (org.apache.commons.configuration.ConfigurationException e) {
            logger.error(e.getMessage(), e);
        }
    }


    public static String getJavaExec() {
        return config.getString("java.home");
    }

    public static String getSearchGuiFolder() {
        return config.getString("searchgui.directory");
    }

    public static String getSearchGuiConfFolder() {
        return config.getString("searchgui.directory") + iFolderSeparator + "conf";
    }

    public static String getSearchGuiArchive() {
        return config.getString("searchgui.jar");
    }


    public static File getWorkSpacePath() {
        return new File(config.getString("workspace.file"));
    }



    public static File getWorkSpace() {
        if (iWorkSpace == null) {
//            iWorkSpace = Files.createTempDir();
            iWorkSpace = new File(getWorkSpacePath(), String.valueOf(System.currentTimeMillis()));
            iWorkSpace.mkdir();
        }
        return iWorkSpace;
    }


    public static File getTmpFile(String aID) throws IOException {
        if (iWorkSpace == null) {
            iWorkSpace = getWorkSpace();
        }
        File lFile = new File(iWorkSpace, aID + ".tmp");
        lFile.createNewFile();
        return lFile;
    }

    public static File getTmpFile(String aID, boolean aTimeStamp) throws IOException {
        if (iWorkSpace == null) {
            iWorkSpace = getWorkSpace();
        }
        File lFile = new File(iWorkSpace, aID + "_" + System.currentTimeMillis() + ".tmp");
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

            ptmFactory = PTMFactory.getInstance();
            ptmFactory.importModifications(lModFile);
            ptmFactory.importModifications(lUserModFile);

            logger.debug("loaded PTMFactory (size: " + ptmFactory.getPtmMap().size() + " mods)");

        } catch (IOException e) {
            logger.error("error initializing OMSSA mods", e);
            logger.error(e.getMessage(), e);
        } catch (XmlPullParserException e) {
            logger.error("error initializing OMSSA mods", e);
            logger.error(e.getMessage(), e);
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
        if (iSearchGUIPropertiesConfiguration == null) {

            try {
                File lPropertiesFile = new File(getSearchGuiFolder(), RelimsProperties.iFolderSeparator + "conf" + iFolderSeparator + "default_SearchGUI.properties");
                iSearchGUIPropertiesConfiguration = new PropertiesConfiguration(lPropertiesFile);
                return iSearchGUIPropertiesConfiguration;

            } catch (ConfigurationException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return iSearchGUIPropertiesConfiguration;
    }

    public static String getSearchGuiArchivePath() {
        return getSearchGuiFolder() + iFolderSeparator + getSearchGuiArchive();
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
        return "searchgui.fasta.default";
    }
}

