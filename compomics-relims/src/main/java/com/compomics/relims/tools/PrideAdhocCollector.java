package com.compomics.relims.tools;

import com.compomics.pridexmltomgfconverter.tools.PrideXMLToMGFConverter;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.filemanager.FileManager;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.*;
import com.google.common.io.Files;
import org.apache.log4j.Logger;
import uk.ac.ebi.pride.jaxb.xml.PrideXmlReader;

import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * This class is a
 */
public class PrideAdhocCollector {

    final static int[] PRIDE_EXPERIMENTS = new int[]{1,2,3};
    private static Logger logger = Logger.getLogger(PrideAdhocCollector.class);
    Function<PrideXmlReader, HashMap> iFunction = fSpectrumTypeExtractor;


    public PrideAdhocCollector() {
        RelimsProperties.initialize();

        FileManager iFileGrabber = FileManager.getInstance();
        PrideXMLToMGFConverter iPrideXMLConverter = PrideXMLToMGFConverter.getInstance();
        try {

            HashSet lResultColumns = Sets.newHashSet();
            HashMap<String, Map> lFullResults = Maps.newHashMap();
            for (int lPrideExperiment : PRIDE_EXPERIMENTS) {
                File lPrideXML = iFileGrabber.getPrideXML(lPrideExperiment);
                iPrideXMLConverter.init(lPrideXML);

                HashMap lMap = iFunction.apply(iPrideXMLConverter.getPrideXmlReader());
                lResultColumns.addAll(lMap.keySet());
                lFullResults.put(iPrideXMLConverter.getPrideXmlReader().getExpAccession(), lMap);

                iPrideXMLConverter.clearTempFiles();
            }

            generateReport(lFullResults);


        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

    }

    private void generateReport(HashMap<String, Map> aFullResults) throws IOException {
        File lTmp = File.createTempFile("pride_adhoc", "csv");
        logger.debug(String.format("writing results to %s", lTmp.getAbsolutePath()));

        BufferedWriter lWriter = Files.newWriter(lTmp, Charset.defaultCharset());
        List<String> lVariables = Lists.newArrayList();

        Joiner lJoiner = Joiner.on(",");

        List<String> lHeader = Lists.newArrayList();
        lHeader.add("accession");
        lHeader.addAll(lVariables);

        lWriter.write(lJoiner.join(lVariables));
        lWriter.newLine();
        logger.debug(String.format("writing header %s", lJoiner.join(lHeader)));
        for(String lExpAccession : aFullResults.keySet()){
            List<String> lObservation = Lists.newArrayList();
            lObservation.add(lExpAccession);
            for(String lVariable : lVariables){
                Object value = aFullResults.get(lExpAccession).get(lVariable);
                if(value == null){
                    lObservation.add("NA");
                }else{
                    lObservation.add(value.toString());
                }
            }
            logger.debug(String.format("writing observation %s", lJoiner.join(lObservation)));
            lWriter.write(lJoiner.join(lObservation));
            lWriter.newLine();
        }
    }


    private static Function<PrideXmlReader, HashMap> fSpectrumTypeExtractor = new Function<PrideXmlReader, HashMap>() {
        @Override
        public HashMap apply(@Nullable PrideXmlReader input) {
            HashMultiset<Integer> lCounterSet = HashMultiset.create();
            int lCounter = 0;
            for (String lSpectrumId : input.getSpectrumIds()) {

                logger.debug(lCounter++ + " " + lSpectrumId);
                lCounterSet.add(input.getSpectrumMsLevel(lSpectrumId));
            }

            HashMap<String, Integer> lResult = Maps.newHashMap();
            for (Multiset.Entry lEntry : lCounterSet.entrySet()) {
                lResult.put(lEntry.getElement().toString(), lEntry.getCount());
            }

            return lResult;
        }
    };


    public static void main(String[] args) {
        new PrideAdhocCollector();
    }
}
