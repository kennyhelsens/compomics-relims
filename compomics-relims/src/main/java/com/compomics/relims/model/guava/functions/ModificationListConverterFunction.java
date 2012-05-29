package com.compomics.relims.model.guava.functions;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.google.common.base.Function;
import com.google.common.base.Joiner;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is a
 */
public class ModificationListConverterFunction implements Function<ModificationList, String> {
    public String apply(@Nullable ModificationList aModificationList) {
        List<String> lModifications = new ArrayList<String>();
        if (aModificationList != null) {
            for (Object o : aModificationList.getFixedModifications()) {
                Modification lFixMod = (Modification) o;
                lModifications.add(lFixMod.getType());
            }
        }
        if (aModificationList != null) {
            for (Object o : aModificationList.getVariableModifications()) {
                Modification lVarMod = (Modification) o;
                lModifications.add(lVarMod.getType());
            }
        }
        Collections.sort(lModifications, String.CASE_INSENSITIVE_ORDER);
        return Joiner.on(",").join(lModifications);
    }
}