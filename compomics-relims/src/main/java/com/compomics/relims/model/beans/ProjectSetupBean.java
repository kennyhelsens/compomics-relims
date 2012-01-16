package com.compomics.relims.model.beans;

import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.compomics.mascotdatfile.util.mascot.Parameters;
import com.compomics.relims.model.UserModsFile;
import com.google.common.collect.Lists;

import java.util.ArrayList;

/**
 * This class is a
 */
public class ProjectSetupBean {

    ArrayList<Parameters> iParameterSet = Lists.newArrayList();
    ArrayList<ModificationList> iModificationLists = Lists.newArrayList();
    private ArrayList<String> iVariableMatchedPTMs;
    private ArrayList<String> iFixedMatchedPTMs;
    private UserModsFile iUserModsFile;
    private int iProjectID;

    public ProjectSetupBean() {
    }

    public ArrayList<ModificationList> getModificationLists() {
        return iModificationLists;
    }

    public void setModificationLists(ArrayList<ModificationList> aModificationLists) {
        iModificationLists = aModificationLists;
    }

    public ArrayList<Parameters> getParameterSet() {
        return iParameterSet;
    }

    public void setParameterSets(ArrayList<Parameters> aParameterSet) {
        iParameterSet = aParameterSet;
    }

    public ArrayList<String> getVariableMatchedPTMs() {
        return iVariableMatchedPTMs;
    }

    public void setVariableMatchedPTMs(ArrayList<String> aVariableMatchedPTMs) {
        iVariableMatchedPTMs = aVariableMatchedPTMs;
    }

    public ArrayList<String> getFixedMatchedPTMs() {
        return iFixedMatchedPTMs;
    }

    public void setFixedMatchedPTMs(ArrayList<String> aFixedMatchedPTMs) {
        iFixedMatchedPTMs = aFixedMatchedPTMs;
    }

    public void setUserModsFile(UserModsFile aUserModsFile) {
        iUserModsFile = aUserModsFile;
    }

    public UserModsFile getUserModsFile() {
        return iUserModsFile;
    }

    public int getProjectID() {
        return iProjectID;
    }

    public void setProjectID(int aProjectID) {
        iProjectID = aProjectID;
    }
}
