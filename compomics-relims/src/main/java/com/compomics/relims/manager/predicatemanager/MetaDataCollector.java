package com.compomics.relims.manager.predicatemanager;

import com.compomics.relims.conf.RelimsProperties;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class MetaDataCollector {

    private static int iTimeout = 30;
    private static String sDriverName = "org.sqlite.JDBC";
    private static String directoryPath;
    private static String sTempDb;
    private static String sJdbc = "jdbc:sqlite";
    private static String metaDatabaseURL;
    private static String[] columnNames = null;
    private static Logger logger = Logger.getLogger(MetaDataCollector.class);
    private static File csvFile;

    private static void setup() throws ClassNotFoundException, Exception {
        directoryPath = RelimsProperties.getTaskDatabaseLocation().getAbsolutePath();
        csvFile = RelimsProperties.getPrideMetaDataFile();
        new File(directoryPath).mkdirs();
        System.out.println(directoryPath);
        sTempDb = directoryPath + "/pridemeta.db";
        metaDatabaseURL = sJdbc + ":" + sTempDb;
        File tempDB = new File(sTempDb);
        Class.forName(sDriverName);
        if (!tempDB.exists()) {
            setupTable();
            fillDb();
        }
    }

    private static String[] getHeaders() {
        try {
            FileReader fr = new FileReader(csvFile);
            BufferedReader reader = new BufferedReader(fr);
            String st = "";
            String headerLine = reader.readLine();
            columnNames = headerLine.split("\t");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            return columnNames;
        }
    }

    private static void fillDb() throws SQLException {
        FileReader fr = null;
        try {
            fr = new FileReader(csvFile);
            BufferedReader reader = new BufferedReader(fr);
            String st = "";
            String headerLine = reader.readLine();
            while ((st = reader.readLine()) != null) {
                try {
                    storeEntry(st.split("\t"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            logger.error(ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                logger.error(ex);
            }
        }


    }

    private static boolean setupTable() throws SQLException {
        StringBuilder creationQuery = new StringBuilder("CREATE TABLE pridemeta (");
        String[] columnNames = getHeaders();
        if (columnNames == null) {
            throw new SQLException("No headers found");
        } else {
            for (String aColumn : columnNames) {
                creationQuery.append(aColumn).append(",");
            }
            creationQuery.replace(creationQuery.lastIndexOf(","), creationQuery.length(), ");");
        }
        doQuery(creationQuery.toString());
        return true;
    }

    private static void doQuery(String query) throws SQLException {
        // create a database connection
        Connection conn = DriverManager.getConnection(metaDatabaseURL);
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

    public static HashMap<String, String> getProjects(String query) throws SQLException {
        HashMap<String, String> availableProjects = new HashMap<String, String>();
        // create a database connection
        Connection conn = DriverManager.getConnection(metaDatabaseURL);
        ResultSet rs = null;
        try {
            Statement stmt = conn.createStatement();
            try {
                stmt.setQueryTimeout(iTimeout);
                rs = stmt.executeQuery(query);
                System.out.println("Executed : " + query);
                while (rs.next()) {
                    availableProjects.put(rs.getString(1), rs.getString(2));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    stmt.close();
                } catch (Exception ignore) {
                }
                return availableProjects;
            }
        } finally {
            try {
                conn.close();
            } catch (Exception ignore) {
            }
        }
    }

    public static Map<String, Object> lookUpMetaData(long projectID) throws SQLException {
        Map<String, Object> projectMetaData = new LinkedHashMap<String, Object>();
        Connection conn = null;
        PreparedStatement prepstmt = null;
        ResultSet resultSet = null;
        try {
            conn = DriverManager.getConnection(metaDatabaseURL);
            prepstmt = conn.prepareStatement("SELECT * FROM pridemeta WHERE accession = ?");
            prepstmt.setLong(1, projectID);
            resultSet = prepstmt.executeQuery();
            //store everything in a returning map
            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    projectMetaData.put(metaData.getColumnLabel(i), resultSet.getObject(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
            prepstmt.close();
            resultSet.close();
        }
        return projectMetaData;
    }

    public static void main(String[] args) throws Exception {
        setup();

    }

    private static void storeEntry(String[] split) throws SQLException {
        StringBuilder query = new StringBuilder("INSERT INTO pridemeta VALUES (");
        for (String aValue : split) {
            query.append("'").append(aValue).append("',");
        }
        query.replace(query.lastIndexOf(","), query.length(), "").append(")");
        doQuery(query.toString());
    }

    public static List<String> getEnzymes() {
        List<String> enzymeList = new ArrayList<String>();
        Connection conn = null;
        PreparedStatement prepstmt = null;
        ResultSet resultSet = null;
        try {
            conn = DriverManager.getConnection(metaDatabaseURL);
            prepstmt = conn.prepareStatement("SELECT distinct enzyme FROM pridemeta");
            resultSet = prepstmt.executeQuery();
            //store everything in a returning map
            List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
            while (resultSet.next()) {
                enzymeList.add(resultSet.getString("enzyme"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
                prepstmt.close();
                resultSet.close();
            } catch (SQLException ex) {
                logger.error(ex);
            }
        }
        return enzymeList;
    }
}
