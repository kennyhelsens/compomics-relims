package com.compomics.relims.model.provider.pride;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.logic.modification.impl.OmssaModificationMarshallerImpl;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.observer.ResultObserver;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is a
 */
public class PrideDataProvider implements DataProvider {

    private static Logger logger = Logger.getLogger(PrideDataProvider.class);
    private ExperimentService iPrideService;

    public PrideDataProvider() {
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
        iPrideService = (ExperimentService) lContext.getBean("experimentService");
    }

    public long getNumberOfSpectraForProject(long aProjectID) {
        return iPrideService.getNumberOfSpectra("" + aProjectID);
    }

    public Set<AnalyzerData> getInstrumentsForProject(long aProjectID) {
        AnalyzerData lAnalyzerData = iPrideService.getAnalyzerData(String.valueOf(aProjectID));
        HashSet<AnalyzerData> lResults = Sets.newHashSet();
        lResults.add(lAnalyzerData);
        return lResults;
    }

    public HashSet<String> getProteinAccessionsForProject(long aProjectID) {
        return Sets.newHashSet(iPrideService.getProteinAccessions("" + aProjectID));
    }

    public long getNumberOfPeptidesForProject(long aProjectID) {
        return iPrideService.getNumberOfPeptides("" + aProjectID);
    }

    public File getSpectraForProject(long aProjectID) throws IOException {
        return iPrideService.getSpectrumCacheAsMgfFile("" + aProjectID, false);
    }

    public long getNumberOfSearchesForProject(long aProjectid) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public RelimsProjectBean buildProjectBean(long aProjectid) {

        logger.info(String.format("retrieving all information for project %s", aProjectid));
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();


        // Helper method to load al the Spectra from Pride
        loadSpectraFromPride(aProjectid);


        RelimsProjectBean lRelimsProjectBean = new RelimsProjectBean();
        lRelimsProjectBean.setProjectID((int) aProjectid);

        Set<Modification> lModificationSet = Sets.newHashSet();


        logger.debug("estimating PTMs via inspecting the modified_sequence values of the PSMs");
        ResultObserver.sendHeartBeat();
        // Do not run PRIDE asap automatic, but retrieve the PTMs from the modified sequence values.
        ModificationService lModificationService = (ModificationService) lContext.getBean("modificationService");

        lModificationSet = lModificationService.loadExperimentModifications(aProjectid);
        for (Modification lModification : lModificationSet) {
            logger.debug(String.format("Resolved PTM '%s' with mass '%f' from modified sequence", lModification.getName(), lModification.getMassShift()));
        }

        if (RelimsProperties.appendPrideAsapAutomatic()) {
            logger.debug("estimating PTMs via Pride-asap");
            // Run PRIDE asap automatic mode
            PrideSpectrumAnnotator lSpectrumAnnotator;
            lSpectrumAnnotator = (PrideSpectrumAnnotator) lContext.getBean("prideSpectrumAnnotator");
            lSpectrumAnnotator.annotate(String.valueOf(aProjectid));

            Map<Modification, Integer> lPrideAsapModificationsMap = lModificationService.getUsedModifications(lSpectrumAnnotator.getSpectrumAnnotatorResult());

            Set<Modification>lPrideAsapModifications = lPrideAsapModificationsMap.keySet();

            for (Modification lPrideAsapModification : lPrideAsapModifications) {
                Modification lAsapModification = lPrideAsapModification;

                if (lModificationSet.add(lAsapModification) == true) {
                    logger.debug(String.format("Pride-ASAP additionally resolved ", lAsapModification.getName()));
                }
            }
        }


        OmssaModificationMarshaller marshaller = new OmssaModificationMarshallerImpl();
        UserModCollection lUserModCollection = marshaller.marshallModifications(lModificationSet);

        lRelimsProjectBean.setStandardModificationList(lUserModCollection);


        // Set precursor and fragment errors
        Set<AnalyzerData> lAnalyzerDataSet = getInstrumentsForProject(aProjectid);

        double lPrecursorError = 0.0;
        double lFragmentError = 0.0;

        for (AnalyzerData lNext : lAnalyzerDataSet) {

            Double lNextPrecursorMassError = lNext.getPrecursorMassError();
            if (lPrecursorError > 0.0 && lNextPrecursorMassError != lPrecursorError) {
                throw new RelimsException("There are multiple Mass Analyzers with different Precursor Mass errors for this project!!");
            }
            lPrecursorError = lNextPrecursorMassError;

            Double lNextFragmentMassError = lNext.getFragmentMassError();
            if (lFragmentError > 0.0 && lFragmentError == lNextFragmentMassError) {
                throw new RelimsException("There are multiple Mass Analyzers with different Fragment Mass errors for this project!!");
            }
            lFragmentError = lNextFragmentMassError;

        }

        lRelimsProjectBean.setPrecursorError(lPrecursorError);
        lRelimsProjectBean.setFragmentError(lFragmentError);


        return lRelimsProjectBean;
    }


    private void loadSpectraFromPride(long aProjectid) {
        // Try maximum three times to get the spectra. Otherwise fail.
        int i = 0;
        int max = 3;
        boolean spectraRetrieved = false;
        while (i < max && !spectraRetrieved) {

            try {
                iPrideService.buildSpectrumCacheForExperiment("" + aProjectid);
                spectraRetrieved = true;

                if (false) {
                    // Hacky code, otherwise we cannot catch the SQLException ...
                    throw new SQLException();
                }

            } catch (SQLException e) {
                logger.debug(String.format("catched sqlexception while loading spectrum from Pride (attempt %s/%s)", (i + 1), 3));
                if (e.getMessage().contains("Already closed")) {
                    //retry!!
                } else {
                    throw new RelimsException(e);
                }
            }
            i++;
        }

        if (!spectraRetrieved) {
            String lMessage = String.format("Failed to retrieve spectra for project %s", aProjectid);
            logger.debug(lMessage);
            throw new RelimsException(lMessage);
        }

        logger.debug("");
    }

    public String toString() {
        return "PrideDataProvider";
    }

}
