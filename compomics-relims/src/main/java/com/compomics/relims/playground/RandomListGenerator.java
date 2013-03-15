/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.playground;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Kenneth
 */
public class RandomListGenerator {

    private static String outputFilePath = "C:/Users/Kenneth/Desktop/randomPrideList.txt";

    public static void main(String[] args) {
        //load all pride projects
        ArrayList<Integer> projectList = getPrideProjects();
        //shuffle the projects
        Collections.shuffle(projectList);
        //write every entry to a listfile
        writeToFile(projectList);
    }

    private static void writeToFile(ArrayList<Integer> projectList) {
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            BufferedWriter bwriter = new BufferedWriter(writer);
            for (int aProjectId : projectList) {
                bwriter.write(""+aProjectId);
                bwriter.newLine();
            }
            bwriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ArrayList<Integer> getPrideProjects() {
//TODO finish this connectivity 
        int aProjectID;
        ArrayList<Integer> projectList = new ArrayList<Integer>();
        String dbUrl = "jdbc:mysql://193.62.194.210:5000/pride_2";
        String dbUser = "inspector";
        String dbPass = "inspector";
        String dbClass = "com.mysql.jdbc.Driver";
        //String query = "Select experiment_id,short_label from pride_experiment";
        // query = "select table_name, column_name from information_schema.columns";

        //9606 = HUMAN ! 

        String query = "select exp.accession as accession, exp.title as title "
                + "from pride_experiment exp, mzdata_sample_param sample "
                + "where sample.parent_element_fk = exp.mz_data_id "
                + "and sample.accession = 9606 order by exp.accession+0 asc";


        try {
            Class.forName(dbClass);
            try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                java.sql.Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery(query);
                int rowsize = 0;
                while (rs.next()) {
                    rowsize++;
                    aProjectID = rs.getInt("accession");
                    projectList.add(aProjectID);
                }
                try {
                    con.close();
                } catch (SQLException sqlex) {
                }
            }
        } //end try
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Could not retrieve data from the online database...");
        } finally {
        }
        return projectList;

    }  //end main
}
