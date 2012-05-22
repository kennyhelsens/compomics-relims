package com.compomics.relims.model;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.omssa.xsd.LocationTypeEnum;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.omssa.xsd.UserModCollection;
import com.compomics.relims.model.guava.functions.DoubleRounderFunction;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class is a
 */
public class UserModsFile {
    private static Logger logger = Logger.getLogger(UserModsFile.class);
    private DoubleRounderFunction iDoubleRounderFunction = new DoubleRounderFunction(4);

    private ArrayList<Modification> iMascotModifications = Lists.newArrayList();
    private ArrayList<UserMod> iOMSSAXSDModifications = Lists.newArrayList();

    public UserModsFile() {

    }

    public void write(File aFile) throws IOException {
        UserModCollection lUserModCollection = new UserModCollection();

        for (Modification lModification : iMascotModifications) {
            String lShortName = lModification.getShortType();
            String lResidue = lModification.getLocation();
            String lLocation = lModification.getLocation().toLowerCase();
            double lMonoMass = iDoubleRounderFunction.apply(lModification.getMass());
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
        if (iOMSSAXSDModifications != null && iOMSSAXSDModifications.size() > 0) {
            lUserModCollection.addAll(iOMSSAXSDModifications);
        }

        int lNumberOfMods = lUserModCollection.size();
        logger.debug("writing " + lNumberOfMods + " usermods to  " + aFile.getCanonicalPath());

        lUserModCollection.build(aFile);
    }

    public ArrayList<String> getFixedModsAsString() {
        ArrayList<String> lResult = new ArrayList<String>();
        for (Modification lModification : iMascotModifications) {
            if (lModification.isFixed()) {
                lResult.add(getModificationNameID(lModification, hasLocation((lModification))));
            }
        }

        if(iOMSSAXSDModifications != null){
            for (UserMod lRelimsMod : iOMSSAXSDModifications) {
                if (lRelimsMod.isFixed()) {
                    lResult.add(lRelimsMod.getModificationName());
                }
            }
        }

        return lResult;
    }

    public ArrayList<String> getVarModsAsString() {
        ArrayList<String> lResult = new ArrayList<String>();
        for (Modification lModification : iMascotModifications) {
            if (lModification.isFixed() == false) {
                lResult.add(getModificationNameID(lModification, hasLocation((lModification))));
            }
        }

        if (iOMSSAXSDModifications != null) {
            for (UserMod lRelimsMod : iOMSSAXSDModifications) {
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


    public void setMascotModifications(ArrayList<Modification> aMascotModifications) {
        iMascotModifications = aMascotModifications;
    }

    public void setOMSSAXSDModifications(ArrayList<UserMod> aOMSSAXSDModifications) {
        iOMSSAXSDModifications = aOMSSAXSDModifications;
    }
}
