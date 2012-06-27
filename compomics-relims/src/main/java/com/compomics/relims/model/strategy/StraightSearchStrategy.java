package com.compomics.relims.model.strategy;

import com.compomics.relims.concurrent.SearchCommandGenerator;
import com.compomics.relims.concurrent.SearchCommandStraight;
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
public class StraightSearchStrategy implements SearchStrategy {

    private static Logger logger = Logger.getLogger(StraightSearchStrategy.class);

    private List<File> iSpectrumFiles = Lists.newArrayList();

    public void fill(SearchList aSearchList, RelimsProjectBean aRelimsProjectBean) {
        SearchCommandGenerator lSearchBean = null;

        lSearchBean = new SearchCommandStraight(aRelimsProjectBean, iSpectrumFiles);
        aSearchList.add(lSearchBean);

    }

    public void addSpectrumFile(File aSpectrumFile) {
        iSpectrumFiles.add(aSpectrumFile);
    }

    public String getName() {
        return "StraightSearchStrategy";
    }

    public String getDescription() {
        return "Run single search on default protein sequence databases";
    }

}
