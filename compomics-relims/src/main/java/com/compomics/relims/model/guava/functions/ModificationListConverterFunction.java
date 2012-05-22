package com.compomics.relims.model.guava.functions;

import com.compomics.mascotdatfile.util.mascot.FixedModification;
import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.compomics.mascotdatfile.util.mascot.VariableModification;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is a
 */
public class ModificationListConverterFunction implements Function<ModificationList, String> {
    public String apply(@Nullable ModificationList aModificationList) {
        ArrayList<String> lModifications = new ArrayList<String>();
        for (Object o : aModificationList.getFixedModifications()) {
            FixedModification lFixMod = (FixedModification) o;
            lModifications.add(lFixMod.getType());
        }
        for (Object o : aModificationList.getVariableModifications()) {
            VariableModification lVarMod = (VariableModification) o;
            lModifications.add(lVarMod.getType());
        }
        Collections.sort(lModifications, String.CASE_INSENSITIVE_ORDER);
        return Joiner.on(",").join(lModifications);
    }
}