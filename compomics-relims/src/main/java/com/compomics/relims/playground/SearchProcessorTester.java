package com.compomics.relims.playground;

import com.compomics.relims.concurrent.SearchCommandGenerator;
import com.compomics.relims.concurrent.SearchCommandVarMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;
import com.google.common.collect.Lists;
import eu.isas.peptideshaker.cmd.PeptideShakerCLI;
import eu.isas.peptideshaker.cmd.PeptideShakerCLIInputBean;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ArrayList;

/**
 * This class is a
 */
public class SearchProcessorTester {
    private static Logger logger = Logger.getLogger(SearchProcessorTester.class);

    public static void main(String[] args) {
        // log the current searchgui settings
        RelimsProperties.logSettings();

        // define a test omssa file
        String lFilename = "/private/var/folders/34/ylh6_5j53f7gxdh1_cytlpr80000gn/T/1322065931616-0/1224_1322066023243/mergefile_1224.omx";
        File lFile = new File(lFilename);

        // define a test spectrumfile
        String lSpectrumFilename = "/private/var/folders/34/ylh6_5j53f7gxdh1_cytlpr80000gn/T/1322065931616-0/mergefile_1224.mgf";
        File lSpectrumFile = new File(lSpectrumFilename);
        ArrayList<File> lSpectrumFiles = Lists.newArrayList();
        lSpectrumFiles.add(lSpectrumFile);

        // create a dummy project setupbean
        RelimsProjectBean lProjectSetupBean = new RelimsProjectBean();
        lProjectSetupBean.setProjectID(0);

        // simulate a SearchCommandGenerator
        SearchCommandGenerator lSearchBean = new SearchCommandVarMod("test", lProjectSetupBean, lSpectrumFiles);

        lSearchBean.setSearchResultFolder(lFile.getParentFile());

        // create a searchlist around this searchbean
        SearchList lSearchList = new SearchList();
        lSearchList.add(lSearchBean);

        // create a search processor using this searchbean
        PeptideShakerCLIInputBean lPeptideShakerCLIInputBean = new PeptideShakerCLIInputBean();

        lPeptideShakerCLIInputBean.setInput(lSearchBean.getSearchResultFolder());
        lPeptideShakerCLIInputBean.setOutput(lSearchBean.getSearchResultFolder());
        lPeptideShakerCLIInputBean.setPSMFDR(1.0);
        lPeptideShakerCLIInputBean.setPeptideFDR(1.0);
        lPeptideShakerCLIInputBean.setProteinFDR(1.0);
        lPeptideShakerCLIInputBean.setExperimentID(String.format("projectid_%d", lSearchBean.getProjectId()));
        lPeptideShakerCLIInputBean.setSampleID(lSearchBean.getName());

        PeptideShakerCLI lPeptideShakerCLI = new PeptideShakerCLI(lPeptideShakerCLIInputBean);
        lPeptideShakerCLI.call();

        System.exit(0);
    }
}
