package com.compomics.relims.concurrent;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.relims.manager.filemanager.FileManager;
import com.compomics.relims.manager.filemanager.RepositoryManager;
import com.compomics.relims.model.beans.PeptideShakerJobBean;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchGUIJobBean;
import com.compomics.relims.model.guava.predicates.PredicateManager;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.interfaces.ModificationResolver;
import com.compomics.relims.model.interfaces.ProjectRunner;
import com.compomics.relims.model.interfaces.SearchStrategy;
import com.compomics.relims.model.provider.ProjectProvider;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.util.experiment.identification.SearchParameters;
import com.google.common.base.Predicate;
import java.io.File;
import java.io.IOException;
import static java.lang.String.format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class RelimsJobController extends Observable implements ProjectRunner {

    /**
     * Plain logger
     */
    private static Logger logger = Logger.getLogger(RelimsJobController.class);
    /**
     * a RelimsProjectBean stores all required data to start a process. It is an
     * intermediate step to build the searchParameters. TODO : is this still
     * required?
     */
    private RelimsProjectBean relimsProjectBean;
    /**
     * Predicates are requirements that need to be forfilled before runtime.
     */
    private PredicateManager predicateManager;
    /**
     * Dataprovider is a type of class that handles the loading and handling of
     * the required data (spectra etc)
     */
    private DataProvider dataProvider;
    /**
     * the ModificationResolver sets the fixed modifications? ---> TODO:FIGURE
     * THIS OUT
     */
    private ModificationResolver modificationResolver;
    /**
     * the projectProvider is the class that handles the usage local project
     * lists to run them. This is used in the classic relims...TODO:
     * re-implement this?
     */
    private ProjectProvider projectProvider;
    /**
     * the project accession number...
     */
    private long projectID;
    /**
     * the searchresultfolder is actually the workspace, adapted to the certain
     * project with a timestamp and other features in the name of the folder.
     */
    private File searchResultFolder;
    /**
     * an instance of the class that contains all required information to build
     * and run a searchGUI process
     */
    private SearchGUIJobBean searchGUIJobBean;
    /**
     * an instance of the class that contains all required information to build
     * and run a peptideshaker process
     */
    private PeptideShakerJobBean lPeptideShakerJobBean;
    /**
     * TODO : Are these not the same ?
     */
    private String sampleID;
    private String experimentID;
    /**
     * This is the MGF file. Since searchgui accepts a list of MGF files as
     * well, I haven't taken out the list implementation from this class.
     */
    private File spectrumFile;
    /**
     * A ProgressManager to store the state of the project and monitor it
     */
    private ProgressManager progressManager = ProgressManager.getInstance();
    /**
     * The filegrabber is a generic tool to aquire MGF files and
     * searchparameters files in a resultfolder. //TODO : it currently only
     * grabs the first file with the required extension that is found. Should
     * these be put in a list? In theory, only ONE file per extension should be
     * generated anyway...
     */
    private FileManager fileGrabber = FileManager.getInstance();

    @Override
    public void setPredicateManager(PredicateManager aPredicateManager) {
        predicateManager = aPredicateManager;
    }

    @Override
    public void setSearchStrategy(SearchStrategy aSearchStrategy) {
        //TODO REMOVE THIS COMPLETELY?
        return;
    }

    @Override
    public void setProjectID(long projectID) {
        this.projectID = projectID;
    }

    @Override
    public void setProjectProvider(ProjectProvider iProjectProvider) {
        this.projectProvider = iProjectProvider;
    }

    public void setProject(RelimsProjectBean aRelimsProjectBean) {
        relimsProjectBean = aRelimsProjectBean;
    }

    public void setDataProvider(DataProvider aDataProvider) {
        dataProvider = aDataProvider;
    }

    public void setModificationResolver(ModificationResolver aModificationResolver) {
        modificationResolver = aModificationResolver;
    }

    //Prepare and run the searchgui job (different pathways to do so)
    private boolean prepareSearchGUIFromScratch() throws IOException, ConfigurationException {
        spectrumFile = null;
        searchResultFolder = new File(ProcessVariableManager.getResultsFolder());
        //  ProcessVariableManager.setSearchResultFolder(searchResultFolder.getAbsolutePath().toString());
        setDataProvider(projectProvider.getDataProvider());

        logger.debug(format("loading MS/MS spectra for project %s from %s", projectID, dataProvider.toString()));
        if (dataProvider.isProjectValuable("" + projectID)) {
            logger.debug("Attempting to get modifications");
            setModificationResolver(modificationResolver = projectProvider.getModificationResolver());
            logger.debug("Building projectbean");
            relimsProjectBean = projectProvider.getProject(projectID);
            setProject(relimsProjectBean);
            modificationResolver.resolveModificationList(relimsProjectBean);
            ProcessVariableManager.setProjectID(projectID);
            //make a projectBean
            long lProjectid = relimsProjectBean.getProjectID();

            // GET THE SPECTRA FILE 

            spectrumFile = dataProvider.getSpectraForProject(projectID);

            logger.debug("creating projectrunner for " + lProjectid);

            Collection<Predicate> lPredicates = predicateManager.createCollection(
                    PredicateManager.Types.INSTRUMENT //                    PredicateManager.Types.PROJECT_SIZE,
                    //                    PredicateManager.Types.SPECIES,
                    //                    PredicateManager.Types.SEARCH_SET_SIZE
                    );

            logger.debug(format("validating project contents by %d predices", lPredicates.size()));
            for (Predicate lProjectPredicate : lPredicates) {
                boolean lResult = lProjectPredicate.apply(relimsProjectBean);
                if (!lResult) {
                    logger.error("END " + lProjectid);
                    return false;
                }
            }

            try {

                if (spectrumFile != null) {
                    searchGUIJobBean = new SearchGUIJobBean("" + lProjectid, projectProvider.toString(), relimsProjectBean, spectrumFile);
                    List<File> lSpectrumFileList = new ArrayList<File>();
                    lSpectrumFileList.add(spectrumFile);
                    searchGUIJobBean.setSearchResultFolder(new File(ProcessVariableManager.getResultsFolder()));
                    searchGUIJobBean.setiName(lProjectid + "");
                    searchGUIJobBean.setiRelimsProjectBean(relimsProjectBean);
                    searchGUIJobBean.setiSpectrumFiles(lSpectrumFileList);
                    sampleID = searchGUIJobBean.getName();
                    logger.debug(format("running search %s", sampleID));
                    if (searchGUIJobBean.launch() == 0) {
                        experimentID = sampleID;
                        return true;
                    } else {
                        progressManager.setEndState(Checkpoint.PROCESSFAILURE);
                        return false;
                    }
                } else {
                    logger.debug("Aborting task...");
                    return false;
                }
            } catch (Exception e) {
                logger.error("ERROR OCCURRED FOR PROJECT " + lProjectid);
                logger.error(e);
                e.printStackTrace();
                progressManager.setState(Checkpoint.FAILED, e);
            } finally {
                dataProvider.clearResources();
                return true;
            }
        } else {
            logger.debug("Failed to aquire spectra !");
            progressManager.setEndState(Checkpoint.PRIDEFAILURE);
            return false;
        }
    }

    private boolean prepareSearchGUIFromFiles() throws IOException, ConfigurationException {
        try {
            searchResultFolder = new File(ProcessVariableManager.getResultsFolder());
            //  ProcessVariableManager.setResultsFolder(searchResultFolder.getAbsolutePath().toString());
            // ProcessVariableManager.setSearchResultFolder(searchResultFolder.getAbsolutePath().toString());
            ProcessVariableManager.setProjectID(projectID);
            long lProjectid = projectID;

            logger.debug("creating projectrunner for " + lProjectid);
            // GET THE SPECTRA FILE          
            logger.debug(format("loading MS/MS spectra for project %s from the repository", lProjectid));
            logger.info("Looking for project in repository...");
            spectrumFile = fileGrabber.getGenericMGFFile(searchResultFolder.getAbsolutePath().toString());
            File repositoryParametersFile = new File(searchResultFolder.getAbsolutePath().toString() + "/SearchGUI.parameters");
            if (spectrumFile != null && spectrumFile.exists()) {
                searchGUIJobBean = new SearchGUIJobBean("" + lProjectid, projectProvider.toString(), repositoryParametersFile, spectrumFile);
                List<File> lSpectrumFileList = new ArrayList<File>();
                lSpectrumFileList.add(spectrumFile);
                searchGUIJobBean.setSearchResultFolder(new File(ProcessVariableManager.getResultsFolder()));
                searchGUIJobBean.setiName(lProjectid + "");
                searchGUIJobBean.setiSpectrumFiles(lSpectrumFileList);
                searchGUIJobBean.setParametersFile(repositoryParametersFile);
                sampleID = searchGUIJobBean.getName();
                experimentID = "" + lProjectid; // format("projectid_%d", lSearchGUI.getProjectId());
                logger.debug(format("running search %s", sampleID));
                if (searchGUIJobBean.launch() == 0) {
                    experimentID = sampleID;
                    return true;
                } else {
                    progressManager.setEndState(Checkpoint.PROCESSFAILURE);
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("ERROR OCCURRED FOR PROJECT " + projectID);
            logger.error(e);
            progressManager.setState(Checkpoint.FAILED, e);;
            return false;
        }
    }

    private boolean prepareSearchGUIFromClientInput(SearchParameters searchParameters) throws IOException, ConfigurationException {
        try {
            searchResultFolder = new File(ProcessVariableManager.getResultsFolder());
            //  ProcessVariableManager.setResultsFolder(searchResultFolder.getAbsolutePath().toString());
            // ProcessVariableManager.setSearchResultFolder(searchResultFolder.getAbsolutePath().toString());
            ProcessVariableManager.setProjectID(projectID);
            long lProjectid = projectID;

            logger.debug("creating projectrunner for " + lProjectid);
            // GET THE SPECTRA FILE          
            logger.debug(format("loading MS/MS spectra for project %s from the repository", lProjectid));
            logger.info("Looking for project in repository...");
            File repositoryParametersFile = new File(searchResultFolder.getAbsolutePath().toString() + "/SearchGUI.parameters");
            if (spectrumFile != null && spectrumFile.exists()) {
                searchGUIJobBean = new SearchGUIJobBean("" + lProjectid, projectProvider.toString(), repositoryParametersFile, spectrumFile);
                List<File> lSpectrumFileList = new ArrayList<File>();
                lSpectrumFileList.add(spectrumFile);
                searchGUIJobBean.setSearchResultFolder(new File(ProcessVariableManager.getResultsFolder()));
                searchGUIJobBean.setiName(lProjectid + "");
                searchGUIJobBean.setiSpectrumFiles(lSpectrumFileList);
                searchGUIJobBean.setSearchParameters(searchParameters);
                searchGUIJobBean.setParametersFile(repositoryParametersFile);
                sampleID = searchGUIJobBean.getName();
                experimentID = "" + lProjectid; // format("projectid_%d", lSearchGUI.getProjectId());
                logger.debug(format("running search %s", sampleID));
                if (searchGUIJobBean.launch() == 0) {
                    experimentID = sampleID;
                    return true;
                } else {
                    progressManager.setEndState(Checkpoint.PROCESSFAILURE);
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("ERROR OCCURRED FOR PROJECT " + projectID);
            logger.error(e);
            progressManager.setState(Checkpoint.FAILED, e);;
            return false;
        } finally {
            return true;
        }
    }
    //Prepare the peptideshaker job using the output from searchgui

    private boolean prepareAndLaunchPeptideShaker() {
        //PEPTIDESHAKER -----------------------------------------------------------------------
        logger.debug("processing the search results with PeptideShaker");
        File peptideShakerFolder = new File(RelimsProperties.getPeptideShakerArchivePath().replace(RelimsProperties.getPeptideShakerArchive(), ""));
        File lPeptideShakerResultsFolder = searchResultFolder;
        //get the searchparametersfile from the searchgui output, the command USES this file so it has to be loaded for now
        File searchParametersFile = new File(searchResultFolder.getAbsolutePath().toString() + "/SearchGUI.parameters");
        if (spectrumFile.exists() && searchParametersFile.exists()) {
            Command.setWorkFolder(peptideShakerFolder);
            lPeptideShakerJobBean = new PeptideShakerJobBean(projectID, searchParametersFile, spectrumFile, searchGUIJobBean.getSearchResultFolder());
            lPeptideShakerJobBean.setOutFolder(lPeptideShakerResultsFolder);
            //  lPeptideShakerJobBean.setSearchGUIResultsFolder(searchGUIJobBean.getSearchResultFolder());
            double lFDR = RelimsProperties.getFDR();
            lPeptideShakerJobBean.setPepfdr(lFDR);
            lPeptideShakerJobBean.setProtfdr(lFDR);
            lPeptideShakerJobBean.setPsmfdr(lFDR);
            lPeptideShakerJobBean.setSampleName(sampleID);
            lPeptideShakerJobBean.setExperimentName(experimentID);
            lPeptideShakerJobBean.setAscore(false);
            // Run PeptideShaker
            // IF the return value = 0 (= system.exit.value) then the process ran correctly. (Timeout etc will change this value)
            if (lPeptideShakerJobBean.launch() == 0) {
                logger.debug(format(
                        "finished PeptideShakerCLI on project '%s', sample '%s'",
                        experimentID,
                        sampleID));
                storeInRepository();
                lPeptideShakerJobBean.removeJunk();
                return true;
            } else {
                progressManager.setState(Checkpoint.PROCESSFAILURE);
                lPeptideShakerJobBean.removeJunk();
                return false;
            }
        } else {
            lPeptideShakerJobBean.removeJunk();
            return false;
        }
    }

    @Override
    public String call() {
        logger.setLevel(Level.DEBUG);
        String provider = "mslims";
        boolean runPeptideshaker;
        try {
            //if appendPrideAsapAutomatic = off (selected option in client) --> use only the searchparameters given by
            //the client
            if (projectProvider.getClass().toString().contains("mslims")) {
                provider = "mslims";
            } else {
                provider = "pride";
            }
            ProcessVariableManager.setResultsFolder(RelimsProperties.createWorkSpace(projectID, provider).getAbsolutePath());
            if (RelimsProperties.appendPrideAsapAutomatic()) {
                if (!RepositoryManager.hasBeenRun(provider, projectID)) {
                    logger.debug("Project was not located in repository. Building from scratch...");
                    runPeptideshaker = prepareSearchGUIFromScratch();
                } else {
                    logger.debug("Project was located in repository. Building from files...");
                    runPeptideshaker = prepareSearchGUIFromFiles();
                }
            } else {
                logger.debug("Project will be run with user specified searchparameters...");
                runPeptideshaker = prepareSearchGUIFromClientInput(ProcessVariableManager.getSearchParameters());
            }
            if (runPeptideshaker && progressManager.getState() != Checkpoint.PROCESSFAILURE) {
                logger.debug("Preparing peptideshaker");
                prepareAndLaunchPeptideShaker();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error("ERROR OCCURRED FOR PROJECT " + projectID);
            logger.error(e);
            if (e instanceof java.lang.IllegalArgumentException) {
                progressManager.setEndState(Checkpoint.MODFAILURE);
            } else {
                if (provider.equals("pride")) {
                    progressManager.setEndState(Checkpoint.PRIDEFAILURE);
                } else {
                    progressManager.setState(Checkpoint.FAILED, e);
                }
            }
        }
        //nullcheck to prevent standalone relims to delete its folders
        if (ProcessVariableManager.getClassicMode()) {
            if (progressManager.getEndState() == Checkpoint.FAILED || progressManager.getEndState() == Checkpoint.PRIDEFAILURE) {
                //              fileGrabber.deleteResultFolder();
            }
        }
        return "";
    }

    private void storeInRepository() {
        if (projectProvider.getClass().toString().contains("mslims")) {
            RepositoryManager.copyToRepository("mslims", new File(ProcessVariableManager.getSearchResultFolder()), projectID);
            logger.debug("Stored sourcefiles (MGF / SearchParameters) to MSLIMS repository");
        } else {
            RepositoryManager.copyToRepository("pride", new File(ProcessVariableManager.getSearchResultFolder()), projectID);
            logger.debug("Stored sourcefiles (MGF / SearchParameters) to PRIDE repository");
        }
    }
}
