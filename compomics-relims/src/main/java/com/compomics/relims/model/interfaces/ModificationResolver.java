package com.compomics.relims.model.interfaces;

import com.compomics.relims.model.beans.RelimsProjectBean;

import java.io.File;

/**
 * This class is a
 */
public interface ModificationResolver {
    public void resolveModificationList(RelimsProjectBean iRelimsProjectBean);
    public void persistUserMods(File aFile);
}
