package com.compomics.relims.tools;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.provider.ColimsConnectionProvider;
import org.apache.log4j.Logger;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Scanner;

import static com.compomics.relims.conf.RelimsProperties.getColimsDbSchema;
import static com.compomics.relims.conf.RelimsProperties.getColimsDbServer;

/**
 * This class holds everything to initiate the latest
 * compomics-colims database
 */
public class ColimsInitiatorTool {

    private static Logger logger = Logger.getLogger(ColimsInitiatorTool.class);
    private static boolean DROP_IF_EXISTS = true;
    private static String SQL_CHECK_DATABASE = "SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME = '%s'";
    private static String SQL_DROP_SCHEMA = "DROP SCHEMA %s";
    private static String SQL_CREATE_SCHEMA = "CREATE SCHEMA %s";
    private static String SQL_SELECT_SCHEMA = "USE %s";
    private static String SQL_SOURCE_SQL = "SOURCE %s";
    private Connection iConn;

    public ColimsInitiatorTool() {
        iConn = ColimsConnectionProvider.getConnection();
        try {
            handleSchema();
            setupTables();
            logger.debug(String.format("successfully initiated colims schema %s on %s", getColimsDbSchema(), getColimsDbServer()));

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally{
            try {
                logger.debug("closing database connection");
                iConn.close();
                logger.debug("exiting now");
                System.exit(0);
            } catch (SQLException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * This method creates the colims tables via API calls to colims-core
     */
    private void setupTables() throws IOException, SQLException {
        logger.debug(String.format("loading colims application context", getColimsDbSchema()));

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("colims-core-context.xml");
        LocalSessionFactoryBean sessionFactoryBean = (LocalSessionFactoryBean) applicationContext.getBean("&sessionFactory");

        logger.debug(String.format("loading SQL to setup colims-schema on %s", getColimsDbSchema()));
        SchemaExport schemaExport = new SchemaExport(sessionFactoryBean.getConfiguration());
        Path lTempFile = Files.createTempFile("colims-schema", ".sql");
        schemaExport.setOutputFile(lTempFile.toString());
        schemaExport.setFormat(true);
        schemaExport.setDelimiter(";");
        schemaExport.execute(true, false, false, true);
        logger.debug(String.format("written SQL to %s", lTempFile.toString()));


        PreparedStatement ps;
        logger.debug(String.format("executing SQL to create tables"));
        // Select database
        ps = iConn.prepareStatement(String.format(SQL_SELECT_SCHEMA, getColimsDbSchema()));
        ps.execute();
        ps.close();

        // Execute table creation
        executeSqlScript(lTempFile.toFile());


    }

    public static void main(String[] args) throws SQLException {
        RelimsProperties.initialize(false);
        new ColimsInitiatorTool();

        
    }

    /**
     * This method checks whether the schema exists, and drops the schema if needed.
     * If the constraints are passed, it will create a new schema on which the tables can be created.
     *
     * @throws SQLException
     */
    private void handleSchema() throws SQLException {
        PreparedStatement ps;
        logger.debug(String.format("checking if schema %s exists", getColimsDbSchema()));
        ps = iConn.prepareStatement(String.format(SQL_CHECK_DATABASE, getColimsDbSchema()));

        ResultSet lResultSet = ps.executeQuery();
        boolean lSchemaExists = lResultSet.next();
        ps.close();

        if (lSchemaExists) {
            // colims schema exists
            if (DROP_IF_EXISTS) {
                logger.debug(String.format("dropping existing schema %s", getColimsDbSchema()));
                ps = iConn.prepareStatement(String.format(SQL_DROP_SCHEMA, getColimsDbSchema()));
                ps.execute();
                ps.close();
            } else {
                abort(String.format("not allowed to drop existing schema %s", getColimsDbSchema()));
            }
        }

        logger.debug(String.format("creating schema %s for colims", getColimsDbSchema()));
        ps = iConn.prepareStatement(String.format(SQL_CREATE_SCHEMA, getColimsDbSchema()));
        ps.execute();
        ps.close();
    }

    private void executeSqlScript(File inputFile) {

        // Delimiter
        String delimiter = ";";

        // Create scanner
        Scanner scanner;
        try {
            scanner = new Scanner(inputFile).useDelimiter(delimiter);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
            return;
        }

        // Loop through the SQL file statements
        Statement currentStatement = null;
        while(scanner.hasNext()) {

            // Get statement
            String rawStatement = scanner.next() + delimiter;
            try {
                // Execute statement
                if(!rawStatement.equals("\n;")){
                    currentStatement = iConn.createStatement();
                    currentStatement.execute(rawStatement);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                // Release resources
                if (currentStatement != null) {
                    try {
                        currentStatement.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                currentStatement = null;
            }
        }
    }

    private static void abort(String aMessage) {
        logger.debug("aborting colims initiation:");
        logger.debug(aMessage);
        logger.debug("exiting now");
        System.exit(0);
    }
}
