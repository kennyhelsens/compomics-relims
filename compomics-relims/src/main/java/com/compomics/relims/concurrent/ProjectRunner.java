package com.compomics.relims.concurrent;

import com.compomics.mascotdatfile.util.interfaces.MascotDatfileInf;
import com.compomics.mascotdatfile.util.interfaces.Modification;
import com.compomics.mascotdatfile.util.mascot.ModificationList;
import com.compomics.mascotdatfile.util.mascot.Parameters;
import com.compomics.mslims.db.accessors.Project;
import com.compomics.omssa.xsd.UserMod;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.guava.functions.SpeciesFinderFunction;
import com.compomics.relims.guava.predicates.InstrumentPredicate;
import com.compomics.relims.guava.predicates.ModificationSetPredicate;
import com.compomics.relims.guava.predicates.ProjectSizePredicate;
import com.compomics.relims.guava.predicates.SpeciesPredicate;
import com.compomics.relims.model.SearchList;
import com.compomics.relims.model.SearchProcessor;
import com.compomics.relims.model.beans.ProjectSetupBean;
import com.compomics.relims.model.beans.SearchBean;
import com.compomics.relims.model.mslims.DatfileIterator;
import com.compomics.relims.model.mslims.MsLimsProvider;
import com.google.common.collect.Lists;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;


/**
 * This class is a
 */
public class ProjectRunner extends Observable implements Runnable {
    private static Logger logger = Logger.getLogger(ProjectRunner.class);
    private final Project iProject;
    private ProjectSizePredicate iProjectSizePredicate;
    private InstrumentPredicate iInstrumentPredicate;
    private ModificationSetPredicate iModificationSetPredicate;
    private SpeciesPredicate iSpeciesPredicate;


    public ProjectRunner(Project aProjectID) {
        iProject = aProjectID;
        ArrayList lAllowedInstruments = new ArrayList();
        lAllowedInstruments.add(8);
        lAllowedInstruments.add(9);
        lAllowedInstruments.add(10);

        iProjectSizePredicate = new ProjectSizePredicate(10000, 1000);
        iInstrumentPredicate = new InstrumentPredicate(new HashSet<Integer>(lAllowedInstruments));
        iModificationSetPredicate = new ModificationSetPredicate();
        iSpeciesPredicate = new SpeciesPredicate(SpeciesFinderFunction.SPECIES.HUMAN, 50);

    }

    public void run() {
        try {

            long lProjectid = iProject.getProjectid();
            logger.debug("created new projectrunner on " + lProjectid);

            if (!iProjectSizePredicate.apply(iProject)) {
                logger.debug("END " + lProjectid);
                return;
            }

            if (!iInstrumentPredicate.apply(iProject)) {
                logger.debug("END " + lProjectid);
                return;
            }

            if (!iSpeciesPredicate.apply(iProject)) {
                logger.debug("END " + lProjectid);
                return;
            }

            logger.debug("retrieving setup for project " + lProjectid);
            ProjectSetupBean lProjectSetupBean = buildProjectSetup(lProjectid);

            logger.debug("comparing Mascot modification sets within project " + lProjectid);
            if (!iModificationSetPredicate.apply(lProjectSetupBean)) {
                logger.debug("END" + lProjectid);
                return;
            }

            ModificationList lModificationList = lProjectSetupBean.getModificationLists().get(0);
            ArrayList<Modification> lFixMods = Lists.newArrayList(lModificationList.getFixedModifications());
            ArrayList<Modification> lVarMods = Lists.newArrayList(lModificationList.getVariableModifications());
            ArrayList<Modification> lMods = Lists.newArrayList();
            lMods.addAll(lFixMods);
            lMods.addAll(lVarMods);

            logger.debug("loading MS/MS spectra from project");
            ArrayList<File> iSpectrumFiles = Lists.newArrayList();
            File lSpectrumFile = MsLimsProvider.getInstance().getSpectraForProject(lProjectid);
            iSpectrumFiles.add(lSpectrumFile);


            SearchList lSearchList = new SearchList();
            SearchBean lSearchBean = null;

            // First define a search without the relims modification.
            lSearchBean = new SearchBean("original", lFixMods, lVarMods, lProjectSetupBean, iSpectrumFiles);
            lSearchList.add(lSearchBean);

            ArrayList<UserMod> lRelimsMods = RelimsProperties.getRelimsMods();
            for (UserMod lRelimsModification : lRelimsMods) {
                ArrayList<UserMod> lRelimsModList = new ArrayList<UserMod>();
                lRelimsModList.add(lRelimsModification);
                lSearchBean = new SearchBean(lRelimsModification.getModificationName(), lFixMods, lVarMods, lRelimsModList, lProjectSetupBean, iSpectrumFiles);
                lSearchList.add(lSearchBean);
            }

            logger.debug("launching the searchlist with " + lSearchList.size() + " variants");
            for (Object o : lSearchList) {
                SearchBean lSearch = (SearchBean) o;
                logger.debug("starting to run search " + lSearch.getName());
                Command.run(lSearch.generateCommand());
            }

            logger.debug("processing the search results");
            SearchProcessor lSearchProcessor = new SearchProcessor(lSearchList);
            lSearchProcessor.process();

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
    }


    private ProjectSetupBean buildProjectSetup(long aProjectid) {
        ProjectSetupBean lProjectSetupBean = new ProjectSetupBean();

        DatfileIterator lIterator = MsLimsProvider.getInstance().getDatfilesForProject(aProjectid);
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
