package com.compomics.relims.manager.progressmanager;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Kenneth
 */
public enum Checkpoint {
//process states...

    STARTING,
    LOADINGPROVIDERS,
    FAILED,
    RUNRELIMS,
    PREPAREPROJECTRUNNER,
    RUNNINGRELIMS,
    CANCELLED,
    PREPAREPRIDEASAP,
    APPLYINGSETTINGS,
    VALIDATINGPROJECT,
    LOADINGSPECTRA,
    LOADINGMODS,
    CREATESEARCHCOMMANDS,
    RUNSEARCHGUI,
    RUNPEPTIDESHAKER,
    FINISHED,
    FINISHING ,
    //Server states...
    IDLE,
    REGISTER,
    RUNNING,
    AWAITINGDELETION,
    //Failure states
    PRIDEFAILURE,
    PROCESSFAILURE,
    PEPTIDESHAKERFAILURE,
    TIMEOUTFAILURE,
    CONVERSIONERROR
,   CLASSICRELIMS}
