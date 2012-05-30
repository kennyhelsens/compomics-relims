package com.compomics.relims.model.provider.mslims;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.UserModsFile;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.guava.functions.MascotDatfileModificationMatchFunction;
import com.compomics.relims.model.interfaces.ModificationResolver;
import com.compomics.util.experiment.biology.PTM;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a
 */
public class ModificationResolverMslimsImpl implements ModificationResolver {

    private static Logger logger = Logger.getLogger(ModificationResolverMslimsImpl.class);

    private static MascotDatfileModificationMatchFunction iMascotDatfileModificationMatcher = new MascotDatfileModificationMatchFunction();

    protected ArrayList<String> iFixedMatchedPTMStrings = null;
    protected ArrayList<String> iVariableMatchedPTMStrings = null;

    protected UserModsFile iUserModsFile = null;

    private RelimsProjectBean iRelimsProjectBean = null;


    public void resolveModificationList(RelimsProjectBean aRelimsProjectBean) {
        iRelimsProjectBean = aRelimsProjectBean;

        List<UserMod> lStandardModificationList = aRelimsProjectBean.getStandardModificationList();
        List<UserMod> lExtraModificationList = aRelimsProjectBean.getExtraModificationList();

        List<UserMod> lAllModifications = Lists.newArrayList();
        lAllModifications.addAll(lStandardModificationList);
        lAllModifications.addAll(lExtraModificationList);


        iFixedMatchedPTMStrings = Lists.newArrayList();
        iVariableMatchedPTMStrings = Lists.newArrayList();

        List<UserMod> lUnresolvedModifications = Lists.newArrayList();

        for (UserMod lUserMod : lAllModifications) {

            // Could we find an appropriate match for this Modification?
            PTM lMatchedPTM = iMascotDatfileModificationMatcher.apply(lUserMod);
            if (lMatchedPTM != null) {
                if (lUserMod.isFixed()) {
                    iFixedMatchedPTMStrings.add(lMatchedPTM.getName());
                } else {
                    iVariableMatchedPTMStrings.add(lMatchedPTM.getName());
                }
                logger.debug("match OK " + lUserMod.getModificationName() + " " + lUserMod.getLocation());
            } else {
                logger.debug("match FAIL " + lUserMod.getModificationName() + " " + lUserMod.getLocation());
                lUnresolvedModifications.add(lUserMod);
            }
        }

        int lMatchCount = iFixedMatchedPTMStrings.size() + iVariableMatchedPTMStrings.size();
        int lOriginalCount = lAllModifications.size();

        // Persist the so-far unknown usermods in the PTMFactory.
        boolean lBuildUserMods = false;
        if (lUnresolvedModifications.size() > 0) {
            lBuildUserMods = true;
        }

        // Be sure that all modifications have been matched!
        String lErrorMessage = "not all mods were matched! (original:" + lOriginalCount + " - matched: " + lMatchCount + ")";
        Preconditions.checkArgument(lOriginalCount == lMatchCount, lErrorMessage);

        iUserModsFile = new UserModsFile();

        if (lBuildUserMods) {
            int lPTMFactorySize = RelimsProperties.getPTMFactory(false).getPTMs().size();
            int lUserModsSize = lUnresolvedModifications.size();

            logger.debug("loading " + lUserModsSize + " extra usermods for the current search (PTMFactory size:" + lPTMFactorySize + ")");

            iUserModsFile.setOMSSAXSDModifications(lUnresolvedModifications);

            iVariableMatchedPTMStrings.addAll(iUserModsFile.getVarModsAsString());
            iFixedMatchedPTMStrings.addAll(iUserModsFile.getFixedModsAsString());
        }

        iRelimsProjectBean.setFixedMatchedPTMs(iFixedMatchedPTMStrings);
        iRelimsProjectBean.setVariableMatchedPTMs(iVariableMatchedPTMStrings);

        persistUserMods(RelimsProperties.getSearchGuiUserModFile());
    }


    public void persistUserMods(File aFile) {
        if (aFile.exists()) {
            aFile.delete();
        }
        try {
            aFile.createNewFile();
            iUserModsFile.write(aFile);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        iRelimsProjectBean.setUserModsFile(iUserModsFile);
    }
}
