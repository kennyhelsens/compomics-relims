package com.compomics.relims.model.strategy;

import com.compomics.relims.concurrent.SearchGUICommandGenerator;
import com.compomics.relims.concurrent.SearchGUICommandVarDb;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;
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

    private List<File> iSpectrumFiles = Lists.newArrayList();

    public void fill(SearchList aSearchList, RelimsProjectBean aRelimsProjectBean) {
        SearchGUICommandGenerator lSearchGUIBean = null;

        String[] lDatabaseVarIDs = RelimsProperties.getDatabaseVarIDs();
        for (String lDatabaseVarID : lDatabaseVarIDs) {
            lSearchGUIBean = new SearchGUICommandVarDb(lDatabaseVarID, lDatabaseVarID, aRelimsProjectBean, iSpectrumFiles);
            aSearchList.add(lSearchGUIBean);
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
