package com.compomics.relims.model;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.omssa.xsd.LocationTypeEnum;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.model.guava.functions.DoubleRounderFunction;

public class UserModConverter {
    private static DoubleRounderFunction iDoubleRounderFunction = new DoubleRounderFunction(4);

    public static UserMod convert(Modification lMascotModification) {
        UserMod lUserMod = new UserMod();

        String lResidue = lMascotModification.getLocation();
        String lLocation = lMascotModification.getLocation().toLowerCase();
        double lMonoMass = iDoubleRounderFunction.apply(lMascotModification.getMass());
        boolean hasResidue = hasLocation(lMascotModification);

        // Get LocationType.
        LocationTypeEnum lLocationTypeEnum = null;
        if (lLocation.startsWith("n_term") || lLocation.startsWith("n-term")) {
            // Nterm.
            String[] lSplit = lLocation.split(" ");
            if (lSplit.length == 1 || lSplit[1].toLowerCase().equals("protein")) {
                lLocationTypeEnum = LocationTypeEnum.MODNP;
            } else {
                lLocationTypeEnum = LocationTypeEnum.MODNPAA;
                lResidue = lSplit[1];
            }

        } else if (lLocation.startsWith("c_term") || lLocation.startsWith("c-term")) {
            String[] lSplit = lLocation.split(" ");
            if (lSplit.length == 1 || lSplit[1].toLowerCase().equals("protein")) {
                lLocationTypeEnum = LocationTypeEnum.MODCP;
            } else {
                lLocationTypeEnum = LocationTypeEnum.MODCPAA;
                lResidue = lSplit[1];
            }
        } else {
            lLocationTypeEnum = LocationTypeEnum.MODAA;
        }


        String lModificationName = getModificationNameID(lMascotModification, hasResidue);

        lUserMod.setFixed(lMascotModification.isFixed());
        lUserMod.setLocationType(lLocationTypeEnum);
        lUserMod.setLocation(lLocation);
        lUserMod.setMass(lMonoMass);
        lUserMod.setModificationName(lModificationName);
        if (hasResidue) {
            lUserMod.setLocation(lResidue);
        }
        return lUserMod;
    }


    public static boolean hasLocation(Modification lModification) {

        String lLocation = lModification.getLocation().toLowerCase();
        boolean hasResidue = false;

        if (lLocation.startsWith("n_term") || lLocation.startsWith("n-term")) {
            // Nterm.
            String[] lSplit = lLocation.split(" ");
            if (lSplit.length == 1 || lSplit[1].toLowerCase().equals("protein")) {
            } else {
                hasResidue = true;
            }

        } else if (lLocation.startsWith("c_term") || lLocation.startsWith("c-term")) {
            String[] lSplit = lLocation.split(" ");
            if (lSplit.length == 1 || lSplit[1].toLowerCase().equals("protein")) {
            } else {
                hasResidue = true;
            }
        } else {
            hasResidue = true;
        }
        return hasResidue;
    }

    public static String getModificationNameID(Modification aModification, boolean hasLocation) {
         String lModificationName = aModification.getType();
         if (hasLocation) {
             lModificationName = lModificationName.toLowerCase() + " of " + aModification.getLocation();
         }
         return lModificationName;
     }


}