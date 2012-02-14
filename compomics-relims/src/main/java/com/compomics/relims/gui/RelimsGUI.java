package com.compomics.relims.gui;

import com.compomics.mslims.db.accessors.Project;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.gui.listener.ConfigurationSaveListener;
import com.compomics.relims.gui.listener.MyRunnerClassesModel;
import com.compomics.relims.interfaces.ProjectRunner;
import com.compomics.relims.model.mslims.MsLimsProvider;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class is a
 */
public class RelimsGUI implements Observer {

    static private ExecutorService iService = Executors.newSingleThreadExecutor();

    private static Logger logger = Logger.getLogger(RelimsGUI.class);

    private JPanel jpanContent;
    private JButton btnStart;
    private JButton btnOptions;
    private JScrollPane scrollLayout;
    private JTextArea txtMessagePanel;
    private JButton iStopButton;
    private JPanel jpanButtons;
    private JTable tblOptions;
    private JButton btnSaveOptions;
    private JPanel jpanOptions;
    private JSplitPane splitContent;
    private JComboBox cmbRunnerClasses;

    private int iProjectCounter = 0;
    public ResultObserver iResultObserver;

    public static void main(String[] args) {
        JFrame lFrame = new JFrame("RelimsGUI");
        lFrame.setContentPane(new RelimsGUI().$$$getRootComponent$$$());
        lFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        lFrame.pack();
        lFrame.setVisible(true);

//        try {
//            UIManager.setLookAndFeel(new SyntheticaStandardLookAndFeel());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }

    /**
     * Construct the GUI
     */
    public RelimsGUI() {
        $$$setupUI$$$();
        txtMessagePanel.setEditable(false);
        TextAreaAppender.setTextArea(txtMessagePanel);
        logger.debug("initialized GUI");
        logger.debug("setting listeners");

        TableModel lTableModel = new RelimsPropertiesTableModel();
        tblOptions.setModel(lTableModel);
        tblOptions.setRowHeight(15);

        cmbRunnerClasses.setModel(new MyRunnerClassesModel());


        setListeners();

        /**
         * Sets an UncaughtExceptionHandler and executes the thread by the ExecutorsService
         */
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());

    }

    /**
     * Append the button listeners of the GUI
     */
    private void setListeners() {
        btnStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aActionEvent) {

                ExecutorService withinExecutor = Executors.newFixedThreadPool(1);

//                lRandomProjects.addAll(0, MsLimsProvider.getInstance().getProjects(new int[]{731}));


                iProjectCounter = 0;

                withinExecutor.submit(new Runnable() {


                    public void run() {
                        try {
                            iResultObserver = new ResultObserver();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                        try {

                            int lRandomSize = RelimsProperties.getRandomProjectAttempts();
                            ArrayList<Project> lRandomProjects = MsLimsProvider.getInstance().getRandomProjects(lRandomSize);
                            ArrayList<Project> lPreDefinedProjects = MsLimsProvider.getInstance().getPreDefinedProjects();

                            ArrayList<Project> lAllProjects = Lists.newArrayList();
                            lAllProjects.addAll(lPreDefinedProjects);
                            lAllProjects.addAll(lRandomProjects);

                            logger.debug("clicked start button");
                            logger.debug("selecting " + lRandomSize + " random projects");

                            List<Future> iFutures = Lists.newArrayList();


                            for (Project lProject : lAllProjects) {
                                String lClassID = cmbRunnerClasses.getSelectedItem().toString();
                                Class lRelimsClass = RelimsProperties.getRelimsClass(lClassID);
                                Object o = lRelimsClass.newInstance();

                                ProjectRunner lCallable = (ProjectRunner) o;
                                lCallable.setProject(lProject);

                                Observable lObservable = (Observable) o;
                                lObservable.addObserver(RelimsGUI.this);
                                lObservable.addObserver(iResultObserver);

                                iFutures.add(iService.submit(lCallable));
                            }
//
//                            for (Future lFuture : iFutures) {
//                                logger.info(String.format("Callable started"));
//                                String result = lFuture.get().toString();
//                                System.out.println(String.format("Callable finished with result %s", result));
//                                logger.info(String.format("Callable finished with result %s", result));
//                            }


                        } catch (ClassNotFoundException e) {
                            logger.error(e.getMessage(), e);
                        } catch (InstantiationException e) {
                            logger.error(e.getMessage(), e);
                        } catch (IllegalAccessException e) {
                            logger.error(e.getMessage(), e);
//                        } catch (InterruptedException e) {
//                            logger.error(e.getMessage(), e);
//                        } catch (ExecutionException e) {
//                            logger.error(e.getMessage(), e);
                        }
                    }
                });

            }
        });


        iStopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent aActionEvent) {
                shutdown();
            }
        });

        btnSaveOptions.addActionListener(new ConfigurationSaveListener());
    }

    private void shutdown() {
        List<Runnable> lRunnables = iService.shutdownNow();
        for (Runnable lRunnable : lRunnables) {
            logger.debug("shutting down " + lRunnable.toString());
        }
        iService = Executors.newSingleThreadExecutor();
    }


    public void update(Observable aObservable, Object o) {
        synchronized (this) {
            iProjectCounter++;
            logger.debug("PROJECT SUCCES COUNT " + iProjectCounter + "(" + o.toString() + ").");
            if (iProjectCounter == RelimsProperties.getMaxSucces()) {
                shutdown();
            }
        }
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        jpanContent = new JPanel();
        jpanContent.setLayout(new GridBagLayout());
        jpanContent.setEnabled(true);
        jpanContent.setPreferredSize(new Dimension(1000, 600));
        scrollLayout = new JScrollPane();
        GridBagConstraints gbc;
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(scrollLayout, gbc);
        txtMessagePanel = new JTextArea();
        txtMessagePanel.setEditable(false);
        txtMessagePanel.setSelectionEnd(0);
        txtMessagePanel.setSelectionStart(0);
        txtMessagePanel.setText("");
        scrollLayout.setViewportView(txtMessagePanel);
        jpanButtons = new JPanel();
        jpanButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        jpanContent.add(jpanButtons, gbc);
        btnStart = new JButton();
        btnStart.setText("Start");
        jpanButtons.add(btnStart);
        iStopButton = new JButton();
        iStopButton.setText("Stop");
        jpanButtons.add(iStopButton);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return splitContent;
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    private class ResultObserver implements Observer {

        public BufferedWriter iObservingWriter;

        private ResultObserver() throws IOException {
            File lFile = RelimsProperties.getTmpFile("runner.results.csv");
            iObservingWriter = Files.newWriter(lFile, Charset.defaultCharset());
        }

        public void update(Observable aObservable, Object o) {
            try {
                iObservingWriter.write(o.toString());
                iObservingWriter.newLine();
                iObservingWriter.flush();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        public void close() throws IOException {
            if (iObservingWriter != null) {
                iObservingWriter.flush();
                iObservingWriter.close();
            }
        }
    }
}
