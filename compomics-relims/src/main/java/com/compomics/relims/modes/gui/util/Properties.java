package com.compomics.relims.modes.gui.util;

import com.compomics.relims.conf.RelimsProperties;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * This class makes it easy to get the version number from the pom file, and the
 * path to the jar file.
 *
 */
public class Properties {

    private static Logger logger = Logger.getLogger(Properties.class);

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
    public String getRootFolder() {
//        String path = getClass().getResource("Properties.class").getPath();
        String path = RelimsProperties.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        try {
            path = URLDecoder.decode(path, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

        if (path.matches(".*jar.*")) {
            // Working with a jar file!
            if (path.lastIndexOf("/compomics-relims") != -1) {
//            path = path.substring(5, path.lastIndexOf("/compomics-relims"));
                path = path.substring(0, path.lastIndexOf("/compomics-relims"));
                path = path.replace("%20", " ");
                path = path.replace("%5b", "[");
                path = path.replace("%5d", "]");

                if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
                    path = path.replace("/", "\\");
                }
            } else {
                path = ".";
            }
        } else {
            // Working from IDE!
            // Working with a jar file!
            if (path.lastIndexOf("/compomics-relims") != -1) {
                //            path = path.substring(5, path.lastIndexOf("/compomics-relims"));
                path = path.substring(0, path.lastIndexOf("/compomics-relims") + 17);
                path = path.replace("%20", " ");
                path = path.replace("%5b", "[");
                path = path.replace("%5d", "]");

                if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
                    path = path.replace("/", "\\");
                }
            } else {
                path = ".";
            }
        }
        return path;
    }
}
