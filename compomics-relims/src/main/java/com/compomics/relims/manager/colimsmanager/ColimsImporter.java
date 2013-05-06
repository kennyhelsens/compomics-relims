/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.manager.colimsmanager;

import com.compomics.colims.core.exception.MappingException;
import com.compomics.colims.core.exception.PeptideShakerIOException;
import com.compomics.colims.core.io.PeptideShakerIO;
import com.compomics.colims.core.io.impl.PeptideShakerIOImpl;
import com.compomics.colims.core.io.mapper.UtilitiesExperimentMapper;
import com.compomics.colims.core.io.model.PeptideShakerImport;
import com.compomics.colims.core.service.ProjectService;
import com.compomics.colims.model.Experiment;
import com.compomics.colims.model.Project;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kenneth
 */
public class ColimsImporter {
    // static variables
    private static Logger logger = Logger.getLogger(ColimsImporter.class);
    private static ColimsImporter singleton = null;

    // instance variables
    private UtilitiesExperimentMapper experimentMapper;
    private ProjectService projectService;
    private Project project;


    public static ColimsImporter getInstance() {
        if (singleton == null) {
            singleton = new ColimsImporter();
        }
        return singleton;
    }

    public ColimsImporter() {
        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("colims-core-context.xml");
        experimentMapper = (UtilitiesExperimentMapper) applicationContext.getBean("experimentMapper");
        projectService = (ProjectService) applicationContext.getBean("projectService");
    }

    public void transferToColims(File cpsFile, File fastaFile, File mgfFile, String aColimsProjectTitle) throws PeptideShakerIOException, MappingException {
        PeptideShakerIO relimsToColims = new PeptideShakerIOImpl();
        logger.info("Start importing PeptideShaker file " + cpsFile.getAbsolutePath());
        PeptideShakerImport peptideShakerImport = relimsToColims.importPeptideShakerCpsArchive(cpsFile);
        logger.info("Finished importing PeptideShaker file " + cpsFile.getAbsolutePath());

        //set mgf and fasta files
        List<File> mgfFiles = new ArrayList<File>();
        mgfFiles.add(mgfFile);
        peptideShakerImport.setMgfFiles(mgfFiles);
        peptideShakerImport.setFastaFile(fastaFile);

        logger.info("Start mapping experiment for MSexperiment " + peptideShakerImport.getMsExperiment().getReference());
        Experiment experiment = new Experiment();
        experimentMapper.map(peptideShakerImport, experiment);
        logger.info("Stop mapping experiment for MSexperiment " + peptideShakerImport.getMsExperiment().getReference());

        project = new Project();
        project.setTitle(aColimsProjectTitle);
        logger.info("Start persisting project " + project.getTitle());
        project.getExperiments().add(experiment);

        //set entity relations
        experiment.setProject(project);
        projectService.save(project);
        logger.info("Finished persisting project " + project.getTitle());
    }
}
