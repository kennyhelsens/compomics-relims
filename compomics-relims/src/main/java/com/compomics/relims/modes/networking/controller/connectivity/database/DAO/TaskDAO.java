/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import com.compomics.relims.modes.networking.controller.taskobjects.Task;
import com.compomics.relims.modes.networking.controller.taskobjects.TaskContainer;
import com.compomics.util.experiment.identification.SearchParameters;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class TaskDAO {

    private Logger logger = Logger.getLogger(TaskDAO.class);
    private long newTaskID = 0L;
    private TaskContainer taskMap;
    private String clientID;
    private HashMap<String, String> tasksToStore = new LinkedHashMap<>();
    private List<String> subList;

    public void updateTaskStatus(long taskID, String status) {
        PreparedStatement statement = null;
        Connection conn = null;
        try {
            conn = DAO.getConnection(logger.getName());
            String query = "update Tasks set TaskState = ? where TaskID=?";
//            statement = conn.prepareStatement("BEGIN");
            //           statement.execute();
            statement = conn.prepareStatement(query);
            statement.setString(1, status.toString());
            statement.setLong(2, taskID);
            statement.setQueryTimeout(60);
            statement.executeUpdate();
//            statement = conn.prepareStatement("COMMIT");
//            statement.execute();
            statement.close();
        } catch (SQLException ex) {
            logger.error("Error changing status of task " + taskID + " to " + status);
            logger.error(ex);
        } finally {
            if (statement != null) {
                statement = null;
            }
            DAO.release(conn);
        }
    }

    public void updateSearchParameters(long taskID, SearchParameters searchParameters) {
        PreparedStatement statement = null;
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
            conn = DAO.getConnection(logger.getName());
            String query = "update Tasks set searchParameters = ? where TaskID=?";
//            statement = conn.prepareStatement("BEGIN");
            //           statement.execute();
            statement = conn.prepareStatement(query);
            statement.setBytes(1, searchparameterToStore);
            statement.setLong(2, taskID);
            statement.setQueryTimeout(60);
            statement.executeUpdate();
//            statement = conn.prepareStatement("COMMIT");
//            statement.execute();
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

    public void deleteTask(long taskID) {
        PreparedStatement statement = null;
        Connection conn = null;
        // Make PreparedStatements
        try {
            conn = DAO.getConnection(logger.getName());
//            statement = conn.prepareStatement("BEGIN");
//            statement.execute();
            String query = "delete from Tasks where TaskID = ?";
            statement = conn.prepareStatement(query);
            statement.setLong(1, taskID);
            statement.setQueryTimeout(60);
            statement.executeUpdate();
//            statement = conn.prepareStatement("COMMIT");
//            statement.execute();
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

    public String readTask(long taskID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        String message = null;
        try {
            conn = DAO.getConnection(logger.getName());
//            statement = conn.prepareStatement("BEGIN");
//            statement.execute();
            //     String query = "select * from " + RelimsProperties.getTaskDatabaseName()+".Tasks where TaskID = ?";
            String query = "select * from Tasks where TaskID = ?";
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
            rs.close();
            statement.close();
        } catch (SQLException ex) {
            message = "Could not read stream !";
            logger.error(ex);
        } finally {
            if (rs != null) {
                rs = null;
            }
            if (statement != null) {
                statement = null;
            }
            DAO.release(conn);
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
        String foundFasta;
        Boolean foundAllowPipeline = false;
        byte[] foundSearchParameters = null;
        Task relimsWorkingTask = null;
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        //find new task and make a workingTask out of it
        try {
            conn = DAO.getConnection(logger.getName());
            //           statement = conn.prepareStatement("BEGIN");
            //           statement.execute();
            String query = "select * from Tasks where (TaskState = ?) order by Timestamp LIMIT 1";
            statement = conn.prepareStatement(query);
            statement.setString(1, "NEW");
            rs = statement.executeQuery();
            if (rs.next()) {
                foundTaskID = rs.getLong("TaskID");
                foundProjectID = rs.getString("ProjectID");
                foundSource = rs.getString("SourceID");
                foundUserID = rs.getString("ClientID");
                foundFasta = rs.getString("Fasta");
                foundAllowPipeline = rs.getBoolean("usePride");
                relimsWorkingTask = new Task(foundTaskID, foundProjectID, foundSource, foundUserID, foundFasta);
            }
            rs.close();
            statement.close();
        } catch (SQLException ex) {
            logger.error(ex);
        } finally {
            if (rs != null) {
                rs = null;
            }
            if (statement != null) {
                statement = null;
            }
            DAO.release(conn);
            return relimsWorkingTask;

        }
    }

    public long startupSweep() {
        PreparedStatement statement = null;
        Connection conn = null;
        logger.info("Sweeping database for incorrectly shutdown tasks...");
        long updates = 0L;
        boolean sweeped = false;
        while (!sweeped) {
            try {
                conn = DAO.getConnection(logger.getName());
                //         statement = conn.prepareStatement("BEGIN");
                //         statement.execute();
                String query = "update Tasks set TaskState = ? where TaskState=? OR TaskState=?";
                statement = conn.prepareStatement(query);
                statement.setString(1, "NEW");
                statement.setString(2, "RUNNING");
                statement.setString(3, "FAILED");
                updates = statement.executeUpdate();
                sweeped = true;
                logger.debug(updates + " tasks have been rescheduled...");
                //         statement = conn.prepareStatement("COMMIT");
                //         statement.execute();
                statement.close();
            } catch (SQLException ex) {
                if (!ex.toString().contains("database is locked")) {
                    //            System.err.println(ex);
                    //          ex.printStackTrace();
                    sweeped = false;
                    ex.printStackTrace();
                }
            } finally {
                if (statement != null) {
                    statement = null;
                }
                if (sweeped) {
                    logger.info("Reset " + updates + " tasks");
                }
                DAO.release(conn);
            }
        }
        return updates;
    }

    public boolean storeTasks(List<String> aProjectIDList, boolean allowPrideAsaPipeline, String fasta,String sourceID) throws IOException {
        PreparedStatement statement = null;
        Connection conn = null;
        boolean success = false;

        try {
            conn = DAO.getConnection(logger.getName());
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            for (String aProjectID : aProjectIDList) {
                String query = "";
                query = " insert into Tasks"
                        + " (ProjectID,TaskState,ClientID,SourceID,TimeStamp,ProjectName,Fasta,usePride) "
                        + "values (?,?,?,?,?,?,?,?)";
                success = false;
                int attempts = 0;
                while (!success) {
                    try {
                        attempts++;
                        statement = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                        statement.setString(1, aProjectID);
                        statement.setString(2, "NEW");
                        statement.setString(3, clientID);
                        statement.setString(4, sourceID);
                        java.sql.Timestamp sqlDate = new java.sql.Timestamp(new java.util.Date().getTime());
                        statement.setTimestamp(5, sqlDate);
                        statement.setString(6, tasksToStore.get(aProjectID));
                        statement.setString(7, fasta);
                        statement.setBoolean(8, allowPrideAsaPipeline);
                        statement.execute();
                        success = true;
                        //                 statement = conn.prepareStatement("COMMIT");
                        //                 statement.execute();
                        statement.close();
                    } catch (ArrayIndexOutOfBoundsException e) {
                        logger.error(e);
                    } catch (SQLException e) {
                        logger.error(e);
                        success = false;
                        if (attempts > 9) {
                            DAO.release(conn);
                            return false;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logger.error("Error creating new Task");
            logger.error(ex);
            success = false;
        } finally {
            if (statement != null) {
                statement = null;
            }
            DAO.release(conn);
            return success;
        }
    }

    public Map<String, Long> pushTaskMapToDB(TaskContainer taskMap) {

        //WorkerPool.setDatabaseLocked(true);
        this.taskMap = taskMap;
        Map<String, Long> generatedTaskIDs = new HashMap<>();
        clientID = taskMap.getName();
        tasksToStore = taskMap.getTaskList();
        List<String> taskList = new ArrayList<>();

        //break up the taskcontainer into managable subtasks (10000 projects max)
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
            try {
                if (taskList.size() > batchSize) {
                    subList = taskList.subList(0, batchSize);
                } else {
                    subList = taskList.subList(0, remainder);
                }
                //store tasks in a future object...
                storeTasks(subList, taskMap.isPrideAsaEnabled(), taskMap.getFasta(),taskMap.getSourceID());
                taskList.removeAll(subList);
                i++;
            } catch (Exception e) {
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

    public void resetTask(long taskID) {
        PreparedStatement statement = null;
        Connection conn = null;
        if (taskID != 0L) {
            try {
                conn = DAO.getConnection(logger.getName());
                //         statement = conn.prepareStatement("BEGIN");
                //         statement.execute();
                statement = conn.prepareStatement("update Tasks set TaskState = ? where TaskID=?");
                statement.setString(1, "NEW");
                statement.setLong(2, taskID);
                statement.setQueryTimeout(60);
                statement.executeUpdate();
                //         statement = conn.prepareStatement("COMMIT");
                //         statement.execute();
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
    }

    public int resetFailedTasks() {
        PreparedStatement statement = null;
        Connection conn = null;
        int restored = 0;
        try {
            conn = DAO.getConnection(logger.getName());
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            statement = conn.prepareStatement("update Tasks set TaskState ='NEW' where TaskState = 'FAILED'");
            statement.setQueryTimeout(60);
            restored = statement.executeUpdate();
            //     statement = conn.prepareStatement("COMMIT");
            //     statement.execute();
            statement.close();
        } catch (SQLException ex) {
            logger.error(ex);
        } finally {
            if (statement != null) {
                statement = null;
            }
            DAO.release(conn);
            return restored;
        }
    }

    public HashMap<String, Object> getTaskInformation(long taskID) {
        PreparedStatement statement = null;
        Connection conn = null;
        HashMap<String, Object> informationMap = new HashMap<>();
        ResultSet rs = null;

        try {
            //get the columncount
            conn = DAO.getConnection(logger.getName());
            //     statement = conn.prepareStatement("BEGIN");
            //     statement.execute();
            statement = conn.prepareStatement("select * from Tasks inner join WorkerSpecs on Tasks.TaskID = WorkerSpecs.TaskID where Tasks.TaskID = ?");
            statement.setLong(1, taskID);
            rs = statement.executeQuery();

            if (rs.next()) {
                ResultSetMetaData rsmd = rs.getMetaData();
                int NumOfCol = rsmd.getColumnCount();
                for (int i = 1; i <= NumOfCol; i++) {
                    informationMap.put(rsmd.getColumnName(i), rs.getObject(rsmd.getColumnName(i)));
                }
            }
            //         statement = conn.prepareStatement("COMMIT");
            //         statement.execute();
            statement.close();
            rs.close();

        } catch (SQLException ex) {
            ex.printStackTrace();
            logger.error(ex);
        } finally {
            if (statement != null) {
                statement = null;
            }
            if (rs != null) {
                rs = null;
            }
            DAO.release(conn);
            return informationMap;
        }
    }

    public String getProjectID(long taskID) {
        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        conn = DAO.getConnection(logger.getName());
        String projectID = "unknown";
        try {
            String query = ("select ProjectID from Tasks where TaskID = ?");
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
            return projectID;
        }
    }
}
