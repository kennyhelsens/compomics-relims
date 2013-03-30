package com.compomics.relims.model.beans;

import com.compomics.relims.concurrent.Command;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.filemanager.FileManager;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * This class is a
 */
public class PeptideShakerJobBean {

    /**
     * a plain logger
     */
    private static Logger logger = Logger.getLogger(PeptideShakerJobBean.class);
    /**
     * parameters for the peptideshaker command line
     */
    private boolean ascore = false;
    private File outFolder = null;
    private File searchGUIResultsFolder = null;
    private double pepfdr = 1.0;
    private double protfdr = 1.0;
    private double psmfdr = 1.0;
    private String exp = "default_exp";
    private String sample = "default_sample";
    private String idFilesFolder = null;
    private String spectrumFolder = null;
    private final long projectId;
    private StringBuilder PSCommandLine;
    private File searchParametersFile;
    private File spectra;
    private File jobDirectory;
    private List<String> identifications;
    private ArrayList<String> psCommandLine;
    private String identificationFiles;
    private final File resultFolder;
    private double maxPrecursorError = 10.0;

    public PeptideShakerJobBean(long projectId, File searchParameters, File spectra, File searchGuiResultsFolder) {
        logger.debug("Collecting PeptideShaker parameters");
        this.projectId = projectId;
        this.searchParametersFile = searchParameters;
        this.spectra = spectra;
        this.jobDirectory = new File(ProcessVariableManager.getResultsFolder());
        this.resultFolder = searchGuiResultsFolder;
        this.identificationFiles = searchGuiResultsFolder.getAbsolutePath() + "/" + projectId + ".omx," + searchGuiResultsFolder.getAbsolutePath() + "/" + projectId + ".t.xml";
        logger.debug("Getting identification files from " + searchGuiResultsFolder.getAbsolutePath());
    }

    public PeptideShakerJobBean(long projectId) {
        this.projectId = projectId;
        this.resultFolder = new File(RelimsProperties.getPeptideShakerResultsFolder());
    }

    public void setMaxPrecursorError(double error) {
        this.maxPrecursorError = error;
    }

    public void setAscore(boolean aAscore) {
        ascore = aAscore;
    }

    public static void setLogger(Logger aLogger) {
        logger = aLogger;
    }

    public void setOutFolder(File aOutFolder) {
        outFolder = aOutFolder;
    }

    public void setPepfdr(double aPepfdr) {
        pepfdr = aPepfdr;
    }

    public void setProtfdr(double aProtfdr) {
        protfdr = aProtfdr;
    }

    public void setPsmfdr(double aPsmfdr) {
        psmfdr = aPsmfdr;
    }

    public void setSearchGUIResultsFolder(File aSearchGUIResultsFolder) {
        searchGUIResultsFolder = aSearchGUIResultsFolder;
    }

    public void setExperimentName(String aExp) {
        exp = aExp;
    }

    public void setIdentificationFiles(String anIdFilesFolder) {
        idFilesFolder = anIdFilesFolder;
    }

    public void setSpectrumFolder(String MGFLocation) {
        spectrumFolder = MGFLocation;
    }

    public void setIdentificationFolder(String aSpectrumFolder) {
        spectrumFolder = aSpectrumFolder;
    }

    public void setSampleName(String aSample) {
        sample = aSample;
    }

    public ArrayList<String> getPeptideShakerCommandString() {
        ArrayList<String> PSCommandLine = new ArrayList<String>();

        //Check if the MGF file is more than 0kb ---> can occur after PRIDE.XML conversion

        if (this.spectra.length() > 0 && this.spectra.exists()) {

            PSCommandLine.clear();
            PSCommandLine.add("java ");
            PSCommandLine.add("-cp ");
            PSCommandLine.add(RelimsProperties.getPeptideShakerArchivePath());
            PSCommandLine.add(" eu.isas.peptideshaker.cmd.PeptideShakerCLI ");
            PSCommandLine.add("-experiment ");
            PSCommandLine.add(ProcessVariableManager.getProjectId() + " ");
            PSCommandLine.add("-sample ");
            PSCommandLine.add("AutoReprocessed " + ProcessVariableManager.getProjectId() + " ");
            PSCommandLine.add("-replicate 1 ");
            PSCommandLine.add("-identification_files ");
            PSCommandLine.add(identificationFiles);
            PSCommandLine.add(" -spectrum_files ");
            PSCommandLine.add(this.spectra.getAbsolutePath().toString());
            PSCommandLine.add(" -search_params ");
            PSCommandLine.add(this.searchParametersFile.getAbsolutePath().toString());
            PSCommandLine.add(" -exclude_unknown_ptms ");
            PSCommandLine.add("0");
            PSCommandLine.add(" -max_precursor_error_type ");
            PSCommandLine.add("1");
            PSCommandLine.add(" -max_precursor_error ");
            PSCommandLine.add("" + maxPrecursorError);
            PSCommandLine.add(" -a_score ");
            PSCommandLine.add("1");
            if (RelimsProperties.getPeptideShakerCPSOutput()) {
                PSCommandLine.add(" -out ");
                PSCommandLine.add(jobDirectory.getAbsolutePath().toString() + "/" + ProcessVariableManager.getProjectId() + ".cps");
            }
            //Required for R
            if (RelimsProperties.getPeptideShakerTSVOutput()) {
                PSCommandLine.add(" -out_txt_1 ");
                PSCommandLine.add(jobDirectory.getAbsolutePath().toString());
            }
            //Requested by Uniprot
            if (RelimsProperties.getPeptideShakerUniprotOutput()) {
                PSCommandLine.add(" -out_txt_2 ");
                PSCommandLine.add(jobDirectory.getAbsolutePath().toString());
            }
            System.err.println("");
            System.err.println("PEPTIDESHAKERCOMMAND");
            for (String aParam : PSCommandLine) {
                System.err.print(aParam);
            }
            System.err.println("");
            return PSCommandLine;
        } else {
            return null;
        }
    }

    public int launch() {

        psCommandLine = getPeptideShakerCommandString();
        if (psCommandLine != null) {
            File peptideShakerFolder = new File(RelimsProperties.getPeptideShakerArchivePath()).getParentFile();
            Command.setWorkFolder(peptideShakerFolder);
            StringBuilder totalCommandLine = new StringBuilder();
            for (String aCmd : psCommandLine) {
                totalCommandLine.append(aCmd);
            }
            logger.debug(totalCommandLine.toString());
            return Command.call(totalCommandLine.toString());
        } else {
            return 1; //System exit value of 1 means a failed process
        }
    }

    public void removeJunk() {
        //omx
        File omxFile = new File(resultFolder.getAbsolutePath() + "/" + projectId + ".omx");
        File xtandemFile = new File(resultFolder.getAbsolutePath() + "/" + projectId + "t.xml");
        File mgfFile = new File(resultFolder.getAbsolutePath() + "/" + projectId + ".mgf");
        FileUtils.deleteQuietly(xtandemFile);
        FileUtils.deleteQuietly(omxFile);
        FileUtils.deleteQuietly(mgfFile);
        logger.info("Removed searchengine result files to reduce foldersize");
    }
}
