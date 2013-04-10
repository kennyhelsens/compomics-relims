/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.colimsmanager;

import com.compomics.colims.core.exception.PeptideShakerIOException;
import com.compomics.colims.core.io.PeptideShakerIO;
import com.compomics.colims.core.io.impl.PeptideShakerIOImpl;
import java.io.File;

/**
 *
 * @author Kenneth
 */
public class ColimsImporter {
    public static void transferToColims(File cpsFile) throws PeptideShakerIOException{
     PeptideShakerIO relimsToColims = new PeptideShakerIOImpl();
     relimsToColims.importPeptideShakerCpsArchive(cpsFile);
    }
}
