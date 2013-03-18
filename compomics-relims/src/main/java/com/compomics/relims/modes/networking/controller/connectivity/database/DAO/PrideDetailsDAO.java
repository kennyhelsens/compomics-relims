/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;


import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kenneth
 */
public class PrideDetailsDAO {

    private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DatabaseService.class);
 
    public boolean createPrideDetails(List<ConversionError> errorList, String projectID) {

        PreparedStatement statement = null;
        ResultSet rs = null;
        Connection conn = null;
        if (errorList == null) {
            errorList = new ArrayList<ConversionError>();
        }

        boolean createSucces = false;
        List<String> errors = null;

        try {
            conn = DAO.getConnection();

            //statement.execute("BEGIN");

            String query = "insert into " + RelimsProperties.getDbPrefix() + "PrideDetails"
                    + " values (?,?,?,?)";
            statement = conn.prepareStatement(query);
            // how to insert multiple rows like this with a statement ?
            for (ConversionError anError : errorList) {
                statement.setString(1, projectID);
                statement.setInt(2, anError.getCode());
                statement.setString(3, anError.getDescription());
                statement.setString(4, anError.getSeverityLevel());
                statement.executeUpdate();
            }
            //statement.execute("COMMIT");
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
            return createSucces;
        }
    }
}
