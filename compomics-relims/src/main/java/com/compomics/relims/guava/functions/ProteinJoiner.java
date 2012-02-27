package com.compomics.relims.guava.functions;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is a
 */
public class ProteinJoiner implements Function<ArrayList<String>, String> {
    Joiner lJoiner = Joiner.on("||");

    public String apply(@Nullable ArrayList<String> input) {
        String lResult = lJoiner.join(input);
        lResult = lResult.replaceAll(" ", "_");
        return lResult;
    }
};
