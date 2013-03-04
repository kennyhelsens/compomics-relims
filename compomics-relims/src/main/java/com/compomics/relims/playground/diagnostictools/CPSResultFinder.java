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
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

/**
 *
 * @author Kenneth
 */
public class CPSResultFinder {

    /*
     * To change this template, choose Tools | Templates
     * and open the template in the editor.
     */
    public static void findCPS(File directory) {

        TreeMap<Date, File> hitList = new TreeMap<Date, File>();
        File[] files = directory.listFiles();
        File[] subFiles;
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        Calendar calendar = Calendar.getInstance();
        Date today = calendar.getTime();

        try {
            try {
                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file) {
                        if (file.getName().endsWith(".cps") || file.getName().endsWith(".CPS")) {
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
                            hitList.put(new Date(aSubFile.lastModified()), aSubFile);
                        }
                    }
                }
                System.out.println(" ");

                if (hitList.isEmpty()) {
                    System.out.println("No projects with hits were found...");
                } else {
                    System.out.println("Projects with hits : ");
                    System.out.println("Total hits : " + hitList.size() + "/" + directory.listFiles().length);
                }
                int i = 0;
                for (Date aHit : hitList.keySet()) {
                    i++;
                    System.out.println(i);
                    System.out.println("ProjectID : [" + hitList.get(aHit).getName().replace(".cps", "") + "] created at : " + sdf.format(aHit));
                    System.out.println("Location : " + hitList.get(aHit).getAbsolutePath());
                }

                System.out.println("");
                System.out.println("Results as found on " + sdf.format(today));
            } catch (Exception e) {//Catch exception if any
                e.printStackTrace();
                System.err.println("Error: " + e.getMessage());
            } finally {
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
                                System.out.println("Writing  to resultfile...");
                                FileWriter fstream = new FileWriter(outputFile, true);
                                BufferedWriter out = new BufferedWriter(fstream);
                                try {
                                    out.append("Projects with hits : ");
                                    out.newLine();
                                    if (hitList.isEmpty()) {
                                        out.append("Total hits : none");
                                    } else {
                                        out.append("Total hits : " + hitList.size());
                                    }
                                    out.newLine();
                                    int i = 0;
                                    for (Date aHit : hitList.keySet()) {
                                        i++;
                                        System.out.println(i + ".");
                                        out.append("ProjectID : [" + hitList.get(aHit).getName().replace(".cps", "") + "] created at : " + sdf.format(aHit));
                                        out.newLine();
                                        out.append("Location : " + hitList.get(aHit).getAbsolutePath());
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void findCPSAndExport(File directory, File outputFile) {
        try {
            //  String directory = "Z:/remote_relims/Results/admin";
            // String directory = "C:/Users/Kenneth/Documents/NetBeansProjects/Remote_Relims/Output/data/admin/";
            // File outputFile = new File("C:/Users/Kenneth/Documents/NetBeansProjects/Remote_Relims/Output/hitlist.txt");
            if (outputFile.exists()) {
                outputFile.delete();
            }
            FileWriter fstream = new FileWriter(outputFile, true);
            BufferedWriter out = new BufferedWriter(fstream);
            try {
                //create an output file : 
                //Close the output stream
                File[] files;
                File[] subFiles;
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                Map<Date, String> hitList = new TreeMap<Date, String>();

                FileFilter fileFilter = new FileFilter() {
                    public boolean accept(File file) {
                        if (file.getName().endsWith(".cps")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
                files = directory.listFiles();
                //ALL these filese are directories...
                out.append("Projects with hits : ");
                out.newLine();
                for (File aFile : files) {
                    if (aFile.isDirectory()) {
                        subFiles = aFile.listFiles(fileFilter);
                        for (File aSubFile : subFiles) {
                            hitList.put(new Date(aSubFile.lastModified()), aSubFile.getName().replace(".cps", ""));
                        }
                    }
                }
                out.newLine();
                if (hitList.isEmpty()) {
                    out.append("Total hits : none");
                } else {
                    out.append("Total hits : " + hitList.size());
                }
                out.newLine();
                for (Date aHit : hitList.keySet()) {
                    out.append(sdf.format(aHit) + " : projectID = " + hitList.get(aHit).replace(".cps", ""));
                    out.newLine();
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
