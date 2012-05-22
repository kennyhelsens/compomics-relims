package com.compomics.relims.model.guava.functions;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class is a
 */
public class ModificationMatchFunction implements Function<Modification, PTM> {
    DoubleRounderFunction iDoubleRounderFunction = new DoubleRounderFunction(2);
    private static Logger logger = Logger.getLogger(ModificationMatchFunction.class);

    public PTM apply(@Nullable Modification aModification) {
        PTMFactory lPTMFactory = RelimsProperties.getPTMFactory(false);

        double lSpecifiedMass = iDoubleRounderFunction.apply(aModification.getMass());

        Iterator<PTM> lPtmIterator = lPTMFactory.getPtmIterator();
        while (lPtmIterator.hasNext()) {
            PTM lPTM = lPtmIterator.next();
            ArrayList<String> lPTMResidues = lPTM.getResidues();
            double lRunningMass = iDoubleRounderFunction.apply(lPTM.getMass());
            if (Double.compare(lSpecifiedMass, lRunningMass) == 0) {

                // Amino acid modification should not have a mascot "term-like" location
                if (lPTM.getType() == 0) {
                    if (aModification.getLocation().toLowerCase().indexOf("term") > 0) {
                        continue;
                    }
                    if (aModification.getLocation().length() == 1) {
                        String lModAA = aModification.getLocation().substring(0, 1);
                        for (String lResidue : lPTMResidues) {
                            if (lModAA.equals(lResidue)) {
                                logMatchOk(aModification, lPTM);
                                return lPTM;
                            }
                            ;
                        }
                    }
                }else{
                    if (aModification.getLocation().toLowerCase().indexOf("n-term") >= 0) {
                        for (String lResidue : lPTMResidues) {
                            if (lResidue.equals("[")) {
                                logMatchOk(aModification, lPTM);
                                return lPTM;
                            }
                        }
                    } else if (aModification.getLocation().toLowerCase().indexOf("c-term") >= 0) {
                        for (String lResidue : lPTMResidues) {
                            if (lResidue.equals("]")) {
                                logMatchOk(aModification, lPTM);
                                return lPTM;
                            }
                        }
                    }
                }


            }
        }
        return null;
    }

    private void logMatchOk(Modification aModification, PTM aPTM) {
        String lOmssaName = aPTM.getName();
        String lOmssaLocation = Joiner.on(",").join(aPTM.getResidues());
        String lMascotName = aModification.getType();
        String lMascotLocation = aModification.getLocation();
        logger.debug("matched " + lOmssaName + ":" + lOmssaLocation + "(OMSSA) to " + lMascotName + ":" + lMascotLocation + "(MASCOT).");
    }
}
