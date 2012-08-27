package com.compomics.relims.model.guava.predicates;

import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * This class is a
 */
public class InstrumentPredicate implements Predicate<RelimsProjectBean> {
    private static Logger logger = Logger.getLogger(InstrumentPredicate.class);
    private String[] iAllowedInstrumentIDs = null;
    private final DataProvider iDataProvider;

    public InstrumentPredicate(DataProvider aDataProvider) {
        iDataProvider = aDataProvider;
        iAllowedInstrumentIDs = RelimsProperties.getAllowedInstruments();
    }

    public boolean apply(@Nullable RelimsProjectBean aProjectBean) {
        long lProjectid = 0;

        if (aProjectBean != null) {
            lProjectid = aProjectBean.getProjectID();
        }

        boolean accept =false;
        Set<AnalyzerData> lAnalyzerDataSet = iDataProvider.getInstrumentsForProject(aProjectBean.getProjectID());

        for (AnalyzerData lAnalyzerData : lAnalyzerDataSet) {

            // check for FT
            if(lAnalyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.FT)){
                for (String lAllowedInstrumentID : iAllowedInstrumentIDs) {
                    if(lAllowedInstrumentID.toLowerCase().equals("ft")){
                        logger.debug("accept mass analyzer 'ft'");
                        accept = true;
                    }
                }
            }

            // check for orbitrap
            if(lAnalyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.ORBITRAP)){
                for (String lAllowedInstrumentID : iAllowedInstrumentIDs) {
                    if(lAllowedInstrumentID.toLowerCase().equals("orbitrap")){
                        logger.debug("accept mass analyzer 'orbitrap'");
                        accept = true;
                    }
                }
            }

            // check for iontrap
            if(lAnalyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.IONTRAP)){
                for (String lAllowedInstrumentID : iAllowedInstrumentIDs) {
                    if(lAllowedInstrumentID.toLowerCase().equals("iontrap")){
                        logger.debug("accept mass analyzer 'iontrap'");
                        accept = true;
                    }
                }
            }

            // check for orbitrap
            if(lAnalyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.TOF)){
                for (String lAllowedInstrumentID : iAllowedInstrumentIDs) {
                    if(lAllowedInstrumentID.toLowerCase().equals("tof")){
                        logger.debug("accept mass analyzer 'tof'");
                        accept = true;
                    }
                }
            }

            // check for UNKNOWNN instrument
            if(lAnalyzerData.getAnalyzerFamily().equals(AnalyzerData.ANALYZER_FAMILY.UNKNOWN)){
                for (String lAllowedInstrumentID : iAllowedInstrumentIDs) {
                    if(lAllowedInstrumentID.toLowerCase().equals("unknown")){
                        logger.debug("accept mass analyzer 'unknown'");
                        accept = true;
                    }
                }
            }



        }

        if (!accept) {
            logger.debug("project " + lProjectid + " has non-allowed instruments (" + Joiner.on(",").join(lAnalyzerDataSet) + ")");
            logger.debug("instruments for project " + lProjectid + " are NOT OK");
            return false;
        }

        logger.debug("instruments for project " + lProjectid + " are OK");
        return true;
    }
}
