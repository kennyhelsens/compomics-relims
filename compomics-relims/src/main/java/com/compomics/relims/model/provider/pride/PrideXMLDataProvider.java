package com.compomics.relims.model.provider.pride;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.logic.PrideXmlSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.logic.modification.impl.OmssaModificationMarshallerImpl;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.service.PrideXmlExperimentService;
import com.compomics.pride_asa_pipeline.service.PrideXmlModificationService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.filemanager.FileManager;
import com.compomics.relims.manager.processmanager.processguard.RelimsException;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.google.common.collect.Sets;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class is a
 */
public class PrideXMLDataProvider implements DataProvider {

    private static Logger logger = Logger.getLogger(PrideXMLDataProvider.class);
    private PrideXmlExperimentService iPrideService;
    private ProgressManager progressManager = ProgressManager.getInstance();
    private FileManager fileGrabber = FileManager.getInstance();
    private ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();
    private PrideXmlSpectrumAnnotator lSpectrumAnnotator;

    public PrideXMLDataProvider() {
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
        iPrideService = (PrideXmlExperimentService) lContext.getBean("prideXmlExperimentService");
    }

    public void clearResources() {
        lSpectrumAnnotator.clearTmpResources();
    }

    public long getNumberOfSpectraForProject(long aProjectID) {
        return iPrideService.getNumberOfSpectra();
    }

    public Set<AnalyzerData> getInstrumentsForProject(long aProjectID) {
        AnalyzerData lAnalyzerData;
        try {
            lAnalyzerData = iPrideService.getAnalyzerData();
        } catch (NullPointerException e) {
            //set DEFAULT analyzerdata TODO MAKE THIS RELIMSPROPERTY
            lAnalyzerData = new AnalyzerData(1.0, 1.0, AnalyzerData.ANALYZER_FAMILY.ORBITRAP);
        }
        HashSet<AnalyzerData> lResults = Sets.newHashSet();
        lResults.add(lAnalyzerData);
        return lResults;
    }

    public HashSet<String> getProteinAccessionsForProject(long aProjectID) {
        return (HashSet) iPrideService.getProteinAccessions();
    }

    public long getNumberOfPeptidesForProject(long aProjectID) {
        return iPrideService.getNumberOfPeptides();
    }

    /*  public File getSpectraForProjectOLD(long aProjectID) throws IOException {
     return iPrideService.getSpectrumCacheAsMgfFile("" + aProjectID, false);
     }*/
    public long getNumberOfSearchesForProject(long aProjectid) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    @Override
    public File getSpectraForProject(long aProjectid) throws IOException {
        File destinationFile = null;
        File prideXMLFile;
        List<ConversionError> errorList;
        try {
            //Get the XML file from the repository...
            prideXMLFile = fileGrabber.getPrideXML(aProjectid);
            // MAKE AN MGF FILE
            if (prideXMLFile != null) {
                destinationFile = new File(RelimsProperties.getWorkSpace().getAbsolutePath() + "/" + aProjectid + ".mgf");
                //Save the MGF file in the resultFolder       
                errorList = iPrideService.getSpectraAsMgf(prideXMLFile, destinationFile);
                //Get the errorList and store it in the results later
                ProcessVariableManager.setConversionErrorList(errorList);
                if (destinationFile == null) {
                    logger.error("The Pride provider could not load an MGF-file.");
                    // try to get it from other source = good idea?
                }
            }
        } catch (Exception e) {
            logger.error(e);
            //  e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED, e);
            logger.error("Pride caused a failure :" + e);
        } finally {
            return destinationFile;
        }
    }

    @Override
    public RelimsProjectBean buildProjectBean(long aProjectid) {
        logger.setLevel(Level.ALL);
        logger.debug(String.format("retrieving searchparameters and modifications for project %s", aProjectid));
        logger.debug("warning, if this is the first time the project is run, it might take a considerable amount of time to retrieve the suggested searchparameters...");

        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();

        RelimsProjectBean lRelimsProjectBean = new RelimsProjectBean(aProjectid);

        if (RelimsProperties.appendPrideAsapAutomatic()) {
            File xmlFile = fileGrabber.getPrideXML(aProjectid);
            logger.debug("estimating PTMs via Pride-asap");
            // Run PRIDE asap automatic mode

            lSpectrumAnnotator = (PrideXmlSpectrumAnnotator) lContext.getBean("prideXmlSpectrumAnnotator");
            PrideXmlModificationService lModificationService = (PrideXmlModificationService) lContext.getBean("prideXmlModificationService");
            lRelimsProjectBean.setProjectID((int) aProjectid);
            Set<Modification> lModificationSet = Sets.newHashSet();

            logger.debug("estimating PTMs via inspecting the modified_sequence values of the PSMs");

            // Do not run PRIDE asap automatic, but retrieve the PTMs from the modified sequence values.

            try {
                lSpectrumAnnotator.initIdentifications(xmlFile);

                if (lSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
                    //ProgressManager.setState(Checkpoint.PRIDEFAILURE);
                    logger.error("Pride found no usefull identifications.");
                }
                lModificationSet = lModificationService.loadExperimentModifications();
                for (Modification lModification : lModificationSet) {
                    logger.debug(String.format("Resolved PTM '%s' with mass '%f' from modified sequence", lModification.getName(), lModification.getMassShift()));
                    //PUT THEM IN THE PTM FACTORY AS NEW PTMS HERE !!!!
                }
                lSpectrumAnnotator.annotate(xmlFile);
            } catch (Exception e) {
                logger.error("Could not initiate spectrumAnnotator : no identifications found");
            }
            Map<Modification, Integer> lPrideAsapModificationsMap = new HashMap<Modification, Integer>();
            try {
                lPrideAsapModificationsMap = lModificationService.getUsedModifications(lSpectrumAnnotator.getSpectrumAnnotatorResult());
            } catch (NullPointerException e) {
                logger.error("Pride-asa did not resolve find modifications");
            }
            Set<Modification> lPrideAsapModifications = lPrideAsapModificationsMap.keySet();
            logger.debug("Pride-ASAP additionally resolved :");
            for (Modification lPrideAsapModification : lPrideAsapModifications) {
                Modification lAsapModification = lPrideAsapModification;
                if (lModificationSet.add(lAsapModification) == true) {
                    logger.debug(lAsapModification.getName());
                }
            }
            OmssaModificationMarshaller marshaller = new OmssaModificationMarshallerImpl();
            UserModCollection lUserModCollection = marshaller.marshallModifications(lModificationSet);
            try {
                lUserModCollection.build(RelimsProperties.getSearchGuiUserModFile());
            } catch (IOException ex) {
                logger.error(ex);
            }
            lRelimsProjectBean.setStandardModificationList(lUserModCollection);

            // Set precursor and fragment errors
            Set<AnalyzerData> lAnalyzerDataSet = getInstrumentsForProject(aProjectid);
            // get the estimated chargeset from pride !
            lRelimsProjectBean.setCharges(lSpectrumAnnotator.getConsideredChargeStates());

            double lPrecursorError = 0.0;
            double lFragmentError = 0.0;
            try {

                for (AnalyzerData lNext : lAnalyzerDataSet) {

                    logger.warn(lNext.getAnalyzerFamily().toString()
                            + " (precursor error : " + lNext.getPrecursorMassError()
                            + " , fragment error" + lNext.getFragmentMassError() + ")");
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
            } catch (NullPointerException e) {
                logger.error("A nullpointer exception occurred, setting precursorAcc and fragmentIonAcc to default");
                lPrecursorError = 1.0;
                lFragmentError = 1.0;
            }

            lRelimsProjectBean.setPrecursorError(lPrecursorError);
            //26/03/2013 - setting the fragmentErrorType is not possible ---> need to keep it in da untill resolved
            lRelimsProjectBean.setFragmentError(lFragmentError);
        }
        logger.setLevel(Level.DEBUG);
        logger.debug("Retrieved searchparameters from Pride xml");

        return lRelimsProjectBean;
    }

    public String toString() {
        return "PrideDataProvider for PrideXML";
    }

    @Override
    public boolean isProjectValuable(String experimentID) {
        return true;
        /*  List<Identification> idlist = iPrideService.loadExperimentIdentifications(experimentID).getCompleteIdentifications();
  
         if (idlist.isEmpty() || idlist == null) {
         System.out.println("This project does not contain usefull according to the pipeline");
         System.out.println("Aborting project...");
         //return false;
         return true;
         } else {
         System.out.println("This project contains usefull identifications");
         appendIfValuable("Usefull", experimentID);
         return true;
         }*/
    }
}
