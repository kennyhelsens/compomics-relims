package com.compomics.relims.concurrent;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * This class is a
 */
public class SearchCommandVarDb extends SearchCommandGenerator {
    private static Logger logger = Logger.getLogger(SearchCommandVarDb.class);

    private String iDbVarId = null;

    public SearchCommandVarDb(String aName, String aDbVarId, RelimsProjectBean aRelimsProjectBean, List<File> aSpectrumFiles) {
        super(aName, aRelimsProjectBean, aSpectrumFiles);
        iDbVarId = aDbVarId;
    }

    protected void applyChildMethods() {
        logger.debug(String.format("overriding fasta database %s to searchgui configuration", iDbVarId));
        iSearchGuiConfiguration.setProperty("DATABASE_FILE", RelimsProperties.getDatabaseFilename(iDbVarId));
    }

}
