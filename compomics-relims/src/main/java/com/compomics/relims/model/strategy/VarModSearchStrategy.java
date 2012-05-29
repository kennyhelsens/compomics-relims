package com.compomics.relims.model.strategy;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.concurrent.SearchCommandVarMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;
import com.compomics.relims.model.interfaces.SearchCommandGenerator;
import com.compomics.relims.model.interfaces.SearchStrategy;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a
 */
public class VarModSearchStrategy implements SearchStrategy {

    private static Logger logger = Logger.getLogger(VarModSearchStrategy.class);

    private List<File> iSpectrumFiles = Lists.newArrayList();

    public void fill(SearchList<SearchCommandGenerator> aSearchList, RelimsProjectBean aRelimsProjectBean) {

        ModificationList lModificationList = aRelimsProjectBean.getModificationLists().get(0);
        ArrayList<Modification> lFixMods = Lists.newArrayList(lModificationList.getFixedModifications());
        ArrayList<Modification> lVarMods = Lists.newArrayList(lModificationList.getVariableModifications());
        ArrayList<Modification> lMods = Lists.newArrayList();
        lMods.addAll(lFixMods);
        lMods.addAll(lVarMods);

        // First define a search without the relims modification.
        SearchCommandGenerator lSearchBean = null;
        lSearchBean = new SearchCommandVarMod("original", lFixMods, lVarMods, aRelimsProjectBean, iSpectrumFiles);
        aSearchList.add(lSearchBean);


        ArrayList<UserMod> lRelimsMods = RelimsProperties.getRelimsMods();
        for (UserMod lRelimsModification : lRelimsMods) {
            ArrayList<UserMod> lRelimsModList = new ArrayList<UserMod>();
            lRelimsModList.add(lRelimsModification);


            lSearchBean = new SearchCommandVarMod(
                    lRelimsModification.getModificationName(),
                    lFixMods,
                    lVarMods,
                    lRelimsModList,
                    aRelimsProjectBean,
                    iSpectrumFiles);

            aSearchList.add(lSearchBean);
        }
    }

    public void addSpectrumFile(File aSpectrumFile) {
        iSpectrumFiles.add(aSpectrumFile);
    }

    public String getName() {
        return "VarDBStrategy";
    }

    public String getDescription() {
        return "Run n parallel searches in n Protein sequence databases";
    }
}
