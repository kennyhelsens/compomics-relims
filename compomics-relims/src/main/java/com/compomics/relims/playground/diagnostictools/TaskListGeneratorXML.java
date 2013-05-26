/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.playground.diagnostictools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 *
 * @author Kenneth
 */
public class TaskListGeneratorXML {

    public static void main(String[] args) {
        try {
            String directory = "Z:/PRIDE-DATA/PRIDE-FTP-DOWNLOAD/";
            File outputFile = new File("Z:/remote_relims/xmlprojects.txt");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileWriter fstream = new FileWriter(outputFile, true);
            BufferedWriter out = new BufferedWriter(fstream);
            try {
                //create an output file : 
                //Close the output stream
                File dir = new File(directory);
                File[] files;
                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file) {
                        if (file.getName().endsWith(".xml") || file.getName().endsWith(".xml.gz") || file.getName().endsWith(".xml.gz.1")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
                files = dir.listFiles(fileFilter);
                if (files.length
                        == 0) {
                    System.out.println("Either dir does not exist or is not a directory");
                } else {

                    Set<String> projectIDs = new LinkedHashSet<String>();
                    for (int i = 0; i < files.length; i++) {
                        File filename = files[i];
                        String projectID = filename.getName().toString();
                        projectID = projectID.replace("PRIDE_Exp_Complete_Ac_", "");
                        projectID = projectID.replace(".xml", "");
                        projectID = projectID.replace(".gz", "");
                        if (!projectID.contains(".")) {
                            projectIDs.add(projectID);
                        }
                    }

                    System.out.println("#Files : " + files.length);
                    System.out.println("#Projects : " + projectIDs.size());

                    for (String aProjectID : projectIDs) {
                        out.write(aProjectID);
                        out.newLine();
                    }

                }
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            } finally {
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
