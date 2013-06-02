package com.compomics.relims.modes.networking.worker.feedbackproviders;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.resourcemanager.ResourceManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import org.apache.log4j.Logger;

public class HeartbeatGenerator implements Runnable {

    String serverHostname;
    int serverPort;
    private byte[] data = null;
    private static Socket sock = null;
    private static ObjectInputStream sockInput = null;
    private static ObjectOutputStream sockOutput = null;
    private static final Logger logger = Logger.getLogger(HeartbeatGenerator.class);
    private static boolean shutdownsignal = false;

    public HeartbeatGenerator() {
        try {
            serverHostname = RelimsProperties.getControllerIP();
            serverPort = RelimsProperties.getControllerPort();
        } catch (NullPointerException ex) {
            ex.printStackTrace();
            serverHostname = "localhost";
            serverPort = 6789;
        }
    }

    public HeartbeatGenerator(String serverHostname, int serverPort, byte[] data) {
        this.serverHostname = serverHostname;
        this.serverPort = serverPort;
        this.data = data;
    }

    public static void shutdown() {
        shutdownsignal = true;
        if (sock != null) {
            try {
                sock.close();
            } catch (IOException ex) {
                sock = null;
            }
        }
        if (sockInput != null) {
            try {
                sockInput.close();
            } catch (IOException ex) {
                sockInput = null;
            }
        }
        if (sockOutput != null) {
            try {
                sockOutput.close();
            } catch (IOException ex) {
                sockOutput = null;
            }
        }
    }

    public void sendHeartbeat() throws IOException {
        int failcounter = 0;
        while (!shutdownsignal) {
            try {
                try {
                    sock = new Socket(RelimsProperties.getControllerIP(), RelimsProperties.getControllerPort());
                    sockInput = new ObjectInputStream(sock.getInputStream());
                    sockOutput = new ObjectOutputStream(sock.getOutputStream());
                } catch (IOException e) {
                    logger.warn("Server could not be reached...retrying");
                }
                try {
                    if (sockOutput == null) {
                        failcounter++;
                        if (failcounter >= 40) {
                            logger.error("Server did not send responds for 20 minutes...sleeping for 30 minutes");
                            Thread.sleep(1000 * 60 * 30);
                        }
                    } else {
                        if (ResourceManager.getWorkerPort() != 0) {
                            sockOutput.writeInt(2);
                            sockOutput.flush();
                            sockOutput.writeInt(ResourceManager.getWorkerPort());
                            sockOutput.flush();
                        }
                        sockInput.readBoolean();
                    }
                } catch (SocketException e) {
                } catch (IOException e) {
                    if (e.getMessage() != null) {
                        logger.error(e.getMessage());
                    }
                }

                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ex) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    @Override
    public void run() {
        try {
            HeartbeatGenerator client = new HeartbeatGenerator(RelimsProperties.getControllerIP(), RelimsProperties.getControllerPort(), data);
            client.sendHeartbeat();
        } catch (NullPointerException ex) {
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
}
