/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.playground.diagnostictools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import java.util.TreeMap;

/**
 *
 * @author Kenneth
 */
public class MGFResultFinder {

    private static TreeMap<Date, File> mgfList;

      public static void main(String args[]){
        findMGF(new File("V:/relims_results/admin"));
    }
    
    public static void findMGF(File directory) {
        File[] files = directory.listFiles();
        File[] subFiles;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();
        mgfList = new TreeMap<Date, File>();
        try {
            //  String directory = "Z:/remote_relims/Results/admin";
            // String directory = "C:/Users/Kenneth/Documents/NetBeansProjects/Remote_Relims/Output/data/admin/";
            // File outputFile = new File("C:/Users/Kenneth/Documents/NetBeansProjects/Remote_Relims/Output/mgflist.txt");
            try {
                //create an output file : 
                //Close the output stream

                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file) {
                        if ((file.getName().endsWith("MGF")
                                || file.getName().endsWith(".mgf"))
                                && file.length() > 1024) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };

                //ALL these filese are directories...
                System.out.println(" ");

                for (File aFile : files) {
                    if (aFile.isDirectory()) {
                        subFiles = aFile.listFiles(fileFilter);
                        for (File aSubFile : subFiles) {
                            mgfList.put(new Date(aSubFile.lastModified()), aSubFile);
                        }
                    }
                }
                System.out.println(" ");

                if (mgfList.isEmpty()) {
                    System.out.println("No projects with mgf were found...");
                } else {
                    System.out.println("Projects with mgf : ");
                    System.out.println("Workable mgf : " + mgfList.size() + "/" + directory.listFiles().length);
                }
                int i = 0;
                for (Date aHit : mgfList.keySet()) {
                    i++;
                    System.out.println(i + ".");
                    System.out.println("ProjectID : [" + mgfList.get(aHit).getName().replace(".mgf", "") + "] created at : " + sdf.format(aHit));
                    System.out.println("Location : " + mgfList.get(aHit).getAbsolutePath());
                    long fileSize = mgfList.get(aHit).length();
                    /*0=b
                     1=kb
                     2=mb
                     3=gb
                     4=tb*/
                    int unit = 0;
                    while (fileSize > 1024) {
                        fileSize = fileSize / 1024;
                        unit++;
                    }
                    String unitSize = "b";
                    switch (unit) {
                        case 0:
                            unitSize = "b";
                            break;
                        case 1:
                            unitSize = "kb";
                            break;
                        case 2:
                            unitSize = "mb";
                            break;
                        case 3:
                            unitSize = "gb";
                            break;
                        case 4:
                            unitSize = "tb";
                            break;
                    }
                    System.out.println("Size : " + fileSize + " " + unitSize);
                }
            System.out.println("");
            System.out.println("Results as found on " + sdf.format(today));
        } catch (Exception e) {//Catch exception if any
            e.printStackTrace();
            System.err.println("Error: " + e.getMessage());
        } finally {
            System.out.println("Total mgf : " + mgfList.size() + "/" + directory.listFiles().length);
            System.out.println(" ");
            System.out.println("Would you like to export these results? y/n ");
            Scanner scan = new Scanner(System.in);
            String input = "";
            while (!input.equalsIgnoreCase("exit") && !input.equalsIgnoreCase("n")) {
                input = scan.nextLine();
                if (input.equalsIgnoreCase("n")) {
                    break;
                }
                if (input.equalsIgnoreCase("y")) {
                    boolean saved = false;
                    while (!saved) {
                        System.out.println("Please provide the location of the outputfile :");
                        String outputString = scan.nextLine();
                        File outputFile = new File(outputString);

                        try {
                            System.out.println("Writing to resultfile...");
                            FileWriter fstream = new FileWriter(outputFile, true);
                            BufferedWriter out = new BufferedWriter(fstream);

                            try {
                                out.append("Projects with mgf : ");
                                out.newLine();
                                if (mgfList.isEmpty()) {
                                    out.append("Total mgf : none");
                                } else {
                                    out.append("Total mgf : " + mgfList.size() + "/" + directory.listFiles().length);
                                }
                                out.newLine();
                                int i = 0;
                                for (Date aHit : mgfList.keySet()) {
                                    i++;
                                    System.out.println(i + ".");
                                    out.append("ProjectID : [" + mgfList.get(aHit).getName().replace(".mgf", "") + "] created at : " + sdf.format(aHit));
                                    out.newLine();
                                    out.append("Location : " + mgfList.get(aHit).getAbsolutePath());
                                    out.newLine();

                                    //determine files size

                                    long fileSize = mgfList.get(aHit).length();
                                    /*0=b
                                     1=kb
                                     2=mb
                                     3=gb
                                     4=tb*/
                                    int unit = 0;
                                    while (fileSize > 1024) {
                                        fileSize = fileSize / 1024;
                                        unit++;
                                    }
                                    String unitSize = "b";
                                    switch (unit) {
                                        case 0:
                                            unitSize = "b";
                                            break;
                                        case 1:
                                            unitSize = "kb";
                                            break;
                                        case 2:
                                            unitSize = "mb";
                                            break;
                                        case 3:
                                            unitSize = "gb";
                                            break;
                                        case 4:
                                            unitSize = "tb";
                                            break;
                                    }
                                    out.append("Size : " + fileSize + " " + unitSize);
                                    out.newLine();
                                }
                            } catch (Exception e) {//Catch exception if any
                                System.err.println("Error: " + e.getMessage());
                            } finally {
                                out.newLine();
                                out.append("Results as found on " + sdf.format(today));
                                System.out.println("Finished writing file !");
                                out.newLine();
                                out.close();
                            }
                            saved = true;
                        } catch (Exception e) {
                            System.out.println("An error has occurred please check if the path to the outputfile is correct.");
                        }
                    }
                    break;
                }
                if (!input.equalsIgnoreCase("exit") & !input.equalsIgnoreCase("y") & !input.equalsIgnoreCase("n")) {
                    System.out.println("Please use the following syntax :");
                    System.out.println("[y] to store the results in a file");
                    System.out.println("[n] to not store and exit this program.");
                }
            }


        }
    }
    catch (Exception e

    
        ) {
            e.printStackTrace();
    }
}
}
