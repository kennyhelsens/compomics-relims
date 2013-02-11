package com.compomics.relims.gui;

import com.compomics.relims.concurrent.RelimsJob;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.gui.model.ProjectSourceSelectionModel;
import com.compomics.relims.gui.model.RelimsPropertiesTableModel;
import com.compomics.relims.gui.model.StrategySelectionModel;
import com.compomics.relims.gui.util.Properties;
import com.compomics.relims.observer.Checkpoint;
import com.compomics.relims.observer.ProgressManager;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import net.jimmc.jshortcut.JShellLink;
import org.apache.log4j.Logger;

/**
 * The Relims GUI.
 *
 * @author Kenny Helsens
 * @author Harald Barsnes
 */
public class RelimsNBGUI extends javax.swing.JFrame {

    private static Logger logger = Logger.getLogger(RelimsNBGUI.class);
    protected StrategySelectionModel iStrategySelectionModel = null;
    protected ProjectSourceSelectionModel iProjectSourceSelectionModel = null;
    private RelimsJob iRelimsJob = null;
    protected RelimsJobStarter iRelimsJobStarter;

    /**
     * Creates a new RelimsNBGUI.
     */
    public RelimsNBGUI() {

        // check if a newer version is available on google code
        checkForNewVersion(new Properties().getVersion());

        // add desktop shortcut?
        addShortcutAtDeskTop();

        initComponents();

        TextAreaAppender.setTextArea(txtLogger);
        logger.debug("initialized GUI");
        logger.debug("setting listeners");

        TableModel lTableModel = new RelimsPropertiesTableModel();
        this.tblProperties.setModel(lTableModel);

        TableColumnModel lTableColumnModel = tblProperties.getTableHeader().getColumnModel();
        lTableColumnModel.getColumn(0).setHeaderValue("Property");
        lTableColumnModel.getColumn(1).setHeaderValue("Value");

        iStrategySelectionModel = new StrategySelectionModel();
        iProjectSourceSelectionModel = new ProjectSourceSelectionModel();

        /**
         * Sets an UncaughtExceptionHandler and executes the thread by the
         * ExecutorsService
         */
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());

        // set the title of the frame and add the icon
        this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/resources/image/logo_small.png")));

        setTitle("Relims " + new Properties().getVersion());
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Check if a newer version of Relims is available.
     *
     * @param currentVersion the version number of the currently running Relims
     */
    private static void checkForNewVersion(String currentVersion) {

        try {
            boolean deprecatedOrDeleted = false;

            URL downloadPage = new URL(
                    "http://code.google.com/p/compomics-relims/downloads/detail?name=compmics-relims-" + currentVersion);

            if ((java.net.HttpURLConnection) downloadPage.openConnection() != null) {

                int respons = ((java.net.HttpURLConnection) downloadPage.openConnection()).getResponseCode();

                // 404 means that the file no longer exists, which means that
                // the running version is no longer available for download,
                // which again means that a never version is available.
                if (respons == 404) {
                    deprecatedOrDeleted = true;
                } else {

                    // also need to check if the available running version has been
                    // deprecated (but not deleted)
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(downloadPage.openStream()));

                    String inputLine;

                    while ((inputLine = in.readLine()) != null && !deprecatedOrDeleted) {
                        if (inputLine.lastIndexOf("Deprecated") != -1
                                && inputLine.lastIndexOf("Deprecated Downloads") == -1
                                && inputLine.lastIndexOf("Deprecated downloads") == -1) {
                            deprecatedOrDeleted = true;
                        }
                    }

                    in.close();
                }

                // informs the user about an updated version is available, unless the user is running a beta version
                if (deprecatedOrDeleted && currentVersion.lastIndexOf("beta") == -1) {
                    int option = JOptionPane.showConfirmDialog(null,
                            "A newer version of Relims is available.\n"
                            + "Do you want to upgrade?",
                            "Upgrade Available",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        BareBonesBrowserLaunch.openURL("http://compomics-relims.googlecode.com/");
                        System.exit(0);
                    } else if (option == JOptionPane.CANCEL_OPTION) {
                        System.exit(0);
                    }
                }
            }
        } catch (UnknownHostException e) {
            // ignore exception
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            ProgressManager.setState(Checkpoint.FAILED,e);;
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Ask the user if he/she wants to add a shortcut at the desktop.
     */
    private void addShortcutAtDeskTop() {

        if (!new Properties().getJarFilePath().equalsIgnoreCase(".")
                && System.getProperty("os.name").lastIndexOf("Windows") != -1
                && new File(new Properties().getJarFilePath() + "/resources/conf/firstRun").exists()) {

            // @TODO: add support for desktop icons in mac and linux??

            // delete the firstRun file such that the user is not asked the next time around
            new File(new Properties().getJarFilePath() + "/resources/conf/firstRun").delete();

            int value = JOptionPane.showConfirmDialog(null,
                    "Create a shortcut to Relims on the desktop?",
                    "Create Desktop Shortcut?",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (value == JOptionPane.YES_OPTION) {

                String jarFilePath = new Properties().getJarFilePath();
                String versionNumber = new Properties().getVersion();

                if (!jarFilePath.equalsIgnoreCase(".")) {

                    // remove the initial '/' at the start of the line
                    if (jarFilePath.startsWith("\\") && !jarFilePath.startsWith("\\\\")) {
                        jarFilePath = jarFilePath.substring(1);
                    }

                    String iconFileLocation = jarFilePath + "\\resources\\relims.ico";
                    String jarFileLocation = jarFilePath + "\\compomics-relims-" + versionNumber + ".jar";

                    try {
                        JShellLink link = new JShellLink();
                        link.setFolder(JShellLink.getDirectory("desktop"));
                        link.setName("Relims " + versionNumber);
                        link.setIconLocation(iconFileLocation);
                        link.setPath(jarFileLocation);
                        link.save();
                    } catch (Exception e) {
                        System.out.println("An error occurred when trying to create a desktop shortcut...");
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btnGroupStrategy = new javax.swing.ButtonGroup();
        btnGroupSource = new javax.swing.ButtonGroup();
        backgroundPanel = new javax.swing.JPanel();
        settingsPanel = new javax.swing.JPanel();
        splitMain = new javax.swing.JSplitPane();
        scrlLogger = new javax.swing.JScrollPane();
        txtLogger = new javax.swing.JTextArea();
        tabCore = new javax.swing.JTabbedPane();
        jpanMain = new javax.swing.JPanel();
        mainPanel = new javax.swing.JPanel();
        rdbSourceMSLIMS = new javax.swing.JRadioButton();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        rdbStraight = new javax.swing.JRadioButton();
        rdbSourcePRIDE = new javax.swing.JRadioButton();
        rdbVarMOD = new javax.swing.JRadioButton();
        btnStart = new javax.swing.JButton();
        rdbVarDB = new javax.swing.JRadioButton();
        btnStop = new javax.swing.JButton();
        tableTabPanel = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        scrlTable = new javax.swing.JScrollPane();
        tblProperties = new javax.swing.JTable();
        iconRelims = new javax.swing.JLabel();
        iconCompomics = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Relims");
        setBackground(new java.awt.Color(255, 255, 255));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(255, 255, 255));

        settingsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
        settingsPanel.setOpaque(false);

        splitMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        splitMain.setDividerSize(4);
        splitMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitMain.setResizeWeight(0.6);
        splitMain.setOpaque(false);

        scrlLogger.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Log", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 2, 10))); // NOI18N
        scrlLogger.setViewportBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(204, 204, 204)));
        scrlLogger.setOpaque(false);

        txtLogger.setColumns(20);
        txtLogger.setEditable(false);
        txtLogger.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        txtLogger.setRows(5);
        txtLogger.setWrapStyleWord(true);
        scrlLogger.setViewportView(txtLogger);

        splitMain.setRightComponent(scrlLogger);

        jpanMain.setBackground(new java.awt.Color(255, 255, 255));

        mainPanel.setOpaque(false);

        btnGroupSource.add(rdbSourceMSLIMS);
        rdbSourceMSLIMS.setSelected(true);
        rdbSourceMSLIMS.setText("MSLIMS");
        rdbSourceMSLIMS.setIconTextGap(10);
        rdbSourceMSLIMS.setOpaque(false);
        rdbSourceMSLIMS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourceMSLIMSActionPerformed(evt);
            }
        });

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel2.setText("Strategy");

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel3.setText("Source");

        btnGroupStrategy.add(rdbStraight);
        rdbStraight.setText("Straight");
        rdbStraight.setIconTextGap(10);
        rdbStraight.setOpaque(false);
        rdbStraight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbStraightActionPerformed(evt);
            }
        });

        btnGroupSource.add(rdbSourcePRIDE);
        rdbSourcePRIDE.setText("PRIDE");
        rdbSourcePRIDE.setIconTextGap(10);
        rdbSourcePRIDE.setOpaque(false);
        rdbSourcePRIDE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourcePRIDEActionPerformed(evt);
            }
        });

        btnGroupStrategy.add(rdbVarMOD);
        rdbVarMOD.setSelected(true);
        rdbVarMOD.setText("Variable MODS");
        rdbVarMOD.setIconTextGap(10);
        rdbVarMOD.setOpaque(false);
        rdbVarMOD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbVarMODActionPerformed(evt);
            }
        });

        btnStart.setFont(btnStart.getFont().deriveFont(btnStart.getFont().getStyle() | java.awt.Font.BOLD));
        btnStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/image/start.png"))); // NOI18N
        btnStart.setText("Start");
        btnStart.setBorderPainted(false);
        btnStart.setFocusable(false);
        btnStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });

        btnGroupStrategy.add(rdbVarDB);
        rdbVarDB.setText("Variable DB");
        rdbVarDB.setIconTextGap(10);
        rdbVarDB.setOpaque(false);
        rdbVarDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbVarDBActionPerformed(evt);
            }
        });

        btnStop.setFont(btnStop.getFont().deriveFont(btnStop.getFont().getStyle() | java.awt.Font.BOLD));
        btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/image/exit.png"))); // NOI18N
        btnStop.setText("Stop");
        btnStop.setBorderPainted(false);
        btnStop.setFocusable(false);
        btnStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStopActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(mainPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(mainPanelLayout.createSequentialGroup()
                                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(rdbStraight)
                                                        .add(rdbVarMOD)
                                                        .add(rdbVarDB)
                                                        .add(jLabel2))
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 504, Short.MAX_VALUE)
                                                .add(btnStart)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(btnStop))
                                        .add(mainPanelLayout.createSequentialGroup()
                                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                                        .add(rdbSourceMSLIMS)
                                                        .add(rdbSourcePRIDE)
                                                        .add(jLabel3))
                                                .add(0, 0, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(mainPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(btnStart)
                                        .add(btnStop)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, mainPanelLayout.createSequentialGroup()
                                                .add(jLabel2)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                                .add(rdbVarMOD)
                                                .add(3, 3, 3)
                                                .add(rdbVarDB)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(rdbStraight)))
                                .add(18, 18, 18)
                                .add(jLabel3)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(rdbSourceMSLIMS)
                                .add(3, 3, 3)
                                .add(rdbSourcePRIDE)
                                .addContainerGap(105, Short.MAX_VALUE))
        );

        org.jdesktop.layout.GroupLayout jpanMainLayout = new org.jdesktop.layout.GroupLayout(jpanMain);
        jpanMain.setLayout(jpanMainLayout);
        jpanMainLayout.setHorizontalGroup(
                jpanMainLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jpanMainLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(mainPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
        jpanMainLayout.setVerticalGroup(
                jpanMainLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jpanMainLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(mainPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );

        tabCore.addTab("Main", jpanMain);

        tableTabPanel.setOpaque(false);

        tablePanel.setOpaque(false);

        tblProperties.setModel(new javax.swing.table.DefaultTableModel(
                new Object[][]{
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null},
                        {null, null, null, null}
                },
                new String[]{
                        "Title 1", "Title 2", "Title 3", "Title 4"
                }
        ));
        scrlTable.setViewportView(tblProperties);

        org.jdesktop.layout.GroupLayout tablePanelLayout = new org.jdesktop.layout.GroupLayout(tablePanel);
        tablePanel.setLayout(tablePanelLayout);
        tablePanelLayout.setHorizontalGroup(
                tablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(tablePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(scrlTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 805, Short.MAX_VALUE)
                                .addContainerGap())
        );
        tablePanelLayout.setVerticalGroup(
                tablePanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, tablePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(scrlTable, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE)
                                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout tableTabPanelLayout = new org.jdesktop.layout.GroupLayout(tableTabPanel);
        tableTabPanel.setLayout(tableTabPanelLayout);
        tableTabPanelLayout.setHorizontalGroup(
                tableTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(tableTabPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
        tableTabPanelLayout.setVerticalGroup(
                tableTabPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(tableTabPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(tablePanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );

        tabCore.addTab("Config", tableTabPanel);

        splitMain.setLeftComponent(tabCore);
        tabCore.getAccessibleContext().setAccessibleName("Main");

        org.jdesktop.layout.GroupLayout settingsPanelLayout = new org.jdesktop.layout.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
                settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(settingsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(splitMain)
                                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
                settingsPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, settingsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(splitMain, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 456, Short.MAX_VALUE)
                                .addContainerGap())
        );

        iconRelims.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/image/logo.png"))); // NOI18N

        iconCompomics.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/image/compomics.png"))); // NOI18N

        org.jdesktop.layout.GroupLayout backgroundPanelLayout = new org.jdesktop.layout.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
                backgroundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(backgroundPanelLayout.createSequentialGroup()
                                .add(28, 28, 28)
                                .add(iconRelims)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(iconCompomics)
                                .add(51, 51, 51))
                        .add(backgroundPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(settingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
                backgroundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(backgroundPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .add(backgroundPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                        .add(iconRelims)
                                        .add(org.jdesktop.layout.GroupLayout.TRAILING, backgroundPanelLayout.createSequentialGroup()
                                                .add(iconCompomics)
                                                .add(20, 20, 20)))
                                .add(settingsPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(12, 12, 12))
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(backgroundPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, backgroundPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rdbStraightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbStraightActionPerformed
        String s = RelimsProperties.getRelimsClassList()[2];
        iStrategySelectionModel.setSelectedItem(s);
        logger.debug(String.format("selected strategy %s", s));
    }//GEN-LAST:event_rdbStraightActionPerformed

    /**
     * Close the window and shut down the jvm.
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        dispose();
        System.exit(0); // @TODO: should we do more here? like check if a process is running etc?
    }//GEN-LAST:event_formWindowClosing

    private void rdbSourceMSLIMSActionPerformed(java.awt.event.ActionEvent evt) {
        String s = RelimsProperties.getRelimsSourceList()[0];
        iProjectSourceSelectionModel.setSelectedItem(s);
        logger.debug(String.format("selected source %s", s));
    }

    private void rdbSourcePRIDEActionPerformed(java.awt.event.ActionEvent evt) {
        String s = RelimsProperties.getRelimsSourceList()[1];
        iProjectSourceSelectionModel.setSelectedItem(s);
        logger.debug(String.format("selected source %s", s));
    }

    private void rdbVarMODActionPerformed(java.awt.event.ActionEvent evt) {
        String s = RelimsProperties.getRelimsClassList()[0];
        iStrategySelectionModel.setSelectedItem(s);
        logger.debug(String.format("selected strategy %s", s));
    }

    private void rdbVarDBActionPerformed(ActionEvent aEvt) {
        String s = RelimsProperties.getRelimsClassList()[1];
        iStrategySelectionModel.setSelectedItem(s);
        logger.debug(String.format("selected strategy %s", s));
    }

    private void btnStopActionPerformed(ActionEvent aEvt) {
        if (iRelimsJobStarter != null) {
            iRelimsJobStarter.stop();
        }
    }

    private void btnStartActionPerformed(ActionEvent aEvt) {
        iRelimsJobStarter = new RelimsJobStarter();
        RelimsProperties.logSettings();
        iRelimsJobStarter.start();
    }

    private class RelimsJobStarter implements Runnable {

        private Thread updateThread;

        public void run() {
            String lSearchStrategyID = iStrategySelectionModel.getSelectedItem().toString();
            String lProjectProviderID = iProjectSourceSelectionModel.getSelectedItem().toString();

            iRelimsJob = new RelimsJob(lSearchStrategyID, lProjectProviderID);
            Object lCall = iRelimsJob.call();
            logger.debug(lCall.toString());
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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        // set the look and feel
//        UtilitiesGUIDefaults.setLookAndFeel();

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new RelimsNBGUI();
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.ButtonGroup btnGroupSource;
    private javax.swing.ButtonGroup btnGroupStrategy;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JLabel iconCompomics;
    private javax.swing.JLabel iconRelims;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jpanMain;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JRadioButton rdbSourceMSLIMS;
    private javax.swing.JRadioButton rdbSourcePRIDE;
    private javax.swing.JRadioButton rdbStraight;
    private javax.swing.JRadioButton rdbVarDB;
    private javax.swing.JRadioButton rdbVarMOD;
    private javax.swing.JScrollPane scrlLogger;
    private javax.swing.JScrollPane scrlTable;
    private javax.swing.JPanel settingsPanel;
    private javax.swing.JSplitPane splitMain;
    private javax.swing.JTabbedPane tabCore;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JPanel tableTabPanel;
    private javax.swing.JTable tblProperties;
    private javax.swing.JTextArea txtLogger;
    // End of variables declaration//GEN-END:variables
}
