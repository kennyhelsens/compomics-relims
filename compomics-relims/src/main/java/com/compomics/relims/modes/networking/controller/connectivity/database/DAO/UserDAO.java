/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.remoterelimscontrolserver.connectivity.database.security.KryptoDynamite;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kenneth
 */
public class UserDAO {

    private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DatabaseService.class);

    public boolean createUser(String username, String password, String eMail) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        boolean createSucces = false;
        List<String> errors = new ArrayList<>();

        try {
            conn = DAO.getConnection();
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            String salt = KryptoDynamite.getSalt();
            String hashedP = KryptoDynamite.kryptonize(password + salt);
            String query = "insert into " + RelimsProperties.getDbPrefix() + "Users"
                    + "(Username,HashedP,Salt,eMail) values (?, ?, ?,?);";
            statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, username);
            statement.setString(2, hashedP);
            statement.setString(3, salt);
            statement.setString(4, eMail);
            statement.setQueryTimeout(10);
            statement.execute();
            ResultSet results = statement.getGeneratedKeys();
            if (results.next()) {
                createSucces = true;
            }
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
            logger.debug("New User created : " + username);
        } catch (Exception ex) {
            System.out.println("Error creating new User");
            System.out.println(ex);
            ex.printStackTrace();
            statement.close();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    conn = null;
                }
            }
            DAO.disconnect(conn, rs, statement);
            if (!errors.isEmpty()) {
                createSucces = false;
            }
            return createSucces;
        }
    }

    public String[] getPassword(String userID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        String[] credentials = null;

        try {
            conn = DAO.getConnection();
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            String query = "select HashedP,Salt from " + RelimsProperties.getDbPrefix() + "Users where Username = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, userID);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();
            if (rs.next()) {
                credentials = new String[]{rs.getString("HashedP"), rs.getString("Salt")};
            }
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.error(ex);
        } finally {
            DAO.disconnect(conn, rs, statement);
            return credentials;
        }
    }

    public boolean getUser(String userID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        boolean userExists = false;
        try {
            conn = DAO.getConnection();
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            String query = "select * from " + RelimsProperties.getDbPrefix() + "Users where Username = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, userID);
            statement.setQueryTimeout(60);
            statement.setQueryTimeout(60);
            statement.setQueryTimeout(60);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();
            if (rs.next()) {
                userExists = true;
            }
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
        } finally {
            DAO.disconnect(conn, rs, statement);
            return userExists;
        }
    }

    public boolean isLoginCredentialsCorrect(String userID, String password) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        String credentials[] = this.getPassword(userID);
        String inputHashedP = null;
        String hashedP = null;
        //kryptonize the password with the user's salt

        try {
            String salt = credentials[1];
            hashedP = credentials[0];
            inputHashedP = KryptoDynamite.kryptonize(password + salt);
        } catch (Exception ex) {
            return false;
        }
        if (inputHashedP.equals(hashedP) && inputHashedP != null) {
            return true;
        } else {
            return false;
        }
    }

    public boolean findUsername(String username) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        boolean returnValue = false;

        try {
            conn = DAO.getConnection();
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            String query = "select * from " + RelimsProperties.getDbPrefix() + "Users where Username = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, username);
            statement.setQueryTimeout(60);
            statement.setQueryTimeout(60);
            statement.setQueryTimeout(60);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();
            if (rs.next()) {
                returnValue = true;
            }
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
        } finally {
            DAO.disconnect(conn, rs, statement);
            return returnValue;
        }
    }
}
