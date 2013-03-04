package com.compomics.relims.model.interfaces;

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * This class is a
 */
public interface SearchProcessor {
    public void process() throws SAXException, IOException;

    public void setIncludeProteinDetails(boolean aStatus);

    public void setIncludeExtraModDetails(boolean aStatus);
}
