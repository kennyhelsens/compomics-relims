package com.compomics.relims.modes.networking.client.GUI;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.client.RelimsClientMode;
import com.compomics.relims.modes.networking.client.connectivity.connectors.ServerConnector;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.TaskContainer;
import com.compomics.util.experiment.identification.SearchParameters;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.swing.*;
import org.apache.commons.io.FileUtils;
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

    public NewProjectDialog(String currentUser, String IP, int port, String source, String strategy) {
        this.port = RelimsProperties.getControllerPort();
        this.controllerIP = RelimsProperties.getControllerIP();
        currentUser = currentUser;
        initComponents();
        this.setTitle("New Taskcontainer");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        StrategyButtonGroup.add(rdbVarDB1);
        StrategyButtonGroup.add(rdbVarMOD1);
        StrategyButtonGroup.add(rdbStraight1);

        SourceButtonGroup.add(rdbSourceMSLIMS1);
        SourceButtonGroup.add(rdbSourcePRIDE1);

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

        if (source.equals(
                "TEXTFILE")) {
//            rdbTextfile1.setSelected(true);
//            selectedSource = rdbTextfile1;
        }



        formComponentResized(
                null);
        SwingUtilities.invokeLater(
                new Runnable() {
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
        backgroundPanel = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        rdbSourcePRIDE1 = new javax.swing.JRadioButton();
        rdbSourceMSLIMS1 = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        ImportLists = new javax.swing.JButton();
        ImportLists1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        manualProjectNameField = new javax.swing.JTextField();
        manualInputField = new javax.swing.JTextField();
        ManualSchedule = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        rdbVarDB1 = new javax.swing.JRadioButton();
        rdbVarMOD1 = new javax.swing.JRadioButton();
        rdbStraight1 = new javax.swing.JRadioButton();
        jLabel15 = new javax.swing.JLabel();
        taskContainerName = new javax.swing.JTextField();
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
        okButton = new javax.swing.JButton();

        editModificationsMenuItem.setText("Edit Modifications");
        editModificationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModificationsMenuItemActionPerformed(evt);
            }
        });
        modificationOptionsPopupMenu.add(editModificationsMenuItem);

        jScrollPane3.setViewportView(jTextPane1);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("New taskcontainer");
        setLocationByPlatform(true);
        setResizable(false);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(255, 255, 255));
        backgroundPanel.setRequestFocusEnabled(false);

        jLabel10.setBackground(new java.awt.Color(255, 255, 255));
        jLabel10.setFont(new java.awt.Font("Lucida Grande", 1, 36)); // NOI18N
        jLabel10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/compomics/relims/modes/networking/client/GUI/RelimsLogoSmall.png"))); // NOI18N
        jLabel10.setText(" Relims Automatic Reprocessing");

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Project source", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

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
        jLabel12.setText("Source");
        jLabel12.setPreferredSize(null);

        jLabel7.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel7.setText("Select Projects");
        jLabel7.setPreferredSize(null);

        ImportLists.setText("Import from MSLIMS-database");
        ImportLists.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportListsActionPerformed(evt);
            }
        });

        ImportLists1.setText("Import from a textfile");
        ImportLists1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ImportLists1ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel6.setText("Manual input : ");
        jLabel6.setPreferredSize(null);

        manualProjectNameField.setBorder(javax.swing.BorderFactory.createTitledBorder("Project name"));
        manualProjectNameField.setMaximumSize(new java.awt.Dimension(6, 20));
        manualProjectNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualProjectNameFieldActionPerformed(evt);
            }
        });

        manualInputField.setBorder(javax.swing.BorderFactory.createTitledBorder("ProjectID"));
        manualInputField.setMaximumSize(new java.awt.Dimension(6, 20));
        manualInputField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manualInputFieldActionPerformed(evt);
            }
        });

        ManualSchedule.setText("Add");
        ManualSchedule.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ManualScheduleActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(71, 71, 71))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(25, 25, 25)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(rdbSourcePRIDE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rdbSourceMSLIMS1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(162, 162, 162))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(ImportLists1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(ImportLists, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(manualProjectNameField, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(manualInputField, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(ManualSchedule, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ManualSchedule)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(rdbSourcePRIDE1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdbSourceMSLIMS1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(ImportLists))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ImportLists1)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(manualProjectNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(manualInputField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createTitledBorder("Search settings"), "Search settings", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

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

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(taskContainerName, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(49, 49, 49)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(rdbVarDB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdbStraight1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(rdbVarMOD1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 10, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel15)
                    .addComponent(taskContainerName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(rdbVarDB1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(rdbVarMOD1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(rdbStraight1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(35, 35, 35))
        );

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

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(toRight, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addComponent(ScheduleAll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addComponent(CancelAll, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addComponent(CancelRight, javax.swing.GroupLayout.PREFERRED_SIZE, 104, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(Importedlabels))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 333, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(toRunLabel)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(Importedlabels)
                    .addComponent(toRunLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 234, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(47, 47, 47)
                        .addComponent(toRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(ScheduleAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(CancelRight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(CancelAll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 17, Short.MAX_VALUE)
                .addComponent(okButton))
        );

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 47, Short.MAX_VALUE))
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(backgroundPanelLayout.createSequentialGroup()
                                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 347, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(10, 10, 10)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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

    private void manualProjectNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manualProjectNameFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_manualProjectNameFieldActionPerformed

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

    private void ImportListsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportListsActionPerformed

        if (rdbSourceMSLIMS1.isSelected()) {
            // make a connection to the MSLIMS database and retrieve the possible projects
            getMSLIMSProjects();
        }

        if (rdbSourcePRIDE1.isSelected()) {
            getPrideProjects();
        }

//        if (rdbTextfile1.isSelected()) {
        //
        //      }
    }//GEN-LAST:event_ImportListsActionPerformed

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

    private void rdbSourcePRIDE1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbSourcePRIDE1ActionPerformed
        selectedSource = rdbSourcePRIDE1;
        selectedSource.setName("pride");
        ImportLists.setText("Import projects from PRIDE-database");
    }//GEN-LAST:event_rdbSourcePRIDE1ActionPerformed

    private void rdbSourceMSLIMS1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbSourceMSLIMS1ActionPerformed
        selectedSource = rdbSourceMSLIMS1;
        selectedSource.setName("mslims");
        ImportLists.setText("Import projects from MSLIMS-database");
    }//GEN-LAST:event_rdbSourceMSLIMS1ActionPerformed

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

    private void rdbStraight1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbStraight1ActionPerformed
        selectedStrategy = rdbStraight1;
    }//GEN-LAST:event_rdbStraight1ActionPerformed

    private void rdbVarDB1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbVarDB1ActionPerformed
        selectedStrategy = rdbVarDB1;
    }//GEN-LAST:event_rdbVarDB1ActionPerformed

    private void rdbVarMOD1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdbVarMOD1ActionPerformed
        selectedStrategy = rdbVarMOD1;
    }//GEN-LAST:event_rdbVarMOD1ActionPerformed

    private void ImportLists1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ImportLists1ActionPerformed
        getTextFileProjects();
    }//GEN-LAST:event_ImportLists1ActionPerformed

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
                int rowsize = 0;
                while (rs.next()) {
                    rowsize++;
                    aProjectID = rs.getInt("ProjectID");
                    aProjectTitle = rs.getString("Title");
                    leftModel.addElement(new ProjectListEntry(aProjectID, aProjectTitle));
                } //end while
                // Set the new model ...
                ImportedList.setVisibleRowCount(rowsize + 2);
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
                + "from pride_experiment exp, mzdata_sample_param sample "
                + "where sample.parent_element_fk = exp.mz_data_id "
                + "and sample.accession = 9606 order by exp.accession+0 asc";


        try {
            Class.forName(dbClass);
            try (Connection con = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                java.sql.Statement stmt = con.createStatement();

                ResultSet rs = stmt.executeQuery(query);
                int rowsize = 0;
                leftModel = new DefaultListModel();
                while (rs.next()) {
                    rowsize++;
                    aProjectID = rs.getInt("accession");
                    //aProjectTitle = rs.getString("Title");
                    aProjectTitle = rs.getString("title");
                    leftModel.addElement(new ProjectListEntry(aProjectID, aProjectTitle));
                } //end while
                // Set the new model ...
                ImportedList.setVisibleRowCount(rowsize + 2);
                ImportedList.setModel(leftModel);

                try {
                    con.close();
                } catch (SQLException sqlex) {
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
    private javax.swing.JButton ImportLists1;
    private javax.swing.JList ImportedList;
    private javax.swing.JLabel Importedlabels;
    private javax.swing.JButton ManualSchedule;
    private javax.swing.JButton ScheduleAll;
    private javax.swing.JList ScheduledList;
    private javax.swing.ButtonGroup SourceButtonGroup;
    private javax.swing.ButtonGroup StrategyButtonGroup;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.ButtonGroup defaultSearchparamgroup;
    private javax.swing.JMenuItem editModificationsMenuItem;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextPane jTextPane1;
    private javax.swing.JTextField manualInputField;
    private javax.swing.JTextField manualProjectNameField;
    private javax.swing.JPopupMenu modificationOptionsPopupMenu;
    private javax.swing.JButton okButton;
    private javax.swing.JRadioButton rdbSourceMSLIMS1;
    private javax.swing.JRadioButton rdbSourcePRIDE1;
    private javax.swing.JRadioButton rdbStraight1;
    private javax.swing.JRadioButton rdbVarDB1;
    private javax.swing.JRadioButton rdbVarMOD1;
    private javax.swing.JTextField taskContainerName;
    private javax.swing.JButton toRight;
    private javax.swing.JLabel toRunLabel;
    // End of variables declaration//GEN-END:variables
}
