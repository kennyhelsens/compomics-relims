/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

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
public class ProjectResultDAO {

    private final static Logger logger = Logger.getLogger(ProjectResultDAO.class);

    public boolean storeParameter(long taskID, String projectId, String parameterName, String parameter) {
        PreparedStatement statement = null;
        ResultSet results = null;
        Connection conn = null;
        boolean createSucces = false;
        try {
            conn = DAO.getConnection(logger.getName());
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            String query = "insert into ProjectResults"
                    + "(TaskID,projectID,parameterName,parameterValue) values (?, ?, ?,?);";
            statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1, taskID);
            statement.setString(2, projectId);
            statement.setString(3, parameterName);
            statement.setString(4, parameter);
            statement.execute();
            results = statement.getGeneratedKeys();
            if (results.next()) {
                createSucces = true;
                logger.debug("Stored " + parameterName + " for task " + taskID);
            }
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
            statement.close();
            results.close();
        } catch (SQLException ex) {
            if (statement != null) {
                statement = null;
            }
            if (results != null) {
                results = null;
            }
            logger.error("Error storing " + parameterName + " for task" + taskID);
            logger.error(ex);
        } finally {
            DAO.release(conn);
            return createSucces;
        }
    }

    public void storeMods(long taskID, String projectId, List<String> mods, boolean fixed) {
        String parameterName = "fixed_modification";
        if (!fixed) {
            parameterName = "variable_modification";
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
