package com.compomics.relims.model.beans;

import com.compomics.relims.concurrent.Command;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.util.experiment.identification.SearchParameters;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.io.*;

/**
 * This class is a
 */
public class SearchGUIJobBean {

    /**
     * a plain logger
     */
    private static Logger logger = Logger.getLogger(SearchGUIJobBean.class);
    private String iName = null;
    private RelimsProjectBean iRelimsProjectBean = null;
    private File iSearchResultFolder = null;
    protected long iTimeStamp;
    private SearchParameters searchParameters;
    private File searchParametersFile;
    private ProgressManager progressManager = ProgressManager.getInstance();
    private File spectrumFile = null;

    public SearchGUIJobBean(RelimsProjectBean aRelimsProjectBean) {
        try {
            iRelimsProjectBean = aRelimsProjectBean.clone();
            this.iName = "Relims_" + aRelimsProjectBean.getProjectID();
        } catch (CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public SearchGUIJobBean(File spectrumFile, File repositoryParametersFile, long lProjectid) {
        this.searchParametersFile = repositoryParametersFile;
        this.spectrumFile = spectrumFile;
    }

    public String getiName() {
        return iName;
    }

    public void setiName(String iName) {
        this.iName = iName;
    }

    public RelimsProjectBean getiRelimsProjectBean() {
        return iRelimsProjectBean;
    }

    public void setiRelimsProjectBean(RelimsProjectBean aRelimsProjectBean) {
        try {
            iRelimsProjectBean = aRelimsProjectBean.clone();
        } catch (CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String generateCommand() throws IOException, ConfigurationException, NullPointerException {

        StringBuilder searchGUICommandLine = new StringBuilder();

        //  searchGUICommandLine.append(RelimsProperties.getSearchGuiFolder());
        searchGUICommandLine.append("java ");
        searchGUICommandLine.append("-cp ");
        searchGUICommandLine.append(RelimsProperties.getSearchGuiFolder() + "/" + RelimsProperties.getSearchGuiArchive());
        searchGUICommandLine.append(" eu.isas.searchgui.cmd.SearchCLI ");
        searchGUICommandLine.append("-spectrum_files ");

        if (spectrumFile == null) {
            searchGUICommandLine.append(iRelimsProjectBean.getSpectrumFile().getAbsolutePath());
        } else {
            searchGUICommandLine.append(spectrumFile.getAbsolutePath());
            iRelimsProjectBean.setSpectrumFile(spectrumFile);
        }

        searchGUICommandLine.append(" -output_folder ");
        //figure out what the projectprovider was
        searchGUICommandLine.append(RelimsProperties.getWorkSpace().getAbsolutePath() + "/");

        searchGUICommandLine.append(" -search_params ");

        if (searchParametersFile == null) {
            searchGUICommandLine.append(iRelimsProjectBean.getSearchParamFile().getAbsolutePath());
        } else {
            searchGUICommandLine.append(searchParametersFile.getAbsolutePath());
            iRelimsProjectBean.setSearchParametersFile(searchParametersFile);
        }
        searchGUICommandLine.append(" -ppm ");
        searchGUICommandLine.append("2");
        System.out.println(searchGUICommandLine.toString());

        return searchGUICommandLine.toString();

    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    protected void prepare() {
        //Create the resultFolder
        iTimeStamp = System.currentTimeMillis();
        //Set the searchresultfolder
        iSearchResultFolder = RelimsProperties.getWorkSpace();
    }

    //protected abstract void applyChildMethods() throws Exception;
    public String getName() {
        return iName;
    }

    public File getSearchResultFolder() {
        return iSearchResultFolder;
    }

    public void setSearchResultFolder(File aSearchResultFolder) {
        iSearchResultFolder = aSearchResultFolder;
    }

    public void setParametersFile(File repositoryParametersFile) {
        this.searchParametersFile = repositoryParametersFile;
        try {
            searchParameters = SearchParameters.getIdentificationParameters(repositoryParametersFile);
            searchParameters.setFastaFile(new File(RelimsProperties.getDefaultSearchDatabase()));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public int launch() throws IOException, ConfigurationException {
        prepare();
        File searchGuiFolder = new File(RelimsProperties.getSearchGuiFolder());
        Command.setWorkFolder(searchGuiFolder);
        logger.debug("Launching searchgui from " + searchGuiFolder.getAbsolutePath());
        String searchGUICommandLine = null;
        try {
            searchGUICommandLine = generateCommand();
        } catch (NullPointerException e) {
            logger.error(e);
        }
        if (searchGUICommandLine != null) {
            logger.info(searchGUICommandLine);
            return Command.call(searchGUICommandLine);
        } else {
            logger.error("Could not run searchgui...");
            return 1;
        }
    }

    File getSearchParamFile() {
        return searchParametersFile;
    }
}
