package com.compomics.relims.modes.networking.client.GUI.projectmanager;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.manager.predicatemanager.ProjectPredicate;
import com.compomics.relims.manager.predicatemanager.ProjectPredicateManager;
import com.compomics.relims.modes.networking.client.RelimsClientMode;
import com.compomics.relims.modes.networking.client.connectivity.connectors.ServerConnector;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.TaskContainer;
import com.compomics.util.experiment.identification.SearchParameters;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
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
import java.util.List;
import java.util.Map;
import javax.swing.*;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;
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
    private static RelimsClientJobStarter iRelimsClientJobStarter;
    private SearchParameters searchParameters;
    private javax.swing.JRadioButton selectedStrategy;
    private javax.swing.JRadioButton selectedSource;
    String currentUser = "admin";
    Boolean includeFinishedProjects = true;
    public static final String MODIFICATION_SEPARATOR = "//";
    public static SearchParameters loadedSearchParameters;
    private int port = 0;
    private String controllerIP = null;
    private JDialog filterDialog;

    public NewProjectDialog(String currentUser, String IP, int port, String source, String strategy) {
        this.port = RelimsProperties.getControllerPort();
        this.controllerIP = RelimsProperties.getControllerIP();
        currentUser = currentUser;
        initComponents();
        setLocationRelativeTo(null);
        Toolkit t = Toolkit.getDefaultToolkit();
        Dimension d = t.getScreenSize();
        this.setSize(d.width / 2, 800);
        getContentPane().setBackground(Color.WHITE);
        this.setLayout(new BorderLayout());
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new FlowLayout());
        //  this.setSize(getMaximumSize().width, getMaximumSize().height);
        this.setTitle("New Taskcontainer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        selectedSource = rdbSourcePRIDE1;
        selectedSource.setName("pride");
        StrategyButtonGroup.add(rdbVarDB1);
        StrategyButtonGroup.add(rdbVarMOD1);
        StrategyButtonGroup.add(rdbStraight1);

        SourceButtonGroup.add(rdbSourceMSLIMS1);
        SourceButtonGroup.add(rdbSourcePRIDE1);
        SourceButtonGroup.add(rdbSourceFile);

        rdbVarDB1.setName("dbvar");
        rdbVarMOD1.setName("modvar");
        rdbStraight1.setName("straight");

        rdbSourceMSLIMS1.setName("mslims");
        rdbSourcePRIDE1.setName("pride");

        /*        prioritySlider.setMinorTickSpacing(1);
         prioritySlider.setMajorTickSpacing(1);
         prioritySlider.setPaintTicks(true);
         */
        selectedStrategy = rdbVarMOD1;
        selectedSource = rdbSourcePRIDE1;

        if (strategy.equals(
                "STRAIGHT")) {
            rdbStraight1.setSelected(true);
            selectedStrategy = rdbStraight1;
        }

        if (strategy.equals(
                "VARDB")) {
            rdbVarDB1.setSelected(true);
            selectedStrategy = rdbVarDB1;
        }

        if (strategy.equals(
                "VARMOD")) {
            rdbVarMOD1.setSelected(true);
            selectedStrategy = rdbVarMOD1;
        }

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
        List<String> availableEnzymes = new ArrayList<String>();
        availableEnzymes.add("Trypsin");
        availableEnzymes.add("Cofradic");
        SpinnerListModel enzymeSpinnerModel = new SpinnerListModel();
        enzymeSpinnerModel.setList(availableEnzymes);
        spEnzyme.setModel(enzymeSpinnerModel);
        ProjectPredicateManager.initialize();

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
        TitleLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        Importedlabels = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        ImportedList = new javax.swing.JList();
        toRight = new javax.swing.JButton();
        ScheduleAll = new javax.swing.JButton();
        CancelRight = new javax.swing.JButton();
        CancelAll = new javax.swing.JButton();
        toRunLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ScheduledList = new javax.swing.JList();
        jToggleButton1 = new javax.swing.JToggleButton();
        okButton = new javax.swing.JButton();
        cbShuffle = new javax.swing.JCheckBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        rdbVarDB1 = new javax.swing.JRadioButton();
        rdbVarMOD1 = new javax.swing.JRadioButton();
        rdbStraight1 = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        taskContainerName = new javax.swing.JTextField();
        ManualSchedule = new javax.swing.JButton();
        manualProjectNameField = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        manualInputField = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        rdbSourcePRIDE1 = new javax.swing.JRadioButton();
        rdbSourceMSLIMS1 = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        ImportLists = new javax.swing.JButton();
        spEnzyme = new javax.swing.JSpinner();
        cbEnzyme = new javax.swing.JCheckBox();
        cbTaxonomy = new javax.swing.JCheckBox();
        tfTaxonomyID = new javax.swing.JTextField();
        tfMaxMS2Spectra = new javax.swing.JTextField();
        cbMaxMS2Count = new javax.swing.JCheckBox();
        cbMaxMS1Count = new javax.swing.JCheckBox();
        tfMaxMS1Spectra = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        rdbSourceFile = new javax.swing.JRadioButton();
        cbMinMS1Count = new javax.swing.JCheckBox();
        tfMinMS1Spectra = new javax.swing.JTextField();
        cbMinMS2Count = new javax.swing.JCheckBox();
        tfMinMS2Spectra = new javax.swing.JTextField();

        editModificationsMenuItem.setText("Edit Modifications");
        editModificationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModificationsMenuItemActionPerformed(evt);
            }
        });
        modificationOptionsPopupMenu.add(editModificationsMenuItem);

        jScrollPane3.setViewportView(jTextPane1);

        jFormattedTextField1.setText("jFormattedTextField1");

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New taskcontainer");
        setBackground(new java.awt.Color(255, 255, 255));
        setLocationByPlatform(true);
        setResizable(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        TitleLabel.setBackground(new java.awt.Color(255, 255, 255));
        TitleLabel.setFont(new java.awt.Font("Lucida Grande", 1, 36)); // NOI18N
        TitleLabel.setText("Colims Processing");

        jPanel3.setBackground(new java.awt.Color(255, 255, 255));
        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Task container", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        Importedlabels.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        Importedlabels.setText("Available projects");

        jScrollPane1.setPreferredSize(null);

        ImportedList.setAutoscrolls(false);
        ImportedList.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        ImportedList.setDebugGraphicsOptions(javax.swing.DebugGraphics.NONE_OPTION);
        ImportedList.setDragEnabled(true);
        ImportedList.setLayoutOrientation(javax.swing.JList.VERTICAL_WRAP);
        ImportedList.setMaximumSize(new java.awt.Dimension(80, 20000));
        ImportedList.setMinimumSize(new java.awt.Dimension(80, 200));
        ImportedList.setPreferredSize(null);
        ImportedList.setRequestFocusEnabled(false);
        ImportedList.setVerifyInputWhenFocusTarget(false);
        jScrollPane1.setViewportView(ImportedList);

        toRight.setText(">");
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

        toRunLabel.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        toRunLabel.setText("Tasks in container");

        ScheduledList.setAutoscrolls(false);
        ScheduledList.setDragEnabled(true);
        ScheduledList.setDropMode(javax.swing.DropMode.ON);
        ScheduledList.setMaximumSize(new java.awt.Dimension(80, 20000));
        ScheduledList.setMinimumSize(new java.awt.Dimension(80, 200));
        ScheduledList.setPreferredSize(null);
        jScrollPane2.setViewportView(ScheduledList);

        jToggleButton1.setText("dev");
        jToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jToggleButton1ActionPerformed(evt);
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
        cbShuffle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbShuffleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(Importedlabels))
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(toRunLabel))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(CancelAll, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ScheduleAll, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(CancelRight, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(toRight, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(cbShuffle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(toRunLabel)
                    .addComponent(Importedlabels)
                    .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(toRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(CancelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ScheduleAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(CancelAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbShuffle)
                            .addComponent(okButton))
                        .addGap(31, 31, 31))))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel14.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel14.setText("Searchstrategy");
        jLabel14.setPreferredSize(null);

        rdbVarDB1.setText("Variable DB");
        rdbVarDB1.setIconTextGap(10);
        rdbVarDB1.setOpaque(false);
        rdbVarDB1.setPreferredSize(null);
        rdbVarDB1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbVarDB1ActionPerformed(evt);
            }
        });

        rdbVarMOD1.setSelected(true);
        rdbVarMOD1.setText("Variable MODS");
        rdbVarMOD1.setIconTextGap(10);
        rdbVarMOD1.setName(""); // NOI18N
        rdbVarMOD1.setOpaque(false);
        rdbVarMOD1.setPreferredSize(null);
        rdbVarMOD1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbVarMOD1ActionPerformed(evt);
            }
        });

        rdbStraight1.setText("Straight");
        rdbStraight1.setIconTextGap(10);
        rdbStraight1.setOpaque(false);
        rdbStraight1.setPreferredSize(null);
        rdbStraight1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbStraight1ActionPerformed(evt);
            }
        });

        jLabel15.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel15.setText("TaskContainer name : ");

        taskContainerName.setText("admin");

        ManualSchedule.setText("Add");
        ManualSchedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ManualScheduleActionPerformed(evt);
            }
        });

        manualProjectNameField.setBorder(javax.swing.BorderFactory.createTitledBorder("Project name"));
        manualProjectNameField.setMaximumSize(new java.awt.Dimension(6, 20));
        manualProjectNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualProjectNameFieldActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel6.setText("Manual input : ");
        jLabel6.setPreferredSize(null);

        manualInputField.setBorder(javax.swing.BorderFactory.createTitledBorder("ProjectID"));
        manualInputField.setMaximumSize(new java.awt.Dimension(6, 20));
        manualInputField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualInputFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(74, 74, 74)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(rdbVarMOD1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(rdbStraight1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 107, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addGap(9, 9, 9)
                                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addComponent(manualProjectNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(18, 18, 18)
                                            .addComponent(manualInputField, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                    .addComponent(ManualSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(19, 19, 19))
                            .addComponent(rdbVarDB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addGap(35, 35, 35)
                        .addComponent(taskContainerName, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(taskContainerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdbVarDB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(5, 5, 5)
                        .addComponent(rdbVarMOD1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdbStraight1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(manualProjectNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(manualInputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(ManualSchedule)))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Taskcontainer settings", jPanel2);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

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

        rdbSourceMSLIMS1.setText("MSLIMS");
        rdbSourceMSLIMS1.setIconTextGap(10);
        rdbSourceMSLIMS1.setOpaque(false);
        rdbSourceMSLIMS1.setPreferredSize(null);
        rdbSourceMSLIMS1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourceMSLIMS1ActionPerformed(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel12.setText("Predicates");

        ImportLists.setText("Import projects");
        ImportLists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportListsActionPerformed(evt);
            }
        });

        cbEnzyme.setBackground(new java.awt.Color(255, 255, 255));
        cbEnzyme.setSelected(true);
        cbEnzyme.setText("Enzyme");
        cbEnzyme.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbEnzymeActionPerformed(evt);
            }
        });

        cbTaxonomy.setBackground(new java.awt.Color(255, 255, 255));
        cbTaxonomy.setSelected(true);
        cbTaxonomy.setText("Taxonomy");
        cbTaxonomy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbTaxonomyActionPerformed(evt);
            }
        });

        tfTaxonomyID.setText("9606");

        tfMaxMS2Spectra.setText("1000");

        cbMaxMS2Count.setBackground(new java.awt.Color(255, 255, 255));
        cbMaxMS2Count.setText("Max # MS2 spectra ");
        cbMaxMS2Count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMaxMS2CountActionPerformed(evt);
            }
        });

        cbMaxMS1Count.setBackground(new java.awt.Color(255, 255, 255));
        cbMaxMS1Count.setText("Max # MS1 spectra ");
        cbMaxMS1Count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMaxMS1CountActionPerformed(evt);
            }
        });

        tfMaxMS1Spectra.setText("1000");

        jLabel13.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel13.setText("Source");
        jLabel13.setPreferredSize(null);

        rdbSourceFile.setText("FILE");
        rdbSourceFile.setIconTextGap(10);
        rdbSourceFile.setOpaque(false);
        rdbSourceFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourceFileActionPerformed(evt);
            }
        });

        cbMinMS1Count.setBackground(new java.awt.Color(255, 255, 255));
        cbMinMS1Count.setText("Min # MS1 spectra ");
        cbMinMS1Count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMinMS1CountActionPerformed(evt);
            }
        });

        tfMinMS1Spectra.setText("100");

        cbMinMS2Count.setBackground(new java.awt.Color(255, 255, 255));
        cbMinMS2Count.setText("Min # MS2 spectra ");
        cbMinMS2Count.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbMinMS2CountActionPerformed(evt);
            }
        });

        tfMinMS2Spectra.setText("100");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(rdbSourcePRIDE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(rdbSourceMSLIMS1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdbSourceFile)))
                .addGap(42, 42, 42)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel12)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(cbMaxMS1Count)
                            .addComponent(cbMinMS1Count)
                            .addComponent(cbTaxonomy))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(tfMinMS1Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(cbMinMS2Count)
                                    .addGap(66, 66, 66))
                                .addComponent(ImportLists))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(tfMaxMS1Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(tfTaxonomyID, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(cbEnzyme)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spEnzyme))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(cbMaxMS2Count)
                                        .addGap(18, 18, 18)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(tfMinMS2Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(tfMaxMS2Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE))))))))
                .addGap(60, 60, 60))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel12)
                            .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbTaxonomy)
                            .addComponent(cbEnzyme)
                            .addComponent(tfTaxonomyID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(spEnzyme, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbMaxMS1Count)
                            .addComponent(tfMaxMS1Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(cbMaxMS2Count)
                            .addComponent(tfMaxMS2Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rdbSourcePRIDE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdbSourceMSLIMS1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdbSourceFile))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbMinMS1Count)
                    .addComponent(tfMinMS1Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbMinMS2Count)
                    .addComponent(tfMinMS2Spectra, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(ImportLists)
                .addGap(23, 23, 23))
        );

        jTabbedPane1.addTab("Project settings", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TitleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(TitleLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            Map<String, String> currentUserMap = new HashMap<String, String>();
            currentUserMap.put("username", taskContainerName.getText());
            currentUserMap.put("password", RelimsProperties.getPassword());
            //setting up TaskObject
            TaskContainer tasksForServer = new TaskContainer();
            tasksForServer.setInstructionMap(currentUserMap);
            tasksForServer.updateInstruction("instruction", "doTasks");
            tasksForServer.setStrategyID(selectedStrategy.getName());
            tasksForServer.setSourceID(selectedSource.getName());
            //read the parameters used
            try {
                File currentParameters = new File(RelimsProperties.getConfigFolder().getAbsolutePath() + "/default_parameters.parameters");
                loadedSearchParameters = SearchParameters.getIdentificationParameters(currentParameters);
            } catch (Exception e) {
                e.printStackTrace();
            }
            tasksForServer.setSearchParameters(loadedSearchParameters);

            tasksForServer.updateInstruction("runpipeline", "allow");


            //getting properties
            //TODO MOVE TO LOGIN
            //  String IP = IPfield.getText();
            //   String port = portField.getText();

            //making relimsJob object

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

    private void jToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton1ActionPerformed
        okButtonActionPerformed(evt);
    }//GEN-LAST:event_jToggleButton1ActionPerformed

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

    private void rdbStraight1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbStraight1ActionPerformed
        selectedStrategy = rdbStraight1;
    }//GEN-LAST:event_rdbStraight1ActionPerformed

    private void rdbVarMOD1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbVarMOD1ActionPerformed
        selectedStrategy = rdbVarMOD1;
    }//GEN-LAST:event_rdbVarMOD1ActionPerformed

    private void rdbVarDB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbVarDB1ActionPerformed
        selectedStrategy = rdbVarDB1;
    }//GEN-LAST:event_rdbVarDB1ActionPerformed

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

    private void ImportListsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportListsActionPerformed
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

    }//GEN-LAST:event_ImportListsActionPerformed

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

    private class ProjectListEntry {

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
    private javax.swing.JButton ManualSchedule;
    private javax.swing.JButton ScheduleAll;
    private javax.swing.JList ScheduledList;
    private javax.swing.ButtonGroup SourceButtonGroup;
    private javax.swing.ButtonGroup StrategyButtonGroup;
    private javax.swing.JLabel TitleLabel;
    private javax.swing.JCheckBox cbEnzyme;
    private javax.swing.JCheckBox cbMaxMS1Count;
    private javax.swing.JCheckBox cbMaxMS2Count;
    private javax.swing.JCheckBox cbMinMS1Count;
    private javax.swing.JCheckBox cbMinMS2Count;
    private javax.swing.JCheckBox cbShuffle;
    private javax.swing.JCheckBox cbTaxonomy;
    private javax.swing.ButtonGroup defaultSearchparamgroup;
    private javax.swing.JMenuItem editModificationsMenuItem;
    private javax.swing.JFormattedTextField jFormattedTextField1;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JTextField manualInputField;
    private javax.swing.JTextField manualProjectNameField;
    private javax.swing.JPopupMenu modificationOptionsPopupMenu;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton rdbSourceFile;
    private javax.swing.JRadioButton rdbSourceMSLIMS1;
    private javax.swing.JRadioButton rdbSourcePRIDE1;
    private javax.swing.JRadioButton rdbStraight1;
    private javax.swing.JRadioButton rdbVarDB1;
    private javax.swing.JRadioButton rdbVarMOD1;
    private javax.swing.JSpinner spEnzyme;
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
