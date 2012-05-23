package com.compomics.relims.model.guava.predicates;

import com.compomics.relims.model.interfaces.DataProvider;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * This class is a
 */
public class PredicateManager {


    private ProjectSizePredicate iProjectSizePredicate;
    private InstrumentPredicate iInstrumentPredicate;
    private ModificationSetPredicate iModificationSetPredicate;
    private SpeciesPredicate iSpeciesPredicate;
    private SearchSetSizePredicate iSearchSetSizePredicate;
    private final DataProvider iDataProvider;


    public PredicateManager(DataProvider aDataProvider) {
        iDataProvider = aDataProvider;

        ArrayList lAllowedInstruments = new ArrayList();
        lAllowedInstruments.add(8);
        lAllowedInstruments.add(9);
        lAllowedInstruments.add(10);

        iProjectSizePredicate = new ProjectSizePredicate(aDataProvider);

        iInstrumentPredicate = new InstrumentPredicate(new HashSet<Integer>(lAllowedInstruments));
        iModificationSetPredicate = new ModificationSetPredicate();
        iSpeciesPredicate = new SpeciesPredicate();
        iSearchSetSizePredicate = new SearchSetSizePredicate();

    }

    public ProjectSizePredicate getProjectSizePredicate() {
        return iProjectSizePredicate;
    }

    public InstrumentPredicate getInstrumentPredicate() {
        return iInstrumentPredicate;
    }

    public ModificationSetPredicate getModificationSetPredicate() {
        return iModificationSetPredicate;
    }

    public SearchSetSizePredicate getSearchSetSizePredicate() {
        return iSearchSetSizePredicate;
    }

    public SpeciesPredicate getSpeciesPredicate() {
        return iSpeciesPredicate;
    }
}
