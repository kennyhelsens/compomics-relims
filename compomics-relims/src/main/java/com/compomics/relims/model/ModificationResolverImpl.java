package com.compomics.relims.model;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.guava.functions.OMSSAXSDModificationMatchFunction;
import com.compomics.relims.model.interfaces.ModificationResolver;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import org.apache.log4j.Logger;
import org.xmlpull.v1.XmlPullParserException;

/**
 * This class is a
 */
public class ModificationResolverImpl implements ModificationResolver {

    private static Logger logger = Logger.getLogger(ModificationResolverImpl.class);
    private static OMSSAXSDModificationMatchFunction iOMSSAXSDModificationMatcher = new OMSSAXSDModificationMatchFunction();
    protected HashSet<String> iFixedMatchedPTMStrings = null;
    protected HashSet<String> iVariableMatchedPTMStrings = null;
    protected UserModsFile iUserModsFile = null;
    private RelimsProjectBean iRelimsProjectBean = null;

    public void resolveModificationList(RelimsProjectBean aRelimsProjectBean) {
        iRelimsProjectBean = aRelimsProjectBean;

        List<UserMod> lStandardModificationList = aRelimsProjectBean.getStandardModificationList();
        List<UserMod> lExtraModificationList = aRelimsProjectBean.getExtraModificationList();

        List<UserMod> lAllModifications = Lists.newArrayList();
        lAllModifications.addAll(lStandardModificationList);
        lAllModifications.addAll(lExtraModificationList);

        // Reload the PTM factory.
        //  RelimsProperties.getPTMFactory(true);

        iFixedMatchedPTMStrings = Sets.newHashSet();
        iVariableMatchedPTMStrings = Sets.newHashSet();

        List<UserMod> lUnresolvedModifications = Lists.newArrayList();

        for (UserMod lUserMod : lAllModifications) {

            // Could we find an appropriate match for this Modification?
            PTM lMatchedPTM = iOMSSAXSDModificationMatcher.apply(lUserMod);
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
        try {
            Preconditions.checkArgument(lOriginalCount == lMatchCount, lErrorMessage);
        } catch (IllegalArgumentException e) {
            // catch it here or else entire pride dataprovider will be "failed"
        }
        iUserModsFile = new UserModsFile();


        if (lBuildUserMods) {
            int lPTMFactorySize = RelimsProperties.getPTMFactory(false).getPTMs().size();
            int lUserModsSize = lUnresolvedModifications.size();
            logger.debug("loading " + lUserModsSize + " extra usermods for the current search (PTMFactory size:" + lPTMFactorySize + ")");
            iUserModsFile.setOMSSAXSDModifications(lUnresolvedModifications);
            iVariableMatchedPTMStrings.addAll(iUserModsFile.getVarModsAsString());
            iFixedMatchedPTMStrings.addAll(iUserModsFile.getFixedModsAsString());
        }

        iRelimsProjectBean.setFixedMatchedPTMs(Lists.newArrayList(iFixedMatchedPTMStrings));
        iRelimsProjectBean.setVariableMatchedPTMs(Lists.newArrayList(iVariableMatchedPTMStrings));
    }

    @Override
    public void persistUserMods(File aFile) {
        if (aFile.exists()) {
            aFile.delete();
        }
        try {
            aFile.createNewFile();
            iUserModsFile.write(aFile);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            ProgressManager.setState(Checkpoint.FAILED, e);;
        }
        iRelimsProjectBean.setUserModsFile(iUserModsFile);

    }
}
