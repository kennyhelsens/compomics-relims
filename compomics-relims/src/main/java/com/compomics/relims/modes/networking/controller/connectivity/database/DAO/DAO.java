package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.security.BackupService;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class DAO {

    private static Connection conn;
    private static Logger logger = Logger.getLogger(DatabaseService.class);
    private static String protocol;
    private static String dbName;// the name of the database
    private static File directoryFile;
    private static String directory;
    private static List<String> queryList = new ArrayList<String>();
    private static DAO dao;
    private static boolean connectionInUse;
    private static String connectionHogger;

    public static void shutdown() {
        try {
            if (conn != null) {
                makeSingleConnection();
            }
            BackupService.backupSQLliteDatabase();

        } catch (InterruptedException | SQLException ex) {
            logger.error("Could not backup databases when shutting down...");
        }
    }

    private DAO() {
        try {
            setup();
            makeSingleConnection();
        } catch (SQLException ex) {
            logger.error(ex);
        } catch (InterruptedException ex) {
            logger.error(ex);
        }
    }

    private void setup() {
        RelimsProperties.initialize(false);
        protocol = RelimsProperties.getTaskDatabaseProtocol();
        dbName = RelimsProperties.getTaskDatabaseName();// the name of the database
        directoryFile = new File(RelimsProperties.getConfigFolder().getAbsolutePath().replace("conf", "databases"));
        directory = directoryFile.getAbsolutePath();
        if (!directoryFile.exists()) {
            boolean success = directoryFile.mkdirs();
            if (!success) {
                logger.error("Did not successfully create directory");
            }
        }
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public static String getProtocol() {
        return protocol;
    }

    public static String getDbName() {
        return dbName;
    }

    public static String getDirectory() {
        return directory;
    }

    public static DAO getInstance() {
        if (DAO.dao == null) {
            dao = new DAO();
        }
        return dao;
    }

    public static Connection getConnection(String name) {

        while (connectionInUse) {
            try {
                Thread.sleep(3000);
                logger.debug(connectionHogger + " is hogging the connection !");
            } catch (InterruptedException ex) {
                logger.error(ex);
            }
        }
        connectionHogger = name;
        connectionInUse = true;
        return conn;

    }

    public String getDbPrefix() {
        return RelimsProperties.getDbDatabaseName() + ".";
    }

    private synchronized static void makeSingleConnection() throws SQLException, InterruptedException {
        if (conn == null) {
            File directoryFile = new File(directory);
            if (!directoryFile.exists()) {
                directoryFile.mkdirs();
            }
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection(protocol + directory + "/" + dbName + ".db");
            } catch (ClassNotFoundException ex) {
                logger.error(ex);
            }

        }
    }

    public static void release(Connection conn) {
        connectionInUse = false;
    }

    public static void createTables() {
        Connection connectionInstance = getConnection(logger.getName());
        Statement statement = null;
        setupMajorInitiationQuery();
        try {
            if (RelimsProperties.getTaskDatabaseDriver().contains("derby")) {
                logger.debug("Creating new database...");
                //connectionInstance.setTransactionIsolation(4);
            } else {
                if (!new File(directory).exists()) {
                    (new File(directory)).mkdir();
                }
                statement = connectionInstance.createStatement();
                for (String aSubQuery : queryList) {
                    statement.execute(aSubQuery.toString());
                }
            }
            statement.close();
        } catch (Exception ex) {
            if (!ex.toString().contains("already exists")) {
                ex.printStackTrace();
            }
        } finally {
            if (statement != null) {
                statement = null;
            }
            release(connectionInstance);
        }
    }

    private static void setupMajorInitiationQuery() {


// MAKE TASK TABLE____________________________________________TASK TABLE
        queryList.add("CREATE TABLE Tasks ("
                + "TaskID INTEGER PRIMARY KEY,"
                + "ProjectID VarChar(50),"
                + "TaskState VARCHAR(20),"
                + "ClientID VARCHAR(100),"
                + "SourceID VARCHAR(20),"
                + "ProjectName VarChar(255),"
                + "FASTA VarChar(255),"
                + "usePride int,"
                + "Timestamp TIMESTAMP);");
// MAKE TASK TABLE____________________________________________PRIDE DETAILS : TODO rename this
        queryList.add("CREATE TABLE PRIDEXMLERRORS ("
                + "ProjectID VarChar(50),"
                + "ErrorCode INTEGER,"
                + "Description VarChar(150),"
                + "SeverityLevel VarChar(25));");

// MAKE TASK WorkerSpecs_______________________________________TASK WorkerSpecs : TODO RENAME THIS
        queryList.add("CREATE TABLE WorkerSpecs ("
                //Taskrelated Parameters
                + "TaskID BIGINT,"
                + "TaskTime BIGINT,"
                + "Workerport BIGINT,"
                + "Workerhost VARCHAR(75),"
                //Memory related parameters
                + "committedVirtualMemorySize BIGINT,"
                + "freePhysicalMemorySize BIGINT, "
                + "totalPhysicalMemorySize BIGINT,"
                + "freeSwapSpaceSize BIGINT,"
                + "totalSwapSpaceSize BIGINT,"
                // CPU related parameters
                + "processCPUTime BIGINT,"
                + "cores BIGINT,"
                // System related parameters
                + "OSArch VARCHAR(25),"
                + "OSVersion VARCHAR(25),"
                + "OSName VARCHAR (50),"
                + "SystemCPULoad FLOAT,"
                + "ProcessCPULoad FLOAT,"
                //JAVA related parameters
                + "JAVAVersion VARCHAR(25),"
                //User related parameters
                + "userID VARCHAR(100));");
// MAKE WORKERS TABLE____________________________________________WORKERS TABLE
        queryList.add("CREATE TABLE Workers ("
                + "HostName VARCHAR(75),"
                + "workerPort INTEGER,"
                + "taskID VARCHAR(30));");
// MAKE TASK PROJECT RESULTS_____________________________________PROJECT RESULT TABLE
        queryList.add("CREATE TABLE ProjectResults ("
                + "TaskID BIGINT,"
                + "projectID VarChar(50),"
                + "parameterName VarChar(30),"
                + "parameterValue VarChar(200));");
    }
}
