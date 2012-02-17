package com.compomics.relims.model;

import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * This class is a
 */
public interface SearchProcessor {
    void process() throws SAXException, IOException;

    void setIncludeProteinDetails(boolean aStatus);

    void setIncludeExtraModDetails(boolean aStatus);
}
