/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testingTools;

import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Kenneth
 */
public class FileAnalyzer {

    private static File MGF_FILE = new File("src/test/resources/filetester/3.mgf");
    private static SpectrumFactory sf = SpectrumFactory.getInstance();

    public static void main(String[] args) {
        try {
            sf.addSpectra(MGF_FILE);
            System.out.println("Amount of spectra \t: " + sf.getNSpectra());
            System.out.println("Max intensity \t: " + sf.getMaxIntensity());
            System.out.println("Max m/z \t: " + sf.getMaxMz());
        } catch (FileNotFoundException ex) {
            System.err.println(ex);
        } catch (IOException ex) {
            System.err.println(ex);
        } catch (ClassNotFoundException ex) {
            System.err.println(ex);
        }
    }
}
