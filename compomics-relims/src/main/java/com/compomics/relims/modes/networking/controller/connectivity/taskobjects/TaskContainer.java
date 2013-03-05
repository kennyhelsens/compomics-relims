/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.taskobjects;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author compomics
 */
public class TaskContainer implements Serializable {

    private Map<String, String> instructionMap = new LinkedHashMap<>();
    private LinkedHashMap<String, String> taskList = new LinkedHashMap<>();
    private String sourceID;
    private String strategyID;
    private SearchParameters searchParameters;

    public SearchParameters getSearchParameters() {
        return this.searchParameters;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public LinkedHashMap<String, String> getTaskList() {
        return taskList;
    }

    public void setTaskList(LinkedHashMap<String, String> taskList) {
        this.taskList = taskList;
    }

    public String getSourceID() {
        return sourceID;
    }

    public void setSourceID(String sourceID) {
        this.sourceID = sourceID;
    }

    public String getStrategyID() {
        return strategyID;
    }

    public void setStrategyID(String strategyID) {
        this.strategyID = strategyID;
    }

    public TaskContainer() {
        instructionMap.put("username", null);
        instructionMap.put("password", null);
        instructionMap.put("email", null);
        instructionMap.put("instruction", null);
    }

    public boolean isValid() {

        //is the taskMap not empty?
        if (taskList.isEmpty()) {
            return false;
        }

        // are there projectID's that are too long?

        for (String aProjectID : taskList.keySet()) {
            if (aProjectID.length() >= 50) {
                return false;
            }
        }



        return true;

    }

    public void updateInstruction(String key, String property) {
        instructionMap.put(key, property);
    }

    public String getCurrentUser() {
        return instructionMap.get("username");
    }

    public String getEmail() {
        return instructionMap.get("email");
    }

    public String getPassword() {
        return instructionMap.get("password");
    }

    public String getTaskInstruction() {
        return instructionMap.get("instruction");
    }

    public Map<String, String> getInstructionMap() {
        return this.instructionMap;
    }

    public void setInstructionMap(Map<String, String> instructionMap) {
        this.instructionMap = instructionMap;
    }

    public HashMap<String, String> getList() {
        return this.taskList;
    }

    public void addJob(String projectID, String projectName) {
        taskList.put(projectID, projectName);
    }

    public void removeJob(String projectId) {
        taskList.remove(projectId);
    }

    public boolean isEmpty() {
        boolean empty = false;
        if (taskList.isEmpty()) {
            empty = true;
        }
        return empty;
    }
}
