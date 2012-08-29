package com.compomics.relims.model.beans;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.conf.Utilities;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * This class is a
 */
public class PeptideShakerJobBean {

    private static Logger logger = Logger.getLogger(PeptideShakerJobBean.class);
    /**
     * "\nusage: java -jar PeptideShaker-X.Y.Z [options and input]\n\n"
     * + "   available options and inputs: \n\n"
     * Options:
     * -ascore             Include ascore to estimate the probability of phospho sites
     * -out                PeptideShaker output folder
     * -pep                FDR at PEPTIDE level (default 1% FDR: <1>)
     * -prot               FDR at PROTEIN level (default 1% FDR: <1>)
     * -psm                FDR at PSM level (default 1% FDR: <1>)
     * -search_gui_results SearchGUI result folder
     */

    private boolean ascore = false;
    private File outFolder = null;
    private File searchGUIResultsFolder = null;
    private double pepfdr = 1.0;
    private double protfdr = 1.0;
    private double psmfdr = 1.0;
    private String exp = "default_exp";
    private String sample  = "default_sample";


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

    public void setSampleName(String aSample) {
        sample = aSample;
    }

    public String getPeptideShakerCommandString() {
        Collection<String> lCommandParts = Lists.newArrayList();
        try {
            String lPeptideShakerArchivePath = RelimsProperties.getPeptideShakerArchivePath();
            String lPeptideShakerFolder = RelimsProperties.getPeptideShakerFolder();

            lCommandParts.add(RelimsProperties.getJavaExec());
            lCommandParts.add(String.format("-Xmx%s", RelimsProperties.getPeptideShakerMemory()));
            lCommandParts.add("-cp");

            List<String> lClassPathEntries = Lists.newArrayList();
            lClassPathEntries.add(lPeptideShakerArchivePath);
            lClassPathEntries.add(lPeptideShakerFolder);

            if(Utilities.isWindows()){
                lCommandParts.add("\"" + Joiner.on(";").join(lClassPathEntries) + "\"");
            }else{
                lCommandParts.add(Joiner.on(":").join(lClassPathEntries));
            }

            lCommandParts.add("eu.isas.peptideshaker.cmd.PeptideShakerCLI");

            lCommandParts.add(ascore ? "" : "-ascore");

            lCommandParts.add("-out");
            lCommandParts.add(outFolder.getCanonicalPath());

            lCommandParts.add("-pep");
            lCommandParts.add(String.valueOf(pepfdr));

            lCommandParts.add("-prot");
            lCommandParts.add(String.valueOf(protfdr));

            lCommandParts.add("-psm");
            lCommandParts.add(String.valueOf(psmfdr));

            lCommandParts.add("-experiment");
            lCommandParts.add(exp);

            lCommandParts.add("-sample");
            lCommandParts.add(sample);

            lCommandParts.add("-search_gui_results");
            lCommandParts.add(searchGUIResultsFolder.getCanonicalPath());


        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return Joiner.on(" ").join(lCommandParts);
    }
}
