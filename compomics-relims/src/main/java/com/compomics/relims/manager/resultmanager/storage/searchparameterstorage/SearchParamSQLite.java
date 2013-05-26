/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.resultmanager.storage.searchparameterstorage;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class SearchParamSQLite implements SearchParamStorage {

    private static final Logger logger = Logger.getLogger(SearchParamSQLite.class);
    private File databaseFile;

    public SearchParamSQLite(File SQLiteDatabase) {
        this.databaseFile = SQLiteDatabase;
    }

    public static void main(String[] args) {
        SearchParamSQLite storage = new SearchParamSQLite(new File("V:\\relims_main\\compomics-relims-0.9.1-beta\\resources\\databases\\parameterstorage.sqlite"));
        SearchParameters parameters = new SearchParameters();
        String projectID = "123444";
        try {
            if (storage.storeParameters(projectID, parameters)) {
                logger.debug("Parameters were stored");
            } else {
                logger.error("Parameters could not be stored in the specified database");
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
        try {
            if (storage.retrieveParameters(projectID) != null) {
                logger.debug("Parameters were retrieved");
            } else {
                logger.error("Parameters could not retrieve parameters");
            }
        } catch (IOException ex) {
            logger.error(ex);
        }
    } //CREATE TABLE searchparameters (ProjectID INTEGER PRIMARY KEY, searchparameterfile BLOB)

    @Override
    public boolean storeParameters(String projectID, SearchParameters searchParameters) throws IOException {
        byte[] bytesToStore = serialize(searchParameters);
        Connection conn = null;
        PreparedStatement stm = null;
        try {
            conn = connect();
            stm = conn.prepareStatement("INSERT INTO SearchParameters VALUES(?,?)");
            stm.setString(1, String.valueOf(projectID));
            stm.setBytes(2, bytesToStore);
            int rowsUpdated = stm.executeUpdate();
            return (rowsUpdated > 0);
        } catch (Exception e) {
            logger.error(e);
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stm != null) {
                    stm.close();
                }
            } catch (SQLException ex) {
                logger.error(ex);
                conn = null;
                stm = null;
            }
        }
    }

    @Override
    public SearchParameters retrieveParameters(String projectID) throws IOException {
        byte[] searchParameters = null;
        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            conn = connect();
            stm = conn.prepareStatement("Select searchparameters from SearchParameters where ProjectID = ?");
            stm.setString(1, String.valueOf(projectID));
            rs = stm.executeQuery();
            while (rs.next()) {
                searchParameters = rs.getBytes("searchparameters");
            }
        } catch (Exception e) {
            logger.error(e);
            return null;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stm != null) {
                    stm.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException ex) {
                logger.error(ex);
                conn = null;
                stm = null;
                rs = null;
            }

            if (searchParameters != null) {
                try {
                    return (SearchParameters) deserialize(searchParameters);
                } catch (ClassNotFoundException ex) {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(obj);
        return b.toByteArray();
    }

    private static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(bytes);
        ObjectInputStream o = new ObjectInputStream(b);
        return o.readObject();
    }

    private Connection connect() {
        Connection connection = null;
        ResultSet resultSet = null;
        Statement statement = null;
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager
                    .getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
            return connection;
        } catch (Exception e) {
            logger.error(e);
            return null;
        }
    }

    @Override
    public boolean hasBeenRun(String projectId) {
        boolean hasBeenRun = false;
        Connection conn = null;
        PreparedStatement stm = null;
        ResultSet rs = null;
        try {
            conn = connect();
            stm = conn.prepareStatement("Select projectId from SearchParameters where ProjectID = ?");
            stm.setString(1, String.valueOf(projectId));
            rs = stm.executeQuery();
            if (rs.next()) {
                hasBeenRun = true;
            } else {
                hasBeenRun = false;
            }
        } catch (Exception e) {
            logger.error(e);
            hasBeenRun = false;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
                if (stm != null) {
                    stm.close();
                }
            } catch (SQLException ex) {
                logger.error(ex);
                conn = null;
                stm = null;
            }
            return hasBeenRun;
        }
    }
}