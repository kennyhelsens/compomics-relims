package com.compomics.relims.model.strategy;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.compomics.relims.concurrent.SearchCommandVarDb;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;
import com.compomics.relims.model.interfaces.SearchCommandGenerator;
import com.compomics.relims.model.interfaces.SearchStrategy;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * This class is a
 */
public class VarDBSearchStrategy implements SearchStrategy {

    private static Logger logger = Logger.getLogger(VarDBSearchStrategy.class);

    private List<File> iSpectrumFiles = null;
    private RelimsProjectBean iRelimsProjectBean;

    public void fill(SearchList<SearchCommandGenerator> aSearchList) {


            ModificationList lModificationList = iRelimsProjectBean.getModificationLists().get(0);
            List<Modification> lFixMods = Lists.newArrayList(lModificationList.getFixedModifications());
            List<Modification> lVarMods = Lists.newArrayList(lModificationList.getVariableModifications());
            List<Modification> lMods = Lists.newArrayList();
            lMods.addAll(lFixMods);
            lMods.addAll(lVarMods);


            SearchCommandGenerator lSearchBean = null;

            // First define a search without the relims modification.
            String[] lDatabaseVarIDs = RelimsProperties.getDatabaseVarIDs();
            for (String lDatabaseVarID : lDatabaseVarIDs) {
                lSearchBean = new SearchCommandVarDb(lDatabaseVarID, lFixMods, lVarMods, lDatabaseVarID, iRelimsProjectBean, iSpectrumFiles);
                aSearchList.add(lSearchBean);
            }
    }

    public void setSpectrumFiles(List<File> aSpectrumFiles) {
        iSpectrumFiles = aSpectrumFiles;
    }

    public void setRelimsProjectBean(RelimsProjectBean aRelimsProjectBean) {
        iRelimsProjectBean = aRelimsProjectBean;
    }

    public String getName() {
        return "VarDBStrategy";
    }

    public String getDescription() {
        return "Run n parallel searches with n-1 different PTM sets";
    }

}
