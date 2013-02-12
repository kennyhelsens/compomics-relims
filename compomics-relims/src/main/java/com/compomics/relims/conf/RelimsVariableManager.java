/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.conf;

import com.compomics.pridexmltomgfconverter.errors.ConversionError;
import com.compomics.relims.filemanager.FileGrabber;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.File;
import java.util.List;

/**
 *
 * @author Kenneth
 */
public class RelimsVariableManager {

    /**
     * the searchResultFolder = where all files needed to run both PeptideShaker
     * and SearchGui are located
     */
    static String searchResultFolder = null;
    /**
     * MGF file title name = generated by either pride-asa, pride XML, via
     * MSLIMS OR located in the local repository
     */
    static String MGFFile = null;
    /**
     * Not sure if this is still used...
     */
    static String normalModFile = null;
    /**
     * This timestamp is used to calculate the total time required for this
     * project to run, from start till finish
     */
    static String timeStamp;
    /**
     * The filegrabber is a generic tool to aquire MGF files and
     * searchparameters files in a resultfolder. //TODO : it currently only
     * grabs the first file with the required extension that is found. Should
     * these be put in a list? In theory, only ONE file per extension should be
     * generated anyway...
     */
    static FileGrabber fileGrabber = FileGrabber.getInstance();
    /**
     * The running project's id
     */
    static long projectId;
    /**
     * The current workspace --> = searchresultfolder?
     */
    static String workSpace;
    /**
     * There is support to use a list of spectrumfiles in a searchgui command.
     */
    static List<String> spectrumFiles;
    /**
     * This is equal to the searchresultfolder
     */
    static String resultsFolder;
    /**
     * This is the local folder where the repository is. Every project will
     * eventually have an MGF file and generated SearchParameters, greatly
     * increasing the speed of processing
     */
    static File repositoryFolder;
    /**
     * TaskID = the automatic system's tasknumbering
     *
     * /* static long taskID; /** This is the port the worker is running
     * on...relims does not need this?
     *
     * static int workerport;
     */
    static SearchParameters searchParameters;
    private static List<ConversionError> errorList;
    //public RelimsVariableManager(File propFolder)  {
    //this.repositoryFolder = propFolder;
    //}
    private static boolean classicMode;

    public static void initialize() {
        workSpace = RelimsProperties.getWorkSpace().getAbsolutePath();
        repositoryFolder = new File(RelimsProperties.getRepositoryPath());
    }

    public static void setRepository(File propertiesFolder) {
        RelimsVariableManager.repositoryFolder = propertiesFolder;
    }

    public static File getRepository() {
        return repositoryFolder;
    }

    public static String getMGFFile() {
        return buildSpectraList();
    }

    public static String getIdentificationsList() {
        return buildIdentificationList();
    }

    public static void setMGFFile(String MGFFile) {
        RelimsVariableManager.MGFFile = MGFFile;
    }

    public static String getNormalModFile() {
        return normalModFile;
    }

    public static void setModOMSSAFile(String OMXFile) {
        RelimsVariableManager.normalModFile = OMXFile;
    }

    public static void setSearchResultFolder(String iSearchResultFolder) {
        RelimsVariableManager.searchResultFolder = iSearchResultFolder;
    }

    public static String getSearchResultFolder() {
        return searchResultFolder;
    }

    public static long getProjectId() {
        return projectId;
    }

    public static void setProjectID(long projectId) {
        RelimsVariableManager.projectId = projectId;
    }

    public static void setTimeStampSearch(String iTimeStamp) {
        RelimsVariableManager.timeStamp = iTimeStamp;
    }

    public static String getTimeStampSearch() {
        return timeStamp;
    }

    public static void getResources() {
    }

    public static String buildIdentificationList() {

        List<String> selectedIdentifications = fileGrabber.getIdentificationsList(RelimsVariableManager.workSpace);
        StringBuilder cmdStringIdentifications = new StringBuilder();

        for (String anIdentification : selectedIdentifications) {
            cmdStringIdentifications.append(anIdentification);
            if (!anIdentification.equals(selectedIdentifications.get(selectedIdentifications.size() - 1))) {
                cmdStringIdentifications.append(",");
            }
        }
        return cmdStringIdentifications.toString();
    }

    public static String buildSpectraList() {
        return fileGrabber.getGenericMGFFile(RelimsVariableManager.workSpace).getAbsolutePath().toString();
    }

    public static void setWorkSpace(String workSpace) {
        RelimsVariableManager.workSpace = workSpace;
    }

    public static String getFullMGFFile() {
        return fileGrabber.getGenericMGFFile(RelimsVariableManager.workSpace).getAbsolutePath().toString();
    }

    public static void setSpectrumFiles(List<String> lSpectrumFiles) {
        RelimsVariableManager.spectrumFiles = lSpectrumFiles;
    }

    public static List<String> getSpectrumFiles() {
        return RelimsVariableManager.spectrumFiles;
    }

    public static void setResultsFolder(String resultsFolder) {
        RelimsVariableManager.resultsFolder = resultsFolder;
    }

    public static String getResultsFolder() {
        return RelimsVariableManager.resultsFolder;
    }

    /*  public static long getTaskID() {
     return RelimsVariableManager.taskID;
     }

     public static void setTaskID(long taskID) {
     try {
     RelimsVariableManager.taskID = taskID;
     } catch (Throwable e) {
     e.printStackTrace();
     }
     }

     public static int getWorkerPort() {
     return RelimsVariableManager.workerport;
     }

     public static void setWorkerPort(int workerport) {
     RelimsVariableManager.workerport = workerport;
     }*/
    public static void setSearchParameters(SearchParameters searchParameters) {
        RelimsVariableManager.searchParameters = searchParameters;
    }

    public static SearchParameters getSearchParameters() {
        return RelimsVariableManager.searchParameters;
    }

    public static void setConversionErrorList(List<ConversionError> errorList) {
        RelimsVariableManager.errorList = errorList;
    }

    public static List<ConversionError> getConversionErrorList() {
        return RelimsVariableManager.errorList;
    }

    public static boolean getClassicMode() {
        return RelimsVariableManager.classicMode;
    }

    public static void setClassicMode(boolean classicMode) {
        RelimsVariableManager.classicMode = classicMode;
    }
}
