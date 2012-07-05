package com.compomics.relims.gui.util;

import java.io.InputStream;

/**
 * This class makes it easy to get the version number from the pom file, and the
 * path to the jar file.
 *
 * @author Harald Barsnes
 */
public class Properties {

    /**
     * Creates a new empty Properties object.
     */
    public Properties() {
    }

    /**
     * Retrieves the version number set in the pom file.
     *
     * @return the version number of the tool
     */
    public String getVersion() {

        java.util.Properties p = new java.util.Properties();

        try {
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("compomics-relims.properties");
            p.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return p.getProperty("compomics-relims.version");
    }

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    public String getJarFilePath() {
        String path = getClass().getResource("Properties.class").getPath();

        if (path.lastIndexOf("/compomics-relims-") != -1) {
            path = path.substring(5, path.lastIndexOf("/compomics-relims-"));
            path = path.replace("%20", " ");
            path = path.replace("%5b", "[");
            path = path.replace("%5d", "]");

            if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
                path = path.replace("/", "\\");
            }
        } else {
            path = ".";
        }

        return path;
    }
}
