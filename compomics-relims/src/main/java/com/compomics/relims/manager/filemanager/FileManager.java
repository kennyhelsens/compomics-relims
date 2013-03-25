/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.filemanager;

import com.compomics.relims.manager.variablemanager.ProcessVariableManager;
import com.compomics.relims.conf.RelimsProperties;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class FileManager {

    private static List<String> fileList = new ArrayList<String>();
    private static String projectSpecificRegex;
    private static FileManager fileGrabber;
    private static final Logger logger = Logger.getLogger(FileManager.class);

    private FileManager() {
    }

    public static FileManager getInstance() {
        if (FileManager.fileGrabber == null) {
            FileManager.fileGrabber = new FileManager();
        }
        return FileManager.fileGrabber;
    }

    public static List<String> getIdentificationsList(String directoryName) {

        fileList = new ArrayList<String>();
        File directory = new File(directoryName);
//get all the files from a directory

        File[] files = directory.listFiles();

        for (File aFile : files) {
            if (aFile.getAbsolutePath().endsWith(".omx") || aFile.getAbsolutePath().endsWith(".t.xml")) {
                fileList.add(aFile.getAbsolutePath());
            }
        }
        return fileList;
    }

    public static String getMGFFile(String directoryName) {
        String fileName = ProcessVariableManager.getProjectId() + ".mgf";
        return fileName;
    }

    public String getFullMGFFile(String workSpace) {
        String fileName = workSpace + "/" + ProcessVariableManager.getProjectId() + ".mgf";
        return fileName;
    }

    public File getGenericMGFFile(String workSpace) {
        File dir = new File(workSpace);
        FileFilter fileFilter = new WildcardFileFilter("*.mgf");
        File[] files = dir.listFiles(fileFilter);
        return files[0];
    }

    public static String getIdentificationFiles(String directoryName) {
        StringBuilder fileNames = new StringBuilder();
        List<String> idList = getIdentificationsList(directoryName);
        logger.debug("Found " + idList.size() + " identification files");
        Iterator itr = idList.iterator();
        while (itr.hasNext()) {
            String anIdentificationFile = (String) itr.next();
            fileNames.append(anIdentificationFile);
            logger.debug(anIdentificationFile);
            if (itr.hasNext()) {
                fileNames.append(",");
            }
        }
        /*       
         for (String anIdentificationFile : idList) {
         fileNames.append(anIdentificationFile);
         if (!idList.get(idList.size()).equals(anIdentificationFile)) {
         fileNames.append(",");
         }*/

        //fileNames.append(RelimsProperties.getWorkSpace().toString()).append("/");
        //fileNames.append(ProcessVariableManager.getProjectId()).append(".omx").append(",");
        //fileNames.append(RelimsProperties.getWorkSpace().toString()).append("/");
        //fileNames.append(ProcessVariableManager.getProjectId()).append("t.xml");

        return fileNames.toString();
    }

    public static void deleteResultFolder() {
        if (!ProcessVariableManager.getClassicMode()) {
            File resultFolder = RelimsProperties.getWorkSpace();
            try {
                FileUtils.deleteDirectory(resultFolder);
            } catch (FileNotFoundException e) {
            } catch (IOException ex) {
                System.out.println("Could not delete the resultsfolder");
            }
        }
    }

    public File getOldPrideXML(long projectID) {
        String wildCardFilterString = "*" + projectID + ".xml.gz";
        File dir = new File(RelimsProperties.getLocalPrideXMLRepository());
        FileFilter fileFilter = new WildcardFileFilter(wildCardFilterString);
        File[] files = dir.listFiles(fileFilter);
        if (files != null) {
            return files[0];
        } else {
            return null;
        }
    }

    public File getPrideXML(long projectID) {
        String wildCardFilterString = "PRIDE_Exp_Complete_Ac_" + projectID + ".xml" + "*";
        File dir = new File(RelimsProperties.getLocalPrideXMLRepository());
        FileFilter fileFilter = new WildcardFileFilter(wildCardFilterString);
        File[] files = dir.listFiles(fileFilter);
        if (files != null) {
//try to find the zipped ones first (a LOT more of these...)
            for (File aFile : files) {
                if (aFile.getAbsoluteFile().toString().endsWith(".gz")) {
                    return aFile;
                }
            }
//if not, maybe it is already unzipped !
            for (File aFile : files) {
                if (aFile.getAbsoluteFile().toString().endsWith(".xml")) {
                    return aFile;
                }
            }
            return null;
        } else {
            return null;
        }
    }
}
