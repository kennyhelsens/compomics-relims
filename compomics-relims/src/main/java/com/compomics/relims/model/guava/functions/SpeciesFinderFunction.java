package com.compomics.relims.model.guava.functions;

import static com.compomics.relims.model.guava.functions.SpeciesFinderFunction.SPECIES.*;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.manager.progressmanager.ProgressManager;
import com.google.common.base.Function;
import com.google.common.collect.Sets;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Set;
import javax.annotation.Nullable;
import org.apache.log4j.Logger;

/**
 * This class is a
 */
public class SpeciesFinderFunction implements Function<Set<String>, SpeciesFinderFunction.SPECIES> {
    private static ProgressManager progressManager = ProgressManager.getInstance();
    public double iExpectedMatchFrequency;

    public enum SPECIES {

        HUMAN, YEAST, MOUSE, RAT, DROSOPHILA, OTHER, NA, MIX
    }
    private static Logger logger = Logger.getLogger(SpeciesFinderFunction.class);
    private HashMap<SPECIES, Integer> iSpeciesCounter;

    public SpeciesFinderFunction() {

        iSpeciesCounter = new HashMap<SPECIES, Integer>();
        iSpeciesCounter.put(HUMAN, 0);
        iSpeciesCounter.put(MOUSE, 0);
        iSpeciesCounter.put(RAT, 0);
        iSpeciesCounter.put(DROSOPHILA, 0);
        iSpeciesCounter.put(YEAST, 0);
        iSpeciesCounter.put(OTHER, 0);
        iSpeciesCounter.put(NA, 0);
    }

    /**
     * Returns the result of applying this function to {@code input}. This
     * method is <i>generally expected</i>, but not absolutely required, to have
     * the following properties:
     * <p/>
     * <ul> <li>Its execution does not cause any observable side effects.
     * <li>The computation is <i>consistent with equals</i>; that is, {@link com.google.common.base.Objects#equal
     * Objects.equal}{@code (a, b)} implies that {@code Objects.equal(function.apply(a),
     * function.apply(b))}. </ul>
     *
     * @throws NullPointerException if {@code input} is null and this function
     * does not accept null arguments
     */
    public SpeciesFinderFunction.SPECIES apply(@Nullable Set<String> input) {

        for (String lAccession : input) {
            URL google = null;
            try {
                String lAccessionEntry = lAccession;
                google = new URL("http://www.uniprot.org/uniprot/" + lAccessionEntry + ".fasta");
                URLConnection yc = google.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
                String inputLine;
                int lLineCount = 0;
                while ((inputLine = in.readLine()) != null) {
                    if (lLineCount > 0) {
                        break;
                    }
                    // logic
                    if (inputLine.toLowerCase().indexOf("os=homo sapiens") > 0) {
                        iSpeciesCounter.put(HUMAN, iSpeciesCounter.get(HUMAN) + 1);

                    } else if (inputLine.toLowerCase().indexOf("os=rattus norvegicus") > 0) {
                        iSpeciesCounter.put(RAT, iSpeciesCounter.get(RAT) + 1);

                    } else if (inputLine.toLowerCase().indexOf("os=saccharomyces") > 0) {
                        iSpeciesCounter.put(YEAST, iSpeciesCounter.get(YEAST) + 1);

                    } else if (inputLine.toLowerCase().indexOf("os=drosophila melanogaster") > 0) {
                        iSpeciesCounter.put(DROSOPHILA, iSpeciesCounter.get(DROSOPHILA) + 1);

                    } else if (inputLine.toLowerCase().indexOf("os=mus musculus") > 0) {
                        iSpeciesCounter.put(MOUSE, iSpeciesCounter.get(MOUSE) + 1);

                    } else {
                        iSpeciesCounter.put(OTHER, iSpeciesCounter.get(OTHER) + 1);
                    }

//                    System.out.println(inputLine);
                    lLineCount++;
                }
                in.close();

            } catch (MalformedURLException e) {
                logger.error(e.getMessage(), e);
                progressManager.setState(Checkpoint.FAILED,e);;
                Thread.currentThread().interrupt();
            } catch (FileNotFoundException e) {
                // When the URL cannot be opened, the accession could not be found!
                iSpeciesCounter.put(NA, iSpeciesCounter.get(NA) + 1);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                progressManager.setState(Checkpoint.FAILED,e);;
                Thread.currentThread().interrupt();
            }

        }

        int lExpectedSize = input.size();

        iExpectedMatchFrequency = 0.9;
        if (iSpeciesCounter.get(HUMAN) > lExpectedSize * iExpectedMatchFrequency) {
            return HUMAN;

        } else if (iSpeciesCounter.get(RAT) > lExpectedSize * iExpectedMatchFrequency) {
            return RAT;

        } else if (iSpeciesCounter.get(MOUSE) > lExpectedSize * iExpectedMatchFrequency) {
            return MOUSE;

        } else if (iSpeciesCounter.get(DROSOPHILA) > lExpectedSize * iExpectedMatchFrequency) {
            return DROSOPHILA;

        } else if (iSpeciesCounter.get(YEAST) > lExpectedSize * iExpectedMatchFrequency) {
            return YEAST;

        } else if (iSpeciesCounter.get(OTHER) > lExpectedSize * iExpectedMatchFrequency) {
            return OTHER;
        }

        return MIX;
    }

    public static void main(String[] args) {
        Set<String> lAccessionList = Sets.newHashSet();
        lAccessionList.add("P60709");
        lAccessionList.add("Q96H99");
        lAccessionList.add("Q6P2E6");
        lAccessionList.add("Q6PIY2");
        lAccessionList.add("Q9H8G9");
        lAccessionList.add("Q08AL8");
        lAccessionList.add("Q5M8T4");
        lAccessionList.add("A8JZY6");
        lAccessionList.add("Q86TT8");
        lAccessionList.add("B4DWJ8");


        SpeciesFinderFunction lSpeciesFinderFunction = new SpeciesFinderFunction();
        SpeciesFinderFunction.SPECIES lSpecies = lSpeciesFinderFunction.apply(lAccessionList);

        String lSpeciesName = null;
        switch (lSpecies) {
            case DROSOPHILA:
                lSpeciesName = "drosphila";
                break;

            case HUMAN:
                lSpeciesName = "human";
                break;

            case YEAST:
                lSpeciesName = "yeast";
                break;

            case MOUSE:
                lSpeciesName = "mouse";
                break;

            case RAT:
                lSpeciesName = "rat";
                break;

            case MIX:
                lSpeciesName = "mixture";
                break;

            default:
                lSpeciesName = "unknown";
        }

        System.out.println("Evaluated accesions origin as: " + lSpeciesName);

    }
}
