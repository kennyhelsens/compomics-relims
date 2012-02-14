package com.compomics.relims.model.mslims;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.mslims.db.accessors.Spectrum_file;
import com.compomics.mslims.util.fileio.MascotGenericFile;
import com.compomics.relims.conf.RelimsProperties;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

/**
 * This class is a
 */
public class MsLimsProvider {
    private static MsLimsProvider ourInstance = new MsLimsProvider();
    private static Logger logger = Logger.getLogger(MsLimsProvider.class);

    public static MsLimsProvider getInstance() {
        return ourInstance;
    }

    protected Project[] getAllProjects() {
        try {
            return Project.getAllProjects(MSLIMS.getConnection());
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public ArrayList<Project> getRandomProjects(int lSize) {
        Project[] lAllProjects = getAllProjects();
        Random lRandom = new Random();
        ArrayList<Project> lProjectIds = Lists.newArrayList();
        while (lProjectIds.size() < lSize) {
            lProjectIds.add(lAllProjects[lRandom.nextInt(lAllProjects.length) + 1]);
        }
        return lProjectIds;
    }

    public ArrayList<Project> getPreDefinedProjects() {
        int[] lProjectIntegers = RelimsProperties.getPredifinedProjects();
        ArrayList<Project> lProjects = getProjects(lProjectIntegers);
        return lProjects;
    }

    public ArrayList<Project> getProjects(int[] lProjectIdNumbers) {
        Project[] lAllProjects = getAllProjects();

        ArrayList<Project> lProjects = Lists.newArrayList();
        for (Project lProject : lAllProjects) {
            int lRetain = Ints.indexOf(lProjectIdNumbers, (int) lProject.getProjectid());
            if (lRetain >= 0) {
                lProjects.add(lProject);
            }
        }
        return lProjects;
    }


    public long getNumberOfSpectraForProject(long aProjectID) {
        long lNumberOfSpectra = 0;

        try {
            String lQuery = "select count(distinct spectrumid) from spectrum as s where s.l_projectid=" + aProjectID;

            logger.debug("QUERY - " + lQuery.replaceAll("\\?", ""+aProjectID));

            Statement ps = MSLIMS.getConnection().createStatement();
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
            PreparedStatement ps = MSLIMS.getConnection().prepareStatement(lQuery);
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
            PreparedStatement ps = MSLIMS.getConnection().prepareStatement(lQuery);
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
            PreparedStatement ps = MSLIMS.getConnection().prepareStatement(lQuery);
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

    public DatfileIterator getDatfilesForProject(long aProjectID) {
        return new DatfileIterator(MSLIMS.getConnection(), aProjectID);
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

            PreparedStatement ps = MSLIMS.getConnection().prepareStatement(query.toString());
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


            ps = MSLIMS.getConnection().prepareStatement(query.toString());
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
            try {
                MSLIMS.reset();
            } catch (SQLException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InstantiationException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

//            Thread.currentThread().stop();
            logger.error(e.getMessage(), e);
        }

        return output;
    }
}
