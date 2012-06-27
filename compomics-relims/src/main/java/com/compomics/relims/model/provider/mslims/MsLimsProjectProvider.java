package com.compomics.relims.model.provider.mslims;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.relims.model.provider.ConnectionProvider;
import com.compomics.relims.model.provider.ProjectProvider;
import com.google.common.collect.Lists;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * This class is a
 */
public class MsLimsProjectProvider extends ProjectProvider {

    public MsLimsProjectProvider() {
        super();
        iDataProvider = new MsLimsDataProvider();
    }

    public List<Long> getAllProjects() {
        List<Long> lAllProjectIds = Lists.newArrayList();
        try {
            Project[] lAllProjects = Project.getAllProjects(ConnectionProvider.getConnection());

            for (Project lMsLimsProject : lAllProjects) {
                lAllProjectIds.add(lMsLimsProject.getProjectid());
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return lAllProjectIds;
    }


    public Collection<Long> getRandomProjects(int lSize) {
        List<Long> lAllProjects = this.getAllProjects();
        List<Long> lRandomProjects = Lists.newArrayList();

        Random lRandom = new Random();
        while (lRandomProjects.size() < lSize) {
            lRandomProjects.add(lAllProjects.get(lRandom.nextInt(lAllProjects.size())));
        }
        return lRandomProjects;
    }


}
