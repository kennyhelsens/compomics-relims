/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.Task;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.TaskContainer;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class TaskDAO {

    private Logger logger = Logger.getLogger(DatabaseService.class);
    private long newTaskID = 0L;
    private InputStream in;
    private TaskContainer taskMap;
    private String clientID;
    private LinkedHashMap<String, String> tasksToStore = new LinkedHashMap<>();
    private SearchParameters loadedParameters;
    private List<String> subList;

    public void updateTaskStatus(long taskID, String status) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = DAO.getConnection();
            String query = "update " + RelimsProperties.getDbPrefix() + "Tasks set TaskState = ? where TaskID=?";
//            statement = conn.prepareStatement("BEGIN");
            //           statement.execute();
            statement = conn.prepareStatement(query);
            statement.setString(1, status.toString());
            statement.setLong(2, taskID);
            statement.setQueryTimeout(60);
            statement.executeUpdate();
//            statement = conn.prepareStatement("COMMIT");
//            statement.execute();
        } catch (SQLException ex) {
            logger.error("Error changing status of task " + taskID + " to " + status);
            logger.error(ex);
        } finally {
            DAO.disconnect(conn, rs, statement);
        }
    }

    public void updateSearchParameters(long taskID, SearchParameters searchParameters) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        byte[] searchparameterToStore = null;

//convert searchparam to bytearray 
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(searchParameters);
            oos.flush();
            oos.close();
            bos.close();
            searchparameterToStore = bos.toByteArray();
        } catch (IOException ex) {
            logger.error("Could not resolve searchparameters. Not stored");
        }

        try {
            conn = DAO.getConnection();
            String query = "update " + RelimsProperties.getDbPrefix() + "Tasks set searchParameters = ? where TaskID=?";
//            statement = conn.prepareStatement("BEGIN");
            //           statement.execute();
            statement = conn.prepareStatement(query);
            statement.setBytes(1, searchparameterToStore);
            statement.setLong(2, taskID);
            statement.setQueryTimeout(60);
            statement.executeUpdate();
//            statement = conn.prepareStatement("COMMIT");
//            statement.execute();
        } catch (SQLException ex) {
            logger.error(ex);
        } finally {
            DAO.disconnect(conn, rs, statement);
        }
    }

    public void deleteTask(long taskID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        // Make PreparedStatements
        try {
            conn = DAO.getConnection();
//            statement = conn.prepareStatement("BEGIN");
//            statement.execute();
            String query = "delete from " + RelimsProperties.getDbPrefix() + "Tasks where TaskID = ?";
            statement = conn.prepareStatement(query);
            statement.setLong(1, taskID);
            statement.setQueryTimeout(60);
            statement.executeUpdate();
//            statement = conn.prepareStatement("COMMIT");
//            statement.execute();
        } catch (SQLException ex) {
            logger.error(ex);
        } finally {
            DAO.disconnect(conn, rs, statement);
        }
    }

    public long createTask(String status, String clientID, String strategyID, String sourceID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = DAO.getConnection();
//            statement = conn.prepareStatement("BEGIN");
//            statement.execute();
            String query = "insert into " + RelimsProperties.getDbPrefix() + "Tasks"
                    + "(TaskState,ClientID,RelimsClientJob,TimeStamp) values (?, ?, ?, ?)";
            statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "New");
            statement.setString(2, clientID);
            statement.setString(2, strategyID);
            statement.setString(2, sourceID);
            java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
            statement.setTimestamp(4, sqlDate);
            statement.setQueryTimeout(60);
            statement.execute();
            rs = statement.getGeneratedKeys();
            while (rs.next()) {
                newTaskID = rs.getInt(1);
            }
//            statement = conn.prepareStatement("COMMIT");
//            statement.execute();
            logger.debug(clientID + " created task( " + newTaskID + " )");
        } catch (SQLException ex) {
            logger.error("Error creating new Task");
            logger.error(ex);
            ex.printStackTrace();
        } finally {
            DAO.disconnect(conn, rs, statement);
            return newTaskID;
        }

    }

    public String readTask(long taskID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        String message = null;
        try {
            conn = DAO.getConnection();
//            statement = conn.prepareStatement("BEGIN");
//            statement.execute();
            //     String query = "select * from " + RelimsProperties.getTaskDatabaseName()+".Tasks where TaskID = ?";
            String query = "select * from " + RelimsProperties.getDbPrefix() + "Tasks where TaskID = ?";
            statement = conn.prepareStatement(query);
            statement.setLong(1, taskID);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();
            while (rs.next()) {
                message = "TaskID = " + rs.getString("TaskID")
                        + " ; Status = " + rs.getString("TaskState")
                        + " ; Client ID = " + rs.getString("ClientID")
                        + " ; Timestamp = " + rs.getTimestamp("Timestamp").toString();
            }
//            statement = conn.prepareStatement("COMMIT");
//            statement.execute();
        } catch (SQLException ex) {
            message = "Could not read stream !";
            logger.error(ex);
        } finally {
            DAO.disconnect(conn, rs, statement);
            return message;
        }

    }

    public Task findNewTask() throws NullPointerException {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        long foundTaskID = 0L;
        String foundProjectID;
        String foundStrategy;
        String foundSource;
        String foundUserID;
        Boolean foundAllowPipeline = false;
        byte[] foundSearchParameters = null;
        Task relimsWorkingTask = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        //find new task and make a workingTask out of it
        try {
            conn = DAO.getConnection();
            //           statement = conn.prepareStatement("BEGIN");
            //           statement.execute();
            String query = "select * from " + RelimsProperties.getDbPrefix() + "Tasks where (TaskState = ? or TaskState = ?) order by Timestamp LIMIT 1";
            statement = conn.prepareStatement(query);
            statement.setString(1, "RESCHEDULED");
            statement.setString(2, "NEW");
            statement.setQueryTimeout(30);
            rs = statement.executeQuery();
            if (rs.next()) {
                foundTaskID = rs.getLong("TaskID");
                foundProjectID = rs.getString("ProjectID");
                foundStrategy = rs.getString("StrategyID");
                foundSource = rs.getString("SourceID");
                foundUserID = rs.getString("ClientID");
                foundAllowPipeline = rs.getBoolean("usePride");
                foundSearchParameters = rs.getBytes("searchparameters");
                relimsWorkingTask = new Task(foundTaskID, foundProjectID, foundStrategy, foundSource, foundUserID);
            }
        } catch (Exception ex) {
            System.out.println("TaskDistributor Failed : ");
            if (!ex.toString().contains("The database file is locked)")) {
                logger.error(ex);
                System.out.println(ex);
                ex.printStackTrace();
            } else {
                System.out.println(ex);
            }
        } finally {

            //convert the byteArray back to SearchParameters
            try {
                bais = new ByteArrayInputStream(foundSearchParameters);
                ois = new ObjectInputStream(bais);
                if (foundSearchParameters != null) {
                    bais = new ByteArrayInputStream(foundSearchParameters);
                    ois = new ObjectInputStream(in);
                    relimsWorkingTask.setSearchParameters((SearchParameters) ois.readObject());
                    relimsWorkingTask.setAllowPridePipeline(foundAllowPipeline);
                    bais.close();
                    ois.close();
                }
            } catch (ClassNotFoundException | IOException ex) {
                System.out.println("Could not retrieve valid searchparameters from the database.Using default");
            } finally {
                if (bais != null) {
                    try {
                        bais.close();
                    } catch (IOException ex) {
                        bais = null;
                    }
                }
                if (ois != null) {
                    try {
                        ois.close();
                    } catch (IOException ex) {
                        ois = null;
                    }
                }
                DAO.disconnect(conn, rs, statement);
                return relimsWorkingTask;
            }
        }



    }

    public long startupSweep() {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        logger.debug("Sweeping memory...");
        long updates = 0L;
        boolean sweeped = false;
        while (!sweeped) {
            try {
                conn = DAO.getConnection();
                //         statement = conn.prepareStatement("BEGIN");
                //         statement.execute();
                String query = "update " + RelimsProperties.getDbPrefix() + "Tasks set TaskState = ? where TaskState=? OR TaskState=?";
                statement = conn.prepareStatement(query);
                statement.setString(1, "NEW");
                statement.setString(2, "RUNNING");
                statement.setString(3, "FAILED");
                statement.setQueryTimeout(60);
                updates = statement.executeUpdate();
                sweeped = true;
                logger.debug(updates + " tasks have been rescheduled...");
                //         statement = conn.prepareStatement("COMMIT");
                //         statement.execute();
            } catch (SQLException ex) {
                if (!ex.toString().contains("database is locked")) {
                    //            System.err.println(ex);
                    //          ex.printStackTrace();
                    sweeped = false;
                    ex.printStackTrace();
                }
            } finally {
                if (sweeped) {
                    System.out.println("Reset " + updates + " tasks");
                    DAO.disconnect(conn, rs, statement);
                }
            }
        }
        return updates;
    }

    public boolean storeTasks(List<String> aProjectIDList, SearchParameters searchParameters, String allowPrideAsaPipeline) throws IOException {
        // searchParameters = null;
        byte[] searchparameterToStore = null;
        boolean usePrideAsaPipeline;
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        boolean success = false;

        //convert string to boolean (makeshift solution for now)

        if (allowPrideAsaPipeline.equalsIgnoreCase("allow")) {
            usePrideAsaPipeline = true;
        } else {
            usePrideAsaPipeline = false;
        }
        //convert searchparam to bytearray 
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(searchParameters);
            oos.flush();
            oos.close();
            bos.close();
            searchparameterToStore = bos.toByteArray();
        } catch (IOException ex) {
            logger.error("Could not resolve searchparameters. Not stored");
        }

        try {
            // get the searchblob
            conn = DAO.getConnection();
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            for (String aProjectID : aProjectIDList) {
                String query = "";
                query = " insert into " + RelimsProperties.getDbPrefix() + "Tasks"
                        + " (ProjectID,TaskState,ClientID,StrategyID,SourceID,TimeStamp,ProjectID,usePride,searchparameters) values (?,?,?, ?, ?, ?, ?, ?,?)";
                success = false;
                int attempts = 0;
                while (!success) {
                    try {
                        attempts++;
                        statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                        statement.setString(1, aProjectID);
                        statement.setString(2, "NEW");
                        statement.setString(3, clientID);
                        statement.setString(4, taskMap.getStrategyID());
                        statement.setString(5, taskMap.getSourceID());
                        java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
                        statement.setTimestamp(6, sqlDate);
                        statement.setString(7, tasksToStore.get(aProjectID));
                        statement.setBoolean(8, usePrideAsaPipeline);
                        statement.setBytes(9, searchparameterToStore);
                        statement.setQueryTimeout(60);
                        statement.execute();
                        success = true;
                        //                 statement = conn.prepareStatement("COMMIT");
                        //                 statement.execute();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        success = false;
                        if (attempts > 9) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Error creating new Task");
            logger.error(ex);
            ex.printStackTrace();
            success = false;
        } finally {
            DAO.disconnect(conn, rs, statement);
        }
        return success;
    }

    public Map<String, Long> pushTaskMapToDB(TaskContainer taskMap) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        //WorkerPool.setDatabaseLocked(true);
        this.taskMap = taskMap;
        Map<String, Long> generatedTaskIDs = new HashMap<>();
        clientID = taskMap.getCurrentUser();
        loadedParameters = taskMap.getSearchParameters();
        tasksToStore = taskMap.getTaskList();
        List<String> taskList = new ArrayList<>();

        //break up the taskcontainer into managable subtasks (100 projects max)
        for (String aProject : tasksToStore.keySet()) {
            taskList.add(aProject);
        }

        //Break up the list to send 10000 at a time...
        int batchSize = 10000;
        int taskAmount = taskList.size();
        int taskSteps = taskAmount / batchSize;
        int remainder = taskAmount % batchSize;
        if (remainder > 0) {
            taskSteps++;
        }
        int i = 1;
        while (!taskList.isEmpty()) {
            System.out.println("Storing tasks in batch : batch " + i + " / " + taskSteps);
            try {
                if (taskList.size() > batchSize) {
                    subList = taskList.subList(0, batchSize);
                } else {
                    subList = taskList.subList(0, remainder);
                }
                //store tasks in a future object...
                storeTasks(subList, taskMap.getSearchParameters(), taskMap.getInstructionMap().get("runpipeline"));
                taskList.removeAll(subList);
                i++;
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(e);
                System.out.println("An error has occurred while storing the tasks : " + e);
                break;
            } finally {
                if (i > taskSteps) {
                    break;
                }
            }
        }

        return generatedTaskIDs;
    }

    public List<String[]> getUserTasks(String userID, String queryParameters) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        conn = DAO.getConnection();
        List<String[]> results = new LinkedList<>();
        StringBuilder query = new StringBuilder();
        try {
            if (queryParameters.equals("")) {
                query.append("select ProjectID," + RelimsProperties.getDbPrefix() + "Statistics.TaskID,TaskState,ProjectName from " + RelimsProperties.getDbPrefix() + "Tasks inner join " + RelimsProperties.getDbPrefix() + "Statistics on " + RelimsProperties.getDbPrefix() + "Tasks.TaskID = " + RelimsProperties.getDbPrefix() + "Statistics.TaskID where CLIENTID = ?");
            } else {
                query.append("select ProjectID," + RelimsProperties.getDbPrefix() + "Statistics.TaskID,TaskState,ProjectName from " + RelimsProperties.getDbPrefix() + "Tasks inner join " + RelimsProperties.getDbPrefix() + "Statistics on " + RelimsProperties.getDbPrefix() + "Tasks.TaskID = " + RelimsProperties.getDbPrefix() + "Statistics.TaskID where CLIENTID = ? and ").append(queryParameters);
            }

            String queryString = query.toString();

            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            statement = conn.prepareStatement(queryString);
            statement.setString(1, userID);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();
            while (rs.next()) {
                String projectID = rs.getString("ProjectId");
                String taskState = rs.getString("TaskState");
                String taskID = "" + rs.getLong("TaskID");
                String projectName = rs.getString("ProjectName");
                results.add(new String[]{projectID, taskID, taskState, projectName});
            }
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
        } catch (SQLException ex) {
            System.err.println(ex);
            ex.printStackTrace();

        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        } finally {
            DAO.disconnect(conn, rs, statement);
            return results;
        }

    }

    public void resetTask(long taskID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        if (taskID != 0L) {
            try {
                conn = DAO.getConnection();
                //         statement = conn.prepareStatement("BEGIN");
                //         statement.execute();
                statement = conn.prepareStatement("update " + RelimsProperties.getDbPrefix() + "Tasks set TaskState = ? where TaskID=?");
                statement.setString(1, "NEW");
                statement.setLong(2, taskID);
                statement.setQueryTimeout(60);
                statement.executeUpdate();
                //         statement = conn.prepareStatement("COMMIT");
                //         statement.execute();
            } catch (SQLException ex) {
                logger.error(ex);
            } finally {
                DAO.disconnect(conn, rs, statement);
            }
        }
    }

    public int resetFailedTasks() {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        int restored = 0;
        try {
            conn = DAO.getConnection();
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            statement = conn.prepareStatement("update " + RelimsProperties.getDbPrefix() + "Tasks set TaskState ='NEW' where TaskState = 'FAILED'");
            statement.setQueryTimeout(60);
            restored = statement.executeUpdate();
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
        } catch (SQLException ex) {
            logger.error(ex);
        } finally {
            DAO.disconnect(conn, rs, statement);
            return restored;
        }
    }

    public HashMap<String, Object> getTaskInformation(long taskID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        HashMap<String, Object> informationMap = new HashMap<>();
        ResultSet myResults = null;
        PreparedStatement searchParametersQuery = null;

        try {
            //get the columncount
            conn = DAO.getConnection();
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            statement = conn.prepareStatement("select * from " + RelimsProperties.getDbPrefix() + "Tasks inner join " + RelimsProperties.getDbPrefix() + "Statistics on " + RelimsProperties.getDbPrefix() + "Tasks.TaskID = " + RelimsProperties.getDbPrefix() + "Statistics.TaskID where " + RelimsProperties.getDbPrefix() + "Tasks.TaskID = ?");
            statement.setLong(1, taskID);
            myResults = statement.executeQuery();
            try {
                if (myResults.next()) {
                    ResultSetMetaData rsmd = myResults.getMetaData();
                    int NumOfCol = rsmd.getColumnCount();
                    for (int i = 1; i <= NumOfCol; i++) {
                        informationMap.put(rsmd.getColumnName(i), myResults.getObject(rsmd.getColumnName(i)));
                    }
                }
                //         statement = conn.prepareStatement("COMMIT");
                //         statement.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.error(ex);
        } finally {
            try {
                myResults.close();
                //          searchParametersQuery.close();
            } catch (SQLException ex) {
                myResults = null;
            }
            DAO.disconnect(conn, rs, statement);
            return informationMap;
        }
    }

    public String getProjectID(long taskID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        conn = DAO.getConnection();
        String projectID = "unknown";
        try {
            String query = ("select ProjectID from " + RelimsProperties.getDbPrefix() + "Tasks where TaskID = ?");
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            statement = conn.prepareStatement(query);
            statement.setLong(1, taskID);
            statement.setQueryTimeout(60);
            rs = statement.executeQuery();
            while (rs.next()) {
                projectID = rs.getString("ProjectId");
            }
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
        } catch (SQLException ex) {
            System.err.println(ex);
            ex.printStackTrace();
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        } finally {
            DAO.disconnect(conn, rs, statement);
            return projectID;
        }
    }
}
