/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.general;

import com.compomics.pridexmltomgfconverter.errors.enums.ConversionError;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.util.experiment.identification.SearchParameters;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Kenneth
 */
public class ResourceManager {

    public static int workerPort;
    public static long taskID;
    public static int cores;
    public static long maxMemory;
    public static long taskTime;
    private static String finishState;
    private static long projectID;
    private static String user;
    private static List<ConversionError> conversionErrorList = new ArrayList<ConversionError>();

    public static void loadSystemInformation() {
        cores = Runtime.getRuntime().availableProcessors();
        maxMemory = Runtime.getRuntime().maxMemory();
    }

    public static long getTaskTime() {
        return taskTime;
    }

    public static void setTaskTime(long taskTime) {
        ResourceManager.taskTime = taskTime;
    }

    public static void setWorkerPort(int workerPort) {
        ResourceManager.workerPort = workerPort;
    }

    public static void setTaskID(long taskID) {
        ResourceManager.taskID = taskID;
    }

    public static Long getTaskID() {
        return taskID;
    }

    public static int getCores() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static void setCores(int cores) {
        ResourceManager.cores = cores;
    }

    public static long getMaxMemory() {
        return maxMemory;
    }

    public static void setMaxMemory(long maxMemory) {
        ResourceManager.maxMemory = maxMemory;
    }

    public static LinkedHashMap<String, Object> getAllSystemInfo() {

        Map<String, Object> SystemInformation = new LinkedHashMap<String, Object>();
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        //ALL CPU METHODS
        //-----------------------------------------------------------------------------------------------------------        
        for (Method method : operatingSystemMXBean.getClass().getDeclaredMethods()) {
            method.setAccessible(true);
            if (method.getName().startsWith("get") && Modifier.isPublic(method.getModifiers())) {
                Object value = null;
                try {
                    value = method.invoke(operatingSystemMXBean);
                    SystemInformation.put(method.getName().replace("get", "").toLowerCase(), value);
                } catch (IllegalAccessException e) {
                    SystemInformation.put(method.getName().replace("get", "").toLowerCase(), -1);
                } catch (IllegalArgumentException e) {
                    SystemInformation.put(method.getName().replace("get", "").toLowerCase(), -1);
                } catch (InvocationTargetException e) {
                    SystemInformation.put(method.getName().replace("get", "").toLowerCase(), -1);
                }


            }
        }
        //OS RELATED
        //----------------------------------------------------------------------------------------------------------- 

        SystemInformation.put("osarch", System.getProperty("os.arch"));
        SystemInformation.put("osversion", System.getProperty("os.version"));
        SystemInformation.put("osname", System.getProperty("os.name"));
        SystemInformation.put("javaversion", System.getProperty("java.version") + "");



        //TASK RELATED
        //-----------------------------------------------------------------------------------------------------------       


        SystemInformation.put("taskID", ResourceManager.getTaskID());
        SystemInformation.put("taskTime", ResourceManager.getTaskTime());
        SystemInformation.put("projectID", ResourceManager.getProjectID());
        SystemInformation.put("userID", ResourceManager.getUserID());
        SystemInformation.put("finishState", ResourceManager.getFinishState());
        SystemInformation.put("searchParameters", ResourceManager.getUsedSearchParameters());
        SystemInformation.put("cores", Runtime.getRuntime().availableProcessors());

        //WORKER RELATED
        SystemInformation.put("workerPort", ResourceManager.getWorkerPort());

        //PRIDE RELATED

        if (!conversionErrorList.isEmpty()) {
            SystemInformation.put("PrideXMLErrorList", conversionErrorList);
        }

        return (LinkedHashMap<String, Object>) SystemInformation;
    }

    public static int getWorkerPort() {
        return workerPort;
    }

    public static String getFinishState() {
        return finishState;
    }

    public static void setFinishState(Checkpoint finishState) {
        if (finishState == null) {
            ResourceManager.finishState = "NEW";
        } else {
            ResourceManager.finishState = finishState.toString();
        }
    }

    public static void setProjectID(long projectID) {
        ResourceManager.projectID = projectID;
    }

    public static long getProjectID() {
        return projectID;
    }

    public static void setUserID(String user) {
        ResourceManager.user = user;
    }

    public static String getUserID() {
        return ResourceManager.user;
    }

    public static void setSearchParameters(SearchParameters lSearchParameters) {
        ResourceManager.searchParameters = lSearchParameters;
    }

    public static SearchParameters getUsedSearchParameters() {
        return ResourceManager.searchParameters;
    }
    private static SearchParameters searchParameters;

    public static void setConversionErrors(List<ConversionError> conversionErrorList) {
        if (conversionErrorList != null) {
            ResourceManager.conversionErrorList = conversionErrorList;
        } else {
            ResourceManager.conversionErrorList = new ArrayList<ConversionError>();
        }
    }

    public static List<ConversionError> getConversionErrors() {
        return ResourceManager.conversionErrorList;
    }
}
