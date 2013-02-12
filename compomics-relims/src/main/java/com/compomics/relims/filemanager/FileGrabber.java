/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.filemanager;

import com.compomics.relims.conf.RelimsVariableManager;
import com.compomics.relims.conf.RelimsProperties;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 *
 * @author Kenneth
 */
public class FileGrabber {

    private static List<String> fileList = new ArrayList<>();
    private static String projectSpecificRegex;
    private static FileGrabber fileGrabber;

    private FileGrabber() {
    }

    public static FileGrabber getInstance() {
        if (FileGrabber.fileGrabber == null) {
            FileGrabber.fileGrabber = new FileGrabber();
        }
        return FileGrabber.fileGrabber;
    }

    public static List<String> getIdentificationsList(String directoryName) {


        projectSpecificRegex = RelimsVariableManager.getProjectId() + "_";

        fileList = new ArrayList<>();
        File directory = new File(directoryName);
//get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile()) {
                if (file.getAbsolutePath().toString().contains(projectSpecificRegex)) {
                    if (file.getAbsolutePath().toString().contains(".omx") || file.getAbsolutePath().toString().contains("t.xml")) {
                        String filePath = file.getAbsolutePath().toString();
                        fileList.add(filePath);
                    }
                }
            } else if (file.isDirectory()) {
                getIdentificationsList(file.getAbsolutePath());
            }
        }
        return fileList;
    }

    public static String getMGFFile(String directoryName) {
        String fileName = RelimsVariableManager.getProjectId() + ".mgf";
        return fileName;
    }

    public String getFullMGFFile(String workSpace) {
        String fileName = workSpace + "/" + RelimsVariableManager.getProjectId() + ".mgf";
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


        Iterator itr = idList.iterator();
        while (itr.hasNext()) {
            String anIdentificationFile = (String) itr.next();
            fileNames.append(anIdentificationFile);
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
        //fileNames.append(RelimsVariableManager.getProjectId()).append(".omx").append(",");
        //fileNames.append(RelimsProperties.getWorkSpace().toString()).append("/");
        //fileNames.append(RelimsVariableManager.getProjectId()).append("t.xml");

        return fileNames.toString();
    }

    public static void deleteResultFolder() {
        if (!RelimsVariableManager.getClassicMode()) {
            File resultFolder = RelimsProperties.getWorkSpace();
            try {
                FileUtils.deleteDirectory(resultFolder);
            } catch (FileNotFoundException e) {
            } catch (IOException ex) {
                System.out.println("Could not delete the resultsfolder");
            }
        }
    }

    public File getPrideXML(long projectID) {
        String wildCardFilterString = "*" + projectID + ".xml.gz.1";
        File dir = new File(RelimsProperties.getLocalPrideXMLRepository());
        FileFilter fileFilter = new WildcardFileFilter(wildCardFilterString);
        File[] files = dir.listFiles(fileFilter);
        if (files != null) {
            return files[0];
        } else {
            return null;
        }
    }
}
