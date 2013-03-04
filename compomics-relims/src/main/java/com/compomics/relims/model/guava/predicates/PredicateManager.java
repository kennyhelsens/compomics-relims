package com.compomics.relims.model.guava.predicates;

import com.compomics.relims.model.interfaces.DataProvider;
import com.google.common.base.Predicate;

import java.util.Collection;
import java.util.Vector;

/**
 * This class is a
 */
public class PredicateManager {

    public static enum Types {SPECIES, PROJECT_SIZE, INSTRUMENT, SEARCH_SET_SIZE}

    private ProjectSizePredicate iProjectSizePredicate;
    private InstrumentPredicate iInstrumentPredicate;
    private SpeciesPredicate iSpeciesPredicate;
    private SearchSetSizePredicate iSearchSetSizePredicate;

    private Collection<Predicate> iPredicateCollection;


    public PredicateManager(DataProvider aDataProvider) {
        iInstrumentPredicate = new InstrumentPredicate(aDataProvider);
//        iProjectSizePredicate = new ProjectSizePredicate(aDataProvider);

//        iSpeciesPredicate = new SpeciesPredicate();
//        iSearchSetSizePredicate = new SearchSetSizePredicate();

    }

    public Collection<Predicate> createCollection(Types... aTypes){
        iPredicateCollection = new Vector<Predicate>();
        for (Types lType : aTypes) {

            switch (lType) {

                case SPECIES:
                    iPredicateCollection.add(getSpeciesPredicate());
                    break;

                case PROJECT_SIZE:
                    iPredicateCollection.add(getProjectSizePredicate());
                    break;

                case INSTRUMENT:
                    iPredicateCollection.add(getInstrumentPredicate());
                    break;

                case SEARCH_SET_SIZE:
                    iPredicateCollection.add(getSearchSetSizePredicate());
                    break;
            }

        }

        return iPredicateCollection;
    }

    private ProjectSizePredicate getProjectSizePredicate() {
        return iProjectSizePredicate;
    }

    private InstrumentPredicate getInstrumentPredicate() {
        return iInstrumentPredicate;
    }

    private SearchSetSizePredicate getSearchSetSizePredicate() {
        return iSearchSetSizePredicate;
    }

    private SpeciesPredicate getSpeciesPredicate() {
        return iSpeciesPredicate;
    }
}
