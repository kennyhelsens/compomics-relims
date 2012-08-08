package com.compomics.relims.model.strategy;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.concurrent.SearchGUICommandGenerator;
import com.compomics.relims.concurrent.SearchGUICommandVarMod;
import com.compomics.relims.concurrent.SearchGUICommandVarMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;
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

    public void fill(SearchList aSearchList, RelimsProjectBean aRelimsProjectBean) {
        List<UserMod> lRelimsMods = RelimsProperties.getRelimsMods();
        aRelimsProjectBean.setExtraModificationList(lRelimsMods);

        // First define a search without the relims modification.
        SearchGUICommandGenerator lSearchGUIBean = null;
        lSearchGUIBean = new SearchGUICommandVarMod("original_mod", aRelimsProjectBean, iSpectrumFiles);

        aSearchList.add(lSearchGUIBean);

        // Then define searches for all extra relims modifications.
        for (UserMod lRelimsModification : lRelimsMods) {
            ArrayList<UserMod> lRelimsModList = new ArrayList<UserMod>();
            lRelimsModList.add(lRelimsModification);

            lSearchGUIBean = new SearchGUICommandVarMod(
                    lRelimsModification,
                    aRelimsProjectBean,
                    iSpectrumFiles);

            aSearchList.add(lSearchGUIBean);
        }
    }

    public void addSpectrumFile(File aSpectrumFile) {
        iSpectrumFiles.add(aSpectrumFile);
    }

    public String getName() {
        return "VarMODStrategy";
    }

    public String getDescription() {
        return "Run n parallel searches in with n+1 different modification sets";
    }
}
