package com.compomics.relims.modes.networking.worker.feedbackproviders;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.worker.general.ResourceManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Logger;

public class HeartbeatGenerator implements Runnable {

    String serverHostname;
    int serverPort;
    private byte[] data = null;
    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private static final Logger logger = Logger.getLogger(HeartbeatGenerator.class);

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

    public void sendHeartbeat() throws IOException {
        while (true) {
            try {
                try {
                    sock = new Socket(RelimsProperties.getControllerIP(), RelimsProperties.getControllerPort());
                    sockInput = new ObjectInputStream(sock.getInputStream());
                    sockOutput = new ObjectOutputStream(sock.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("Server could not be reached...Retrying...");
                }
                try {
                    if (sockOutput == null) {
                        logger.error("The socket Output is null");
                        if (RelimsProperties.getControllerIP() == null) {
                              logger.error("IP is null");
                        }
                        if (RelimsProperties.getControllerPort() == 0) {
                              logger.error("port is null");
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
                // Sleep for a bit --> safety measure
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                    e.printStackTrace();
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
