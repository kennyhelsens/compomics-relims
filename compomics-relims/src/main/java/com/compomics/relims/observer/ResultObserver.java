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

/**
 * This class is a
 */
public class ResultObserver implements Observer {
    private static Logger logger = Logger.getLogger(ResultObserver.class);

    private Closable iClosable = null;

    private int iCounter = 0;

    public BufferedWriter iObservingWriter;

    public ResultObserver() throws IOException {
        File lFile = RelimsProperties.getTmpFile("runner.results.csv");
        iObservingWriter = Files.newWriter(lFile, Charset.defaultCharset());
    }

    public void update(Observable aObservable, Object o) {
        try {

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
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setClosable(Closable aClosable) {
        iClosable = aClosable;
    }
}