/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.service;

import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.DAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.PrideDetailsDAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.ResultDAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.StatisticsDAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.TaskDAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.UserDAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.DAO.WorkerDAO;
import com.compomics.relims.modes.networking.controller.connectivity.database.security.BackupService;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.Task;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.TaskContainer;
import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.log4j.Logger;

public class DatabaseService {

    private static Logger logger;
    private final static TaskDAO TaskDAO = new TaskDAO();
    private final static UserDAO UserDAO = new UserDAO();
    private final static WorkerDAO WorkerDAO = new WorkerDAO();
    private final static StatisticsDAO StatisticsDAO = new StatisticsDAO();
    private final static PrideDetailsDAO prideDetailsDAO = new PrideDetailsDAO();
    private final static ResultDAO projectResultDAO = new ResultDAO();
    private final static BackupService backupService = BackupService.getInstance();
    private final static ExecutorService dbExService = Executors.newSingleThreadExecutor();

    /*     
     the default framework is embedded
     */
    protected String framework = RelimsProperties.getTaskDatabaseFramework();
    ;
    protected String driver = RelimsProperties.getTaskDatabaseDriver();
    protected String protocol = RelimsProperties.getTaskDatabaseProtocol();
    protected String dbName = RelimsProperties.getTaskDatabaseName();
    protected String password = RelimsProperties.getTaskDatabasePassword();
    protected File directory;
    protected ResultSet rs = null;
    private static DatabaseService dds;

    private DatabaseService() {
        try {
            this.logger = Logger.getLogger(DatabaseService.class);
            directory = RelimsProperties.getTaskDatabaseLocation();
        } catch (NullPointerException e) {
        }
    }

    public static DatabaseService getInstance() {
        if (dds == null) {
            dds = new DatabaseService();
        }
        return dds;
    }

    //==========COMMON METHODS===============//
    public void setWALMode() throws SQLException {
        Connection setWALConn = null;
        Statement setWALstate = null;
        try {
            setWALConn = DAO.getConnection();
            setWALstate = setWALConn.createStatement();
            setWALstate.execute("PRAGMA journal_mode=WAL");
            DAO.disconnect(setWALConn, rs, setWALstate);
            logger.info("WALMODE set");
        } catch (Exception e) {
            DAO.disconnect(setWALConn, null, setWALstate);
        }
    }

    public synchronized void launch() {
        DAO.initiate();
        // load the desired JDBC driver
        loadDriver();
        try {
            //set WAL mode
            setWALMode();
        } catch (SQLException ex) {
            logger.error(ex);
            ex.printStackTrace();
        }
        logger.debug("Locating database at : " + directory + "...");
    }

    public synchronized boolean createRootUser() {
        return createUser("admin", "admin", "admin@compomics.com");
    }

    private synchronized void loadDriver() {

        try {
            Class.forName(driver).newInstance();
            logger.debug("Loaded the appropriate driver...");
        } catch (ClassNotFoundException cnfe) {
            logger.error("Unable to load the JDBC driver " + driver);
            logger.error("Please check your CLASSPATH.");
        } catch (InstantiationException ie) {
            logger.error("Unable to instantiate the JDBC driver " + driver);
            ie.printStackTrace(System.err);
        } catch (IllegalAccessException iae) {
            logger.error("Not allowed to access the JDBC driver " + driver);
            iae.printStackTrace(System.err);
        }
    }

    public synchronized long startupSweep() {

        return TaskDAO.startupSweep();

    }

    //==========USER RELATED METHODS===============//
    public synchronized boolean isUsernameAvailable(String userID) {

        return UserDAO.findUsername(userID);


    }

    public synchronized boolean createUser(String username, String password, String eMail) {

        //first find out if the user exists...
        if (!UserDAO.findUsername(username)) {
            return UserDAO.createUser(username, password, eMail);
        } else {
            return false;
        }
    }

    public synchronized boolean isLoginCredentialsCorrect(String username, String password) {

        return UserDAO.isLoginCredentialsCorrect(username, password);

    }

//==========TASK RELATED METHODS===============//
    public synchronized void updateTask(long taskID, String status) {

        TaskDAO.updateTaskStatus(taskID, status);

    }

    public synchronized String readTask(long taskID) {

        return TaskDAO.readTask(taskID);

    }

    public synchronized List<String[]> getUserTasks(String userID, String queryParameters) {

        return TaskDAO.getUserTasks(userID, queryParameters);

    }

    public synchronized Map<String, Long> pushTaskMapToDB(TaskContainer taskMap) {

        return TaskDAO.pushTaskMapToDB(taskMap);

    }

    public synchronized void deleteTask(long taskID) {

        TaskDAO.deleteTask(taskID);

    }

    public synchronized void resetTask(long taskID) {

        TaskDAO.resetTask(taskID);

    }

    public synchronized int restoreTasks() {

        return TaskDAO.resetFailedTasks();

    }

    public synchronized Task findNewTask() throws SQLException, NullPointerException {

        return TaskDAO.findNewTask();

    }

    public synchronized String getProjectID(long taskID) {

        return TaskDAO.getProjectID(taskID);

    }

    //==========WORKER RELATED METHODS===============//
    public synchronized void createWorker(String hostname, int workerPort, long taskID) {

        WorkerDAO.createWorker(hostname, workerPort, taskID);

    }

    public synchronized long getTaskID(String hostname, int workerPort) {

        return WorkerDAO.getTaskID(hostname, workerPort);

    }

    public synchronized void deleteWorker(String hostname, int workerPort) {
        WorkerDAO.deleteWorker(hostname, workerPort);
    }

    //==========STATISTIC RELATED METHODS===============//
    public synchronized void storeStatistics(HashMap<String, Object> statistics, String workerhost) {

        StatisticsDAO.createTaskStatistics(statistics, workerhost);
    }

    public synchronized HashMap<String, Object> getAverageStatistics() {

        return StatisticsDAO.getAverageStatistics();
    }

    public synchronized Map<String, Object> getTaskInformation(long taskID) {

        return TaskDAO.getTaskInformation(taskID);
    }

    public synchronized void createTables() {
        logger.info("Creating Tables...");
        DAO.createTables();
    }

    public void storeErrorList(List<ConversionError> errorList, String projectID) {
        prideDetailsDAO.createPrideDetails(errorList, projectID);
    }

    //==========PROJECT RESULT RELATED METHODS===============//
    public void storeResults(long taskID, String projectId, HashMap<String, Object> resultMap) {
        projectResultDAO.storeProjectResults(taskID, projectId, resultMap);
    }
}
