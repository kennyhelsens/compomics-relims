/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.DAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;


/**
 *
 * @author Kenneth
 */
public class StatisticsDAO {

    private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DatabaseService.class);
    private Statement cStatement;
    private DatabaseService dds = DatabaseService.getInstance();

    public boolean createTaskStatistics(HashMap<String, Object> statisticsMap, String workerHost) {

        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;

        boolean createSucces = false;
        List<String> errors = null;

        try {
            conn = DAO.getConnection();
            //statement.execute("BEGIN");
            String query = "insert into " + RelimsProperties.getDbPrefix() + "Statistics"
                    + " values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            logger.debug("Loading statistics into database...");
            statement = conn.prepareStatement(query);
            statement.setLong(1, (Long) statisticsMap.get("taskID"));
            try {
                statement.setLong(2, (Long) statisticsMap.get("taskTime"));
            } catch (NullPointerException e) {
                logger.error("Tasktime was not recorded due to taskfailure...");
                statement.setLong(2, 0);
            }
            statement.setInt(3, (Integer) statisticsMap.get("workerPort"));
            statement.setString(4, (String) workerHost);
            statement.setLong(5, (Long) statisticsMap.get("committedvirtualmemorysize"));
            statement.setLong(6, (Long) statisticsMap.get("freephysicalmemorysize"));
            statement.setLong(7, (Long) statisticsMap.get("totalphysicalmemorysize"));
            statement.setLong(8, (Long) statisticsMap.get("freeswapspacesize"));
            statement.setLong(9, (Long) statisticsMap.get("totalswapspacesize"));
            statement.setLong(10, (Long) statisticsMap.get("processcputime"));
            statement.setInt(11, (Integer) statisticsMap.get("cores"));
            statement.setString(12, (String) statisticsMap.get("osarch"));
            statement.setString(13, (String) statisticsMap.get("osversion"));
            statement.setString(14, (String) statisticsMap.get("osname"));
            statement.setDouble(15, (Double) statisticsMap.get("systemcpuload"));
            statement.setDouble(16, (Double) statisticsMap.get("processcpuload"));
            statement.setString(17, (String) statisticsMap.get("javaversion"));
            statement.setString(18, (String) statisticsMap.get("userid"));
            statement.setQueryTimeout(60);
            statement.execute();
            //statement.execute("COMMIT");
            logger.debug("Statistics for task " + statisticsMap.get("taskID") + " were succesfully registered.");
        } catch (Exception ex) {
            logger.error("Error recording statistics");
            logger.error(ex);
            ex.printStackTrace();
        } finally {
            DAO.disconnect(conn, rs, statement);
            if (errors != null) {
                if (!errors.isEmpty()) {
                    createSucces = false;
                }
            } else {
                createSucces = true;
            }
            String projectID = dds.getProjectID((Long) statisticsMap.get("taskID"));
            if (createSucces) {
                logger.info("SUCCESS : Stored results for task " + statisticsMap.get("taskID") + " - ProjectID : " + projectID);
            } else {
                logger.error("FAILURE : Could not run task " + statisticsMap.get("taskID") + " - ProjectID : " + projectID);
            }
            return createSucces;
        }
    }

    public HashMap<String, Object> getAverageStatistics() {
        DecimalFormat twoDForm = new DecimalFormat("#.##");
        HashMap<String, Object> averageStatistics = new HashMap<String, Object>();
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            // cStatement = conn.createStatement();
            //  cStatement.execute("BEGIN");
            conn = DAO.getConnection();
            String query = "SELECT "
                    + "AVG(taskTime),"
                    + "AVG(systemcpuload),"
                    + "AVG(committedVirtualMemorySize),"
                    + "AVG(cores)"
                    + "FROM " + RelimsProperties.getDbPrefix()+"Statistics";
            statement = conn.prepareStatement(query);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();
            if (rs.next()) {
                averageStatistics.put("taskTime", (twoDForm.format((double) rs.getLong(1) / (60 * 1000))));
                averageStatistics.put("systemCPULoad", twoDForm.format(rs.getDouble(2) * 100));
                averageStatistics.put("committedVirtualMemorySize", twoDForm.format((double) rs.getLong(3) / 1000000000));
                averageStatistics.put("cores", rs.getInt(4));
            } else {
                averageStatistics.put("taskTime", 0L);
                averageStatistics.put("systemCPULoad", 0L);
                averageStatistics.put("committedVirtualMemorySize", 0L);
                averageStatistics.put("cores", 0);
            }
            //cStatement.execute("COMMIT");
            //cStatement.close();
        } catch (SQLException ex) {
            logger.error(ex);
        } finally {
            if (cStatement != null) {
//                cStatement = null;
            }
            DAO.disconnect(conn, rs, statement);
            return averageStatistics;
        }
    }
}
