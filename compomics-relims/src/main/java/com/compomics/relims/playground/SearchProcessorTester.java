package com.compomics.relims.playground;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.interfaces.SearchCommandGenerator;
import com.compomics.relims.model.OMSSASearchProcessor;
import com.compomics.relims.model.SearchList;
import com.compomics.relims.model.SearchProcessor;
import com.compomics.relims.model.beans.ProjectSetupBean;
import com.compomics.relims.model.beans.SearchCommandVarModImpl;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
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
        ProjectSetupBean lProjectSetupBean = new ProjectSetupBean();
        lProjectSetupBean.setProjectID(0);

        // simulate a SearchCommandGenerator
        SearchCommandGenerator lSearchBean = new SearchCommandVarModImpl("test", null, null, lProjectSetupBean, lSpectrumFiles);

        lSearchBean.setSearchResultFolder(lFile.getParentFile());

        // create a searchlist around this searchbean
        SearchList<SearchCommandGenerator> lSearchList = new SearchList<SearchCommandGenerator>();
        lSearchList.add(lSearchBean);

        // create a search processor using this searchbean
        SearchProcessor lSearchProcessor = new OMSSASearchProcessor(lSearchList);
        try {
            lSearchProcessor.process();
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        System.exit(0);
    }
}
