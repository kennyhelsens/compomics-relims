/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.predicatemanager;

import com.compomics.relims.conf.RelimsProperties;
import com.google.common.base.Predicate;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 *
 * @author Kenneth
 */
public class ProjectPredicateManager {

    private static Map<String, Object> projectMetaData = new LinkedHashMap<String, Object>();
    private static final Logger logger = Logger.getLogger(ProjectPredicateManager.class);

    public static boolean evaluatePredicates(long projectID) {
        if (ProjectPredicate.values().length != 0) {
            importProjectFromMetaData(projectID);
            if (!projectMetaData.isEmpty()) {
                for (ProjectPredicate filter : ProjectPredicate.values()) {
                    if (filter.isEnabled()) {
                        if (!filter.applyPredicate(String.valueOf(projectMetaData.get(filter.getColumnName())))) {
                            return false;
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static void initialize() {
        ProjectPredicate.ENZYME.initialize();
    }

    private static void importProjectFromMetaData(long projectID) {
        try {
            projectMetaData.clear();
            projectMetaData = MetaDataCollector.lookUpMetaData(projectID);
        } catch (SQLException ex) {
            logger.error("Could not retrieve project's metadata");
        }
    }
}
