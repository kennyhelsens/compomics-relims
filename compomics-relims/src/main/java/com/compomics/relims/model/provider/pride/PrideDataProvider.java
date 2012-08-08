package com.compomics.relims.model.provider.pride;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.modification.OmssaModiciationMarshaller;
import com.compomics.pride_asa_pipeline.modification.impl.OmssaModificationMarshallerImpl;
import com.compomics.pride_asa_pipeline.pipeline.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is a
 */
public class PrideDataProvider implements DataProvider {

    private static Logger logger = Logger.getLogger(PrideDataProvider.class);
    private ExperimentService iPrideService;
    private ModificationService iModificationService;
    PrideSpectrumAnnotator iAnnotator;

    public PrideDataProvider() {
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
        iPrideService = (ExperimentService) lContext.getBean("experimentService");
        iModificationService = (ModificationService) lContext.getBean("modificationService");
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

        // We will need to cache all spectra in order to run the asap pipeline.
        // So lets cache them here.
        iPrideService.buildSpectrumCacheForExperiment("" + aProjectid);


        RelimsProjectBean lRelimsProjectBean = new RelimsProjectBean();
        lRelimsProjectBean.setProjectID((int) aProjectid);

        Set<Modification> lModificationSet = null;


        logger.debug("estimating PTMs via inspecting the modified_sequence values of the PSMs");
        // Do not run PRIDE asap automatic, but retrieve the PTMs from the modified sequence values.
        lModificationSet = iModificationService.loadExperimentModifications(aProjectid);
        for (Modification lModification : lModificationSet) {
            logger.debug(String.format("Resolved PTM '%s' with mass '%f' from modified sequence", lModification.getName(), lModification.getMassShift()));
        }

        if (RelimsProperties.appendPrideAsapAutomatic()) {
            logger.debug("estimating PTMs via Pride-asap");
            // Run PRIDE asap automatic mode
            ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
            iAnnotator = (PrideSpectrumAnnotator) lContext.getBean("prideSpectrumAnnotator");
            iAnnotator.annotate(String.valueOf(aProjectid));

            Set<Modification> lPrideAsapModifications = iModificationService.getUsedModifications(iAnnotator.getSpectrumAnnotatorResult());
            for (Modification lPrideAsapModification : lPrideAsapModifications) {
                Modification lAsapModification = lPrideAsapModification;

                if(lModificationSet.add(lAsapModification) == true){
                    logger.debug(String.format("Pride-ASAP additionally resolved ", lAsapModification.getName()));
                }
            }
        }

        OmssaModiciationMarshaller marshaller = new OmssaModificationMarshallerImpl();
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

    public String toString() {
        return "PrideDataProvider";
    }

}
