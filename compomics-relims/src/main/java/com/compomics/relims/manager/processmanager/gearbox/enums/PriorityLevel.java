/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.processmanager.gearbox.enums;

/**
 *
 * @author Kenneth
 */
public enum PriorityLevel {

    IDLE(64, 19),
    BELOW_NORMAL(16384, 10),
    NORMAL(32, 0),
    ABOVE_NORMAL(32768, -6),
    HIGH(128, -12),
    REAL_TIME(256, -19);
    private int windowsCode;
    private int linuxCode;

    private PriorityLevel(int winValue, int linValue) {
        this.windowsCode = winValue;
        this.linuxCode = linValue;
    }

    public int getWindowsCode() {
        return this.windowsCode;
    }

    public int getLinuxCode() {
        return this.linuxCode;
    }

    public PriorityLevel getNext() {
        return values()[(ordinal() + 1) % values().length];
    }

    public PriorityLevel getPrevious() {
        if (this.name().toLowerCase().contains("idle")) {
            return PriorityLevel.REAL_TIME;
        } else {
            return values()[(ordinal() - 1) % values().length];
        }
    }

    public static boolean isIntensive(PriorityLevel priority) {
        PriorityLevel boundary = PriorityLevel.BELOW_NORMAL;
        if (priority.ordinal() > boundary.ordinal()) {
            return true;
        } else {
            return false;
        }
    }
}
