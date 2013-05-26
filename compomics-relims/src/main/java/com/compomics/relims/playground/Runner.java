package com.compomics.relims.playground;

import com.compomics.omssa.xsd.LocationTypeEnum;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.omssa.xsd.UserModCollection;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * This class is a
 */
public class Runner {


    private static Logger logger = Logger.getLogger(Runner.class);

    public static void main(String[] args) {
        UserMod lUserMod = new UserMod();
        lUserMod.setLocationType(LocationTypeEnum.MODC);
        lUserMod.setModificationName("arginylation");
        lUserMod.setMass(50.00);

        UserMod lUserMod2 = new UserMod();
        lUserMod2.setLocationType(LocationTypeEnum.MODAA);
        lUserMod2.setModificationName("oxidation");
        lUserMod2.setLocation("M");
        lUserMod2.setMass(16.00);

        UserModCollection lUserModCollection = new UserModCollection();
        lUserModCollection.add(lUserMod);
        lUserModCollection.add(lUserMod2);

        File lFile = new File("/Users/kennyhelsens/tmp/test2.mods.xml");
        try {
            lFile.createNewFile();
            lUserModCollection.build(lFile);
        } catch (IOException e) {
        }

        System.out.println(lUserModCollection.toString());

    }

}
