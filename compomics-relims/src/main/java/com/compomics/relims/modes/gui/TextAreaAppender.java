package com.compomics.relims.modes.gui;

import com.google.common.base.Joiner;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.spi.LoggingEvent;

import javax.swing.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Simple example of creating a Log4j appender that will
 * write to a JTextArea.
 */
public class TextAreaAppender extends ConsoleAppender {

    static private JTextArea jTextArea = null;
    static private ExecutorService iService = Executors.newSingleThreadExecutor();

    static private Deque iMessages = new LinkedList();

    static long MAX_SIZE = 1000;

    /**
     * Set the target JTextArea for the logging information to appear.
     */
    static public void setTextArea(JTextArea jTextArea) {
        TextAreaAppender.jTextArea = jTextArea;
    }

    /**
     * Format and then append the loggingEvent to the stored
     * JTextArea.
     */
    public synchronized void append(LoggingEvent loggingEvent) {

        final String message = this.layout.format(loggingEvent);
        iMessages.addFirst(message);
        final String lContent = Joiner.on("").join(iMessages);
        iService.submit(new Runnable() {
            public void run() {
                jTextArea.setText(lContent);
            }
        });

        if(iMessages.size() > MAX_SIZE){
            iMessages.removeLast();
        }
    }
}
