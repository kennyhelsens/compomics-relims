/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.modes.networking.client.GUI;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 *
 * @author Kenneth
 */
public class TextAreaAppender extends WriterAppender {

    static private JTextArea jTextArea = null;
    static private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm:ss z");

    /**
     * Set the target JTextArea for the logging information to appear.
     */
    static public void setTextArea(JTextArea jTextArea) {
        TextAreaAppender.jTextArea = jTextArea;
    }

    /**
     * Format and then append the loggingEvent to the stored JTextArea.
     */
    @Override
    public void append(LoggingEvent loggingEvent) {

        Date now = new Date();
        final String timeStamp = timeFormat.format(now) + " : ";
        this.layout = new SimpleLayout();
        final String message = this.layout.format(loggingEvent);
        // Append formatted message to textarea using the Swing Thread.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    if (jTextArea.getLineCount() == 250) {
                        jTextArea.setText("");
                        jTextArea.append(timeStamp + "The activity logger was reset to prevent stack-overflow...");
                    }
                    jTextArea.append(timeStamp + message);
                } catch (NullPointerException e) {
                }
            }
        });
    }
}
