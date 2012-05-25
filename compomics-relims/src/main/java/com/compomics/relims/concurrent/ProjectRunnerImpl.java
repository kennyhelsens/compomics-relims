package com.compomics.relims.concurrent;

import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;
import com.compomics.relims.model.guava.predicates.PredicateManager;
import com.compomics.relims.model.interfaces.ProjectRunner;
import com.compomics.relims.model.interfaces.SearchCommandGenerator;
import com.compomics.relims.model.interfaces.SearchStrategy;
import com.compomics.relims.model.provider.mslims.MsLimsDataProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import eu.isas.peptideshaker.cmd.PeptideShakerCLI;
import eu.isas.peptideshaker.cmd.PeptideShakerCLIInputBean;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;


/**
 * This class is a
 */
public class ProjectRunnerImpl extends Observable implements ProjectRunner {
    private static Logger logger = Logger.getLogger(ProjectRunnerImpl.class);

    private RelimsProjectBean iRelimsProjectBean;

    private PredicateManager iPredicateManager;
    private SearchStrategy iSearchStrategy;

    public ProjectRunnerImpl() {

    }

    public void setPredicateManager(PredicateManager aPredicateManager) {
        iPredicateManager = aPredicateManager;
    }

    public String call() {
        try {

            long lProjectid = iRelimsProjectBean.getProjectID();
            logger.debug("created new projectrunner on " + lProjectid);

            Predicate<RelimsProjectBean> lProjectSizePredicate = iPredicateManager.getProjectSizePredicate();
            if (!lProjectSizePredicate.apply(iRelimsProjectBean)) {
                logger.debug("END " + lProjectid);
                return "Premature end for project size";
            }


            Predicate<RelimsProjectBean> lInstrumentPredicate = iPredicateManager.getInstrumentPredicate();
            if (!lInstrumentPredicate.apply(iRelimsProjectBean)) {
                logger.debug("END " + lProjectid);
                return "Premature end for instrument type";
            }

            Predicate<RelimsProjectBean> lSearchSetSizePredicate = iPredicateManager.getSearchSetSizePredicate();
            if (!lSearchSetSizePredicate.apply(iRelimsProjectBean)) {
                logger.debug("END " + lProjectid);
                return "Premature end for search set size";
            }

            Predicate<RelimsProjectBean> lSpeciesPredicate = iPredicateManager.getSpeciesPredicate();
            if (!lSpeciesPredicate.apply(iRelimsProjectBean)) {
                logger.debug("END " + lProjectid);
                return "Premature end for species type";
            }


            Predicate<RelimsProjectBean> lModificationSetPrediate = iPredicateManager.getModificationSetPredicate();
            logger.debug("comparing Mascot modification sets within project " + lProjectid);
            if (!lModificationSetPrediate.apply(iRelimsProjectBean)) {
                logger.debug("END" + lProjectid);
                return "Premature end for distinct modification sets";
            }

            logger.debug("loading MS/MS spectra from project");

            ArrayList<File> lSpectrumFiles = Lists.newArrayList();
            File lSpectrumFile = MsLimsDataProvider.getInstance().getSpectraForProject(lProjectid);
            lSpectrumFiles.add(lSpectrumFile);

            iSearchStrategy.setSpectrumFiles(lSpectrumFiles);

            SearchList lSearchCommandList = new SearchList();
            iSearchStrategy.fill(lSearchCommandList, iRelimsProjectBean);


            logger.debug("launching the searchlist with " + lSearchCommandList.size() + " MOD variants");

            for (Object lSearchCommand : lSearchCommandList) {
                SearchCommandGenerator lSearch = (SearchCommandGenerator) lSearchCommand;
                String lCommand = lSearch.generateCommand();

                logger.debug("starting to run search " + lSearch.getName());
                Command.run(lCommand);

                logger.debug("processing the search results with PeptideShaker");

                PeptideShakerCLIInputBean lPeptideShakerCLIInputBean = new PeptideShakerCLIInputBean();

                lPeptideShakerCLIInputBean.setInput(lSearch.getSearchResultFolder());
                lPeptideShakerCLIInputBean.setOutput(lSearch.getSearchResultFolder());
                lPeptideShakerCLIInputBean.setPSMFDR(1.0);
                lPeptideShakerCLIInputBean.setPeptideFDR(1.0);
                lPeptideShakerCLIInputBean.setProteinFDR(1.0);
                lPeptideShakerCLIInputBean.setExperimentID(String.format("projectid_%d", lSearch.getProjectId()));
                lPeptideShakerCLIInputBean.setSampleID(lSearch.getName());

                PeptideShakerCLI lPeptideShakerCLI = new PeptideShakerCLI(lPeptideShakerCLIInputBean);
                lPeptideShakerCLI.call();

                logger.debug(String.format(
                        "finished PeptideShakerCLI on project '%s', sample '%s'",
                        lPeptideShakerCLIInputBean.getExperimentID(),
                        lPeptideShakerCLIInputBean.getSampleID()));

            }

            synchronized (iRelimsProjectBean) {
                setChanged();
                notifyObservers(iRelimsProjectBean);
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
        }

        return ("SUCCES");
    }


    public void setProject(RelimsProjectBean aRelimsProjectBean) {
        iRelimsProjectBean = aRelimsProjectBean;
    }

    public void setSearchStrategy(SearchStrategy aSearchStrategy) {
        iSearchStrategy = aSearchStrategy;
    }

}
