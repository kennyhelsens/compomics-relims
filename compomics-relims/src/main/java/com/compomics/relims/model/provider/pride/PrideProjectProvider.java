package com.compomics.relims.model.provider.pride;

import com.compomics.pride_asa_pipeline.service.ExperimentService;
import com.compomics.pride_asa_pipeline.spring.ApplicationContextProvider;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.provider.ProjectProvider;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.springframework.context.ApplicationContext;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is a
 */
public class PrideProjectProvider extends ProjectProvider {

    ExperimentService iPrideService = null;
    Collection<Long> iExperimentIds = null;

    public PrideProjectProvider() {
        super();
        //Read relimsproperties to get the xml or web version  
        if (RelimsProperties.getPrideDataSource()) {
            iDataProvider = new PrideXMLDataProvider();
            ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
            iPrideService = (ExperimentService) lContext.getBean("prideXmlExperimentService");
        } else {
            iDataProvider = new PrideDataProvider();
            ApplicationContext lContext = ApplicationContextProvider.getInstance().getApplicationContext();
            iPrideService = (ExperimentService) lContext.getBean("dbExperimentService");
        }


    }

    public Collection<Long> getAllProjects() {
        if (iExperimentIds == null) {
            Map<String, String> lAllExperimentsMap = iPrideService.findAllExperimentAccessions();
            Set<String> lProjects = lAllExperimentsMap.keySet();
            iExperimentIds = Collections2.transform(lProjects, new Function<String, Long>() {
                public Long apply(@Nullable String input) {
                    return Long.parseLong(input);
                }
            });
        }
        return iExperimentIds;
    }

    public Collection<Long> getRandomProjects(int lSize) {
        Iterator<Long> lIterator = iExperimentIds.iterator();
        List<Long> lResult = Lists.newArrayList();
        for (int i = 0; i < lSize; i++) {
            lResult.add(lIterator.next());
        }
        return lResult;
    }
}
