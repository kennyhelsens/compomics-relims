package com.compomics.relims.tools;

import com.compomics.pride_asa_pipeline.service.PrideXmlExperimentService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.filemanager.FileManager;
import org.springframework.context.ApplicationContext;

import java.io.File;

/**
 * This class is a
 */
public class PrideAdhocCollector {

    final static int[] PRIDE_EXPERIMENTS = new int[]{3};
    private FileManager fileGrabber = FileManager.getInstance();
    private PrideXmlExperimentService iPrideService;


    public PrideAdhocCollector() {
        RelimsProperties.initialize();
        ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
        iPrideService = (PrideXmlExperimentService) lContext.getBean("prideXmlExperimentService");


        for (int lPrideExperiment : PRIDE_EXPERIMENTS) {
            File lPrideXML = fileGrabber.getPrideXML(lPrideExperiment);
            iPrideService.init(lPrideXML);
            System.out.println(iPrideService.getNumberOfSpectra());
        }
    }

    public static void main(String[] args) {
        new PrideAdhocCollector();
    }
}
