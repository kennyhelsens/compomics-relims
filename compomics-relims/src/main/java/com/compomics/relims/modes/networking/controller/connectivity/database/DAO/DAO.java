package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

public class DAO {

    private static Connection conn;
    private static Logger logger = Logger.getLogger(DatabaseService.class);
    private static String protocol;
    private static String dbName;// the name of the database
    private static File directoryFile;
    private static String directory;
    private static List<String> queryList = new ArrayList<String>();
    private static DataSource dataSource;
    private static final Object lock = new Object();

    private DAO() {
        RelimsProperties.initialize(false);
        protocol = RelimsProperties.getTaskDatabaseProtocol();
        dbName = RelimsProperties.getTaskDatabaseName();// the name of the database
        directoryFile = new File(RelimsProperties.getConfigFolder().getAbsolutePath().replace("conf", "databases"));
        dbName = RelimsProperties.getTaskDatabaseName();// the name of the database
        directory = directoryFile.getAbsolutePath();
        if (!protocol.contains("derby")) {
            dbName = dbName + ".db";
        }
        if (!directoryFile.exists()) {
            boolean success = directoryFile.mkdirs();
            if (!success) {
                logger.error("Did not successfully create directory");
            }
        }
        this.dataSource = setupDataSource(protocol + directory + "/" + dbName);
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

    public static void initiate() {
        new DAO();
    }

    public synchronized static Connection getConnection() {
        // synchronized (dataSource) {
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
//        }
        return null;
    }

    public String getDbPrefix() {
        return RelimsProperties.getDbDatabaseName() + ".";
    }

    public static DataSource setupDataSource(String connectURI) {
        GenericObjectPool test = new GenericObjectPool(null);
        test.setMaxActive(500);
        test.setMaxIdle(Integer.MAX_VALUE);
        ObjectPool connectionPool = test;
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(connectURI, null);
        PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
        PoolingDataSource dataSource = new PoolingDataSource(connectionPool);

        return dataSource;
    }
    //make only 1 connection !

    public synchronized void initializePoollessConnection() throws SQLException, InterruptedException {
        if (conn == null) {
            File directoryFile = new File(directory);
            if (protocol.contains("derby")) {
                conn = DriverManager.getConnection(protocol + directory + dbName);
            } else {
                if (!directoryFile.exists()) {
                    directoryFile.mkdirs();
                    conn = DriverManager.getConnection(protocol + directory + dbName + ".db");
                } else {
                    conn = DriverManager.getConnection(protocol + directory + dbName + ".db");
                }
            }
        }
    }
    //use the connectionPool

    public synchronized void initializeConnectionPool() throws SQLException, InterruptedException {
        if (dataSource == null) {
            File directoryFile = new File(directory);
            dataSource = setupDataSource(protocol + directory + "/" + dbName + ".db");
            if (!directoryFile.exists()) {
                directoryFile.mkdirs();
            }
        }
    }

    private static synchronized Connection prepareWritingConnection() {
        synchronized (lock) {
            Connection connectionInstance = null;
            Boolean success = false;
            while (!success) {
                Statement statement = null;
                try {
                    connectionInstance = getConnection();
                    //connectionInstance.setAutoCommit(false);
                    statement = connectionInstance.createStatement();
                    statement.execute("begin immediate");
                    connectionInstance.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

                    success = true;
                } catch (SQLException ex) {
                    if (!ex.toString().contains("cannot start a transaction within a transaction") && !ex.toString().contains("database is locked")) {
                        ex.printStackTrace();
                    }
                }
            }
            return connectionInstance;
        }
    }

    public synchronized Connection prepareReadingConnection() {
        synchronized (lock) {
            Connection connectionInstance = null;
            Boolean success = false;
            while (!success) {
                Statement statement = null;
                try {
                    connectionInstance = getConnection();
                    connectionInstance.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
                    success = true;
                } catch (SQLException ex) {
                    if (!ex.toString().contains("cannot start a transaction within a transaction")) {
                        ex.printStackTrace();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return connectionInstance;
        }
    }

    public static void disconnect(@Nullable Connection conn, @Nullable ResultSet rs, @Nullable Statement statement) {
        try {
            statement.execute("commit");
            rs.close();
        } catch (Exception ex) {
            if (rs != null) {
                rs = null;
            }
            try {
                statement.close();
            } catch (Exception ex2) {
                if (statement != null) {
                    statement = null;
                }
            }
            try {
                conn.close();
            } catch (Exception ex3) {
                if (conn != null) {
                    conn = null;
                }
            }
        }
    }

    public static void createTables() {
        Connection connectionInstance = null;
        Statement statement = null;
        setupMajorInitiationQuery();
        try {
            if (RelimsProperties.getTaskDatabaseDriver().contains("derby")) {
                logger.debug("Creating new database...");
                connectionInstance = prepareWritingConnection();
                //connectionInstance.setTransactionIsolation(4);
            } else {
                if (!new File(directory).exists()) {
                    (new File(directory)).mkdir();
                }
                //conn = DriverManager.getConnection("jdbc:sqlite:" + directory + "/" + dbName + ".db");
                connectionInstance = prepareWritingConnection();
                statement = connectionInstance.createStatement();
                for (String aSubQuery : queryList) {
                    statement.execute(aSubQuery.toString());
                }
            }
        } catch (Exception ex) {
            if (!ex.toString().contains("already exists")) {
                ex.printStackTrace();
            }
        } finally {
            disconnect(connectionInstance, null, statement);
        }
    }

    private static void setupMajorInitiationQuery() {


// MAKE TASK TABLE____________________________________________TASK TABLE
        queryList.add("CREATE TABLE Tasks ("
                + "TaskID INTEGER PRIMARY KEY,"
                + "ProjectID VarChar(50),"
                + "STRATEGYID VarChar(50),"
                + "SOURCEID VarChar(50),"
                + "TaskState VARCHAR(20),"
                + "ClientID VARCHAR(100),"
                + "ProjectName VarChar(255),"
                + "Timestamp TIMESTAMP,"
                + "usePride BOOLEAN,"
                + "searchParameters BLOB);");
// MAKE USER TABLE____________________________________________TASK USERS : TODO this is obsolete
        queryList.add("CREATE TABLE Users ("
                + "UserID INTEGER PRIMARY KEY,"
                + "Username VARCHAR(30),"
                + "HashedP VARCHAR(256),"
                + "Salt VARCHAR(50),"
                + "eMail VARCHAR(100));");
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
        /*// MAKE TASK PRIDE METADATA_____________________________________PRIDE META DATA TABLE
         queryList.add("CREATE TABLE PrideMetaData ("
         + "projectID VarChar(50),"
         + "parameterName VarChar(30),"
         + "parameterValue VarChar(200));");*/
    }

    public void setTimeOut(Connection connection) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
            statement.execute("CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY('sqlite.locks.deadlockTimeout', '30')");
            logger.debug("Set timeout configuration");
        } catch (SQLException e) {
            logger.debug("Could not set configuration");
            e.printStackTrace();
            logger.debug(e);
        } finally {
            disconnect(conn, null, statement);
        }
    }
}
