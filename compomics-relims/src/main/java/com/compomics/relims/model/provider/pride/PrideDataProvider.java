package com.compomics.relims.model.provider.pride;

import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.interfaces.DataProvider;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * This class is a
 */
public class PrideDataProvider implements DataProvider {

    private static Logger logger = Logger.getLogger(PrideDataProvider.class);


    public long getNumberOfSpectraForProject(long aProjectID) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public HashSet<Integer> getInstrumentsForProject(long aProjectID) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public HashSet<String> getProteinAccessionsForProject(long aProjectID) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public long getNumberOfPeptidesForProject(long aProjectID) {
        // @TODO add code to build single MGF file for MS/MS spectra in specfied experiment/project ID
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public File getSpectraForProject(long aProjectID) throws IOException {
        // @TODO add code to build single MGF file for MS/MS spectra in specfied experiment/project ID
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public long getNumberOfSearchesForProject(long aProjectid) {
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public RelimsProjectBean buildProjectBean(long aProjectid) {
        // @TODO add code for spectrum annotation pipeline
        throw new RelimsException("NOT YET IMPLEMENTED");
    }

    public String toString(){
        return "PrideDataProvider";
    }

}
