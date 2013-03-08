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
import com.compomics.relims.manager.variablemanager.RelimsVariableManager;
import com.compomics.relims.manager.processmanager.processguard.RelimsException;
import com.compomics.relims.manager.filemanager.FileManager;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * This class is a
 */
public class PrideXMLDataProvider implements DataProvider {

    private static Logger logger = Logger.getLogger(PrideXMLDataProvider.class);
    private PrideXmlExperimentService iPrideService;
    RelimsProjectBean iRelimsProjectBean = new RelimsProjectBean();
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
        AnalyzerData lAnalyzerData = iPrideService.getAnalyzerData();
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
                destinationFile = new File(RelimsVariableManager.getResultsFolder() + "/" + aProjectid + ".mgf");
                //Save the MGF file in the resultFolder          
                errorList = iPrideService.getSpectraAsMgf(prideXMLFile, destinationFile);
                //Get the errorList and store it in the results later
                RelimsVariableManager.setConversionErrorList(errorList);
                if (destinationFile == null) {
                    logger.error("The Pride provider could not load an MGF-file.");
                    // try to get it from other source = good idea?
                }
                for (ConversionError anError : errorList) {
                    logger.error(anError.getDescription());
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
        logger.setLevel(Level.ERROR);


        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();

        RelimsProjectBean lRelimsProjectBean = new RelimsProjectBean();

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

                for (Modification lModification : lModificationSet) {
                    logger.debug(String.format("Resolved PTM '%s' with mass '%f' from modified sequence", lModification.getName(), lModification.getMassShift()));
                }
                lModificationSet = lModificationService.loadExperimentModifications();


                lSpectrumAnnotator.annotate(xmlFile);
            } catch (Exception e) {
                System.out.println(e);
                logger.error(e.getMessage());
                logger.error(e.getCause());
                ProgressManager.setState(Checkpoint.PRIDEFAILURE, e);
            }

            Map<Modification, Integer> lPrideAsapModificationsMap = lModificationService.getUsedModifications(lSpectrumAnnotator.getSpectrumAnnotatorResult());
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
        }
        logger.setLevel(Level.DEBUG);
        logger.debug("Retrieved searchparameters from remote Pride");
        // Clean MGF resources after project success
        PrideXmlSpectrumAnnotator lSpectrumAnnotator;
        lSpectrumAnnotator = (PrideXmlSpectrumAnnotator) applicationContext.getBean("prideXmlSpectrumAnnotator");

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

    private void appendIfValuable(String type, String experimentID) {
        BufferedWriter out = null;
        try {
            String directory = RelimsProperties.getRepositoryPath() + "/PRIDE/";
            File outputFile = new File(directory + type + ".txt");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileWriter fstream = new FileWriter(outputFile, true);
            out = new BufferedWriter(fstream);

            out.write(experimentID);
            out.newLine();
            out.close();
        } catch (Exception e) {//Catch exception if any
        } finally {
            if (out != null) {
                out = null;
            }
        }
    }
}
