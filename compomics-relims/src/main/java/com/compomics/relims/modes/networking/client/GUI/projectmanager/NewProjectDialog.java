package com.compomics.relims.modes.networking.client.GUI.projectmanager;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.predicatemanager.MetaDataCollector;
import com.compomics.relims.manager.predicatemanager.ProjectPredicate;
import com.compomics.relims.manager.predicatemanager.ProjectPredicateManager;
import com.compomics.relims.modes.networking.client.RelimsClientMode;
import com.compomics.relims.modes.networking.client.connection.ServerConnector;
import com.compomics.relims.modes.networking.controller.taskobjects.TaskContainer;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.gui.UtilitiesGUIDefaults;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import org.apache.log4j.Logger;

/**
 * The SearchGUI settings dialog.
 *
 * @author Harald Barsnes
 */
public class NewProjectDialog extends javax.swing.JFrame {

    private DefaultListModel rightModel = new DefaultListModel();
    private DefaultListModel leftModel = new DefaultListModel();
    private File defaultFolderFile;
    private Logger logger = Logger.getLogger(NewProjectDialog.class);
    private javax.swing.JRadioButton selectedStrategy;
    private javax.swing.JRadioButton selectedSource;
    String currentUser = "admin";
    Boolean includeFinishedProjects = true;
    public static final String MODIFICATION_SEPARATOR = "//";
    public static SearchParameters loadedSearchParameters;
    private int port = 0;
    private String controllerIP = null;
    private JDialog filterDialog;

    public NewProjectDialog(String currentUser, String IP, int port, String source) {
        boolean numbusLookAndFeelSet = false;
        try {
            numbusLookAndFeelSet = UtilitiesGUIDefaults.setLookAndFeel();
        } catch (Exception e) {
        }

        this.port = RelimsProperties.getControllerPort();
        this.controllerIP = RelimsProperties.getControllerIP();
        currentUser = currentUser;
        initComponents();
        setLocationRelativeTo(null);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SourceButtonGroup.add(rdbSourceMSLIMS1);
        SourceButtonGroup.add(rdbSourcePRIDE1);
        SourceButtonGroup.add(rdbSourceFile);

        rdbSourceMSLIMS1.setName("mslims");
        rdbSourcePRIDE1.setName("pride");

        selectedSource = rdbSourcePRIDE1;

        if (source.equals(
                "MSLIMS")) {
            rdbSourceMSLIMS1.setSelected(true);
            selectedSource = rdbSourceMSLIMS1;
        }

        if (source.equals(
                "PRIDE")) {
            rdbSourcePRIDE1.setSelected(true);
            selectedSource = rdbSourcePRIDE1;
        }

        if (source.equals("TEXTFILE")) {
//            rdbTextfile1.setSelected(true);
//            selectedSource = rdbTextfile1;
        }
//SET ENZYMES
        cobEnzyme.removeAllItems();
        for (String anEnzyme : MetaDataCollector.getEnzymes()) {
            cobEnzyme.addItem(anEnzyme);
        }
        cobEnzyme.setSelectedIndex(0);
//SET FASTA
        cobFasta.removeAllItems();
        File fastaRepo = RelimsProperties.getFastaRepository();
        for (File aFasta : fastaRepo.listFiles()) {
            if (aFasta.getAbsolutePath().toLowerCase().endsWith(".fasta")) {
                cobFasta.addItem(aFasta.getName());
            }
        }
        cobFasta.setSelectedIndex(0);

        formComponentResized(
                null);
        SwingUtilities.invokeLater(
                new Runnable() {
            @Override
            public void run() {
                setVisible(true);
            }
        });
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        modificationOptionsPopupMenu = new javax.swing.JPopupMenu();
        editModificationsMenuItem = new javax.swing.JMenuItem();
        StrategyButtonGroup = new javax.swing.ButtonGroup();
        SourceButtonGroup = new javax.swing.ButtonGroup();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        defaultSearchparamgroup = new javax.swing.ButtonGroup();
        jFormattedTextField1 = new javax.swing.JFormattedTextField();
        Importedlabels1 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ImportedList = new javax.swing.JList();
        toRight = new javax.swing.JButton();
        ScheduleAll = new javax.swing.JButton();
        CancelRight = new javax.swing.JButton();
        CancelAll = new javax.swing.JButton();
        toRunLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ScheduledList = new javax.swing.JList();
        jLabel7 = new javax.swing.JLabel();
        manualInputField = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        manualProjectNameField = new javax.swing.JTextField();
        ManualSchedule = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        cbShuffle = new javax.swing.JCheckBox();
        Importedlabels = new javax.swing.JLabel();
        Importedlabels2 = new javax.swing.JLabel();
        possibleImportsLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();
        taskContainerName = new javax.swing.JTextField();
        cobFasta = new javax.swing.JComboBox();
        jPanel2 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        rdbSourceMSLIMS1 = new javax.swing.JRadioButton();
        rdbSourcePRIDE1 = new javax.swing.JRadioButton();
        rdbSourceFile = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        cbMinMS2Count = new javax.swing.JCheckBox();
        cbMaxMS1Count = new javax.swing.JCheckBox();
        tfMinMS1Spectra = new javax.swing.JTextField();
        cbMinMS1Count = new javax.swing.JCheckBox();
        tfTaxonomyID = new javax.swing.JTextField();
        cbEnzyme = new javax.swing.JCheckBox();
        cbTaxonomy = new javax.swing.JCheckBox();
        tfMinMS2Spectra = new javax.swing.JTextField();
        tfMaxMS2Spectra = new javax.swing.JTextField();
        cbMaxMS2Count = new javax.swing.JCheckBox();
        tfMaxMS1Spectra = new javax.swing.JTextField();
        cobEnzyme = new javax.swing.JComboBox();
        ImportLists = new javax.swing.JButton();

        editModificationsMenuItem.setText("Edit Modifications");
        editModificationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModificationsMenuItemActionPerformed(evt);
            }
        });
        modificationOptionsPopupMenu.add(editModificationsMenuItem);

        jScrollPane3.setViewportView(jTextPane1);

        jFormattedTextField1.setText("jFormattedTextField1");

        Importedlabels1.setText("Available projects");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Respin - Automatic Reprocessing Pipeline");
        setBackground(new java.awt.Color(255, 255, 255));
        setFocusCycleRoot(false);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(790, 700));
        setPreferredSize(new java.awt.Dimension(790, 700));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Task Container"));
        jPanel3.setOpaque(false);

        jScrollPane1.setPreferredSize(new java.awt.Dimension(250, 250));

        ImportedList.setAutoscrolls(false);
        ImportedList.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        ImportedList.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        ImportedList.setDragEnabled(true);
        ImportedList.setLayoutOrientation(javax.swing.JList.VERTICAL_WRAP);
        ImportedList.setMaximumSize(new java.awt.Dimension(80, 200));
        ImportedList.setMinimumSize(new java.awt.Dimension(80, 200));
        ImportedList.setPreferredSize(null);
        ImportedList.setRequestFocusEnabled(false);
        ImportedList.setVerifyInputWhenFocusTarget(false);
        jScrollPane1.setViewportView(ImportedList);

        toRight.setText(">");
        toRight.setToolTipText("Add");
        toRight.setPreferredSize(null);
        toRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                toRightActionPerformed(evt);
            }
        });

        ScheduleAll.setText(">>");
        ScheduleAll.setPreferredSize(null);
        ScheduleAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ScheduleAllActionPerformed(evt);
            }
        });

        CancelRight.setText("<");
        CancelRight.setPreferredSize(null);
        CancelRight.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelRightActionPerformed(evt);
            }
        });

        CancelAll.setText("<<");
        CancelAll.setPreferredSize(null);
        CancelAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelAllActionPerformed(evt);
            }
        });

        toRunLabel.setText("Tasks in container");

        jScrollPane2.setPreferredSize(new java.awt.Dimension(250, 250));

        ScheduledList.setAutoscrolls(false);
        ScheduledList.setDragEnabled(true);
        ScheduledList.setDropMode(javax.swing.DropMode.ON);
        ScheduledList.setMaximumSize(new java.awt.Dimension(80, 200));
        ScheduledList.setMinimumSize(new java.awt.Dimension(80, 200));
        ScheduledList.setPreferredSize(null);
        jScrollPane2.setViewportView(ScheduledList);

        jLabel7.setText("ProjectID");

        manualInputField.setMaximumSize(new java.awt.Dimension(6, 20));
        manualInputField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualInputFieldActionPerformed(evt);
            }
        });

        jLabel8.setText("Description");

        manualProjectNameField.setMaximumSize(new java.awt.Dimension(6, 20));
        manualProjectNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualProjectNameFieldActionPerformed(evt);
            }
        });

        ManualSchedule.setText("Add to container");
        ManualSchedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ManualScheduleActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cbShuffle.setBackground(new java.awt.Color(255, 255, 255));
        cbShuffle.setText("Shuffle tasks before sending");
        cbShuffle.setIconTextGap(10);
        cbShuffle.setOpaque(false);
        cbShuffle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbShuffleActionPerformed(evt);
            }
        });

        Importedlabels.setText("Available projects");

        Importedlabels2.setText("Projects passed filters : ");

        possibleImportsLabel.setText("0");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(manualInputField, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(12, 12, 12)
                        .addComponent(jLabel8)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(manualProjectNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 299, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ManualSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(toRight, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(CancelRight, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(ScheduleAll, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(CancelAll, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(Importedlabels)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(Importedlabels2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(possibleImportsLabel)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(cbShuffle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(8, 8, 8))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(toRunLabel)
                                .addGap(0, 0, Short.MAX_VALUE))))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(manualInputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ManualSchedule)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8)
                    .addComponent(manualProjectNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(toRunLabel)
                    .addComponent(Importedlabels))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(toRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CancelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ScheduleAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CancelAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(okButton)
                    .addComponent(cbShuffle)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(Importedlabels2)
                        .addComponent(possibleImportsLabel)))
                .addContainerGap())
        );

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("General settings"));
        jPanel1.setOpaque(false);

        jLabel16.setText("Fasta ");

        jLabel15.setText("Resultfolder name");

        taskContainerName.setText("admin");
        taskContainerName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                taskContainerNameActionPerformed(evt);
            }
        });

        cobFasta.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cobFasta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cobFastaActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel15)
                    .addComponent(jLabel16))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(taskContainerName, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cobFasta, javax.swing.GroupLayout.PREFERRED_SIZE, 519, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(taskContainerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(cobFasta, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Import projects"));
        jPanel2.setOpaque(false);

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Source"));
        jPanel4.setOpaque(false);

        rdbSourceMSLIMS1.setText("MSLIMS");
        rdbSourceMSLIMS1.setIconTextGap(10);
        rdbSourceMSLIMS1.setOpaque(false);
        rdbSourceMSLIMS1.setPreferredSize(null);
        rdbSourceMSLIMS1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourceMSLIMS1ActionPerformed(evt);
            }
        });

        rdbSourcePRIDE1.setSelected(true);
        rdbSourcePRIDE1.setText("PRIDE");
        rdbSourcePRIDE1.setIconTextGap(10);
        rdbSourcePRIDE1.setOpaque(false);
        rdbSourcePRIDE1.setPreferredSize(null);
        rdbSourcePRIDE1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourcePRIDE1ActionPerformed(evt);
            }
        });

        rdbSourceFile.setText("FILE");
        rdbSourceFile.setIconTextGap(10);
        rdbSourceFile.setOpaque(false);
        rdbSourceFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourceFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdbSourcePRIDE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdbSourceMSLIMS1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdbSourceFile))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(rdbSourcePRIDE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdbSourceMSLIMS1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rdbSourceFile)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder("Project Filters"));
        jPanel6.setOpaque(false);

        cbMinMS2Count.setBackground(new java.awt.Color(255, 255, 255));
        cbMinMS2Count.setText("Min # MS2 spectra ");
        cbMinMS2Count.setOpaque(false);
        cbMinMS2Count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMinMS2CountActionPerformed(evt);
            }
        });

        cbMaxMS1Count.setBackground(new java.awt.Color(255, 255, 255));
        cbMaxMS1Count.setText("Max # MS1 spectra ");
        cbMaxMS1Count.setOpaque(false);
        cbMaxMS1Count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMaxMS1CountActionPerformed(evt);
            }
        });

        tfMinMS1Spectra.setText("100");

        cbMinMS1Count.setBackground(new java.awt.Color(255, 255, 255));
        cbMinMS1Count.setText("Min # MS1 spectra ");
        cbMinMS1Count.setOpaque(false);
        cbMinMS1Count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMinMS1CountActionPerformed(evt);
            }
        });

        tfTaxonomyID.setText("9606");

        cbEnzyme.setBackground(new java.awt.Color(255, 255, 255));
        cbEnzyme.setSelected(true);
        cbEnzyme.setText("Enzyme");
        cbEnzyme.setOpaque(false);
        cbEnzyme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEnzymeActionPerformed(evt);
            }
        });

        cbTaxonomy.setBackground(new java.awt.Color(255, 255, 255));
        cbTaxonomy.setSelected(true);
        cbTaxonomy.setText("Taxonomy");
        cbTaxonomy.setOpaque(false);
        cbTaxonomy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTaxonomyActionPerformed(evt);
            }
        });

        tfMinMS2Spectra.setText("100");

        tfMaxMS2Spectra.setText("1000");

        cbMaxMS2Count.setBackground(new java.awt.Color(255, 255, 255));
        cbMaxMS2Count.setText("Max # MS2 spectra ");
        cbMaxMS2Count.setOpaque(false);
        cbMaxMS2Count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMaxMS2CountActionPerformed(evt);
            }
        });

        tfMaxMS1Spectra.setText("1000");

        cobEnzyme.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cobEnzyme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cobEnzymeActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbMinMS1Count)
                    .addComponent(cbTaxonomy)
                    .addComponent(cbMaxMS1Count))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfMaxMS1Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfMinMS1Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfTaxonomyID, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbMaxMS2Count)
                    .addComponent(cbEnzyme)
                    .addComponent(cbMinMS2Count))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tfMaxMS2Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cobEnzyme, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tfMinMS2Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(69, Short.MAX_VALUE))
        );

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cobEnzyme, tfMaxMS1Spectra, tfMaxMS2Spectra, tfMinMS1Spectra, tfMinMS2Spectra, tfTaxonomyID});

        jPanel6Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cbEnzyme, cbMaxMS1Count, cbMaxMS2Count, cbMinMS1Count, cbMinMS2Count, cbTaxonomy});

        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfTaxonomyID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbEnzyme)
                    .addComponent(cobEnzyme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTaxonomy))
                .addGap(3, 3, 3)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfMaxMS1Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbMaxMS2Count)
                    .addComponent(tfMaxMS2Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbMaxMS1Count))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tfMinMS1Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbMinMS2Count)
                    .addComponent(tfMinMS2Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbMinMS1Count))
                .addContainerGap(27, Short.MAX_VALUE))
        );

        ImportLists.setText("Import");
        ImportLists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportListsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(ImportLists, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(20, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(ImportLists))
                    .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Open the ModificationsDialog.
     *
     * @param evt
     */
    private void editModificationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModificationsMenuItemActionPerformed
    }//GEN-LAST:event_editModificationsMenuItemActionPerformed

    /**
     * Resize the layered panes.
     *
     * @param evt
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
    }//GEN-LAST:event_formComponentResized

    private void CancelAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelAllActionPerformed
        //ProjectListEntry[] selectedItems = new ProjectListEntry[rightModel.size()];

        // rightModel.copyInto(selectedItems);

        //  for (ProjectListEntry item : selectedItems) {
        //      leftModel.addElement(item);
        //  }

        rightModel = new DefaultListModel();

        // ImportedList.setModel(leftModel);
        ScheduledList.setModel(rightModel);
    }//GEN-LAST:event_CancelAllActionPerformed

    private void CancelRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelRightActionPerformed
        int index = ScheduledList.getSelectedIndex();
        if (index > -1) {
            // leftModel.addElement((ProjectListEntry) ScheduledList.getSelectedValue());
            rightModel.removeElementAt(index);
        }
        //ImportedList.setModel(leftModel);
        ScheduledList.setModel(rightModel);
    }//GEN-LAST:event_CancelRightActionPerformed

    private void ScheduleAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ScheduleAllActionPerformed

        ProjectListEntry[] selectedItems = new ProjectListEntry[leftModel.size()];

        leftModel.copyInto(selectedItems);

        for (ProjectListEntry item : selectedItems) {
            rightModel.addElement(item);
        }

        //    leftModel = new DefaultListModel();

        //   ImportedList.setModel(leftModel);
        ScheduledList.setModel(rightModel);
    }//GEN-LAST:event_ScheduleAllActionPerformed

    private void toRightActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_toRightActionPerformed

        List index = ImportedList.getSelectedValuesList();
        if (!index.isEmpty()) {
            for (Object anEntry : index) {
                rightModel.addElement((ProjectListEntry) anEntry);
            }
        }
        ScheduledList.setModel(rightModel);
    }//GEN-LAST:event_toRightActionPerformed

//GEN-FIRST:event_okButtonActionPerformed
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {
        if (rightModel.isEmpty()) {
            logger.error("Not allowed to send blank statements...");
        } else {
            //setting up TaskObject
            TaskContainer tasksForServer = new TaskContainer();
            tasksForServer.setName(taskContainerName.getText());
            tasksForServer.setSourceID(selectedSource.getName());
            tasksForServer.setFasta(String.valueOf(cobFasta.getSelectedItem()));
            tasksForServer.enablePipeline();


            boolean allowToPass = true;

            for (int i = 0; i < rightModel.getSize(); i++) {
                ProjectListEntry aProject = (ProjectListEntry) rightModel.elementAt(i);
                tasksForServer.addJob(aProject.getProjectIDAsString(), aProject.getProjectTitle());
                if (aProject.getProjectIDAsString().length() > 50) {
                    allowToPass = false;
                    logger.error("The input for this projectID was too long : " + rightModel.elementAt(i).toString() + " ...");
                    break;
                }
            }

            if (allowToPass) {
                ServerConnector connector = new ServerConnector();
                connector.setConnectionParameters(controllerIP, port);
                try {
                    if (cbShuffle.isSelected()) {
                        tasksForServer.shuffleTasks();
                    }
                    Map<Long, Long> generatedTaskIDs = connector.SendToServer(tasksForServer);
                    logger.debug("Recieved TaskIDs from controlserver...");
                    connector.resetConnectionParameters();
                    JOptionPane.showMessageDialog(this, "Tasks were send succesfully");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    connector.resetConnectionParameters();
                    JOptionPane.showMessageDialog(this, "An error has occurred while transmitting tasks...");
                }
            } else {
                JOptionPane.showMessageDialog(this, "An error has occurred while transmitting tasks...");
                logger.error("Transmitting of projectID's cancelled...");
            }

        }

    }

//GEN-LAST:event_okButtonActionPerformed

    private void cbEnzymeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbEnzymeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbEnzymeActionPerformed

    private void cbTaxonomyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbTaxonomyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbTaxonomyActionPerformed

    private void cbMaxMS2CountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMaxMS2CountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbMaxMS2CountActionPerformed

    private void cbMaxMS1CountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMaxMS1CountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbMaxMS1CountActionPerformed

    private void ManualScheduleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ManualScheduleActionPerformed

        if (!rightModel.contains(manualInputField.getText()) && !manualInputField.getText().isEmpty() && !manualProjectNameField.getText().isEmpty()) {
            if (manualInputField.getText().length() > 50) {
                logger.error("Input is too long : " + manualInputField.getText());
            } else {
                //element can't be put double
                ProjectListEntry customListItem = new ProjectListEntry(manualInputField.getText(), manualProjectNameField.getText());
                rightModel.addElement(customListItem);
                ScheduledList.setModel(rightModel);
            }
        } else {
            logger.error("This project has already been scheduled for these tasks...");
        }
    }//GEN-LAST:event_ManualScheduleActionPerformed

    private void manualInputFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualInputFieldActionPerformed
    }//GEN-LAST:event_manualInputFieldActionPerformed

    private void manualProjectNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualProjectNameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_manualProjectNameFieldActionPerformed

    private void rdbSourceMSLIMS1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbSourceMSLIMS1ActionPerformed
        selectedSource = rdbSourceMSLIMS1;
        selectedSource.setName("mslims");
    }//GEN-LAST:event_rdbSourceMSLIMS1ActionPerformed

    private void rdbSourcePRIDE1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbSourcePRIDE1ActionPerformed
        selectedSource = rdbSourcePRIDE1;
        selectedSource.setName("pride");
    }//GEN-LAST:event_rdbSourcePRIDE1ActionPerformed

    private void rdbSourceFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbSourceFileActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_rdbSourceFileActionPerformed

    private void cbMinMS1CountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMinMS1CountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbMinMS1CountActionPerformed

    private void cbMinMS2CountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbMinMS2CountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbMinMS2CountActionPerformed

    private void cbShuffleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbShuffleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbShuffleActionPerformed

    private void taskContainerNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_taskContainerNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_taskContainerNameActionPerformed

    private void cobEnzymeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cobEnzymeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cobEnzymeActionPerformed

    private void cobFastaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cobFastaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cobFastaActionPerformed

    private void ImportListsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportListsActionPerformed

        if (rdbSourceFile.isSelected()) {
            getTextFileProjects();
        } else {

            StringBuilder query = new StringBuilder().append("Select * from pridemeta where ");
            List<String> filters = new ArrayList<String>();
            leftModel.clear();
            ImportedList.setModel(leftModel);
            if (cbMaxMS1Count.isSelected()) {
                ProjectPredicate.MS1MAX.enable();
                ProjectPredicate.setMaxMS1Count(tfMaxMS1Spectra.getText());
                filters.add("MS1 < " + "'" + tfMaxMS1Spectra.getText() + "'");
            }

            if (cbMaxMS2Count.isSelected()) {
                ProjectPredicate.MS2MAX.enable();
                ProjectPredicate.setMaxMS1Count(tfMaxMS2Spectra.getText());
                filters.add("MS2 < " + "'" + tfMaxMS2Spectra.getText() + "'");
            }

            if (cbMinMS1Count.isSelected()) {
                ProjectPredicate.MS1MIN.enable();
                ProjectPredicate.setMinMS1Count(tfMinMS1Spectra.getText());
                filters.add("MS1 > " + "'" + tfMinMS1Spectra.getText() + "'");
            }

            if (cbMinMS2Count.isSelected()) {
                ProjectPredicate.MS2MIN.enable();
                ProjectPredicate.setMinMS1Count(tfMinMS2Spectra.getText());
                filters.add("MS2 > " + "'" + tfMinMS2Spectra.getText() + "'");
            }

            if (cbEnzyme.isSelected()) {
                ProjectPredicate.ENZYME.enable();
                ProjectPredicate.setEnzyme(cobEnzyme.getSelectedItem().toString());
                filters.add("enzyme = '" + cobEnzyme.getSelectedItem().toString() + "'");
            }

            if (cbTaxonomy.isSelected()) {
                ProjectPredicate.TAXONOMYID.enable();
                ProjectPredicate.setTaxonomyID(tfTaxonomyID.getText().toString());
                filters.add("taxid = '" + tfTaxonomyID.getText().toString() + "'");
            }

            for (String aFilterQuery : filters) {
                query.append(aFilterQuery + " AND ");
            }
            query.replace(query.lastIndexOf(" AND "), query.length(), "");


            try {
                LinkedHashMap<String, String> availableProjects = MetaDataCollector.getProjects(query.toString());
                for (String aProjectID : availableProjects.keySet()) {
                    ProjectListEntry entry = new ProjectListEntry(aProjectID, availableProjects.get(aProjectID));
                    leftModel.addElement(entry);

                    ImportedList.setVisibleRowCount(leftModel.getSize());
                }
            } catch (SQLException ex) {
                logger.error(ex);
            } finally {
                ImportedList.setModel(leftModel);
                possibleImportsLabel.setText(String.valueOf(leftModel.getSize()));
            }
            /*
             //Set predicates from filters...

             if (cbMaxMS1Count.isSelected()) {
             ProjectPredicate.MS1MAX.enable();
             ProjectPredicate.setMaxMS1Count(tfMaxMS1Spectra.getText());
             }

             if (cbMaxMS2Count.isSelected()) {
             ProjectPredicate.MS2MAX.enable();
             ProjectPredicate.setMaxMS1Count(tfMaxMS2Spectra.getText());
             }

             if (cbMinMS1Count.isSelected()) {
             ProjectPredicate.MS1MIN.enable();
             ProjectPredicate.setMinMS1Count(tfMinMS1Spectra.getText());
             }

             if (cbMinMS2Count.isSelected()) {
             ProjectPredicate.MS2MIN.enable();
             ProjectPredicate.setMinMS1Count(tfMinMS2Spectra.getText());
             }

             if (cbEnzyme.isSelected()) {
             ProjectPredicate.ENZYME.enable();
             ProjectPredicate.setEnzyme(spEnzyme.getValue().toString());
             }

             if (cbTaxonomy.isSelected()) {
             ProjectPredicate.TAXONOMYID.enable();
             ProjectPredicate.setTaxonomyID(tfTaxonomyID.getText().toString());
             }
             SwingWorker worker = new SwingWorker() {
             Thread waitingThread;

             @Override
             protected void done() {
             filterDialog.dispose();
             if (waitingThread.isAlive()) {
             waitingThread.interrupt();
             }
             }

             @Override
             protected Object doInBackground() throws Exception {
             //set filterdialog
             filterDialog = new JDialog();
             filterDialog.setLocationRelativeTo(null);
             filterDialog.setTitle("Please Wait...");
             filterDialog.setBackground(Color.GRAY);
             filterDialog.add(new JLabel("Filtering ... ", JLabel.CENTER));
             filterDialog.validate();
             filterDialog.setSize(300, 150);
             filterDialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
             filterDialog.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
             filterDialog.setAlwaysOnTop(true);
             filterDialog.setResizable(false);
             filterDialog.pack();
             filterDialog.addWindowListener(new java.awt.event.WindowAdapter() {
             });

             waitingThread = new Thread(new Runnable() {
             @Override
             public void run() {
             filterDialog.setVisible(true);
             }
             });
             waitingThread.start();
             if (rdbSourceMSLIMS1.isSelected()) {
             // make a connection to the MSLIMS database and retrieve the possible projects
             getMSLIMSProjects();
             }

             if (rdbSourcePRIDE1.isSelected()) {
             getPrideProjects();
             }

             if (rdbSourceFile.isSelected()) {
             getTextFileProjects();
             }
             return null;
             }
             };
             worker.execute();
             */
        }
    }//GEN-LAST:event_ImportListsActionPerformed

    private class ProjectListEntry implements Comparable {

        int projectID = 0;
        String projectTitle = null;

        private ProjectListEntry(String projectIDAsString, String projectTitle) {
            this.projectID = Integer.parseInt(projectIDAsString);
            this.projectTitle = projectTitle;
        }

        private ProjectListEntry(int projectID, String projectTitle) {
            this.projectID = projectID;
            this.projectTitle = projectTitle;
        }

        public int getProjectID() {
            return this.projectID;
        }

        public String getProjectIDAsString() {
            return "" + this.projectID;
        }

        @Override
        public String toString() {
            return projectID + "  -  " + projectTitle;
        }

        private String getProjectTitle() {
            return projectTitle;
        }

        @Override
        public boolean equals(Object aThat) {
            //check for self-comparison
            if (this == aThat) {
                return true;
            }

            if (!(aThat instanceof ProjectListEntry)) {
                return false;
            }
            //cast to native object is now safe
            ProjectListEntry that = (ProjectListEntry) aThat;
            return that.getProjectID() == this.getProjectID();
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + this.projectID;
            return hash;
        }

        @Override
        public int compareTo(Object o) {
            int balance = 0;
            ProjectListEntry that = (ProjectListEntry) o;
            if (that.getProjectID() > this.getProjectID()) {
                balance = 1;
            } else if (that.getProjectID() == this.getProjectID()) {
                balance = 0;
            } else if (that.getProjectID() < this.getProjectID()) {
                balance = -1;
            }
            return balance;
        }
    }

    private class RelimsClientJobStarter implements Runnable {

        private Thread updateThread;

        @Override
        public void run() {
            String selectedStrategyName = selectedStrategy.getName();
            String selectedSourceName = selectedSource.getName();
            String[] settings = new String[]{selectedStrategyName, selectedSourceName};
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

    private void getTextFileProjects() {

        try {
            String path = RelimsClientMode.class
                    .getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, "UTF-8");
            defaultFolderFile = new File(decodedPath);
        } catch (UnsupportedEncodingException ex) {
        }
        FileChooser fileopen = new FileChooser();
        fileopen.setFileFilter(null);
        fileopen.setCurrentDirectory(defaultFolderFile);
        // FileFilter filter = new FileNameExtensionFilter(".txt", ".TXT");
        // fileopen.addChoosableFileFilter(filter);
        fileopen.addChoosableFileFilter(new TextFilter());
        int ret = fileopen.showDialog(null, "Open file");

        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileopen.getSelectedFile();
            //Place reading code here...
            try {
                // Open the file that is the first
                // command line parameter
                FileInputStream fstream = new FileInputStream(file.getAbsoluteFile());
                // Get the object of DataInputStream
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                String strLine;

                //Read line by line
                leftModel = new DefaultListModel();
                while ((strLine = br.readLine()) != null) {
                    //add the item to the modellist
                    if (!leftModel.contains(strLine)) {
                        leftModel.addElement(new ProjectListEntry(strLine.toString(), "Project confirmed in Repository"));
                    }
                }
                ImportedList.setVisibleRowCount(leftModel.getSize());
                ImportedList.setModel(leftModel);
                ScheduledList.setModel(rightModel);
                ImportedList.setVisibleRowCount(leftModel.getSize());
                ImportedList.updateUI();
                //Close the input stream
                br.close();
                logger.debug("Finished importing projects from text file...");
            } catch (Exception e) {//Catch exception if any
                System.err.println("Error: " + e.getMessage());
            }

        }
    }

    private void getMSLIMSProjects() {

        int aProjectID;
        String aProjectTitle;
        String dbUrl = "jdbc:mysql://muppet03.ugent.be/projects";
        String dbUser = "kenneth";
        String dbPass = "kenneth,13*";
        String dbClass = "com.mysql.jdbc.Driver";
        String query = "Select projectID,title from project";

        try {
            Class.forName("com.mysql.jdbc.Driver");
            try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                java.sql.Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(query);
                leftModel = new DefaultListModel();
                while (rs.next()) {
                    aProjectID = rs.getInt("ProjectID");
                    aProjectTitle = rs.getString("Title");
                    if (ProjectPredicateManager.evaluatePredicates(aProjectID)) {
                        leftModel.addElement(new ProjectListEntry(aProjectID, aProjectTitle));
                    }
                } //end while
                // Set the new model ...
                ImportedList.setVisibleRowCount(leftModel.getSize() + 2);
                ImportedList.setModel(leftModel);

                try {
                    con.close();
                } catch (SQLException sqlex) {
                }
            }
        } //end try
        catch (ClassNotFoundException | SQLException e) {
            System.out.println("Could not retrieve data from the online database...");
        } finally {
        }

    }  //end main

    private void getPrideProjects() {
//TODO finish this connectivity 
        int aProjectID;
        String aProjectTitle;

        String dbUrl = "jdbc:mysql://193.62.194.210:5000/pride_2";
        String dbUser = "inspector";
        String dbPass = "inspector";
        String dbClass = "com.mysql.jdbc.Driver";
        //String query = "Select experiment_id,short_label from pride_experiment";
        // query = "select table_name, column_name from information_schema.columns";

        //9606 = HUMAN ! 

        String query = "select exp.accession as accession, exp.title as title "
                + "from pride_experiment exp order by exp.accession+0 asc";
        try {
            Class.forName(dbClass);
            try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                java.sql.Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery(query);
                leftModel = new DefaultListModel();
                while (rs.next()) {
                    aProjectID = rs.getInt("accession");
                    if (ProjectPredicateManager.evaluatePredicates(aProjectID)) {
                        aProjectTitle = rs.getString("title");
                        ProjectListEntry entry = new ProjectListEntry(aProjectID, aProjectTitle);
                        if (!leftModel.contains(entry)) {
                            leftModel.addElement(entry);
                        }
                    }
                }
                // Set the new model ...
                ImportedList.setVisibleRowCount(leftModel.getSize() + 2);
                ImportedList.setModel(leftModel);

                try {
                    con.close();
                } catch (SQLException sqlex) {
                    logger.error("Could not communicate with pride-database. Reason : ");
                    logger.error(sqlex);
                }
            }
        } //end try
        catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            System.out.println("Could not retrieve data from the online database...");
        } finally {
        }

    }  //end main
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelAll;
    private javax.swing.JButton CancelRight;
    private javax.swing.JButton ImportLists;
    private javax.swing.JList ImportedList;
    private javax.swing.JLabel Importedlabels;
    private javax.swing.JLabel Importedlabels1;
    private javax.swing.JLabel Importedlabels2;
    private javax.swing.JButton ManualSchedule;
    private javax.swing.JButton ScheduleAll;
    private javax.swing.JList ScheduledList;
    private javax.swing.ButtonGroup SourceButtonGroup;
    private javax.swing.ButtonGroup StrategyButtonGroup;
    private javax.swing.JCheckBox cbEnzyme;
    private javax.swing.JCheckBox cbMaxMS1Count;
    private javax.swing.JCheckBox cbMaxMS2Count;
    private javax.swing.JCheckBox cbMinMS1Count;
    private javax.swing.JCheckBox cbMinMS2Count;
    private javax.swing.JCheckBox cbShuffle;
    private javax.swing.JCheckBox cbTaxonomy;
    private javax.swing.JComboBox cobEnzyme;
    private javax.swing.JComboBox cobFasta;
    private javax.swing.ButtonGroup defaultSearchparamgroup;
    private javax.swing.JMenuItem editModificationsMenuItem;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextField manualInputField;
    private javax.swing.JTextField manualProjectNameField;
    private javax.swing.JPopupMenu modificationOptionsPopupMenu;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel possibleImportsLabel;
    private javax.swing.JRadioButton rdbSourceFile;
    private javax.swing.JRadioButton rdbSourceMSLIMS1;
    private javax.swing.JRadioButton rdbSourcePRIDE1;
    private javax.swing.JTextField taskContainerName;
    private javax.swing.JTextField tfMaxMS1Spectra;
    private javax.swing.JTextField tfMaxMS2Spectra;
    private javax.swing.JTextField tfMinMS1Spectra;
    private javax.swing.JTextField tfMinMS2Spectra;
    private javax.swing.JTextField tfTaxonomyID;
    private javax.swing.JButton toRight;
    private javax.swing.JLabel toRunLabel;
    // End of variables declaration//GEN-END:variables
}
