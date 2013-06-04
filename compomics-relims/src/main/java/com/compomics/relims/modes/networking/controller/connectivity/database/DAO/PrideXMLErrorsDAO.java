package com.compomics.relims.modes.networking.controller.connectivity.database.DAO;

import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import static com.compomics.relims.modes.networking.controller.connectivity.database.DAO.DAO.getConnection;
import com.compomics.relims.modes.networking.controller.connectivity.database.service.DatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Kenneth
 */
public class PrideXMLErrorsDAO {

    private org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DatabaseService.class);

    public boolean createPRIDEXMLERRORS(List<ConversionError> errorList, String projectID) {

        PreparedStatement statement = null;
        Connection conn = null;
        if (errorList == null) {
            errorList = new ArrayList<ConversionError>();
        }

        boolean createSucces = false;
        List<String> errors = null;

        try {
            conn = DAO.getConnection(logger.getName());

            //statement.execute("BEGIN");

            String query = "insert into PRIDEXMLERRORS"
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
            statement.close();
        } catch (SQLException ex) {
            logger.error("Error recording WorkerSpecs");
            logger.error(ex);
            ex.printStackTrace();
        } finally {
            if (statement != null) {
                statement = null;
            }
            if (errors != null) {
                if (!errors.isEmpty()) {
                    createSucces = false;
                }
            } else {
                createSucces = true;
            }
            DAO.release(conn);
            return createSucces;
        }
    }
}
