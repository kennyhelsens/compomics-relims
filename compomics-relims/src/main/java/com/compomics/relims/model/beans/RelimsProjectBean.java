package com.compomics.relims.model.beans;

import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.compomics.relims.model.UserModsFile;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is a
 */
public class RelimsProjectBean {

    ArrayList<ModificationList> iModificationLists = Lists.newArrayList();
    private ArrayList<String> iVariableMatchedPTMs;
    private ArrayList<String> iFixedMatchedPTMs;
    private UserModsFile iUserModsFile;
    private long iProjectID;

    public RelimsProjectBean() {
    }

    public ArrayList<ModificationList> getModificationLists() {
        return iModificationLists;
    }

    public void setModificationLists(ArrayList<ModificationList> aModificationLists) {
        iModificationLists = aModificationLists;
    }

    public List<String> getVariableMatchedPTMs() {
        return iVariableMatchedPTMs;
    }

    public void setVariableMatchedPTMs(ArrayList<String> aVariableMatchedPTMs) {
        iVariableMatchedPTMs = aVariableMatchedPTMs;
    }

    public List<String> getFixedMatchedPTMs() {
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

    public long getProjectID() {
        return iProjectID;
    }

    public void setProjectID(long aProjectID) {
        iProjectID = aProjectID;
    }

    @Override
    public String toString() {
        return "" + getProjectID();
    }
}
