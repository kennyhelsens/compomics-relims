/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.controller.connectivity.taskobjects;

import com.compomics.util.experiment.identification.SearchParameters;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author compomics
 */
public class TaskContainer implements Serializable {

    private Map<String, String> instructionMap = new LinkedHashMap<>();
    private HashMap<String, String> TaskMap = new HashMap<>();
    private String sourceID;
    private String strategyID;
    private SearchParameters searchParameters;

    public SearchParameters getSearchParameters() {
        return this.searchParameters;
    }

    public void setSearchParameters(SearchParameters searchParameters) {
        this.searchParameters = searchParameters;
    }

    public HashMap<String, String> getTaskList() {
        return TaskMap;
    }

    public void setTaskList(LinkedHashMap<String, String> taskList) {
        this.TaskMap = taskList;
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
        if (TaskMap.isEmpty()) {
            return false;
        }

        // are there projectID's that are too long?

        for (String aProjectID : TaskMap.keySet()) {
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
        return this.TaskMap;
    }

    public void addJob(String projectID, String projectName) {
        TaskMap.put(projectID, projectName);
    }

    public void shuffleTasks() {
        HashMap<String, String> tempTasks = new HashMap<String, String>();
        List<Object> randomKeys = Arrays.asList(TaskMap.keySet().toArray());
        Collections.shuffle(randomKeys);
        for (Object aKey : randomKeys) {
            tempTasks.put(String.valueOf(aKey), TaskMap.get(String.valueOf(aKey)));
            TaskMap.remove(String.valueOf(aKey));
        }
        TaskMap = tempTasks;
    }

    public void removeJob(String projectId) {
        TaskMap.remove(projectId);
    }

    public boolean isEmpty() {
        boolean empty = false;
        if (TaskMap.isEmpty()) {
            empty = true;
        }
        return empty;
    }
}
