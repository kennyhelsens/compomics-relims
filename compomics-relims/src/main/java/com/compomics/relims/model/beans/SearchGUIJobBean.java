package com.compomics.relims.model.beans;

import com.compomics.relims.concurrent.Command;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.variablemanager.RelimsVariableManager;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.preferences.ModificationProfile;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import no.uib.jsparklines.data.XYDataPoint;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 * This class is a
 */
public class SearchGUIJobBean {

    /**
     * a plain logger
     */
    private static Logger logger = Logger.getLogger(SearchGUIJobBean.class);
    private File spectra;
    private String iName = null;
    private RelimsProjectBean iRelimsProjectBean = null;
    private File iSearchResultFolder = null;
    private List<File> iSpectrumFiles = new ArrayList<File>();
    protected PropertiesConfiguration iSearchGuiConfiguration;
    protected long iTimeStamp;
    private ArrayList<String> searchGUICommandLine;
    private SearchParameters searchParameters;
    private File searchParametersFile;
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    private String iProjectProvider;
    private ProgressManager progressManager = ProgressManager.getInstance();
    private String command;

    public SearchGUIJobBean(String aName, String provider, File searchParameters, File aSpectrumFile) {

        try {
            this.searchParametersFile = null;
            iProjectProvider = provider;
            searchParametersFile = searchParameters;
            iName = aName;
            iSpectrumFiles.add(aSpectrumFile);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public SearchGUIJobBean(String aName, String provider, RelimsProjectBean aRelimsProjectBean, List<File> aSpectrumFiles) {
        try {
            iProjectProvider = provider;
            iRelimsProjectBean = aRelimsProjectBean.clone();
        } catch (CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
        }
        iName = aName;
        iSpectrumFiles = aSpectrumFiles;
    }

    public SearchGUIJobBean(String aName, String provider, RelimsProjectBean aRelimsProjectBean, File aSpectrumFile) {

        try {
            this.searchParametersFile = null;
            iProjectProvider = provider;
            iRelimsProjectBean = aRelimsProjectBean.clone();
        } catch (CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
        }
        iName = aName;
        iSpectrumFiles.add(aSpectrumFile);

    }

    public SearchGUIJobBean(String aName, String provider, RelimsProjectBean aRelimsProjectBean, File aSpectrumFile, File aSearchParametersFile) {
        try {
            this.searchParametersFile = aSearchParametersFile;
            iProjectProvider = provider;
            iRelimsProjectBean = aRelimsProjectBean.clone();
        } catch (CloneNotSupportedException e) {
            logger.error(e.getMessage(), e);
        }
        iName = aName;
        iSpectrumFiles.add(aSpectrumFile);
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

    public List<File> getiSpectrumFiles() {
        return iSpectrumFiles;
    }

    public void setiSpectrumFiles(List<File> iSpectrumFiles) {
        this.iSpectrumFiles = iSpectrumFiles;
    }

    public ArrayList<String> generateCommand() throws IOException, ConfigurationException {

        searchGUICommandLine = new ArrayList<String>();

        //the actual command
        searchGUICommandLine.clear();
        //  searchGUICommandLine.append(RelimsProperties.getSearchGuiFolder());
        searchGUICommandLine.add("java ");
        searchGUICommandLine.add("-cp ");
        searchGUICommandLine.add(RelimsProperties.getSearchGuiArchive());
        searchGUICommandLine.add(" eu.isas.searchgui.cmd.SearchCLI ");

        searchGUICommandLine.add("-spectrum_files ");

        //TODO THIS HAS TO BE A LIST OF THE SPECTRA
        //FOR NOW FIX              

        for (File aSpectrum : iSpectrumFiles) {
            searchGUICommandLine.add(iSpectrumFiles.get(0).getAbsolutePath().toString());
            if (iSpectrumFiles.indexOf(aSpectrum) != iSpectrumFiles.size() && iSpectrumFiles.indexOf(aSpectrum) != 0) {
                searchGUICommandLine.add(",");
            }
        }

        searchGUICommandLine.add(" -output_folder ");
        //figure out what the projectprovider was
        searchGUICommandLine.add(RelimsVariableManager.getResultsFolder() + "/");

        searchGUICommandLine.add(" -search_params ");
        searchGUICommandLine.add(searchParametersFile.getAbsolutePath());

        System.err.println("");
        System.err.println("SEARCHGUICOMMAND");
        StringBuilder commandLine = new StringBuilder();
        for (String aParam : searchGUICommandLine) {
            commandLine.append(aParam);
        }
        return searchGUICommandLine;

    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    protected void createSearchParametersFile() {

        File searchParametersRepositoryFile = new File(RelimsVariableManager.getSearchResultFolder() + "/SearchGUI.parameters");
        File fastaFile = new File(RelimsProperties.getDefaultSearchDatabase());
        //makeBlastDB(fastaFile) Searchgui should take care of this !;
        if (searchParametersRepositoryFile.exists()) {
            try {
                searchParameters = SearchParameters.getIdentificationParameters(searchParametersRepositoryFile);
                searchParameters.setFastaFile(fastaFile);
                logger.debug("USING FASTA : " + searchParameters.getFastaFile().getAbsolutePath());
            } catch (FileNotFoundException ex) {
                logger.error(ex);
            } catch (IOException ex) {
                logger.error(ex);
            } catch (ClassNotFoundException ex) {
                logger.error(ex);
            }
        } else {
            try {
                searchParameters = new SearchParameters();// this should be default..;
                searchParameters.setFastaFile(fastaFile);
                logger.debug("USING FASTA : " + searchParameters.getFastaFile().getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.debug("Project was not found in repository. Remaking searchparameters file");
            //Set the modifications 
            List<String> modifications = iRelimsProjectBean.getFixedMatchedPTMs();
            ModificationProfile modProfile = new ModificationProfile();

            logger.debug("setting fixed modifications to searchparameters file");
            //Add the relims-mods

            for (String aMod : modifications) {
                try {
                    modProfile.addFixedModification(ptmFactory.getPTM(aMod));
                } catch (Exception e) {
                    logger.error(e);
                    logger.debug("failed to set " + aMod + " in the modificationprofile.");
                }
            }

            modifications = iRelimsProjectBean.getVariableMatchedPTMs();
            logger.debug("setting variabele modifications to searchparameters file");
            for (String aMod : modifications) {
                try {
                    if (!modProfile.contains(aMod)) {
                        modProfile.addVariableModification(ptmFactory.getPTM(aMod));
                    }
                } catch (Exception e) {
                    logger.error(e);
                    logger.debug("failed to set " + aMod + " in the modificationprofile.");
                }
            }
            searchParameters.setModificationProfile(modProfile);

            // Set other parameters (defaults)

            try {
                File enzymesFile = new File(RelimsProperties.getSearchGuiFolder() + "/resources/conf/searchGUI_enzymes.xml");
                enzymeFactory.importEnzymes(enzymesFile);
                if (iRelimsProjectBean.getEnzyme() != null) {
                    Enzyme enzyme = iRelimsProjectBean.getEnzyme();
                }
                Enzyme enzyme = enzymeFactory.getEnzyme("Trypsin");
                searchParameters.setEnzyme(enzyme);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            searchParameters.setPrecursorAccuracy(iRelimsProjectBean.getPrecursorError());
            searchParameters.setFragmentIonAccuracy(iRelimsProjectBean.getFragmentError());
            searchParameters.setnMissedCleavages(RelimsProperties.getMissedCleavages());
            // Precursor
            searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.DA);
            validate(searchParameters);
        }
        System.out.println("Searchparameters were loaded");
        try {
            //save the file to the correct folder
            searchParametersFile = new File(RelimsProperties.getWorkSpace().toString() + "/SearchGUI.parameters ");
            SearchParameters.saveIdentificationParameters(searchParameters, searchParametersFile);
            logger.debug("Loaded parameters...");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private void makeBlastDB(File fasta) {
        File tempBatch = null;
        if (needsFormatting(fasta)) {
            logger.info("The fasta file needs formatting for OMSSA...");
            ProcessBuilder processBuilder = new ProcessBuilder();
            //find the makeblastDB tool...
            String osName = null;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                osName = "windows";
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                osName = "mac";
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                osName = "windows";
            } else {
                logger.error("Unsupported operating system !");
            }

            //MAKE A BATCH FILE !!!!
            try {
                tempBatch = makeBlastDbBatch();
                ProcessBuilder pb = new ProcessBuilder(tempBatch.getAbsolutePath());
                Process process = pb.start();
                int exitStatus = process.waitFor();
                if (process.exitValue() == 0) {
                    logger.debug("Fastafile was successfully formatted !");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(ex);
            } finally {
                if (tempBatch != null) {
                    if (tempBatch.exists()) {
                        tempBatch.delete();
                        logger.debug("Cleaned up temporary batch file...");
                    }
                }
            }
        } else {
            logger.debug("Fastafile does not need formatting !");
        }
    }

    public boolean needsFormatting(File fasta) {
        boolean result = true;
        String[] list = fasta.getParentFile().list();
        // Get the filename.
        String name = fasta.getName();

        // Find all three processed files.
        boolean phr = false;
        boolean pin = false;
        boolean psq = false;
        for (int i = 0; i < list.length; i++) {
            String s = list[i];
            if (s.equals(name + ".phr")) {
                phr = true;
            }
            if (s.equals(name + ".pin")) {
                pin = true;
            }
            if (s.equals(name + ".psq")) {
                psq = true;
            }
        }

        if (phr && pin && psq) {
            result = false;
        }

        return result;
    }

    public static File makeBlastDbBatch() {
        String osName = null;
        BufferedWriter out = null;
        File tempBatch = null;
        try {
            RelimsProperties.initialize();
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                osName = "windows";
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                osName = "mac";
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                osName = "windows";
            } else {
                System.out.println("Unsupported operating system !");
            }

            File makeBlastDbFile = new File(RelimsProperties.getSearchGuiFolder() + "/resources/makeblastdb/" + osName + "/makeblastdb");
            File fastaParentFolder = new File(RelimsProperties.getDefaultSearchDatabase()).getParentFile();
            String currentDir = System.getProperty("user.dir");
            //MAKE A BATCH FILE !!!!
            String driveLetter = "" + fastaParentFolder.getAbsolutePath().charAt(0);

            tempBatch = new File("./tempRelims.bat");
            out = new BufferedWriter(new FileWriter(tempBatch));
            //1 = move this process to the fastaParent
            out.write(driveLetter + ":");
            logger.debug(driveLetter + ":");
            out.newLine();
            out.write("cd " + fastaParentFolder.getAbsolutePath());
            logger.debug("cd " + fastaParentFolder.getAbsolutePath());
            out.newLine();
            out.write(makeBlastDbFile.getAbsolutePath() + " -in " + RelimsProperties.getDefaultSearchDatabase());
            logger.debug(makeBlastDbFile.getAbsolutePath() + " -in " + RelimsProperties.getDefaultSearchDatabase());
            out.newLine();
            out.write("cd " + RelimsProperties.getSearchGuiFolder());
            logger.debug("cd " + RelimsProperties.getSearchGuiFolder());
            out.newLine();
            //2 = tell the process to run the makeblastdb
            out.close();
        } catch (IOException e) {
            if (out != null) {
                out = null;
            }
        } finally {
            return tempBatch;
        }
    }

    private void validate(SearchParameters searchParameters) {
        //ENZYME
        if (searchParameters.getEnzyme() == null) {
            logger.debug("Enzyme was null!");
            searchParameters.setEnzyme(enzymeFactory.getEnzyme("Trypsin"));
        }
        logger.debug("Using enzyme : " + searchParameters.getEnzyme());


        if (searchParameters.getFastaFile() == null) {
            logger.debug("Fasta was null!");
            searchParameters.setFastaFile(new File(RelimsProperties.getDefaultSearchDatabase()));
        }
        logger.debug("Using Fasta : " + searchParameters.getFastaFile().getName());


        if (searchParameters.getFractionMolecularWeightRanges() == null) {
            logger.debug("Fraction Molecular Weight Range was null!");
            searchParameters.setFractionMolecularWeightRanges(new HashMap<String, XYDataPoint>());
        }
        logger.debug("Using Fraction Molecular Weight Range : " + searchParameters.getFractionMolecularWeightRanges());


        if (searchParameters.getFragmentIonAccuracy() == null) {
            logger.debug("Fragment Ion Accuracy was null");
            searchParameters.setFragmentIonAccuracy(0.0);
        }
        logger.debug("Using Fragment Ion Accuracy : " + searchParameters.getFractionMolecularWeightRanges());

        if (searchParameters.getHitListLength() == null) {
            logger.debug("Hitlist length was null!");
            searchParameters.setHitListLength(0);
        }
        logger.debug("Using Hitlist length : " + searchParameters.getHitListLength());

        if (searchParameters.getIonSearched1() == null) {
            logger.debug("Ion 1 was null!");
            searchParameters.setIonSearched1("");
        }
        logger.debug("Using Ion 1 searched : " + searchParameters.getIonSearched1());
        if (searchParameters.getIonSearched2() == null) {
            logger.debug("Ion 2 was null!");
            searchParameters.setIonSearched2("");
        }

        logger.debug("Using Ion 2 searched : " + searchParameters.getIonSearched2());

        if (searchParameters.getMaxEValue() == null) {
            logger.debug("Max E-value was null!");
            searchParameters.setMaxEValue(0.0);
        }

        logger.debug("Using Max E-value : " + searchParameters.getMaxEValue());

        if (searchParameters.getMaxPeptideLength() == null) {
            logger.debug("Max peptide length was null!");
            searchParameters.setMaxPeptideLength(0);
        }

        logger.debug("Using Max Peptide Length : " + searchParameters.getMaxPeptideLength());

        if (searchParameters.getModificationProfile() == null) {
            logger.debug("Mod-profile was null!");
            searchParameters.setModificationProfile(new ModificationProfile());
        }

        ArrayList<String> modList = searchParameters.getModificationProfile().getAllModifications();

        logger.debug("Using Mod-profile : ");
        for (String aMod : modList) {
            logger.debug("Modification : " + aMod);
        }

        if (searchParameters.getnMissedCleavages() == null) {
            logger.debug("Missed Cleavages was null!");
            searchParameters.setnMissedCleavages(0);
        }
        logger.debug("Using Missed Cleavages : " + searchParameters.getnMissedCleavages());

        if (searchParameters.getPrecursorAccuracy() == null) {
            logger.debug("Precursor Accuracy was null!");
            searchParameters.setPrecursorAccuracy(0.0);
        }
        logger.debug("Using Precursor Accuracy : " + searchParameters.getPrecursorAccuracy());
    }

    protected void prepare() {
        //Create the resultFolder
        iTimeStamp = System.currentTimeMillis();
        iSearchResultFolder = new File(RelimsVariableManager.getSearchResultFolder());
        RelimsVariableManager.setSearchResultFolder(iSearchResultFolder.toString());
        createSearchParametersFile();
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
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public int launch() throws IOException, ConfigurationException {
        prepare();
        //
        File searchGuiFolder = new File(RelimsProperties.getSearchGuiFolder());
        Command.setWorkFolder(searchGuiFolder);
        StringBuilder totalCommandLine = new StringBuilder();
        searchGUICommandLine = generateCommand();

        for (String aCmd : searchGUICommandLine) {
            totalCommandLine.append(aCmd);
        }
        logger.debug(totalCommandLine.toString());
        System.out.println(totalCommandLine.toString());
        return Command.call(totalCommandLine.toString());
    }
}
