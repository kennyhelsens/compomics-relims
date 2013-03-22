package com.compomics.relims.model;

import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.omssa.xsd.UserModCollection;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * This class is a
 */
public class UserModsFile {

    private static Logger logger = Logger.getLogger(UserModsFile.class);
    private List<Modification> iMascotModifications = Lists.newArrayList();
    private List<UserMod> iOMSSAXSDModifications = Lists.newArrayList();

    public void write(File aFile) throws IOException {
  
        UserModCollection lUserModCollection = new UserModCollection();

        for (Modification lMascotModification : iMascotModifications) {
            UserMod lUserMod = UserModConverter.convert(lMascotModification);
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

    public Collection<String> getFixedModsAsString() {
        Collection<String> lResult = new ArrayList<String>();
        for (Modification lModification : iMascotModifications) {
            if (lModification.isFixed()) {
                lResult.add(UserModConverter.getModificationNameID(lModification, UserModConverter.hasLocation((lModification))));
            }
        }

        if (iOMSSAXSDModifications != null) {
            for (UserMod lRelimsMod : iOMSSAXSDModifications) {
                if (lRelimsMod.isFixed()) {
                    lResult.add(lRelimsMod.getModificationName());
                }
            }
        }

        return lResult;
    }

    public Collection<String> getVarModsAsString() {
        Collection<String> lResult = new ArrayList<String>();
        for (Modification lModification : iMascotModifications) {
            if (!lModification.isFixed()) {
                lResult.add(UserModConverter.getModificationNameID(lModification, UserModConverter.hasLocation(lModification)));
            }
        }

        if (iOMSSAXSDModifications != null) {
            for (UserMod lRelimsMod : iOMSSAXSDModifications) {
                if (!lRelimsMod.isFixed()) {
                    lResult.add(lRelimsMod.getModificationName());
                }
            }
        }

        return lResult;
    }

    public void setMascotModifications(List<Modification> aMascotModifications) {
        iMascotModifications = aMascotModifications;
    }

    public void setOMSSAXSDModifications(List<UserMod> aOMSSAXSDModifications) {
        iOMSSAXSDModifications = aOMSSAXSDModifications;
    }
}
