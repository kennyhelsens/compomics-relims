package com.compomics.relims.model.guava.functions;

import com.compomics.relims.observer.Checkpoint;
import com.compomics.relims.observer.ProgressManager;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.collections.FastHashMap;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * This class is a
 */
public class ProteinJoinerFunction implements Function<ArrayList<String>, String> {

    Joiner lJoiner = Joiner.on("||");
    private static Logger logger = Logger.getLogger(ProteinJoinerFunction.class);
    private static FastHashMap iProteinMap;
    private boolean isPeptideStartIncluded = false;
    private String iCurrentPeptide = null;
    private static ProgressManager progressManager = ProgressManager.getInstance();
    
    public String apply(@Nullable ArrayList<String> input) {
        if (isPeptideStartIncluded) {
            ArrayList<String> lNewList = Lists.newArrayList();
            int lStartIndex = -1;

            for (String lAccession : lNewList) {
                Object lProteinSequenceObject = iProteinMap.get(lAccession);
                if (lProteinSequenceObject != null) {
                    String lProteinSequenceString = lProteinSequenceObject.toString();
                    lStartIndex = lProteinSequenceString.indexOf(iCurrentPeptide);
                }
                lNewList.add(lAccession + "*" + lStartIndex);
            }

            input = lNewList; // set the input pointer to the newly created List that includes the start indices.
        }

        String lResult = lJoiner.join(input);
        lResult = lResult.replaceAll(" ", "_");
        return lResult;
    }

    public void setsPeptideStartIncluded(boolean aSPeptideStartIncluded) {
        isPeptideStartIncluded = aSPeptideStartIncluded;
    }

    public void setCurrentPeptide(String aCurrentPeptide) {
        iCurrentPeptide = aCurrentPeptide;
    }

    public static void initProteinMap(File aFastaFile) {

        if (iProteinMap != null) {
            iProteinMap.clear();
        }
        iProteinMap = new FastHashMap();

        try {
            BufferedReader lReader = Files.newReader(aFastaFile, Charset.defaultCharset());
            String lCurrentSequence = null;
            String lCurrentAccession = null;

            boolean lineIsAccession = false;
            String line = null;

            while ((line = lReader.readLine()) != null) {
                if (line.startsWith(">")) {
                    lCurrentAccession = line.substring(1); // remove '>'
                    lineIsAccession = true;
                } else {
                    lCurrentSequence = line;
                    lineIsAccession = false;
                }

                if (!lineIsAccession) {
                    // the last line we have read is a sequence,
                    // so we can persist the accession-sequence pair into the hashmap.
                    iProteinMap.put(lCurrentAccession, lCurrentSequence);
                }
            }

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            progressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            progressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
        }

    }
}