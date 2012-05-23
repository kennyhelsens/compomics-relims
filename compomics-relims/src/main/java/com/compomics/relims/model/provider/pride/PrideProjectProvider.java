package com.compomics.relims.model.provider.pride;

import com.compomics.relims.exception.RelimsException;
import com.compomics.relims.model.provider.ProjectProvider;
import com.compomics.relims.model.interfaces.DataProvider;

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
