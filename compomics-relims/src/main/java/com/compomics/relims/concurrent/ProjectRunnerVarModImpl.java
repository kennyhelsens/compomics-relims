package com.compomics.relims.concurrent;

import com.compomics.mascotdatfile.util.interfaces.MascotDatfileInf;
import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.compomics.mascotdatfile.util.mascot.Parameters;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.guava.predicates.*;
import com.compomics.relims.model.interfaces.DataProvider;
import com.compomics.relims.model.interfaces.ProjectRunner;
import com.compomics.relims.model.interfaces.SearchCommandGenerator;
import com.compomics.relims.model.interfaces.SearchProcessor;
import com.compomics.relims.model.*;
import com.compomics.relims.model.beans.RelimsProjectBean;
import com.compomics.relims.model.DatfileIterator;
import com.compomics.relims.model.MsLimsDataProvider;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;


/**
 * This class is a
 */
public class ProjectRunnerVarModImpl extends Observable implements ProjectRunner {
    private static Logger logger = Logger.getLogger(ProjectRunnerVarModImpl.class);

    private DataProvider iDataProvider;
    private RelimsProjectBean iRelimsProjectBean;

    private PredicateManager iPredicateManager;

    public ProjectRunnerVarModImpl() {

    }

    public void setPredicateManager(PredicateManager aPredicateManager) {
        iPredicateManager = aPredicateManager;
    }

    public String call() {
        try {

            long lProjectid = iRelimsProjectBean.getProjectid();
            logger.debug("created new projectrunner on " + lProjectid);

            Predicate<RelimsProjectBean> lProjectSizePredicate = iPredicateManager.getProjectSizePredicate();
            if (!lProjectSizePredicate.apply(iRelimsProjectBean)) {
                logger.debug("END " + lProjectid);
                return "Premature end for project size";
            }



            if (!iInstrumentPredicate.apply(iProject)) {
                logger.debug("END " + lProjectid);
                return "Premature end for instrument type";
            }


            if (!iSearchSetSizePredicate.apply(iProject)) {
                logger.debug("END " + lProjectid);
                return "Premature end for search set size";
            }
            if (!iSpeciesPredicate.apply(iProject)) {
                logger.debug("END " + lProjectid);
                return "Premature end for species type";
            }

            logger.debug("retrieving setup for project " + lProjectid);
            RelimsProjectBean lProjectSetupBean = buildProjectSetup(lProjectid);

            logger.debug("comparing Mascot modification sets within project " + lProjectid);
            if (!iModificationSetPredicate.apply(lProjectSetupBean)) {
                logger.debug("END" + lProjectid);
                return "Premature end for distinct modification sets";
            }

            ModificationList lModificationList = lProjectSetupBean.getModificationLists().get(0);
            ArrayList<Modification> lFixMods = Lists.newArrayList(lModificationList.getFixedModifications());
            ArrayList<Modification> lVarMods = Lists.newArrayList(lModificationList.getVariableModifications());
            ArrayList<Modification> lMods = Lists.newArrayList();
            lMods.addAll(lFixMods);
            lMods.addAll(lVarMods);

            logger.debug("loading MS/MS spectra from project");
            ArrayList<File> iSpectrumFiles = Lists.newArrayList();
            File lSpectrumFile = MsLimsDataProvider.getInstance().getSpectraForProject(lProjectid);
            iSpectrumFiles.add(lSpectrumFile);


            SearchList lSearchList = new SearchList();
            SearchCommandGenerator lSearchBean = null;

            // First define a search without the relims modification.
            lSearchBean = new SearchCommandVarModImpl("original", lFixMods, lVarMods, lProjectSetupBean, iSpectrumFiles);
            lSearchList.add(lSearchBean);

            ArrayList<UserMod> lRelimsMods = RelimsProperties.getRelimsMods();
            for (UserMod lRelimsModification : lRelimsMods) {
                ArrayList<UserMod> lRelimsModList = new ArrayList<UserMod>();
                lRelimsModList.add(lRelimsModification);
                lSearchBean = new SearchCommandVarModImpl(lRelimsModification.getModificationName(), lFixMods, lVarMods, lRelimsModList, lProjectSetupBean, iSpectrumFiles);
                lSearchList.add(lSearchBean);
            }

            logger.debug("launching the searchlist with " + lSearchList.size() + " MOD variants");
            for (Object o : lSearchList) {
                SearchCommandGenerator lSearch = (SearchCommandGenerator) o;
                logger.debug("starting to run search " + lSearch.getName());
                Command.run(lSearch.generateCommand());
            }

            logger.debug("processing the search results");

            if (RelimsProperties.useOmssa()) {
                logger.debug("processing omssa results");
                SearchProcessor lSearchProcessor = new OmssaSearchProcessor(lSearchList);
                lSearchProcessor.process();
            }

            if (RelimsProperties.useTandem()) {
                logger.debug("processing xtandem results");
                SearchProcessor lSearchProcessor = new XTandemSearchProcessor(lSearchList);
                lSearchProcessor.process();
            }

            synchronized (iProject) {
                setChanged();
                notifyObservers(iProject);
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } catch (ConfigurationException e) {
            logger.error(e.getMessage(), e);
        } catch (SAXException e) {
            logger.error(e.getMessage(), e);
        }
        return ("SUCCES");
    }


    public void setProject(RelimsProjectBean aRelimsProjectBean) {
        iRelimsProjectBean = aRelimsProjectBean;
    }

    public void setDataProvider(DataProvider aDataProvider) {
        iDataProvider = aDataProvider;
    }



    private RelimsProjectBean buildProjectSetup(long aProjectid) {
        RelimsProjectBean lProjectSetupBean = new RelimsProjectBean();

        DatfileIterator lIterator = MsLimsDataProvider.getInstance().getDatfilesForProject(aProjectid);
        ArrayList<Parameters> lParameterSets = Lists.newArrayList();
        ArrayList<ModificationList> lModificationLists = Lists.newArrayList();
        while (lIterator.hasNext()) {
            MascotDatfileInf lNext = lIterator.next();
            lParameterSets.add(lNext.getParametersSection());
            lModificationLists.add(lNext.getModificationList());
        }
        lProjectSetupBean.setModificationLists(lModificationLists);
        lProjectSetupBean.setParameterSets(lParameterSets);
        lProjectSetupBean.setProjectID((int) aProjectid);

        return lProjectSetupBean;
    }
}
