/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.taskobjects;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.Serializable;

/**
 *
 * @author Kenneth
 */
public class Task implements Serializable {

    private long taskID = 0L;
    private String projectID;
    private String sourceID;
    private String userID;
    private String taskName;
    private SearchParameters searchParameters;
    private boolean allowPridePipeline = true;
    private String fasta = null;

    public Task(long foundTaskID, String foundProjectID, String sourceID, String userID,String fasta) {
        this.taskID = foundTaskID;
        this.projectID = foundProjectID;
        this.sourceID = sourceID;
        this.userID = userID;
        this.fasta = fasta;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public String getFasta(){
        return fasta;
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
