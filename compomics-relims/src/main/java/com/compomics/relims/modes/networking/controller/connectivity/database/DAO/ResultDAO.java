/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.conf.RelimsProperties;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class ResultDAO {

    private final static Logger logger = Logger.getLogger(ResultDAO.class);

    public boolean storeParameter(long taskID, String projectId, String parameterName, String parameter) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        boolean createSucces = false;
        List<String> errors = new ArrayList<>();

        try {
            conn = DAO.getConnection();
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            String query = "insert into " + RelimsProperties.getDbPrefix() + "Users"
                    + "(TaskID,projectID,parameterName,parameter) values (?, ?, ?,?);";
            statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, taskID);
            statement.setString(2, projectId);
            statement.setString(3, parameterName);
            statement.setString(4, parameter);
            statement.execute();
            ResultSet results = statement.getGeneratedKeys();
            if (results.next()) {
                createSucces = true;
                logger.debug("Stored " + parameterName + " for task " + taskID);
            }
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
        } catch (Exception ex) {
            logger.error("Error storing " + parameterName + " for task" + taskID);
            logger.error(ex);
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

    public void storeMods(long taskID, String projectId, List<String> mods, boolean fixed) {
        String parameterName = "fixed modification";
        if (!fixed) {
            parameterName = "variable modification";
        }
        int i = 1;
        for (String aMod : mods) {
            storeParameter(taskID, projectId, parameterName + "_" + i, aMod);
        }
    }

    public void storeProjectResults(long taskID, String projectId, HashMap<String, Object> resultMap) {
        for (String aResultParameter : resultMap.keySet()) {
            switch (aResultParameter) {
                case "varmod":
                    storeMods(taskID, projectId, (List<String>) resultMap.get(aResultParameter), false);
                    break;
                case "fixmod":
                    storeMods(taskID, projectId, (List<String>) resultMap.get(aResultParameter), true);
                    break;
                default:
                    storeParameter(taskID, projectId, aResultParameter, resultMap.get(aResultParameter).toString());
                    break;
            }
        }
    }
}
