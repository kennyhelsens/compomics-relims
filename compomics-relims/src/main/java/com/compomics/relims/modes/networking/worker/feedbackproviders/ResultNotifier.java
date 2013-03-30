/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.worker.feedbackproviders;

// Tue Nov  2 18:34:53 EST 2004
//
// Written by Sean R. Owens, sean at guild dot net, released to the
// public domain.  Share and enjoy.  Since some people argue that it is
// impossible to release software to the public domain, you are also free
// to use this code under any version of the GPL, LPGL, or BSD licenses,
// or contact me for use of another license.
// http://darksleep.com/player
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.progressmanager.Checkpoint;
import com.compomics.relims.modes.networking.worker.general.ResourceManager;
import com.compomics.relims.modes.networking.worker.general.ResultManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

public class ResultNotifier {

    String serverHostname;
    int serverPort;
    private byte[] data = null;
    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private static final Logger logger = Logger.getLogger(ResultNotifier.class);

    public ResultNotifier() {
        try {
            serverHostname = RelimsProperties.getControllerIP();
            serverPort = RelimsProperties.getControllerPort();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            serverHostname = "localhost";
            serverPort = 6791;
        }
    }

    public ResultNotifier(String serverHostname, int serverPort, byte[] data) {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        this.data = data;
    }

    public boolean sendResults(Checkpoint state) {
        boolean sucess = false;
        try {
            try {
                sock = new Socket(serverHostname, serverPort);
                sockInput = new ObjectInputStream(sock.getInputStream());
                sockOutput = new ObjectOutputStream(sock.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace(System.err);
                return false;
            }
            try {
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("finishState", state.toString());
                resultMap.put("workerPort", ResourceManager.getWorkerPort());
                resultMap.put("taskID", ResourceManager.getTaskID());
                resultMap.put("projectID", ResourceManager.getProjectID());
                resultMap.put("systemInfoMap", ResourceManager.getAllSystemInfo());
                resultMap.put("PrideXMLErrorList", ResourceManager.getConversionErrors());
                ResultManager resultManager = new ResultManager();
                Map<String, Object> projectResultMap = resultManager.buildResultMap();
                if (projectResultMap == null) {
                    logger.error("Projectresultmap could not be built : null");
                } else if (projectResultMap.isEmpty()) {
                    logger.error("Projectresultmap could not be built : empty");
                }
                resultMap.put("projectResult", projectResultMap);
                sockOutput.writeInt(1);
                sockOutput.flush();
                sockOutput.writeObject(resultMap);
                sockOutput.flush();
                //sockInput.readBoolean();
                sucess = true;
            } catch (IOException e) {
                sucess = false;
                e.printStackTrace(System.err);
            }
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }

        } finally {
             return sucess;
        }
    }

    public boolean sendStatistics(HashMap<String, Object> resultMap) {
        boolean sentStatistics = false;

        try {
            try {
                sock = new Socket(serverHostname, serverPort);
                sockInput = new ObjectInputStream(sock.getInputStream());
                sockOutput = new ObjectOutputStream(sock.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
            try {
                sockOutput.writeInt(1);
                sockOutput.flush();
                sockOutput.writeObject(resultMap);
                sockOutput.flush();
                //sockInput.readBoolean();
                sentStatistics = true;
            } catch (IOException e) {
                e.printStackTrace(System.err);
            }
            // Sleep for a bit so the action doesn't happen to fast - this is purely for reasons of demonstration, and not required technically.
            try {
                Thread.sleep(500);
            } catch (Exception e) {
            }

        } finally {
            try {
                sock.close();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Exception closing socket.");
                e.printStackTrace(System.err);
            } finally {
                return sentStatistics;
            }
        }
    }
}
