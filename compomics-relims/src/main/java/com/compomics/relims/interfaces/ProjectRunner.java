package com.compomics.relims.interfaces;

import com.compomics.mslims.db.accessors.Project;

import java.util.concurrent.Callable;

/**
 * This class is a
 */
public interface ProjectRunner extends Callable<String> {
    String call();

    void setProject(Project aProject);
}
