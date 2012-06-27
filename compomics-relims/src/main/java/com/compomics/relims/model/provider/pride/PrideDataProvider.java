package com.compomics.relims.model.provider.pride;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.modification.OmssaModiciationMarshaller;
import com.compomics.pride_asa_pipeline.modification.impl.OmssaModificationMarshallerImpl;
import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.service.ModificationService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
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

    public PrideDataProvider() {
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
        iPrideService = (ExperimentService) lContext.getBean("experimentService");
        iModificationService = (ModificationService) lContext.getBean("modificationService");
    }

    public long getNumberOfSpectraForProject(long aProjectID) {
        return iPrideService.getNumberOfSpectra("" + aProjectID);
    }

    public HashSet<Integer> getInstrumentsForProject(long aProjectID) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public HashSet<String> getProteinAccessionsForProject(long aProjectID) {
        return Sets.newHashSet(iPrideService.getProteinAccessions("" + aProjectID));
    }

    public long getNumberOfPeptidesForProject(long aProjectID) {
        return iPrideService.getNumberOfPeptides("" + aProjectID);
    }

    public File getSpectraForProject(long aProjectID) throws IOException {
        return iPrideService.getSpectraAsMgfFile("" + aProjectID);
    }

    public long getNumberOfSearchesForProject(long aProjectid) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public RelimsProjectBean buildProjectBean(long aProjectid) {

        RelimsProjectBean lRelimsProjectBean = new RelimsProjectBean();
        lRelimsProjectBean.setProjectID((int) aProjectid);

        Set<Modification> lModificationSet = iModificationService.loadExperimentModifications(aProjectid);
        OmssaModiciationMarshaller marshaller = new OmssaModificationMarshallerImpl();
        UserModCollection lUserModCollection = marshaller.marshallModifications(lModificationSet);


        lRelimsProjectBean.setStandardModificationList(lUserModCollection);

        return lRelimsProjectBean;

    }

    public String toString() {
        return "PrideDataProvider";
    }

}
