/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.playground;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Kenneth
 */
public class MetaDataCollector {

    private static int iTimeout = 30;
    private static String sDriverName = "org.sqlite.JDBC";
    private static String sTempDb = "pridemeta.db";
    private static String sJdbc = "jdbc:sqlite";
    private static String sDbUrl = sJdbc + ":" + sTempDb;
    private static File csvFile = new File("C:\\Users\\Kenneth\\Documents\\exampleTable.txt");

    private static void setup() throws ClassNotFoundException, Exception {
        Class.forName(sDriverName);
        makeTable();
        fillDatabase(csvFile);
    }

    private static void makeTable() throws Exception {

        String sMakeTable = "CREATE  TABLE pridemeta ("
                + "  `Accession` INT NOT NULL ,"
                + "  `species` VARCHAR(255) NULL ,"
                + "  `taxonomyID` VARCHAR(2555) NULL ,"
                + "  `Tissue` VARCHAR(255) NULL ,"
                + "  `BrendaID` VARCHAR(255) NULL ,"
                + "  `PTM` VARCHAR(255) NULL ,"
                + "  `SpectraCount` INT NULL ,"
                + "  `ProteinCount` INT NULL ,"
                + "  `PeptideCount` INT NULL ,"
                + "  `enzyme` VARCHAR(45) NULL DEFAULT 'Trypsin' ,"
                + "  `ms1` INT NULL ,"
                + "  `ms2` INT NULL ,"
                + "  `other` VARCHAR(255) NULL ,"
                + "  PRIMARY KEY (`Accession`) )";
        doQuery(sMakeTable);
    }

    private static void fillDatabase(File csvFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String line;
        while ((line = br.readLine()) != null) {
            line.replace(";", ":");
            line.replace("--", "-");
            line.replace("'", "");
            String[] values = line.split("\t");    //your seperator
            //Convert String to right type. Integer, double, date etc.
            if (!values[0].contains("Accession")) {
                //Trim String Values
                for (String aValue : values) {
                    if (aValue.length() > 255) {
                        aValue = aValue.substring(0, 254);
                    }
                }
                String updateString = ("INSERT INTO pridemeta VALUES("
                        + Integer.parseInt(values[0]) + ",'"
                        + values[3] + "','"
                        + values[4] + "','"
                        + values[5] + "','"
                        + values[6] + "','"
                        + values[7] + "',"
                        + Integer.parseInt(values[8]) + ","
                        + Integer.parseInt(values[9]) + ","
                        + Integer.parseInt(values[10]) + ",'"
                        + values[13] + "','"
                        + Integer.parseInt(values[14]) + "','"
                        + Integer.parseInt(values[15]) + "','"
                        + values[16] + "');");
                try {
                    doQuery(updateString);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Could not store");
                }
            }
        }
        br.close();
    }

    private static void updateEnzymes(File csvFile) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String line;
        while ((line = br.readLine()) != null) {
            line.replace(";", ":");
            line.replace("--", "-");
            line.replace("'", "");
            String[] values = line.split("\t");    //your seperator
            //Convert String to right type. Integer, double, date etc.
            if (!values[0].contains("Accession")) {
                //Trim String Values
                for (String aValue : values) {
                    if (aValue.length() > 255) {
                        aValue = aValue.substring(0, 254);
                    }
                }
                String updateString = ("UPDATE pridemeta SET enzyme = " + values[1] + " where Accession = " + values[0] + ";");
                try {
                    doQuery(updateString);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("Could not store");
                }
            }
        }
        br.close();
    }

    private static void doQuery(String query) throws SQLException {
        // create a database connection
        Connection conn = DriverManager.getConnection(sDbUrl);
        try {
            Statement stmt = conn.createStatement();
            try {
                stmt.setQueryTimeout(iTimeout);
                stmt.executeUpdate(query);
                System.out.println("Executed : " + query);
            } finally {
                try {
                    stmt.close();
                } catch (Exception ignore) {
                }
            }
        } finally {
            try {
                conn.close();
            } catch (Exception ignore) {
            }
        }
    }

    public static void main(String[] args) throws Exception {
        setup();
    }
}
