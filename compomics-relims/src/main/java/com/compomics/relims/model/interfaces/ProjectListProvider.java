package com.compomics.relims.model.interfaces;

/**
 * This class is a
 */
public interface ProjectListProvider {
    /**
     * Returns the next projectid from the list.
     * Returns -1 if no more projects left.
     * @return
     */
    public long nextProjectID();
}
