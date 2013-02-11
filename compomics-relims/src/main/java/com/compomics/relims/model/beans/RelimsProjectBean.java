package com.compomics.relims.model.beans;

import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.model.UserModsFile;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * This class is a
 */
public class RelimsProjectBean implements Cloneable{

    private long iProjectID = -1;

    private UserModsFile iUserModsFile = new UserModsFile();

    private List<String> iVariableMatchedPTMs = Lists.newArrayList();
    private List<String> iFixedMatchedPTMs = Lists.newArrayList();
    private List<UserMod> iStandardModificationList = Lists.newArrayList();
    private List<UserMod> iExtraModificationList = Lists.newArrayList();

    private double iPrecursorError = 1.0;
    private double iFragmentError = 1.0;

    public RelimsProjectBean() {
    }

    public List<UserMod> getStandardModificationList(){
        return iStandardModificationList;
    }

    public List<UserMod> getExtraModificationList(){
        return iExtraModificationList;
    }


    public void setStandardModificationList(List<UserMod> aStandardModificationList){
        iStandardModificationList.clear();
        iStandardModificationList.addAll(aStandardModificationList);
    }

    public void setExtraModificationList(List<UserMod> aExtraModificationList) {
        iExtraModificationList.clear();
        iExtraModificationList.addAll(aExtraModificationList);
    }


    public List<String> getVariableMatchedPTMs() {
        return iVariableMatchedPTMs;
    }

    public void setVariableMatchedPTMs(List<String> aVariableMatchedPTMs) {
        iVariableMatchedPTMs.clear();
        iVariableMatchedPTMs.addAll(aVariableMatchedPTMs);
    }

    public List<String> getFixedMatchedPTMs() {
        return iFixedMatchedPTMs;
    }

    public void setFixedMatchedPTMs(List<String> aFixedMatchedPTMs) {
        iFixedMatchedPTMs.clear();
        iFixedMatchedPTMs.addAll(aFixedMatchedPTMs);
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

    public void setFragmentError(double aFragmentError) {
        iFragmentError = aFragmentError;
    }

    public void setPrecursorError(double aPrecursorError) {
        iPrecursorError = aPrecursorError;
    }

    public double getFragmentError() {
        return iFragmentError;
    }

    public double getPrecursorError() {
        return iPrecursorError;
    }

    @Override
    public String toString() {
        return "" + getProjectID();
    }

    @Override
    public RelimsProjectBean clone() throws CloneNotSupportedException {
        RelimsProjectBean lProjectBean = new RelimsProjectBean();

        lProjectBean.setProjectID(getProjectID());

        lProjectBean.setStandardModificationList(getStandardModificationList());
        lProjectBean.setExtraModificationList(getExtraModificationList());

        lProjectBean.setUserModsFile(getUserModsFile());

        lProjectBean.setFixedMatchedPTMs(getFixedMatchedPTMs());
        lProjectBean.setVariableMatchedPTMs(getVariableMatchedPTMs());

        lProjectBean.setPrecursorError(getPrecursorError());
        lProjectBean.setFragmentError(getFragmentError());

        return lProjectBean;
    }
}
