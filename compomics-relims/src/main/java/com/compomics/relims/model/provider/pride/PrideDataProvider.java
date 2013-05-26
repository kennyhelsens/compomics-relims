package com.compomics.relims.model.provider.pride;

import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.pride_asa_pipeline.logic.PrideSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.logic.PrideXmlSpectrumAnnotator;
import com.compomics.pride_asa_pipeline.logic.modification.OmssaModificationMarshaller;
import com.compomics.pride_asa_pipeline.logic.modification.impl.OmssaModificationMarshallerImpl;
import com.compomics.pride_asa_pipeline.model.AnalyzerData;
import com.compomics.pride_asa_pipeline.model.Modification;
import com.compomics.pride_asa_pipeline.service.DbExperimentService;
import com.compomics.pride_asa_pipeline.service.DbModificationService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.compomics.pridexmltomgfconverter.tools.PrideXMLToMGFConverter;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.filemanager.FileManager;
import com.compomics.relims.manager.processmanager.processguard.RelimsException;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.google.common.collect.Sets;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

/**
 * This class is a
 */
public class PrideDataProvider implements DataProvider {

    private static Logger logger = Logger.getLogger(PrideDataProvider.class);
    private DbExperimentService iPrideService;
    private ProgressManager progressManager = ProgressManager.getInstance();
    private PrideXMLToMGFConverter prideXMLConverter;
    private FileManager fileGrabber = FileManager.getInstance();
    private ApplicationContext applicationContext = ApplicationContextProvider.getInstance().getApplicationContext();

    public PrideDataProvider() {
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
        iPrideService = (DbExperimentService) lContext.getBean("dbExperimentService");
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

    /*  public File getSpectraForProjectOLD(long aProjectID) throws IOException {
     return iPrideService.getSpectrumCacheAsMgfFile("" + aProjectID, false);
     }*/
    public long getNumberOfSearchesForProject(long aProjectid) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    @Override
    public File getSpectraForProject(long aProjectid) throws IOException {
        File destinationFile = null;
        File MGFFile = null;
        File returningFile = null;
        try {
            // MAKE AN MGF FILE
            destinationFile = new File(ProcessVariableManager.getResultsFolder() + "/" + aProjectid + ".mgf");
            //Save the MGF file in the resultFolder               
            if (destinationFile.exists()) {
                logger.info("Saving mgf file to : " + destinationFile.getAbsolutePath().toString());
                returningFile = destinationFile;
            } else {
                // System.out.println("Could not locate : " + destinationFile.getAbsolutePath().toString());
                logger.info("Returning pride-generated-mgf file");
                MGFFile = getMGFFromPride(aProjectid);
                returningFile = MGFFile;
            }
        } catch (Exception e) {
            logger.error("The Pride provider could not load an MGF-file.");
            logger.error(e);
            //e.printStackTrace();
            ProgressManager.setEndState(Checkpoint.PRIDEFAILURE);
            return returningFile;
        } finally {
            return returningFile;
        }
    }

    @Override
    public RelimsProjectBean buildProjectBean(long aProjectid) {
        logger.debug(String.format("retrieving searchparameters and modifications for project %s", aProjectid));
        logger.debug("warning, if this is the first time the project is run, it might take a considerable amount of time to retrieve the suggested searchparameters...");
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();

        RelimsProjectBean lRelimsProjectBean = new RelimsProjectBean(aProjectid);

        Set<Modification> lModificationSet = Sets.newHashSet();

        logger.debug("estimating PTMs via inspecting the modified_sequence values of the PSMs");

        // Do not run PRIDE asap automatic, but retrieve the PTMs from the modified sequence values.

        DbModificationService lModificationService = (DbModificationService) lContext.getBean("dbModificationService");
        lModificationSet = lModificationService.loadExperimentModifications(aProjectid);

        for (Modification lModification : lModificationSet) {
            logger.debug(String.format("Resolved PTM '%s' with mass '%f' from modified sequence", lModification.getName(), lModification.getMassShift()));
        }

        if (RelimsProperties.appendPrideAsapAutomatic()) {
            logger.debug("estimating PTMs via Pride-asap");
            // Run PRIDE asap automatic mode
            PrideSpectrumAnnotator lSpectrumAnnotator;
            lSpectrumAnnotator = (PrideSpectrumAnnotator) lContext.getBean("prideSpectrumAnnotator");
            try {
                lSpectrumAnnotator.initIdentifications(String.valueOf(aProjectid));
                if (lSpectrumAnnotator.getIdentifications().getCompleteIdentifications().isEmpty()) {
                    //ProgressManager.setState(Checkpoint.PRIDEFAILURE);
                    logger.error("Pride found no usefull identifications.");
                }
                lSpectrumAnnotator.annotate(String.valueOf(aProjectid));
            } catch (Exception e) {
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
            //*100 = conversion to PPM from da
            lRelimsProjectBean.setPrecursorError(lPrecursorError);
            lRelimsProjectBean.setFragmentError(lFragmentError);
        }
        logger.debug("Retrieved searchparameters from remote Pride");
        PrideSpectrumAnnotator lSpectrumAnnotator;
        lSpectrumAnnotator = (PrideSpectrumAnnotator) applicationContext.getBean("prideSpectrumAnnotator");
        lSpectrumAnnotator.clearTmpResources();
        return lRelimsProjectBean;
    }

    private File getMGFFromPride(long aProjectid) {
        // Try maximum three times to get the spectra. Otherwise fail.
        int i = 0;
        int max = 3;
        File MGFFile = new File(ProcessVariableManager.getResultsFolder() + "/" + aProjectid + ".mgf");
        boolean spectraRetrieved = false;
        while (i < max && !spectraRetrieved) {
            try {
                logger.debug("Getting mgf from pride: attempt " + i + "from " + max);
                iPrideService.getSpectraAsMgfFile("" + aProjectid, MGFFile, true);
                if (MGFFile.length() > 0) {
                    spectraRetrieved = true;
                    logger.debug("Succeeded in retrieving MGF file from pride");
                } else {
                    logger.error("The provider's MGF is empty...");
                    progressManager.setState(Checkpoint.PRIDEFAILURE);
                    spectraRetrieved = false;
                }
                if (!spectraRetrieved) {
                    // Hacky code, otherwise we cannot catch the SQLException ...
                    throw new SQLException("Error retrieving data from database");
                }
            } catch (Exception e) {
                if (e instanceof SQLException) {
                    logger.error(String.format("Encountered sqlexception while loading spectrum from Pride (attempt %s/%s)", (i + 1), 3));
                } else if (e instanceof IOException) {
                    logger.error("An error occurred during the handling of files...");
                    logger.error(e);
                } else {
                    logger.error("An error has occurred : " + e.getCause());
                }
                logger.debug("Failed to retrieve spectra ( attempt :" + i + " / " + max + " )");
            } finally {
                i++;
            }
        }

        if (!spectraRetrieved) {
            String lMessage = String.format("Failed to retrieve spectra for project %s", aProjectid);
            logger.debug(lMessage);
            throw new RelimsException(lMessage);
        }
        return MGFFile;

    }

    public String toString() {
        return "PrideDataProvider from Pride Web";
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

    @Override
    public void clearResources() {
        //TODO code for cleanup operations
    }
}
