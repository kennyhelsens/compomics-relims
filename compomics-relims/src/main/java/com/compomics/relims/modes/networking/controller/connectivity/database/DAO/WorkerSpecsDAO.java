/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Kenneth
 */
public class WorkerSpecsDAO {

    private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DatabaseService.class);
    private DatabaseService dds = DatabaseService.getInstance();

    public boolean createTaskWorkerSpecs(HashMap<String, Object> WorkerSpecsMap, String workerHost) {

        PreparedStatement statement = null;
        Connection conn = null;

        boolean createSucces = false;
        List<String> errors = null;

        try {
            conn = DAO.getConnection(logger.getName());
            //statement.execute("BEGIN");
            String query = "insert into WorkerSpecs"
                    + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            logger.debug("Loading WorkerSpecs into database...");
            statement = conn.prepareStatement(query);
            statement.setLong(1, (Long) WorkerSpecsMap.get("taskID"));
            try {
                statement.setLong(2, (Long) WorkerSpecsMap.get("taskTime"));
            } catch (NullPointerException e) {
                logger.error("Tasktime was not recorded due to taskfailure...");
                statement.setLong(2, 0);
            }
            statement.setInt(3, (Integer) WorkerSpecsMap.get("workerPort"));
            statement.setString(4, (String) workerHost);
            statement.setLong(5, (Long) WorkerSpecsMap.get("committedvirtualmemorysize"));
            statement.setLong(6, (Long) WorkerSpecsMap.get("freephysicalmemorysize"));
            statement.setLong(7, (Long) WorkerSpecsMap.get("totalphysicalmemorysize"));
            statement.setLong(8, (Long) WorkerSpecsMap.get("freeswapspacesize"));
            statement.setLong(9, (Long) WorkerSpecsMap.get("totalswapspacesize"));
            statement.setLong(10, (Long) WorkerSpecsMap.get("processcputime"));
            statement.setInt(11, (Integer) WorkerSpecsMap.get("cores"));
            statement.setString(12, (String) WorkerSpecsMap.get("osarch"));
            statement.setString(13, (String) WorkerSpecsMap.get("osversion"));
            statement.setString(14, (String) WorkerSpecsMap.get("osname"));
            statement.setDouble(15, (Double) WorkerSpecsMap.get("systemcpuload"));
            statement.setDouble(16, (Double) WorkerSpecsMap.get("processcpuload"));
            statement.setString(17, (String) WorkerSpecsMap.get("javaversion"));
            statement.setString(18, (String) WorkerSpecsMap.get("userid"));
            statement.setQueryTimeout(60);
            statement.execute();
            //statement.execute("COMMIT");
            logger.debug("WorkerSpecs for task " + WorkerSpecsMap.get("taskID") + " were succesfully registered.");
            statement.close();
        } catch (SQLException ex) {
            logger.error("Error recording WorkerSpecs");
            logger.error(ex);
        } finally {
            if (statement != null) {
                statement = null;
            }
            DAO.release(conn);
            if (errors != null) {
                if (!errors.isEmpty()) {
                    createSucces = false;
                }
            } else {
                createSucces = true;
            }
            String projectID = dds.getProjectID((Long) WorkerSpecsMap.get("taskID"));
            if (createSucces) {
                logger.info("SUCCESS : Stored results for task " + WorkerSpecsMap.get("taskID") + " - ProjectID : " + projectID);
            } else {
                logger.error("FAILURE : Could not run task " + WorkerSpecsMap.get("taskID") + " - ProjectID : " + projectID);
            }
            return createSucces;
        }
    }
  }
