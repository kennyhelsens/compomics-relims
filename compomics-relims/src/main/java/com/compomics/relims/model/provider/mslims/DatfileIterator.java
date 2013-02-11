package com.compomics.relims.model.provider.mslims;

import com.compomics.mascotdatfile.util.interfaces.MascotDatfileInf;
import com.compomics.mascotdatfile.util.mascot.enumeration.MascotDatfileType;
import com.compomics.mascotdatfile.util.mascot.factory.MascotDatfileFactory;
import com.compomics.mslims.db.accessors.Datfile;
import com.compomics.peptizer.util.fileio.ConnectionManager;
import com.compomics.peptizer.util.iterators.MsLimsIterationUnit;
import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.observer.Checkpoint;
import com.compomics.relims.observer.ProgressManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;

/**
 * This class is a
 */
public class DatfileIterator implements Iterator<MascotDatfileInf> {

    private static Logger logger = Logger.getLogger(DatfileIterator.class);
    /**
     * An arraylist with the iterationunits that must be fetched by this
     * iterator.
     */
    protected ArrayList<MsLimsIterationUnit> iIterationUnits = null;
    /**
     * The currect iterationunit as an instance field.
     */
    private MsLimsIterationUnit iCurrentIterationUnit;
    /**
     * The index of the currect iterationunit.
     */
    private int iIterationUnitIndex = 0;
    /**
     * The MascotDatfile parsing type to be used.
     */
    private MascotDatfileType iMascotDatfileType = MascotDatfileType.INDEX;
    private PreparedStatement ps;
    private ResultSet rs;

    /**
     * Iterate over the peptide identifications from a ms_lims project.
     *
     * @param aConnection java.sql.connection instance to an ms_lims database.
     * @param aProjectID long identifying the project.
     */
    public DatfileIterator(Connection aConnection, long aProjectID) {
        ConnectionManager.getInstance().setConnection(aConnection);
        iProjectID = aProjectID;
        construct();
        logger.debug("created iterator from datfile " + iIterationUnitIndex + " to " + iIterationUnits.size());
    }

    /**
     * Moves to the next file of the folder.
     *
     * @return boolean true if succesfull, false if failure.
     */
    public MascotDatfileInf next() {
        MascotDatfileInf lMascotDatfileInf = null;
        if (iIterationUnitIndex < iIterationUnits.size()) {
            // Set the next file.
            try {

                // Set the current DatfileIterator to new MascotDatfile.
                logger.debug("loading " + iIterationUnits.get(iIterationUnitIndex) + " for project " + iProjectID);

                // This iterator buffers datfiles from ms_lims!!
                String lQuery = "Select * from datfile where datfileid=?";
                PreparedStatement ps = ConnectionManager.getInstance().getConnection().prepareStatement(lQuery);
                ps.setLong(1, iIterationUnits.get(iIterationUnitIndex).getDatfileID());
                ResultSet rs = ps.executeQuery();

                // Create a datfile table accessor by the query.
                rs.next();
                Datfile lDatfileAccessor = new Datfile(rs);

                // Create a new MascotDatfile instance by the bufferedreader of the Datfile tableaccessor.
                lMascotDatfileInf = MascotDatfileFactory.create(lDatfileAccessor.getBufferedReader(), lDatfileAccessor.getFilename(), iMascotDatfileType);

                ps.close();

                // Raise the index!
                iIterationUnitIndex = iIterationUnitIndex + 1;

            } catch (SQLException e) {
                logger.error(e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
                ProgressManager.setState(Checkpoint.FAILED, e);;
                throw new RelimsException();

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                ProgressManager.setState(Checkpoint.FAILED, e);;
                throw new RelimsException();
            }
        }
        return lMascotDatfileInf;
    }

    /**
     * Returns if there are more IterationUnits left.
     *
     * @return true if there are more files, false otherwise.
     */
    public boolean hasNext() {
        return iIterationUnitIndex < iIterationUnits.size();
    }

    public void remove() {
        throw new IllegalAccessError("The remove method is not implemented on the DatfileIterator.");
        // not implemented.
    }

    /**
     * Getter for property 'mascotDatfileType'.
     *
     * @return Value for property 'mascotDatfileType'.
     */
    public MascotDatfileType getMascotDatfileType() {
        return iMascotDatfileType;
    }

    /**
     * Build the iteration units for an ms_lims iterator.
     *
     * @param aRs ResultSet with three columns <br><br><b>1.</b>
     * datfileid<br><b>2.</b> identificationid<br><b>3.</b> MS/MS spectrum
     * filename
     * @throws SQLException
     */
    protected void buildIterationUnits(final ResultSet aRs) throws SQLException {
        /**
         * This collection will hold different IterationUnit objects.
         */
        iIterationUnits = null;

        // Initialize.
        long lDatfileID = -1l;
        long lIdentificationid = -1l;
        int lQueryNumber = -1;
        MsLimsIterationUnit unit = null;

        // While more identificationid's are returning from the query ..
        while (aRs.next()) {
            // Get the values from this row.
            lDatfileID = aRs.getLong(1);
            lQueryNumber = aRs.getInt(2);
            lIdentificationid = aRs.getLong(3);

            // First row,
            if (iIterationUnits == null) {
                iIterationUnits = new ArrayList<MsLimsIterationUnit>();
                unit = new MsLimsIterationUnit(lDatfileID);
            }

            // If the datfileid of this row is different then the datfileid of the current IterationUnit,
            // then a new IterationUnit must be created.

            if (unit != null) {
                if (unit.getDatfileID() != lDatfileID) {
                    // Store the previous unit in the instance list.
                    iIterationUnits.add(unit);
                    // Create a new unit.
                    unit = new MsLimsIterationUnit(lDatfileID);
                }
                // Always add the the identificationid and filename of this row to the current unit.
                unit.add(lQueryNumber, lIdentificationid);
            }


        }

        if (iIterationUnits == null) {
            // Resultset was empty!
            logger.debug("Iteration failed.");
            return;
        } else {
            iIterationUnits.add(unit);
        }
        //  logger.debug("DEBUG mode, only loading 5 datfiles!!");
        //  iIterationUnitIndex =   iIterationUnits.size()- 5;
        iIterationUnitIndex = 0;
        /*   if (iIterationUnitIndex < 0) {
         iIterationUnitIndex = 0;
         }*/
        // Close fence post, Add the last unit as well!
    }
    /**
     * The project that must be iterated.
     */
    private long iProjectID;

    /**
     * Constructs the ProjectIterator upon construction. A long[] with datfile
     * identifiers will thereby be created.
     */
    private void construct() {


        try {
            String lQuery =
                    "Select i.l_datfileid, i.datfile_query, i.identificationid from identification as i, spectrum as s where i.l_spectrumid=s.spectrumid and s.l_projectid=" + iProjectID + " order by i.l_datfileid";
            Connection lConnection = ConnectionManager.getInstance().getConnection();
            ps = lConnection.prepareStatement(lQuery);
            rs = ps.executeQuery();

            buildIterationUnits(rs);
            // All user information from the query was transformed into IterationUnit's, the construction is completed.
            ps.close();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException ex) {
                    ps = null;
                }
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException ex) {
                        rs = null;
                    }
                }
            }
        }
    }
}