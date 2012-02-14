package com.compomics.relims.model.mslims;

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
public class MSLIMS {
	// Class specific log4j logger for DatabaseObjectFactory instances.
	private static Logger logger = Logger.getLogger(MSLIMS.class);
// ------------------------------ FIELDS ------------------------------

    private static Connection CONNECTION;
    private static int lCounter = 0;

// -------------------------- STATIC METHODS --------------------------

    static {
        try {

            establishConnection();

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

    private static void establishConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
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

        CONNECTION = d.connect(url, lProps);

        logger.info("DatabaseObjectFactory established static connection to " + url + " for user " + user);
    }

    public static void reset() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(CONNECTION != null){
            if(CONNECTION.isClosed() == false){
                CONNECTION.close();
            }
        }
        establishConnection();
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
        return CONNECTION;
    }
}
