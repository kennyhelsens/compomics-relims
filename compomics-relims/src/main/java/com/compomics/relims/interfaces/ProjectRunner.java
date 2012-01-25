package com.compomics.relims.interfaces;

import com.compomics.mslims.db.accessors.Project;

/**
 * This class is a
 */
public interface ProjectRunner extends Runnable {
    void run();

    void setProject(Project aProject);
}
