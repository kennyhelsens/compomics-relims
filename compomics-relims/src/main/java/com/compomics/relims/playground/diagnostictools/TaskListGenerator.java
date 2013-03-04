/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.playground.diagnostictools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Kenneth
 */
public class TaskListGenerator {

    public static void main(String[] args) {
        try {
            String directory = "Z:/remote_relims/Repository/PRIDE";
            File outputFile = new File(directory + "projects.txt");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileWriter fstream = new FileWriter(outputFile,true);
            BufferedWriter out = new BufferedWriter(fstream);
            try {
                //create an output file : 
                //Close the output stream
                File dir = new File(directory);
                File[] files = dir.listFiles();
                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file) {
                        return file.isDirectory();
                    }
                };
                files = dir.listFiles(fileFilter);
                if (files.length
                        == 0) {
                    System.out.println("Either dir does not exist or is not a directory");
                } else {
                    for (int i = 0; i < files.length; i++) {
                        File filename = files[i];
                        String projectID = filename.getName().toString();
                        out.write(projectID);
                        out.newLine();
                        System.out.println(filename.toString());
                    }
                }
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            } finally {
                out.close();
            }
        } catch (IOException e) {
        }
    }
}
