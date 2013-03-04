package com.compomics.relims.modes.networking.feedbackproviders;

import com.compomics.relims.modes.networking.general.ResourceManager;
import com.compomics.remoterelimscontrolserver.general.PropertyManager;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import org.apache.log4j.Logger;

public class HeartbeatGenerator implements Runnable {

    PropertyManager propManager = PropertyManager.getInstance();
    ;
    String serverHostname;
    int serverPort;
    private byte[] data = null;
    private Socket sock = null;
    private ObjectInputStream sockInput = null;
    private ObjectOutputStream sockOutput = null;
    private static final Logger logger = Logger.getLogger(HeartbeatGenerator.class);

    public HeartbeatGenerator() {
        try {
            serverHostname = propManager.readProperty("controlServerIP");
            serverPort = Integer.parseInt(propManager.readProperty("transferPort"));
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
                    sock = new Socket(serverHostname, serverPort);
                    sockInput = new ObjectInputStream(sock.getInputStream());
                    sockOutput = new ObjectOutputStream(sock.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    logger.error("Server could not be reached...Retrying...");
                }

                try {
                    if (sockOutput == null) {
                        System.out.println("The socket Output is null");
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
            String hostname = serverHostname;
            int port = serverPort;
            HeartbeatGenerator client = new HeartbeatGenerator(hostname, port, data);
            client.sendHeartbeat();
        } catch (NullPointerException ex) {
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
}
