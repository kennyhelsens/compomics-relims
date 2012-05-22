package com.compomics.relims.model;

import com.compomics.relims.conf.RelimsProperties;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: kenny
 * Date: Aug 13, 2009
 * Time: 1:59:00 PM
 * <p/>
 * This class
 */
public class ConnectionProvider {
	// Class specific log4j logger for DatabaseObjectFactory instances.
	private static Logger logger = Logger.getLogger(ConnectionProvider.class);
// ------------------------------ FIELDS ------------------------------

    private static Connection CONNECTION;
    private static int lCounter = 0;

// -------------------------- STATIC METHODS --------------------------

    static {
        initiate();
    }

    public static void initiate(){
        try {

            Properties lProps = new Properties();

            String driver = "com.mysql.jdbc.Driver";
            Driver d = (Driver)Class.forName(driver).newInstance();

            String user = RelimsProperties.getDbUserName();
            String pass = RelimsProperties.getDbPass();
            String dbname = RelimsProperties.getDbDatabaseName();
            String adress = RelimsProperties.getDbAdress();

            if(user != null) {
               lProps.put("user", user);
            }

            if(pass != null) {
               lProps.put("password", pass);
            }
            String url = "jdbc:mysql://" + adress + "/" + dbname;

            logger.info("DatabaseObjectFactory established static connection to " + url + " for user " + user);

            CONNECTION = d.connect(url, lProps);


            logger.info("DatabaseObjectFactory established static connection to " + url + " for user " + user);

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        closeConnection();
        logger.debug("closing mslims connection");
        super.finalize();
    }

    public static void closeConnection() throws SQLException {
        CONNECTION.close();
    }

    protected static Connection getConnection(){

        try {
            if(CONNECTION == null || CONNECTION.isClosed()){
                initiate();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        return CONNECTION;
    }
}
