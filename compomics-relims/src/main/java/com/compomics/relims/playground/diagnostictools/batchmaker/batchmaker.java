/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.playground.diagnostictools.batchmaker;

import com.compomics.relims.conf.RelimsProperties;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author Kenneth
 */
public class batchmaker {

    public static void makeBlastDbBatch() {
        String osName = null;
        BufferedWriter out = null;
        try {
            RelimsProperties.initialize(false);
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                osName = "windows";
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                osName = "mac";
            } else if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                osName = "windows";
            } else {
                System.out.println("Unsupported operating system !");
            }

            File makeBlastDbFile = new File(RelimsProperties.getSearchGuiFolder() + "/resources/makeblastdb/" + osName + "/makeblastdb");
            File fastaParentFolder = new File(RelimsProperties.getDefaultSearchDatabase()).getParentFile();
            //MAKE A BATCH FILE !!!!
            String driveLetter = "" + fastaParentFolder.getAbsolutePath().charAt(0);

            File tempBatch = new File("tempRelims.bat");
            out = new BufferedWriter(new FileWriter("C:/Users/Kenneth/Desktop/tempRelims.bat"));
            //1 = move this process to the fastaParent
            out.write(driveLetter + ":");
            out.newLine();
            out.write("cd " + fastaParentFolder.getAbsolutePath());
            out.newLine();
            out.write(makeBlastDbFile.getAbsolutePath() + " -in " + RelimsProperties.getDefaultSearchDatabase());
            out.newLine();
            out.write("pause");
            //2 = tell the process to run the makeblastdb
            out.close();
        } catch (IOException e) {
            if (out != null) {
                out = null;
            }
        }
    }
}