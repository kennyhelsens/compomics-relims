package com.compomics.relims.model.beans;

import com.compomics.relims.concurrent.Command;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.relims.modes.networking.worker.general.ProcessRelocalizer;
import com.compomics.util.experiment.identification.SearchParameters;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.logging.Level;

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
        searchGUICommandLine.append("-Xmx" + RelimsProperties.getAllowedRAM() + "M ");
        searchGUICommandLine.append("-cp ");
        searchGUICommandLine.append(RelimsProperties.getSearchGuiFolder() + "/" + RelimsProperties.getSearchGuiArchive());
        searchGUICommandLine.append(" eu.isas.searchgui.cmd.SearchCLI ");
        searchGUICommandLine.append("-spectrum_files ");

        if (spectrumFile == null) {
            try {
                searchGUICommandLine.append(iRelimsProjectBean.getSpectrumParentFolder().getAbsolutePath());
            } catch (Exception ex) {
                throw new IOException("MGF file could not be loaded into the searchgui bean :" + ex);
            }
        } else {
            searchGUICommandLine.append(spectrumFile.getAbsolutePath());
            iRelimsProjectBean.setSpectrumFile(spectrumFile);
        }

        searchGUICommandLine.append(" -output_folder ");
        //figure out what the projectprovider was
        searchGUICommandLine.append(ProcessRelocalizer.getLocalResultFolder());

        searchGUICommandLine.append(" -search_params ");

        if (searchParametersFile == null) {
            searchGUICommandLine.append(iRelimsProjectBean.getSearchParamFile().getAbsolutePath());
        } else {
            searchGUICommandLine.append(searchParametersFile.getAbsolutePath());
            iRelimsProjectBean.setSearchParametersFile(searchParametersFile);
        }
        searchGUICommandLine.append(" -ppm ");
        searchGUICommandLine.append("2");
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
        try {
            this.searchParametersFile = ProcessRelocalizer.localizeSearchParameters(repositoryParametersFile);
            searchParameters = SearchParameters.getIdentificationParameters(repositoryParametersFile);
            searchParameters.setFastaFile(new File(RelimsProperties.getDefaultSearchDatabase()));
          } catch (IOException | ClassNotFoundException ex) {
            logger.error("Could not localize searchparameters. Using remote file");
            this.searchParametersFile = repositoryParametersFile;
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
            if (searchGUICommandLine != null) {
                logger.info(searchGUICommandLine);
                return Command.call(searchGUICommandLine);
            } else {
                logger.error("Could not run searchgui...");
                return 1;
            }
        } catch (Exception e) {
            logger.error(e);
            return 1;
        }
    }

    File getSearchParamFile() {
        return searchParametersFile;
    }
}
