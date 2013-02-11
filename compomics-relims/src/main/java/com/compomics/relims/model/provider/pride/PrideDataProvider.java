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
import com.compomics.pridexmltomgfconverter.errors.ConversionError;
import com.compomics.pridexmltomgfconverter.tools.PrideXMLToMGFConverter;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.filemanager.FileGrabber;
import com.compomics.relims.conf.RelimsVariableManager;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.observer.Checkpoint;
import com.compomics.relims.observer.ProgressManager;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;

/**
 * This class is a
 */
public class PrideDataProvider implements DataProvider {

    private static Logger logger = Logger.getLogger(PrideDataProvider.class);
    private ExperimentService iPrideService;
    RelimsProjectBean iRelimsProjectBean = new RelimsProjectBean();
    private ProgressManager progressManager = ProgressManager.getInstance();
    private PrideXMLToMGFConverter prideXMLConverter;
    private FileGrabber fileGrabber = FileGrabber.getInstance();

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

    public File getSpectraForProjectOLD(long aProjectID) throws IOException {
        return iPrideService.getSpectrumCacheAsMgfFile("" + aProjectID, false);
    }

    public long getNumberOfSearchesForProject(long aProjectid) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    @Override
    public File getSpectraForProject(long aProjectid) throws IOException {
        if (!RelimsProperties.getPrideMGFSource().equals("prideXML")) {
            System.out.println("Getting spectra from pipeline");
            return getSpectraForProjectFromRemotePride(aProjectid);
        } else {
            System.out.println("Getting spectra from pride.XML");
            File returningFile = getSpectraForProjectFromPrideXML(aProjectid);
            if (returningFile == null) {
                System.out.println("Attempting to retrieve data from external source ");
                returningFile = getSpectraForProjectFromRemotePride(aProjectid);
                if (returningFile == null) {
                    System.out.println("Could not convert " + aProjectid);
                    ProgressManager.setEndState(Checkpoint.PRIDEFAILURE);
                    return null;
                }else{
                    return returningFile;
                }
            }
        }
        return null;
    }

    public File getSpectraForProjectFromRemotePride(long aProjectid) throws IOException {
        File destinationFile = null;
        File MGFFile = null;
        File returningFile = null;
        try {
            // MAKE AN MGF FILE
            destinationFile = new File(RelimsProperties.getWorkSpace().getAbsolutePath().toString() + "/" + aProjectid + ".mgf");
            //Save the MGF file in the resultFolder               
            if (destinationFile.exists()) {
                System.out.println("Saving mgf file to : " + destinationFile.getAbsolutePath().toString());
                returningFile = destinationFile;
            } else {
                System.out.println("Could not locate : " + destinationFile.getAbsolutePath().toString());
                System.out.println("Returning pride-generated-mgf file");
                MGFFile = getMGFFromPride(aProjectid);
                returningFile = MGFFile;
                FileUtils.copyFile(MGFFile, destinationFile);
            }
        } catch (Exception e) {
            System.out.println("The Pride provider could not load an MGF-file.");
            logger.error(e);
            //e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED, e);
            System.out.println("Pride caused a failure :" + e);
            return returningFile;
        } finally {
            return returningFile;
        }
    }

    public File getSpectraForProjectFromPrideXML(long aProjectid) throws IOException {
        prideXMLConverter = PrideXMLToMGFConverter.getInstance();
        File destinationFile;
        File MGFFile;
        File returningFile = null;
        File prideXMLFile;
        List<ConversionError> errorList;
        try {
            //Get the XML file from the repository...
            prideXMLFile = fileGrabber.getPrideXML(aProjectid);
            // MAKE AN MGF FILE
            if (prideXMLFile != null) {
                destinationFile = new File(RelimsProperties.getWorkSpace().getAbsolutePath().toString() + "/" + aProjectid + ".mgf");
                //Save the MGF file in the resultFolder               
                MGFFile = prideXMLConverter.extractMGFFromPrideXML(prideXMLFile, destinationFile, true);
                //Get the errorList and store it in the results later
                errorList = prideXMLConverter.getErrorList();
                RelimsVariableManager.setConversionErrorList(errorList);
                if (MGFFile != null) {
                    returningFile = MGFFile;
                } else {
                    System.out.println("The Pride provider could not load an MGF-file.");
                    // try to get it from other source = good idea?

                }
                //   FileUtils.copyFile(MGFFile, destinationFile);
            }
        } catch (Exception e) {

            logger.error(e);
            //  e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED, e);
            System.out.println("Pride caused a failure :" + e);
        } finally {
            return returningFile;
        }
    }

    @Override
    public RelimsProjectBean buildProjectBean(long aProjectid) {

        System.out.println(String.format("retrieving all information for project %s", aProjectid));
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();

        // Helpermethod to load al the Spectra from Pride
        // loadSpectraFromPride(aProjectid);

        RelimsProjectBean lRelimsProjectBean = new RelimsProjectBean();
        lRelimsProjectBean.setProjectID((int) aProjectid);

        Set<Modification> lModificationSet = Sets.newHashSet();

        System.out.println("estimating PTMs via inspecting the modified_sequence values of the PSMs");
        // Do not run PRIDE asap automatic, but retrieve the PTMs from the modified sequence values.

        ModificationService lModificationService = (ModificationService) lContext.getBean("modificationService");
        lModificationSet = lModificationService.loadExperimentModifications(aProjectid);

        for (Modification lModification : lModificationSet) {
            System.out.println(String.format("Resolved PTM '%s' with mass '%f' from modified sequence", lModification.getName(), lModification.getMassShift()));
        }

        if (RelimsProperties.appendPrideAsapAutomatic()) {
            System.out.println("estimating PTMs via Pride-asap");
            // Run PRIDE asap automatic mode
            PrideSpectrumAnnotator lSpectrumAnnotator;
            lSpectrumAnnotator = (PrideSpectrumAnnotator) lContext.getBean("prideSpectrumAnnotator");
            try {
                lSpectrumAnnotator.initIdentifications(String.valueOf(aProjectid));
                if (lSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
                    ProgressManager.setState(Checkpoint.PRIDEFAILURE);
                    System.out.println("Pride caused a failure : no usefull identifications were found");
                } else {
                    lSpectrumAnnotator.annotate(String.valueOf(aProjectid));
                }
            } catch (Exception e) {
                logger.error(e.getMessage());
                logger.error(e.getCause());
                ProgressManager.setState(Checkpoint.PRIDEFAILURE, e);
            }
            Map<Modification, Integer> lPrideAsapModificationsMap = lModificationService.getUsedModifications(lSpectrumAnnotator.getSpectrumAnnotatorResult());

            Set<Modification> lPrideAsapModifications = lPrideAsapModificationsMap.keySet();

            for (Modification lPrideAsapModification : lPrideAsapModifications) {
                Modification lAsapModification = lPrideAsapModification;

                if (lModificationSet.add(lAsapModification) == true) {
                    System.out.println(String.format("Pride-ASAP additionally resolved ", lAsapModification.getName()));
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

        return lRelimsProjectBean;
    }

    private File getMGFFromPride(long aProjectid) {
        // Try maximum three times to get the spectra. Otherwise fail.
        int i = 0;
        int max = 3;
        File MGF = null;
        boolean spectraRetrieved = false;
        while (i < max && !spectraRetrieved) {

            try {
                MGF = iPrideService.getSpectrumCacheAsMgfFile("" + aProjectid, true);
                if (MGF.length() > 0) {
                    spectraRetrieved = true;
                } else {
                    spectraRetrieved = false;
                }
                if (!spectraRetrieved) {
                    // Hacky code, otherwise we cannot catch the SQLException ...
                    throw new SQLException("Error retrieving data from database");
                }

            } catch (Exception e) {
                logger.error(String.format("Encountered sqlexception while loading spectrum from Pride (attempt %s/%s)", (i + 1), 3));
                if (e.getMessage().contains("Already closed") || e.getMessage().contains("Communications link failure")) {
                    System.out.println("Failed to retrieve spectra ( attempt :" + i + " / " + max + " )");
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
        return MGF;

    }

    public String toString() {
        return "PrideDataProvider";
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
