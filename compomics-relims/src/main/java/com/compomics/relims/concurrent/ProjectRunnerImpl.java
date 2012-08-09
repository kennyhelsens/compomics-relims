package com.compomics.relims.concurrent;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.beans.PeptideShakerJobBean;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.beans.SearchList;
import com.compomics.relims.model.guava.predicates.PredicateManager;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.interfaces.ModificationResolver;
import com.compomics.relims.model.interfaces.ProjectRunner;
import com.compomics.relims.model.interfaces.SearchStrategy;
import com.compomics.relims.model.provider.ProjectProvider;
import com.compomics.relims.observer.ResultObserver;
import com.google.common.base.Predicate;
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
    private ProjectProvider iProjectProvider;
    private long iProjectID;


    public void setPredicateManager(PredicateManager aPredicateManager) {
        iPredicateManager = aPredicateManager;
    }

    public String call() {
        try {

            setDataProvider(iProjectProvider.getDataProvider());
            setModificationResolver(iModificationResolver = iProjectProvider.getModificationResolver());

            setProject(iRelimsProjectBean = iProjectProvider.getProject(iProjectID));

            long lProjectid = iRelimsProjectBean.getProjectID();
            logger.info("creating projectrunner for " + lProjectid);

            Collection<Predicate> lPredicates = iPredicateManager.createCollection(
                    PredicateManager.Types.INSTRUMENT
//                    PredicateManager.Types.PROJECT_SIZE,
//                    PredicateManager.Types.SPECIES,
//                    PredicateManager.Types.SEARCH_SET_SIZE
            );

            logger.debug(format("validating project contents by %d predices", lPredicates.size()));
            for (Predicate lProjectPredicate : lPredicates) {
                boolean lResult = lProjectPredicate.apply(iRelimsProjectBean);
                if (!lResult) {
                    logger.error("END " + lProjectid);
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
                SearchGUICommandGenerator lSearchGUI = (SearchGUICommandGenerator) lSearchCommand;
                String lCommand = lSearchGUI.generateCommand();

                String lSampleID = lSearchGUI.getName();
                String lExperimentID = format("projectid_%d", lSearchGUI.getProjectId());
                logger.debug(format("running search %s", lSampleID));
                // Run searchgui

                ResultObserver.sendHeartBeat();
                Command.run(lCommand);

                logger.debug("processing the search results with PeptideShaker");
                PeptideShakerJobBean lPeptideShakerJobBean = new PeptideShakerJobBean();

                lPeptideShakerJobBean.setOutFolder(lSearchGUI.getSearchResultFolder());
                lPeptideShakerJobBean.setSearchGUIResultsFolder(lSearchGUI.getSearchResultFolder());

                lPeptideShakerJobBean.setPepfdr(1.0);
                lPeptideShakerJobBean.setProtfdr(1.0);
                lPeptideShakerJobBean.setPsmfdr(1.0);

                lPeptideShakerJobBean.setAscore(false);


                // Run PeptideShaker
                ResultObserver.sendHeartBeat();
                Command.run(lPeptideShakerJobBean.getSearchGUICommandString());


                logger.debug(format(
                        "finished PeptideShakerCLI on project '%s', sample '%s'",
                        lExperimentID,
                        lSampleID)
                );
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

    public void setProjectProvider(ProjectProvider aProjectProvider) {
        iProjectProvider = aProjectProvider;
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

    public void setProjectID(long aProjectID) {
        iProjectID = aProjectID;
    }
}
