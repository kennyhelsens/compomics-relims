package com.compomics.relims.model;

import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.interfaces.ProjectProvider;

import java.util.List;

/**
 * This class is a
 */
public class PrideProjectProvider extends ProjectProvider {
    private DataProvider iDataProvider;

    public List<Long> getAllProjects() {
        throw new RelimsException("Not yet implemented!!");
    }

    public List<Long> getRandomProjects(int lSize) {
        throw new RelimsException("Not yet implemented!!");
    }
}
