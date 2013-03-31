package com.compomics.relims.model.beans;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.UserModsFile;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.preferences.ModificationProfile;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.Nullable;
import no.uib.jsparklines.data.XYDataPoint;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * This class is a
 */
public class RelimsProjectBean implements Cloneable {

    private long iProjectID = -1;
    private UserModsFile iUserModsFile = new UserModsFile();
    private List<String> iVariableMatchedPTMs = Lists.newArrayList();
    private List<String> iFixedMatchedPTMs = Lists.newArrayList();
    private List<UserMod> iStandardModificationList = Lists.newArrayList();
    private List<UserMod> iExtraModificationList = Lists.newArrayList();
    private double iPrecursorError = 1.0;
    private double iFragmentError = 1.0;
    private Set<Integer> consideredChargeStates;
    private final static Logger logger = Logger.getLogger(RelimsProjectBean.class);
    private SearchParameters searchParameters;
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    private DataProvider dataProvider;
    private final long projectId;
    private File spectrumFile = null;
    private File searchParametersFile = null;

    public RelimsProjectBean(long projectId) {
        this.projectId = projectId;
    }

    public RelimsProjectBean(long projectId, File spectrumFile, File searchParametersFile) {
        this.projectId = projectId;
        this.searchParametersFile = searchParametersFile;
        this.spectrumFile = spectrumFile;
    }

    public List<UserMod> getStandardModificationList() {
        return iStandardModificationList;
    }

    public List<UserMod> getExtraModificationList() {
        return iExtraModificationList;
    }

    public void setStandardModificationList(List<UserMod> aStandardModificationList) {
        iStandardModificationList.clear();
        iStandardModificationList.addAll(aStandardModificationList);
    }

    public void setExtraModificationList(List<UserMod> aExtraModificationList) {
        iExtraModificationList.clear();
        iExtraModificationList.addAll(aExtraModificationList);
    }

    public List<String> getVariableMatchedPTMs() {
        return iVariableMatchedPTMs;
    }

    public void setVariableMatchedPTMs(List<String> aVariableMatchedPTMs) {
        iVariableMatchedPTMs.clear();
        iVariableMatchedPTMs.addAll(aVariableMatchedPTMs);
    }

    public List<String> getFixedMatchedPTMs() {
        return iFixedMatchedPTMs;
    }

    public void setFixedMatchedPTMs(List<String> aFixedMatchedPTMs) {
        iFixedMatchedPTMs.clear();
        iFixedMatchedPTMs.addAll(aFixedMatchedPTMs);
    }

    public void setUserModsFile(UserModsFile aUserModsFile) {
        iUserModsFile = aUserModsFile;
    }

    public UserModsFile getUserModsFile() {
        return iUserModsFile;
    }

    public long getProjectID() {
        return iProjectID;
    }

    public void setProjectID(long aProjectID) {
        iProjectID = aProjectID;
    }

    public void setFragmentError(double aFragmentError) {
        iFragmentError = aFragmentError;
    }

    public void setPrecursorError(double aPrecursorError) {
        iPrecursorError = aPrecursorError;
    }

    public double getFragmentError() {
        return iFragmentError;
    }

    public double getPrecursorError() {
        return iPrecursorError;
    }

    @Override
    public String toString() {
        return "" + getProjectID();
    }

    @Override
    public RelimsProjectBean clone() throws CloneNotSupportedException {
        RelimsProjectBean lProjectBean = new RelimsProjectBean(this.projectId);

        lProjectBean.setProjectID(getProjectID());
        lProjectBean.setCharges(getCharges());
        lProjectBean.setSpectrumFile(getSpectrumFile());
        lProjectBean.setSearchParameters(getSearchParameters());
        lProjectBean.setStandardModificationList(getStandardModificationList());
        lProjectBean.setExtraModificationList(getExtraModificationList());

        lProjectBean.setUserModsFile(getUserModsFile());

        lProjectBean.setFixedMatchedPTMs(getFixedMatchedPTMs());
        lProjectBean.setVariableMatchedPTMs(getVariableMatchedPTMs());

        lProjectBean.setPrecursorError(getPrecursorError());
        lProjectBean.setFragmentError(getFragmentError());

        return lProjectBean;
    }

    public void setCharges(Set<Integer> consideredChargeStates) {
        this.consideredChargeStates = consideredChargeStates;
    }

    public Set<Integer> getCharges() {
        return consideredChargeStates;
    }

    public void createSearchParameters() {

        File fastaFile = new File(RelimsProperties.getDefaultSearchDatabase());


        searchParameters = new SearchParameters();// this should be default..;

// ======================================================SETTING FASTA FILE

        searchParameters.setFastaFile(fastaFile);
        logger.debug("USING FASTA : " + searchParameters.getFastaFile().getAbsolutePath());

// ======================================================SETTING MOD PROFILE

        logger.debug("building modification profile for searchparameters file");
        ModificationProfile modProfile = buildModProfile();
        searchParameters.setModificationProfile(modProfile);

// ======================================================SETTING ENZYME
        try {
            File enzymesFile = new File(RelimsProperties.getSearchGuiFolder() + "/resources/conf/searchGUI_enzymes.xml");
            enzymeFactory.importEnzymes(enzymesFile);
            Enzyme enzyme = getEnzyme();
            if (enzyme == null) {
                enzyme = enzymeFactory.getEnzyme("Trypsin");
            }
            searchParameters.setEnzyme(enzyme);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

// ======================================================SETTING ACCURACIES
        searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.DA);
        searchParameters.setPrecursorAccuracy(getPrecursorError());
        searchParameters.setFragmentIonAccuracy(getFragmentError());

// ======================================================SETTING MISSED CLEAVAGES
        searchParameters.setnMissedCleavages(RelimsProperties.getMissedCleavages());


// ======================================================SETTING CHARGES        
        Charge[] foundCharges = findCharges();
        searchParameters.setMinChargeSearched(foundCharges[0]);
        searchParameters.setMaxChargeSearched(foundCharges[1]);

// ======================================================VALIDATE PARAMETERS 
        validateSearchParameters(searchParameters);
        //}
        logger.info("Searchparameters were loaded");
    }

    private void validateSearchParameters(SearchParameters searchParameters) {
        //Store the parameters in the config file that is saved with the results as well?
        PropertiesConfiguration config = RelimsProperties.getConfig();

// ======================================================EVALUATE ENZYME
        if (searchParameters.getEnzyme() == null) {
            logger.debug("Enzyme was null!");
            searchParameters.setEnzyme(enzymeFactory.getEnzyme("Trypsin"));
        }
        config.setProperty("used.enzyme", searchParameters.getEnzyme().getName());
        logger.debug("Using enzyme : " + searchParameters.getEnzyme().getName());

// ======================================================EVALUATE FASTA

        if (searchParameters.getFastaFile() == null) {
            logger.debug("Fasta was null!");
            searchParameters.setFastaFile(new File(RelimsProperties.getDefaultSearchDatabase()));
        }
        config.setProperty("used.fasta", searchParameters.getFastaFile().getName());
        logger.debug("Using Fasta : " + searchParameters.getFastaFile().getName());

// ======================================================EVALUATE MOLECULAR WEIGHT RANGE

        if (searchParameters.getFractionMolecularWeightRanges() == null) {
            logger.debug("Fraction Molecular Weight Range was null!");
            searchParameters.setFractionMolecularWeightRanges(new HashMap<String, XYDataPoint>());
        }
        config.setProperty("used.fraction.molecular.weightRanges", searchParameters.getFractionMolecularWeightRanges());
        logger.debug("Using Fraction Molecular Weight Range : " + searchParameters.getFractionMolecularWeightRanges());

// ======================================================EVALUATE FRAGMENTIONACC       
        if (searchParameters.getFragmentIonAccuracy() == null) {
            logger.debug("Fragment Ion Accuracy was null");
            searchParameters.setFragmentIonAccuracy(1.0);
        }
        config.setProperty("used.fragment.ion.Acc", searchParameters.getFragmentIonAccuracy());
        logger.debug("Using Fragment Ion Accuracy : " + searchParameters.getFragmentIonAccuracy());

// ======================================================EVALUATE PRECURSORACC   
        if (searchParameters.getPrecursorAccuracy() == null) {
            logger.debug("Precursor Accuracy was null!");
            searchParameters.setPrecursorAccuracy(1.0);
        }
        config.setProperty("used.precursor.accuracy", searchParameters.getPrecursorAccuracy());
        logger.debug("Using Precursor Accuracy : " + searchParameters.getPrecursorAccuracy());

// ======================================================EVALUATE HITLISTLENGTH          
        if (searchParameters.getHitListLength() == null) {
            logger.debug("Hitlist length was null!");
            searchParameters.setHitListLength(0);
        }
        config.setProperty("used.hitlist.length", searchParameters.getHitListLength());
        logger.debug("Using Hitlist length : " + searchParameters.getHitListLength());

// ======================================================EVALUATE IONS SEARCHED  

        if (searchParameters.getIonSearched1() == null) {
            logger.debug("Ion 1 was null!");
            searchParameters.setIonSearched1("");
        }
        config.setProperty("used.ion1.searched", searchParameters.getIonSearched1());
        logger.debug("Using Ion 1 searched : " + searchParameters.getIonSearched1());
        if (searchParameters.getIonSearched2() == null) {
            logger.debug("Ion 2 was null!");
            searchParameters.setIonSearched2("");
        }
        config.setProperty("used.ion2.searched", searchParameters.getIonSearched2());
        logger.debug("Using Ion 2 searched : " + searchParameters.getIonSearched2());

// ======================================================EVALUATE E VALUE 
        if (searchParameters.getMaxEValue() == null) {
            logger.debug("Max E-value was null!");
            searchParameters.setMaxEValue(100.0);
        }
        config.setProperty("used.max.e.value", searchParameters.getMaxEValue());
        logger.debug("Using Max E-value : " + searchParameters.getMaxEValue());

// ======================================================EVALUATE MAX PEPTIDE LENGTH

        if (searchParameters.getMaxPeptideLength() == null) {
            logger.debug("Max peptide length was null!");
            searchParameters.setMaxPeptideLength(30);
        }
        config.setProperty("used.max.peptide.length", searchParameters.getMaxPeptideLength());
        logger.debug("Using Max Peptide Length : " + searchParameters.getMaxPeptideLength());

// ====================================================EVALUATE VARIABLE MISSED CLEAVAGES     

        if (searchParameters.getnMissedCleavages() == null) {
            logger.debug("Missed Cleavages was null!");
            searchParameters.setnMissedCleavages(0);
        }
        config.setProperty("used.missed.cleaveages", searchParameters.getnMissedCleavages());
        logger.debug("Using Missed Cleavages : " + searchParameters.getnMissedCleavages());

// ====================================================EVALUATE CHARGES    
        if (searchParameters.getMaxChargeSearched() == null) {
            logger.debug("Max Charge Searched was null!");
            searchParameters.setMaxChargeSearched(new Charge(1, 4));
        }
        config.setProperty("used.max.charge", searchParameters.getMaxChargeSearched());
        logger.debug("Using Max Charge : " + searchParameters.getMaxChargeSearched());

        if (searchParameters.getMinChargeSearched() == null) {
            logger.debug("Min Charge Searched was null!");
            searchParameters.setMinChargeSearched(new Charge(1, 1));
        }
        config.setProperty("used.min.charge", searchParameters.getMinChargeSearched());
        logger.debug("Using Min Charge : " + searchParameters.getMinChargeSearched());

// ======================================================EVALUATE FIXED MODIFICATIONS

        ArrayList<String> fixModList = searchParameters.getModificationProfile().getFixedModifications();
        logger.debug("Using Fixed Mod profile : ");
        for (String aMod : fixModList) {
            config.setProperty("used.mod." + fixModList.indexOf(aMod), aMod);
            logger.debug("Fixed Modification : " + aMod);
        }
// ====================================================EVALUATE VARIABLE MODIFICATIONS
        ArrayList<String> varModList = searchParameters.getModificationProfile().getVariableModifications();
        logger.debug("Using Variable Mod profile : ");
        for (String aMod : varModList) {
            config.setProperty("used.mod." + varModList.indexOf(aMod), aMod);
            logger.debug("Var Modification : " + aMod);
        }

    }

    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    public File getSpectrumFile() {
        if (spectrumFile == null) {
            try {
                this.spectrumFile = dataProvider.getSpectraForProject(projectId);
            } catch (Exception e) {
                logger.error("Could not load MGF");
            }
        }
        return spectrumFile;
    }

    public void setDataProvider(DataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    public File getSearchParamFile() {
        if (searchParametersFile == null) {
            searchParametersFile = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/SearchGUI.parameters");
            try {
                searchParameters.saveIdentificationParameters(searchParameters, searchParametersFile);
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(RelimsProjectBean.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | ClassNotFoundException ex) {
                java.util.logging.Logger.getLogger(RelimsProjectBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return searchParametersFile;
    }

    public void setSpectrumFile(File spectrumFile) {
        this.spectrumFile = spectrumFile;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    private ModificationProfile buildModProfile() {
        ModificationProfile modProfile = new ModificationProfile();
        List<UserMod> lStandardModificationList = getStandardModificationList();
        List<UserMod> lExtraModificationList = getExtraModificationList();

        Function<UserMod, PTM> UserModToPTMFunction = new Function<UserMod, PTM>() {
            @Override
            public PTM apply(@Nullable UserMod input) {
                // Prepare the position as an AminoAcidPattern
                AminoAcidPattern pos = new AminoAcidPattern();
                ArrayList<AminoAcid> target = new ArrayList<AminoAcid>();
                for (char aa : input.getLocation().toCharArray()) {
                    target.add(AminoAcid.getAminoAcid(aa));
                }
                pos.setTargeted(0, target);

                // Create a PTM instance from the UserMod data
                PTM res = new PTM(
                        input.getLocationType().getLocationTypeID(),
                        input.getModificationName().toLowerCase(),
                        input.getMass(),
                        pos);
                return res;
            }
        };

        // Join all default and usermod names from the current 
        HashSet<String> ptmFactoryAllModifications = Sets.newHashSet();
        ptmFactoryAllModifications.addAll(ptmFactory.getDefaultModifications());
        ptmFactoryAllModifications.addAll(ptmFactory.getUserModifications());

        // Join all Relims Modifications needed for this SearchGUIJob
        HashSet<UserMod> jobUserMods = Sets.newHashSet();
        jobUserMods.addAll(lStandardModificationList);
        jobUserMods.addAll(lExtraModificationList);

        // Iterate over the relims modifications
        for (UserMod aMod : jobUserMods) {
            try {
                PTM aModPTM = UserModToPTMFunction.apply(aMod);

                boolean isInFactory = false;
                // Check whether the relims modification name is known in the PTMFactory  
                for (String lPtmName : ptmFactoryAllModifications) {
                    PTM lPTM = ptmFactory.getPTM(lPtmName);
                    if (lPTM.isSameAs(aModPTM)) {

                        isInFactory = true;
                        aModPTM = lPTM;
                        //OMSSA is caps sensitive
                        aModPTM.setName(aModPTM.getName().toLowerCase());
                        break;
                    }
                }
                // If not known, convert to Utilities-PTM instance, and add keep the modification name.
                if (!isInFactory) {
                    ptmFactory.addUserPTM(aModPTM);
                    ptmFactoryAllModifications.add(aModPTM.getName().toLowerCase());
                }
                // Add the PTM to the ModificationProfile instance that will be used in the SearchParameters instance.
                if (aMod.isFixed()) {
                    if (!modProfile.getFixedModifications().contains(aModPTM.getName())) {
                        modProfile.addFixedModification(ptmFactory.getPTM(aModPTM.getName().toLowerCase()));
                        logger.debug("Added fixed modification : " + aModPTM.getName().toLowerCase());
                    }
                } else {
                    if (!modProfile.getVariableModifications().contains(aModPTM.getName())) {
                        modProfile.addVariableModification(ptmFactory.getPTM(aModPTM.getName().toLowerCase()));
                        logger.debug("Added variable modification : " + aModPTM.getName().toLowerCase());
                    }
                }
                //  ptmFactory.addUserPTM(aModPTM);
            } catch (Exception e) {
                logger.error(e);
                logger.debug("failed to set " + aMod + " in the modificationprofile.");
            } finally {
            }

        }
        // persist the usermods to the usermod.xml
        File relimsUserMod = RelimsProperties.getUserModsFile();
        try {
            logger.debug("Writing OMSSA modifications to " + relimsUserMod);
            ptmFactory.writeOmssaUserModificationFile(relimsUserMod);
        } catch (IOException ex) {
            logger.error(ex);
        } finally {
            return modProfile;
        }
    }

    private Charge[] findCharges() {

        int maxCharge;
        int minCharge;
        try {
            maxCharge = Collections.max(getCharges());
        } catch (NullPointerException e) {
            maxCharge = 4;
            logger.error("No max charge found, setting default to 4");
        }
        try {
            minCharge = Collections.min(getCharges());
        } catch (NullPointerException e) {
            minCharge = 1;
            logger.error("No min charge found, setting default to 1");
        }
        Charge maxChargeSearched = new Charge(1, maxCharge);
        Charge minChargeSearched = new Charge(1, minCharge);

        return new Charge[]{minChargeSearched, maxChargeSearched};

    }

    private Enzyme getEnzyme() {
        return null;
    }

    void setSearchParametersFile(File searchParametersFile) {
        this.searchParametersFile = searchParametersFile;
    }
}
