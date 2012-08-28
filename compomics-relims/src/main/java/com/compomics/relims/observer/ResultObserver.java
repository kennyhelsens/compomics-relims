package com.compomics.relims.observer;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.model.interfaces.Closable;
import com.google.common.io.Files;
import org.apache.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Future;

/**
 * This class is a
 */
public class ResultObserver implements Observer {
    private static Logger logger = Logger.getLogger(ResultObserver.class);


    private Closable iClosable = null;

    private int iCounter = 0;

    public BufferedWriter iObservingWriter;
    private Future<String> iCurrentFuture;

    private static long iTimeLastHeartbeat = -1l;

    public ResultObserver() throws IOException {
        File lFile = RelimsProperties.getTmpFile("runner.results.csv");
        iObservingWriter = Files.newWriter(lFile, Charset.defaultCharset());

        // Keep
    }

    public void update(Observable aObservable, Object o) {
        try {
            // Update last activity time.
            iTimeLastHeartbeat = System.currentTimeMillis();

            synchronized (this) {
                iCounter++;
                logger.debug("PROJECT SUCCES COUNT " + iCounter + "(" + o.toString() + ").");


            }

            if (iCounter >= RelimsProperties.getMaxSucces()) {

                if (iObservingWriter != null) {
                    iObservingWriter.flush();
                    iObservingWriter.close();
                }

                if (iClosable != null) {
                    iClosable.close();
                }

            }

            iObservingWriter.write(o.toString());
            iObservingWriter.newLine();
            iObservingWriter.flush();

            // Remove the handler to the current Future instance.
            iCurrentFuture = null;

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setClosable(Closable aClosable) {
        iClosable = aClosable;
    }

    public void setActiveFuture(Future<String> aCurrentFuture) {
        iCurrentFuture = aCurrentFuture;
        new ResultsController().start();
        sendHeartBeat();
    }

    /**
     * Call this method to avoid cancelling the Active Future.
     */
    public static void sendHeartBeat() {
        iTimeLastHeartbeat = System.currentTimeMillis();
    }


    private class ResultsController implements Runnable {


        private int MaxHours = RelimsProperties.getMaxJobHours();
        private int MaxMinutes = RelimsProperties.getMaxJobMinutes();
        private int PollingTimeSeconds = RelimsProperties.getPollingTime();
        int updateInterval = PollingTimeSeconds * 1000;

        private long MaxFutureTime = (MaxHours * 60 * 1000) + (MaxMinutes * 60 * 1000);
        //        private long MaxFutureTime = 15000;
        private Thread updateThread;

        public ResultsController() {
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(updateInterval);
                    if (iCurrentFuture != null) {
                        // Only do something is a Future is running.
                        long lTimeSinceLastHeartbeat = System.currentTimeMillis() - iTimeLastHeartbeat;

                        long lTimeLeftMinutes = (MaxFutureTime - lTimeSinceLastHeartbeat) / (1000 * 60);
                        logger.debug(String.format("Heartbeat : job has %s minutes left to complete", lTimeLeftMinutes));

                        if (lTimeSinceLastHeartbeat > MaxFutureTime) {
                            logger.debug("Cancelling ");
                            iCurrentFuture.cancel(true);
                            iCurrentFuture = null;
                        } else if ((MaxFutureTime - lTimeSinceLastHeartbeat) < 5000) {
                            logger.debug(String.format("Job will be cancelled soon! Only '%s' minutes left to complete!!", lTimeLeftMinutes));
                        }
                    }

                } catch (InterruptedException e) {
                    logger.debug(String.format("Actively cancelling analysis due to missing signal (%smin). Increase the max.tim properties for longer search time.", (MaxFutureTime / (60 * 1000))));
                }
            }
        }

        public void start() {
            if (updateThread == null) {
                updateThread = new Thread(this);
                updateThread.start();
            }
        }

        public void stop() {
            if (updateThread != null) {
                updateThread.interrupt();
                updateThread = null;
            }
        }
    }
}