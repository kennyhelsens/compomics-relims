package com.compomics.relims.concurrent;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * This class is a
 */
public class SearchCommandStraight extends SearchCommandGenerator {
    private static Logger logger = Logger.getLogger(SearchCommandStraight.class);

    public SearchCommandStraight(RelimsProjectBean aRelimsProjectBean, List<File> aSpectrumFiles) {
        super("straight", aRelimsProjectBean, aSpectrumFiles);
    }

    protected void applyChildMethods() {
        String lDefaultSearchDatabase = RelimsProperties.getDefaultSearchDatabase();
        logger.debug(String.format("using default fasta database %s to searchgui configuration", lDefaultSearchDatabase));
        iSearchGuiConfiguration.setProperty("DATABASE_FILE", lDefaultSearchDatabase);
    }

}
