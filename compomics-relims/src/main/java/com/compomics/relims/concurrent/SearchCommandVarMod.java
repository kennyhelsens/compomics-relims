package com.compomics.relims.concurrent;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.guava.functions.SearchGuiModStringFunction;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.List;

/**
 * This class is a
 */
public class SearchCommandVarMod extends SearchCommandGenerator {
    private static Logger logger = Logger.getLogger(SearchCommandVarDb.class);
    private UserMod iUserMod = null;

    public SearchCommandVarMod(UserMod aUserMod, RelimsProjectBean aRelimsProjectBean, List<File> aSpectrumFiles) {
        super(aUserMod.getModificationName(), aRelimsProjectBean, aSpectrumFiles);
        iUserMod = aUserMod;
    }

    public SearchCommandVarMod(String aName, RelimsProjectBean aRelimsProjectBean, List<File> aSpectrumFiles) {
        super(aName, aRelimsProjectBean, aSpectrumFiles);
    }

    protected void applyChildMethods() {
        if (iUserMod != null) {
            Function<List<String>, String> lModFormatter = new SearchGuiModStringFunction();
            String lVariableModifications = iSearchGuiConfiguration.getProperty("VARIABLE_MODIFICATIONS").toString();
            List<String> lUpdatedMods = Lists.newArrayList();
            lUpdatedMods.add(lVariableModifications);
            lUpdatedMods.add(iUserMod.getModificationName());

            String lVariableMods = lModFormatter.apply(lUpdatedMods);

            iSearchGuiConfiguration.setProperty("VARIABLE_MODIFICATIONS", lVariableMods);
        }
    }
}
