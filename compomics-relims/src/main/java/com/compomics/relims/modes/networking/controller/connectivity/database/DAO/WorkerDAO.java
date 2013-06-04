/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.workerpool.WorkerRunner;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kenneth
 */
//not yet fully implemented...
public class WorkerDAO {

    private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DatabaseService.class);

    public boolean createWorker(String hostname, int port, long taskID) {
        PreparedStatement statement = null;
        Connection conn = null;
        boolean createSucces = false;
        List<String> errors = new ArrayList<>();
        try {
            conn = DAO.getConnection(logger.getName());
            //      statement = conn.prepareStatement("BEGIN");
            //      statement.execute();
            String query = "insert into Workers"
                    + "(HostName,workerPort,taskID) values (?, ?, ?)";
            statement = conn.prepareStatement(query);
            statement.setString(1, hostname);
            statement.setInt(2, port);
            statement.setLong(3, taskID);
            statement.setQueryTimeout(60);
            statement.execute();
            createSucces = true;
            //      statement = conn.prepareStatement("COMMIT");
            //      statement.execute();
            logger.debug("Worker was registered in database...");
            statement.close();
        } catch (Exception ex) {
            errors.add("Error creating worker");
            logger.error(ex);
            logger.error(ex.getCause());
            logger.error("Error creating new worker");
        } finally {
            if (statement != null) {
                statement = null;
            }
            if (errors != null) {
                if (!errors.isEmpty()) {
                    createSucces = false;
                }
            }
            DAO.release(conn);
            return createSucces;
        }
    }

    public long getTaskID(String hostname, int workerPort) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        long taskID = 0;
        try {
            conn = DAO.getConnection(logger.getName());
            //      statement = conn.prepareStatement("BEGIN");
            //      statement.execute();
            String query = "select taskID Workers where Hostname = ? and workerPort = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, hostname);
            statement.setInt(2, workerPort);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();
            if (rs.next()) {
                taskID = rs.getLong("taskID");
            }
            //      statement = conn.prepareStatement("COMMIT");
            //      statement.execute();
            statement.close();
            rs.close();
        } catch (SQLException ex) {
            logger.error(ex);
        } finally {
            if (statement != null) {
                statement = null;
            }
            if (rs != null) {
                rs = null;
            }
            DAO.release(conn);
            return taskID;
        }
    }

    public void deleteWorker(String hostname, int workerPort) {
        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = DAO.getConnection(logger.getName());
            //      statement = conn.prepareStatement("BEGIN");
            //      statement.execute();
            String query = "delete from Workers where Hostname = ? and workerPort = ?";
            statement = conn.prepareStatement(query);
            statement.setString(1, hostname);
            statement.setInt(2, workerPort);
            statement.setQueryTimeout(60);
            statement.execute();
            //      statement = conn.prepareStatement("COMMIT");
            //      statement.execute();
            statement.close();
        } catch (SQLException ex) {
            logger.error(ex);
        } finally {
            if (statement != null) {
                statement = null;
            }
            DAO.release(conn);
        }
    }

    public List<WorkerRunner> getActiveWorkers() {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        List<WorkerRunner> activeWorkers = new ArrayList<>();
        try {
            conn = DAO.getConnection(logger.getName());
            //      statement = conn.prepareStatement("BEGIN");
            //      statement.execute();
            String query = "select * from Workers";
            statement = conn.prepareStatement(query);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();

            //      statement = conn.prepareStatement("COMMIT");
            //      statement.execute();
            while (rs.next()) {
                WorkerRunner runner = new WorkerRunner(rs.getString("HostName"), rs.getInt("workerPort"));
                activeWorkers.add(runner);
            }
            rs.close();
            statement.close();
        } catch (SQLException ex) {
            logger.error(ex);
            logger.error(ex.getCause());
        }
        if (statement != null) {
            statement = null;
        }
        if (rs != null) {
            rs = null;
        }
        DAO.release(conn);
        return activeWorkers;
    }
}
