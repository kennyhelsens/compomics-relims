/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.predicatemanager;

import com.google.common.base.Predicate;

/**
 *
 * @author Kenneth
 */
public enum ProjectPredicate {

    //BUILD THE PREDICATES
    TAXONOMYID(false, null, "taxonomyID"),
    ENZYME(false, null, "enzyme"),
    MS1MAX(false, null, "ms1"),
    MS1MIN(false, null, "ms1"),
    MS2MAX(false, null, "ms2"),
    MS2MIN(false, null, "ms2");
    //REQUIRED PARAMETERS
    private Boolean enabled = false;
    private Predicate<String> predicate;
    private String columnName;
    //CONDITIONS
    private static int maxMs1Count = 100;
    private static int minMs1Count = 0;
    private static int maxMs2Count = 100;
    private static int minMs2Count = 0;
    private static int requestedTaxonomyID = 9906;
    private static String requestedEnzyme = "Trypsin";

    //SETTERS
    public static void setMaxMS1Count(String text) {
        ProjectPredicate.maxMs1Count = Integer.parseInt(text);
    }

    public static void setMinMS1Count(String text) {
        ProjectPredicate.minMs1Count = Integer.parseInt(text);
    }

    public static void setMaxMS2Count(String text) {
        ProjectPredicate.maxMs2Count = Integer.parseInt(text);
    }

    public static void setMinMS2Count(String text) {
        ProjectPredicate.minMs2Count = Integer.parseInt(text);
    }

    public static void setTaxonomyID(String text) {
        ProjectPredicate.requestedTaxonomyID = Integer.parseInt(text);
    }

    public static void setEnzyme(String text) {
        ProjectPredicate.requestedEnzyme = text;
    }

    public void initialize() {
        MS1MAX.setPredicate(new Predicate<String>() {
            @Override
            public boolean apply(String MS1) {
                try {
                    int MS1AsInt = Integer.parseInt(MS1);
                    return MS1AsInt <= maxMs1Count;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        MS1MIN.setPredicate(new Predicate<String>() {
            @Override
            public boolean apply(String MS1) {
                try {
                    int MS2AsInt = Integer.parseInt(MS1);
                    return MS2AsInt >= minMs1Count;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        MS2MAX.setPredicate(new Predicate<String>() {
            @Override
            public boolean apply(String MS2) {
                try {
                    int MS2AsInt = Integer.parseInt(MS2);
                    return MS2AsInt <= maxMs2Count;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        MS2MIN.setPredicate(new Predicate<String>() {
            @Override
            public boolean apply(String MS2) {
                try {
                    int MS2AsInt = Integer.parseInt(MS2);
                    return MS2AsInt >= minMs2Count;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        ENZYME.setPredicate(new Predicate<String>() {
            @Override
            public boolean apply(String enzyme) {
                boolean correctEnzyme = enzyme.equalsIgnoreCase(requestedEnzyme);
                return correctEnzyme;
            }
        });
        TAXONOMYID.setPredicate(new Predicate<String>() {
            @Override
            public boolean apply(String taxonomyID) {
                try {
                    int taxonomyAsInt = Integer.parseInt(taxonomyID);
                    return taxonomyAsInt == requestedTaxonomyID;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    private ProjectPredicate(Boolean enabled, ProjectPredicate predicate, String columnName) {
        enabled = enabled;
        predicate = predicate;
        this.columnName = columnName;
    }

    //REQUIRED METHODS
    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getColumnName() {
        return columnName;
    }

    private void setPredicate(Predicate<String> predicate) {
        this.predicate = (Predicate<String>) predicate;
    }

    public boolean applyPredicate(String columnName) {
        return predicate.apply(columnName);
    }
}
