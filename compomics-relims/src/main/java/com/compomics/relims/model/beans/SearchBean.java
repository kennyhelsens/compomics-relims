package com.compomics.relims.model.beans;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.guava.functions.ModMatchFunction;
import com.compomics.relims.guava.functions.SearchGuiModStringFunction;
import com.compomics.relims.model.UserModsFile;
import com.compomics.util.experiment.biology.PTM;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is a
 */
public class SearchBean {
    private static ModMatchFunction iModMatcher = new ModMatchFunction();
    private static Logger logger = Logger.getLogger(SearchBean.class);

    private String iName = null;
    private ArrayList<Modification> iFixedModifications = null;
    private ArrayList<Modification> iVariableModifications = null;
    private ArrayList<UserMod> iRelimsModifications = null;
    private ProjectSetupBean iProjectSetupBean = null;

    private File iSearchResultFolder = null;
    private ArrayList<File> iSpectrumFiles;


    public SearchBean(String aName, ArrayList<Modification> aFixMods, ArrayList<Modification> aVarMods, ProjectSetupBean aProjectSetupBean, ArrayList<File> aSpectrumFiles) {
        iName = aName;
        iFixedModifications = aFixMods;
        iVariableModifications = aVarMods;
        iProjectSetupBean = aProjectSetupBean;
        iSpectrumFiles = aSpectrumFiles;
    }

    public SearchBean(String aName, ArrayList<Modification> aFixMods, ArrayList<Modification> aVarMods, ArrayList<UserMod> aRelimsModifications, ProjectSetupBean aProjectSetupBean, ArrayList<File> aSpectrumFiles) {
        iName = aName;
        iFixedModifications = aFixMods;
        iVariableModifications = aVarMods;
        iRelimsModifications = aRelimsModifications;
        iProjectSetupBean = aProjectSetupBean;
        iSpectrumFiles = aSpectrumFiles;
    }

    public String generateCommand() throws IOException, ConfigurationException {

        long lTimeStamp = System.currentTimeMillis();
        iSearchResultFolder = new File(RelimsProperties.getWorkSpace(), ("" + getProjectId() + "_" + lTimeStamp));
        iSearchResultFolder.mkdir();


        logger.debug("matching modification sets");
        resolveModifications();

        logger.debug("setting modifications to searchgui configuration file");
        SearchGuiModStringFunction lSearchGuiModStringFunction = new SearchGuiModStringFunction();
        PropertiesConfiguration lSearchGuiConfiguration = RelimsProperties.getDefaultSearchGuiConfiguration();
        lSearchGuiConfiguration.setProperty("FIXED_MODIFICATIONS", lSearchGuiModStringFunction.apply(iProjectSetupBean.getFixedMatchedPTMs()));
        lSearchGuiConfiguration.setProperty("VARIABLE_MODIFICATIONS", lSearchGuiModStringFunction.apply(iProjectSetupBean.getVariableMatchedPTMs()));
        lSearchGuiConfiguration.setProperty("DATABASE_FILE", "E:\\db\\uniprot_sprot_101104_human_concat.fasta");

        File lSearchGuiConfigurationFile = RelimsProperties.getTmpFile("" + iProjectSetupBean.getProjectID() + "_" + lTimeStamp);
        logger.debug("saving searchgui configuration to " + lSearchGuiConfigurationFile.getCanonicalPath());
        lSearchGuiConfiguration.save(lSearchGuiConfigurationFile);

        SearchGUIJobBean lSearchGUIJobBean = new SearchGUIJobBean();
        lSearchGUIJobBean.setSearch(true);
        lSearchGUIJobBean.setGui(false);

        lSearchGUIJobBean.setOmssa(RelimsProperties.useOmssa());
        lSearchGUIJobBean.setXtandem(RelimsProperties.useTandem());

        lSearchGUIJobBean.setConfig(lSearchGuiConfigurationFile);
        lSearchGUIJobBean.setSpectra(iSpectrumFiles);


        lSearchGUIJobBean.setResults(iSearchResultFolder);

        logger.debug("starting new searchgui process");

        logger.debug("writing results to " + iSearchResultFolder.getCanonicalPath());

        return lSearchGUIJobBean.getSearchGUICommandString();
    }

    private void resolveModifications() {
        ArrayList<String> lFixedMatchedPTMs = Lists.newArrayList();
        ArrayList<String> lVariableMatchedPTMs = Lists.newArrayList();

        ArrayList<Modification> lAllMods = Lists.newArrayList();
        lAllMods.addAll(iVariableModifications);
        lAllMods.addAll(iFixedModifications);

        ArrayList<Modification> lUserMods = Lists.newArrayList();

        try {
            for (Object o : lAllMods) {
                Modification lMod = (Modification) o;
                // Could we find an appropriate match for this Modification?
                PTM lMatchedPTM = iModMatcher.apply(lMod);
                if (lMatchedPTM != null) {
                    if(lMod.isFixed()){
                        lFixedMatchedPTMs.add(lMatchedPTM.getName());
                    }else{
                        lVariableMatchedPTMs.add(lMatchedPTM.getName());
                    }
                    logger.debug("match OK " + lMod.getType() + " " + lMod.getLocation());
                } else {
                    logger.debug("match FAIL " + lMod.getType() + " " + lMod.getLocation());
                    lUserMods.add(lMod);
                }
            }

            // Persist the so-far unknown usermods in the PTMFactory.
            boolean lBuildUserMods = false;
            if(lUserMods.size() > 0){
                lBuildUserMods = true;
            }else if(iRelimsModifications != null && iRelimsModifications.size() > 0){
                lBuildUserMods  = true;
            }

            if (lBuildUserMods) {
                UserModsFile lUserModsFile;
                int lPTMFactorySize = RelimsProperties.getPTMFactory(false).getPtmMap().size();
                int lUserModsSize = lUserMods.size();

                if(iRelimsModifications != null){
                    lUserModsSize = lUserModsSize + iRelimsModifications.size();
                }

                logger.debug("loading " + lUserModsSize + " extra usermods for the current search (PTMFactory size:" + lPTMFactorySize + ")");
                lUserModsFile = new UserModsFile(lUserMods, iRelimsModifications);

                File lFile = RelimsProperties.getSearchGuiUserModFile();

                if (lFile.exists()) {
                    lFile.delete();
                }

                lFile.createNewFile();
                lUserModsFile.write(lFile);

                iProjectSetupBean.setUserModsFile(lUserModsFile);

                lVariableMatchedPTMs.addAll(lUserModsFile.getVarModsAsString());
                lFixedMatchedPTMs.addAll(lUserModsFile.getFixedModsAsString());
            }

            int lMatchCount = lFixedMatchedPTMs.size() + lVariableMatchedPTMs.size();
            if(iRelimsModifications != null){
                lMatchCount = lMatchCount - iRelimsModifications.size();
            }
            int lOriginalCount = lAllMods.size();

            // Be sure that all modifications have been matched!
            String lErrorMessage = "not all mods were matched! (original:" + lOriginalCount + " - matched: " + lMatchCount + ")";
            Preconditions.checkArgument(lOriginalCount == lMatchCount, lErrorMessage);

            iProjectSetupBean.setFixedMatchedPTMs(lFixedMatchedPTMs);
            iProjectSetupBean.setVariableMatchedPTMs(lVariableMatchedPTMs);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getName() {
        return iName;
    }

    public ArrayList<File> getSpectrumFiles() {
        return iSpectrumFiles;
    }

    public int getProjectId(){
        return iProjectSetupBean.getProjectID();
    }

    public File getSearchResultFolder() {
        return iSearchResultFolder;
    }

    public void setSearchResultFolder(File aSearchResultFolder) {
        iSearchResultFolder = aSearchResultFolder;
    }
}
