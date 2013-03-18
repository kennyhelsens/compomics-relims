/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.taskobjects;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.Serializable;

/**
 *
 * @author Kenneth
 */
public class Task implements Serializable {

    private long taskID = 0L;
    private String projectID;
    private String strategyID;
    private String sourceID;
    private String userID;
    private String taskName;
    private SearchParameters searchParameters;
    private boolean allowPridePipeline = true;

    public Task(long foundTaskID, String foundProjectID, String strategyID, String sourceID, String userID) {
        this.taskID = foundTaskID;
        this.projectID = foundProjectID;
        this.strategyID = strategyID;
        this.sourceID = sourceID;
        this.userID = userID;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public void setAllowPridePipeline(Boolean allow) {
        this.allowPridePipeline = allow;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskName() {
        return this.taskName;
    }

    public Task() {
    }

    public long getTaskID() {
        return taskID;
    }

    public String getStrategyID() {
        return strategyID;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setTaskID(long taskID) {
        this.taskID = taskID;
    }

    public String getProjectID() {
        return projectID;
    }

    public void setProjectID(String projectID) {
        this.projectID = projectID;
    }

    public String getUserID() {
        return this.userID;
    }

    public Boolean getAllowPridePipeline() {
        return this.allowPridePipeline;
    }

    public SearchParameters getSearchParameters() {
        return this.searchParameters;
    }
}
