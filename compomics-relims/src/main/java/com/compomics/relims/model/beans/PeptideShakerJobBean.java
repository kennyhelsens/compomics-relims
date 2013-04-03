package com.compomics.relims.model.beans;

import com.compomics.relims.concurrent.Command;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.util.experiment.identification.SearchParameters;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

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
    private boolean ascore = true;
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
    private List<String> identifications;
    private String identificationFiles;
    private final File resultFolder;
    private double maxPrecursorError = 10.0;
    private final SearchParameters searchParameters;

    public PeptideShakerJobBean(RelimsProjectBean lRelimsProjectBean) {

        logger.debug("Collecting PeptideShaker parameters");
        this.projectId = lRelimsProjectBean.getProjectID();
        this.searchParametersFile = lRelimsProjectBean.getSearchParamFile();
        this.searchParameters = lRelimsProjectBean.getSearchParameters();
        this.spectra = lRelimsProjectBean.getSpectrumFile();
        this.resultFolder = RelimsProperties.getWorkSpace();
        this.identificationFiles = resultFolder.getAbsolutePath() + "/" + projectId + ".omx," + resultFolder.getAbsolutePath() + "/" + projectId + ".t.xml";
        logger.debug("Getting identification files from " + resultFolder.getAbsolutePath());
    }

    public String findIdentificationFiles() {
        File omxFile = new File(resultFolder.getAbsolutePath() + "/" + projectId + ".omx");
        File xTandemFile = new File(resultFolder.getAbsolutePath() + "/" + projectId + "t.xml");

        if (omxFile.exists() && xTandemFile.exists()) {
            identificationFiles = omxFile.getAbsolutePath() + "," + xTandemFile.getAbsolutePath();
        }
        if (omxFile.exists()) {
            identificationFiles = omxFile.getAbsolutePath();
        } else if (xTandemFile.exists()) {
            identificationFiles = xTandemFile.getAbsolutePath();
        }
        return identificationFiles;
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

    public String getPeptideShakerCommandString() {
        UpdateMaxPrecursorError();
        StringBuilder PSCommandLine = new StringBuilder();

        if (this.spectra.length() > 0 && this.spectra.exists()) {
            PSCommandLine.append("java ");
            PSCommandLine.append("-cp ");
            PSCommandLine.append(RelimsProperties.getPeptideShakerArchivePath());
            PSCommandLine.append(" eu.isas.peptideshaker.cmd.PeptideShakerCLI ");
            PSCommandLine.append("-experiment ");
            PSCommandLine.append(ProcessVariableManager.getProjectId() + " ");
            PSCommandLine.append("-sample ");
            PSCommandLine.append("AutoReprocessed " + ProcessVariableManager.getProjectId() + " ");
            PSCommandLine.append("-replicate 1 ");
            PSCommandLine.append("-identification_files ");
            PSCommandLine.append(findIdentificationFiles());
            PSCommandLine.append(" -spectrum_files ");
            PSCommandLine.append(this.spectra.getAbsolutePath().toString());
            PSCommandLine.append(" -search_params ");
            PSCommandLine.append(this.searchParametersFile.getAbsolutePath().toString());
            PSCommandLine.append(" -exclude_unknown_ptms ");
            PSCommandLine.append("0");
            PSCommandLine.append(" -max_precursor_error_type ");
            PSCommandLine.append("1");
            PSCommandLine.append(" -max_precursor_error ");
            PSCommandLine.append("" + maxPrecursorError);
            PSCommandLine.append(" -a_score ");
            PSCommandLine.append("1");
            if (RelimsProperties.getPeptideShakerCPSOutput()) {
                PSCommandLine.append(" -out ");
                PSCommandLine.append(resultFolder.getAbsolutePath().toString() + "/" + ProcessVariableManager.getProjectId() + ".cps");
            }
            //Required for R
            if (RelimsProperties.getPeptideShakerTSVOutput()) {
                PSCommandLine.append(" -out_txt_1 ");
                PSCommandLine.append(resultFolder.getAbsolutePath().toString());
            }
            //Requested by Uniprot
            if (RelimsProperties.getPeptideShakerUniprotOutput()) {
                PSCommandLine.append(" -out_txt_2 ");
                PSCommandLine.append(resultFolder.getAbsolutePath().toString());
            }
            return PSCommandLine.toString();
        } else {
            return null;
        }
    }

    public int launch() {

        String psCommandLine = getPeptideShakerCommandString();
        if (psCommandLine != null) {
            File peptideShakerFolder = new File(RelimsProperties.getPeptideShakerArchivePath()).getParentFile();
            Command.setWorkFolder(peptideShakerFolder);
            logger.debug("Launching peptideshaker from " + peptideShakerFolder.getAbsolutePath());
            logger.info(psCommandLine);
            return Command.call(psCommandLine);
        } else {
            return 1; //System exit value of 1 means a failed process
        }
    }

    private void UpdateMaxPrecursorError() {
        try {
            this.maxPrecursorError = searchParameters.getPrecursorAccuracy();
        } catch (Exception e) {
            this.maxPrecursorError = 1.0;
        }
    }
}
