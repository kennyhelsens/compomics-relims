package com.compomics.relims.model.beans;

import com.compomics.relims.conf.RelimsProperties;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a
 */
public class SearchGUIJobBean {

    private static Logger logger = Logger.getLogger(SearchGUIJobBean.class);
    /**
     * "\nusage: java -jar SearchGUI-X.Y.Z [options and input]\n\n"
     * + "   available options and inputs: \n\n"
     * + "   -no_gui : hides the graphical user interface\n"
     * + "   -search : starts the search automatically\n"
     * + "   -omssa : enables OMSSA search\n"
     * + "   -xtandem : enables XTandem search\n"
     * + "   -cf config_file : the configuration file\n"
     * + "   -rf results_folder : the results folder\n"
     * + "   -sf spectra_file_1 [spectra_file_2] [...] : the spectra files, either as files or folders\n");
     */

    private boolean gui = false;
    private boolean search = false;
    private boolean omssa = false;
    private boolean xtandem = false;
    private File config = null;
    private File results = null;
    private List<File> spectra = null;


    public void setConfig(File aConfig) {
        config = aConfig;
    }

    public void setGui(boolean aGui) {
        gui = aGui;
    }

    public void setOmssa(boolean aOmssa) {
        omssa = aOmssa;
    }

    public void setResults(File aResults) {
        results = aResults;
    }

    public void setSearch(boolean aSearch) {
        search = aSearch;
    }

    public void setSpectra(List<File> aSpectra) {
        spectra = aSpectra;
    }

    public void setXtandem(boolean aXtandem) {
        xtandem = aXtandem;
    }

    public String getSearchGUICommandString() {
        ArrayList<String> lCommandParts = Lists.newArrayList();
        try {
            String lSearchGuiArchiveName = RelimsProperties.getSearchGuiArchivePath();
            List<String> lSpectrumFiles = Lists.transform(spectra, new Function<File, String>() {
                public String apply(@Nullable File input) {
                    String lFileName = null;
                    try {
                        lFileName = input.getCanonicalPath();
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                    return lFileName;
                }
            });

            lCommandParts.add(RelimsProperties.getJavaExec());
            lCommandParts.add("-cp");
            lCommandParts.add(lSearchGuiArchiveName);
            lCommandParts.add("eu.isas.searchgui.SearchGUI");
            lCommandParts.add(gui ? "" : "-no_gui");
            lCommandParts.add(search ? "-search" : "");
            lCommandParts.add(omssa ? "-omssa" : "");
            lCommandParts.add(xtandem ? "-xtandem" : "");
            lCommandParts.add("-cf");
            lCommandParts.add(config.getCanonicalPath());
            lCommandParts.add("-rf");
            lCommandParts.add(results.getCanonicalPath());
            lCommandParts.add("-sf");
            lCommandParts.addAll(lSpectrumFiles);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return Joiner.on(" ").join(lCommandParts);
    }
}
