package com.compomics.relims.model.provider.mslims;

import com.compomics.mascotdatfile.util.interfaces.MascotDatfileInf;
import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.compomics.mascotdatfile.util.mascot.Parameters;
import com.compomics.mslims.db.accessors.Spectrum_file;
import com.compomics.mslims.util.fileio.MascotGenericFile;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.provider.ConnectionProvider;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

/**
 * This class is a
 */
public class MsLimsDataProvider implements DataProvider {

    private static MsLimsDataProvider ourInstance = new MsLimsDataProvider();
    private static Logger logger = Logger.getLogger(MsLimsDataProvider.class);

    public static MsLimsDataProvider getInstance() {
        return ourInstance;
    }

    public long getNumberOfSpectraForProject(long aProjectID) {
        long lNumberOfSpectra = 0;

        try {
            String lQuery = "select count(distinct spectrumid) from spectrum as s where s.l_projectid=" + aProjectID;

            logger.debug("QUERY - " + lQuery.replaceAll("\\?", "" + aProjectID));

            Statement ps = ConnectionProvider.getConnection().createStatement();
            ps.execute(lQuery);
            ResultSet lResultSet = ps.getResultSet();

            lResultSet.next();
            lNumberOfSpectra = lResultSet.getLong(1);

            lResultSet.close();
            ps.close();

            return lNumberOfSpectra;

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return lNumberOfSpectra;
    }

    public HashSet<Integer> getInstrumentsForProject(long aProjectID) {
        HashSet<Integer> lInstrumentIDSet = new HashSet<Integer>();

        try {
            String lQuery = "select distinct l_instrumentid from spectrum as s where s.l_projectid=?";
            PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(lQuery);
            ps.setLong(1, aProjectID);

            ResultSet lResultSet = ps.executeQuery();
            while (lResultSet.next()) {
                int lInstrumentID = lResultSet.getInt(1);
                lInstrumentIDSet.add(lInstrumentID);
            }

            lResultSet.close();
            ps.close();

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        return lInstrumentIDSet;
    }

    public HashSet<String> getProteinAccessionsForProject(long aProjectID) {
        HashSet<String> lAccessionSet = Sets.newHashSet();

        try {
            String lQuery = "select distinct accession from identification as i, spectrum as s where i.l_spectrumid=s.spectrumid and s.l_projectid=?";
            PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(lQuery);
            ps.setLong(1, aProjectID);

            ResultSet lResultSet = ps.executeQuery();
            while (lResultSet.next()) {
                String lAccession = lResultSet.getString(1);
                lAccessionSet.add(lAccession);
            }

            lResultSet.close();
            ps.close();

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }

        return lAccessionSet;
    }

    public long getNumberOfPeptidesForProject(long aProjectID) {
        long lNumberOfPeptides = 0;

        try {
            String lQuery = "select count(distinct sequence) from identification as i, spectrum as s where i.l_spectrumid=s.spectrumid and s.l_projectid=?";
            PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(lQuery);
            ps.setLong(1, aProjectID);

            ResultSet lResultSet = ps.executeQuery();
            lResultSet.next();
            lNumberOfPeptides = lResultSet.getLong(1);

            lResultSet.close();
            ps.close();

            return lNumberOfPeptides;

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return lNumberOfPeptides;
    }

    public File getSpectraForProject(long aProjectID) throws IOException {
        // The stats and container thereof.

        int total = 0;
        int needed = 0;

        // for debugging purposes
        int lMaxSpectra;
        if (RelimsProperties.hasSpectrumLimit()) {
            lMaxSpectra = RelimsProperties.getSpectrumLimitCount();
        } else {
            lMaxSpectra = Integer.MAX_VALUE;
        }

        File output = new File(RelimsProperties.getWorkSpace(), "mergefile_" + aProjectID + ".mgf");
        logger.debug("getting all spectra from project " + aProjectID + " in a local file " + output.getCanonicalPath());


        try {
            // Construct the query.
            StringBuffer query = new StringBuffer("select distinct(spectrumid), filename from spectrum where l_projectid=");
            query.append(aProjectID);

            PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(query.toString());
            ResultSet rs = ps.executeQuery();
            HashMap<Integer, String> lSpectrumids = new HashMap<Integer, String>();
            int lSpectrumCounter = 0;
            while (rs.next() && (lSpectrumCounter++ < lMaxSpectra)) {
                int lID = rs.getInt(1);
                String lFilename = rs.getString(2);
                lSpectrumids.put(lID, lFilename);
            }
            ps.close();
            rs.close();


            query = new StringBuffer(Spectrum_file.getBasicSelect());
            String lSpectrumIdJoiner = Joiner.on(",").join(lSpectrumids.keySet());
            query.append(" where l_spectrumid in (" + lSpectrumIdJoiner + ")");


            ps = ConnectionProvider.getConnection().prepareStatement(query.toString());
            rs = ps.executeQuery();

            Vector<String> lSpectrumFiles = new Vector<String>();

            int lCounter = 0;
            while (rs.next()) {
                lCounter++;
                Spectrum_file mgf = new Spectrum_file(rs);
                int lID = (int) mgf.getL_spectrumid();
                String lFilename = lSpectrumids.get(lID);

                MascotGenericFile file = new MascotGenericFile(lFilename, new String(mgf.getUnzippedFile()));
                // Note the use of the 'true' flag, which takes care of substituting the original title with the
                // filename!

                lSpectrumFiles.add(file.toString(true) + "\n\n");
                total++;
            }

            if (!output.exists()) {
                output.createNewFile();
            }

            BufferedWriter bos = new BufferedWriter(new FileWriter(output));
            for (String lSpectrum : lSpectrumFiles) {
                bos.write(lSpectrum);
            }
            bos.flush();
            bos.close();

            rs.close();
            ps.close();

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            ConnectionProvider.initiate();

//            Thread.currentThread().stop();
            logger.error(e.getMessage(), e);
        }

        return output;
    }

    public long getNumberOfSearchesForProject(long aProjectid) {

        long lNumberOfSearches = 0;

        try {
            String lQuery = "select count(distinct title) from identification as i, spectrum as s where i.l_spectrumid=s.spectrumid and s.l_projectid=?";
            PreparedStatement ps = ConnectionProvider.getConnection().prepareStatement(lQuery);
            ps.setLong(1, aProjectid);

            ResultSet lResultSet = ps.executeQuery();
            lResultSet.next();
            lNumberOfSearches = lResultSet.getLong(1);

            lResultSet.close();
            ps.close();

            return lNumberOfSearches;

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return lNumberOfSearches;
    }

    public RelimsProjectBean buildProjectBean(long aProjectid) {

        RelimsProjectBean lRelimsProjectBean = new RelimsProjectBean();

        lRelimsProjectBean.setProjectID((int) aProjectid);

        ArrayList<Parameters> lParameterList = Lists.newArrayList();
        ArrayList<ModificationList> lModificationLists = Lists.newArrayList();

        DatfileIterator lIterator = new DatfileIterator(ConnectionProvider.getConnection(), aProjectid);
        while (lIterator.hasNext()) {
            MascotDatfileInf lMascotDatfile = lIterator.next();
            lModificationLists.add(lMascotDatfile.getModificationList());
        }

        lRelimsProjectBean.setModificationLists(lModificationLists);

        return lRelimsProjectBean;

    }

    public String toString() {
        return "MsLimsDataProvider";
    }

}


