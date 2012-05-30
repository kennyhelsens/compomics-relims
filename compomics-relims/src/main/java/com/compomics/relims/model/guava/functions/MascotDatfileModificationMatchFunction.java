package com.compomics.relims.model.guava.functions;

import com.compomics.omssa.xsd.LocationTypeEnum;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;

/**
 * This class is a
 */
public class MascotDatfileModificationMatchFunction implements Function<UserMod, PTM> {
    DoubleRounderFunction iDoubleRounderFunction = new DoubleRounderFunction(2);
    private static Logger logger = Logger.getLogger(MascotDatfileModificationMatchFunction.class);

    public PTM apply(@Nullable UserMod aMod) {
        PTMFactory lPTMFactory = RelimsProperties.getPTMFactory(false);

        double lSpecifiedMass = 0;
        if (aMod != null) {
            lSpecifiedMass = iDoubleRounderFunction.apply(aMod.getMass());
        }

        for (String lPTMName : lPTMFactory.getPTMs()) {
            PTM lPTM = lPTMFactory.getPTM(lPTMName);
            Iterable<String> lPTMResidues = lPTM.getResidues();
            double lRunningMass = iDoubleRounderFunction.apply(lPTM.getMass());
            if (Double.compare(lSpecifiedMass, lRunningMass) == 0) {

                // Amino acid modification should not have a "non-term" location
                int lLocationTypeID = 0;
                if (aMod != null) {
                    lLocationTypeID = aMod.getLocationType().getLocationTypeID();
                }

                if (lPTM.getType() == 0) {
                    if (aMod != null) {

                        if (lLocationTypeID != 0) {
                            continue;
                        }

                        if (aMod.getLocation().length() == 1) {
                            String lModAA = aMod.getLocation().substring(0, 1);
                            for (String lResidue : lPTMResidues) {
                                if (lModAA.equals(lResidue)) {
                                    logMatchOk(aMod, lPTM);
                                    return lPTM;
                                }
                            }
                        }
                    }

                } else {
                    if (
                            lLocationTypeID == LocationTypeEnum.MODN.getLocationTypeID() ||
                                    lLocationTypeID == LocationTypeEnum.MODNAA.getLocationTypeID() ||
                                    lLocationTypeID == LocationTypeEnum.MODNP.getLocationTypeID() ||
                                    lLocationTypeID == LocationTypeEnum.MODNPAA.getLocationTypeID()
                            ) {

                        for (String lResidue : lPTMResidues) {
                            if (lResidue.equals("[")) {
                                logMatchOk(aMod, lPTM);
                                return lPTM;
                            }
                        }

                    } else if (lLocationTypeID == LocationTypeEnum.MODC.getLocationTypeID() ||
                            lLocationTypeID == LocationTypeEnum.MODCAA.getLocationTypeID() ||
                            lLocationTypeID == LocationTypeEnum.MODCP.getLocationTypeID() ||
                            lLocationTypeID == LocationTypeEnum.MODCPAA.getLocationTypeID()
                            ) {

                        for (String lResidue : lPTMResidues) {
                            if (lResidue.equals("]")) {
                                logMatchOk(aMod, lPTM);
                                return lPTM;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private void logMatchOk(UserMod aModification, PTM aPTM) {
        String lOmssaName = aPTM.getName();
        String lOmssaLocation = Joiner.on(",").join(aPTM.getResidues());
        String lMascotName = aModification.getModificationName();
        String lMascotLocation = aModification.getLocation();
        logger.debug("matched " + lOmssaName + ":" + lOmssaLocation + "(OMSSA) to " + lMascotName + ":" + lMascotLocation + "(Converted MASCOT).");
    }
}
