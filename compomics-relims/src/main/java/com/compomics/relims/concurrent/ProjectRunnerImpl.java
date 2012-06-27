package com.compomics.relims.concurrent;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;
import com.compomics.relims.model.guava.predicates.PredicateManager;
import com.compomics.relims.model.interfaces.*;
import com.google.common.base.Predicate;
import eu.isas.peptideshaker.cmd.PeptideShakerCLI;
import eu.isas.peptideshaker.cmd.PeptideShakerCLIInputBean;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Observable;

import static java.lang.String.format;


/**
 * This class is a
 */
public class ProjectRunnerImpl extends Observable implements ProjectRunner {
    private static Logger logger = Logger.getLogger(ProjectRunnerImpl.class);

    private RelimsProjectBean iRelimsProjectBean;

    private PredicateManager iPredicateManager;
    private SearchStrategy iSearchStrategy;
    private DataProvider iDataProvider;
    private ModificationResolver iModificationResolver;


    public void setPredicateManager(PredicateManager aPredicateManager) {
        iPredicateManager = aPredicateManager;
    }

    public String call() {
        try {

            long lProjectid = iRelimsProjectBean.getProjectID();
            logger.debug("creating projectrunner for " + lProjectid);

            Collection<Predicate> lPredicates = iPredicateManager.createCollection(
//                    PredicateManager.Types.PROJECT_SIZE,
//                    PredicateManager.Types.INSTRUMENT,
//                    PredicateManager.Types.SPECIES,
//                    PredicateManager.Types.SEARCH_SET_SIZE
            );

            logger.debug(format("validating project contents by %d predices", lPredicates.size()));
            for (Predicate lProjectPredicate : lPredicates) {
                boolean lResult = lProjectPredicate.apply(iRelimsProjectBean);
                if (!lResult) {
                    logger.debug("END " + lProjectid);
                    return "Premature end for project size";
                }
            }

            logger.debug(format("loading MS/MS spectra for project %s from %s", lProjectid, iDataProvider.toString()));
            File lSpectrumFile = iDataProvider.getSpectraForProject(lProjectid);
            iSearchStrategy.addSpectrumFile(lSpectrumFile);

            logger.debug("resolving modification sets");
            iModificationResolver.resolveModificationList(iRelimsProjectBean);
            iModificationResolver.persistUserMods(RelimsProperties.getSearchGuiUserModFile());

            logger.debug(format("creating SearchCommands for project %s using SearchStrategy %s", lProjectid, iSearchStrategy.getName()));
            SearchList lSearchCommandList = new SearchList();
            iSearchStrategy.fill(lSearchCommandList, iRelimsProjectBean);

            logger.debug(format("launching the searchlist with %d variants", lSearchCommandList.size()));
            for (Object lSearchCommand : lSearchCommandList) {
                SearchCommandGenerator lSearch = (SearchCommandGenerator) lSearchCommand;
                String lCommand = lSearch.generateCommand();

                logger.debug(format("running search %s", lSearch.getName()));
                Command.run(lCommand);

                logger.debug("processing the search results with PeptideShaker");

                PeptideShakerCLIInputBean lPeptideShakerCLIInputBean = new PeptideShakerCLIInputBean();

                lPeptideShakerCLIInputBean.setInput(lSearch.getSearchResultFolder());
                lPeptideShakerCLIInputBean.setOutput(lSearch.getSearchResultFolder());
                lPeptideShakerCLIInputBean.setPSMFDR(1.0);
                lPeptideShakerCLIInputBean.setPeptideFDR(1.0);
                lPeptideShakerCLIInputBean.setProteinFDR(1.0);
                lPeptideShakerCLIInputBean.setExperimentID(format("projectid_%d", lSearch.getProjectId()));
                lPeptideShakerCLIInputBean.setSampleID(lSearch.getName());

                PeptideShakerCLI lPeptideShakerCLI = new PeptideShakerCLI(lPeptideShakerCLIInputBean);
                lPeptideShakerCLI.call();

                logger.debug(format(
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

        return (format("SUCCES for project %d", iRelimsProjectBean.getProjectID()));
    }


    public void setProject(RelimsProjectBean aRelimsProjectBean) {
        iRelimsProjectBean = aRelimsProjectBean;
    }

    public void setSearchStrategy(SearchStrategy aSearchStrategy) {
        iSearchStrategy = aSearchStrategy;
    }

    public void setDataProvider(DataProvider aDataProvider) {
        iDataProvider = aDataProvider;
    }

    public void setModificationResolver(ModificationResolver aModificationResolver) {
        iModificationResolver = aModificationResolver;
    }
}
