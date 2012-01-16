package com.compomics.relims.model;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.omssa.xsd.LocationTypeEnum;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.relims.guava.functions.DoubleRounder;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is a
 */
public class UserModsFile {
    private static Logger logger = Logger.getLogger(UserModsFile.class);
    private ArrayList<Modification> iModifications;
    private ArrayList<UserMod> iRelimsModifications = null;
    DoubleRounder iDoubleRounder = new DoubleRounder(4);

    public UserModsFile(ArrayList<Modification> aModifications) {
        iModifications = aModifications;
    }

    public UserModsFile(ArrayList<Modification> aModifications, ArrayList<UserMod> aRelimsModifications) {
        iModifications = aModifications;
        iRelimsModifications = aRelimsModifications;
    }

    public void write(File aFile) throws IOException {
        UserModCollection lUserModCollection = new UserModCollection();

        for (Modification lModification : iModifications) {
            String lShortName = lModification.getShortType();
            String lResidue = lModification.getLocation();
            String lLocation = lModification.getLocation().toLowerCase();
            double lMonoMass = iDoubleRounder.apply(lModification.getMass());
            boolean hasResidue = hasLocation(lModification);

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

            UserMod lUserMod = new UserMod();

            String lModificationName = getModificationNameID(lModification, hasResidue);

            lUserMod.setLocationType(lLocationTypeEnum);
            lUserMod.setMass(lMonoMass);
            lUserMod.setModificationName(lModificationName);
            if (hasResidue) {
                lUserMod.setLocation(lResidue);
            }

            lUserModCollection.add(lUserMod);
        }


        // Add the relims mods, if any!
        if (iRelimsModifications != null && iRelimsModifications.size() > 0) {
            lUserModCollection.addAll(iRelimsModifications);
        }

        int lNumberOfMods = lUserModCollection.size();
        logger.debug("writing " + lNumberOfMods + " usermods to  " + aFile.getCanonicalPath());

        lUserModCollection.build(aFile);
    }

    public ArrayList<String> getFixedModsAsString() {
        ArrayList<String> lResult = new ArrayList<String>();
        for (Modification lModification : iModifications) {
            if (lModification.isFixed()) {
                lResult.add(getModificationNameID(lModification, hasLocation((lModification))));
            }
        }

        if(iRelimsModifications != null){
            for (UserMod lRelimsMod : iRelimsModifications) {
                if (lRelimsMod.isFixed()) {
                    lResult.add(lRelimsMod.getModificationName());
                }
            }
        }

        return lResult;
    }

    public ArrayList<String> getVarModsAsString() {
        ArrayList<String> lResult = new ArrayList<String>();
        for (Modification lModification : iModifications) {
            if (lModification.isFixed() == false) {
                lResult.add(getModificationNameID(lModification, hasLocation((lModification))));
            }
        }

        if (iRelimsModifications != null) {
            for (UserMod lRelimsMod : iRelimsModifications) {
                if (lRelimsMod.isFixed() == false) {
                    lResult.add(lRelimsMod.getModificationName());
                }
            }
        }

        return lResult;
    }

    private String getModificationNameID(Modification aModification, boolean hasLocation) {
        String lModificationName = aModification.getType();
        if (hasLocation) {
            lModificationName = lModificationName.toLowerCase() + " of " + aModification.getLocation();
        }
        return lModificationName;
    }

    ;


    private boolean hasLocation(Modification lModification) {

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

}
