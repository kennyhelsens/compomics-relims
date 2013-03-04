package com.compomics.relims.model.provider;

import static com.compomics.relims.conf.RelimsProperties.*;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 * Created by IntelliJ IDEA. User: kenny Date: Aug 13, 2009 Time: 1:59:00 PM
 * <p/>
 * This class
 */
@SuppressWarnings({"FeatureEnvy"})
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

    public static void initiate() {
        try {

            Properties lProps = new Properties();

            String driver = "com.mysql.jdbc.Driver";
            Driver d = (Driver) Class.forName(driver).newInstance();

            String user = getDbUserName();
            String pass = getDbPass();
            String dbname = getDbDatabaseName();
            String adress = getDbAdress();

            if (user != null) {
                lProps.put("user", user);
            }

            if (pass != null) {
                lProps.put("password", pass);
            }
            String url = "jdbc:mysql://" + adress + "/" + dbname;

            logger.debug("DatabaseObjectFactory established static connection to " + url + " for user " + user);

            CONNECTION = d.connect(url, lProps);


            logger.debug("DatabaseObjectFactory established static connection to " + url + " for user " + user);

        } catch (com.mysql.jdbc.exceptions.jdbc4.CommunicationsException e) {
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
            return;
        } catch (InstantiationException e) {
            e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
            return;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
            return;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
            return;
        }catch (Exception e) {
            e.printStackTrace();
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
            return;
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

    public static Connection getConnection() {

        try {
            if (CONNECTION == null || CONNECTION.isClosed()) {
                initiate();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
        }

        return CONNECTION;
    }
}
