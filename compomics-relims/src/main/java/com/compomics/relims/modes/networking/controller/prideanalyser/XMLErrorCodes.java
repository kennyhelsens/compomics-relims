/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.remoterelimscontrolserver.prideanalyser;

/**
 *
 * @author Kenneth
 */
public enum XMLErrorCodes {

    /*WHAT CAN GO WRONG WITH AN XML FILE
     1. no spectra
     */
    MISSING_SPECTRA(0, "No spectra could be found for this project."),
    MISSING_PRECURSORS(1, "No precursors were found for this project"),
    MISSING_MSLEVEL(2, "This file contains no MS-levels."),
    MISSING_CHARGES(3, "This file contains no charges"),
    MISSING_INSTRUMENT(4, "This file contains no valid instrument"),
    WRONG_SPECTRA(5, "This file contains invalid spectra"),
    WRONG_PRECURSORS(6, "This file contains invalid precursors"),
    WRONG_MSLEVEL(7, "This file contains spectra with MS =/= 2"),
    WRONG_CHARGES(8, "This file contains spectra with invalid charges"),
    WRONG_INSTRUMENT(9, "This file contains an instrument that is not standard"), //TODO check if this list is complete
    ;
    private final int code;
    private final String description;

    private XMLErrorCodes(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return code + ": " + description;
    }
}
