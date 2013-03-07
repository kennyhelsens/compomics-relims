package com.compomics.relims.modes.networking.client.GUI;

import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.modes.networking.client.connectivity.connectors.ServerConnector;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.TaskContainer;
import com.compomics.util.experiment.identification.SearchParameters;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MainClientGUI extends javax.swing.JFrame {

    private Map<String, String> currentUserMap = new HashMap<String, String>();

    private MainClientGUI() {
    }
    private static Logger logger = Logger.getLogger(MainClientGUI.class);

    public static MainClientGUI getInstance() {
        if (mainGUI == null) {
            mainGUI = new MainClientGUI();
        }
        return mainGUI;
    }
    private static MainClientGUI mainGUI;
    private File defaultFolderFile;
    private static DefaultListModel leftModel = new DefaultListModel();
    private static DefaultListModel rightModel = new DefaultListModel();
    private static MainClientGUI.RelimsClientJobStarter iRelimsClientJobStarter;
    private SearchParameters searchParameters;
    private javax.swing.JRadioButton selectedStrategy;
    private javax.swing.JRadioButton selectedSource;
    String currentUser = "default";
    Boolean includeFinishedProjects = true;
    public static final String MODIFICATION_SEPARATOR = "//";
    public static SearchParameters loadedSearchParameters;
    public static boolean usingDefault;

    /**
     * Creates new form ClientBooter
     */
    public static void setUsingDefault(boolean b) {
        usingDefault = b;
    }

    public static boolean getUsingDefault() {
        return usingDefault;
    }

    public static Logger getLogger() {
        return logger;
    }
    private int port = 0;
    private String IP = RelimsProperties.getControllerIP();
    private Component jPanel2;

    public static void setSearchParameters(SearchParameters tempSearchParameters) {
        MainClientGUI.loadedSearchParameters = tempSearchParameters;
    }

    public static SearchParameters getSearchParameters() {
        return MainClientGUI.loadedSearchParameters;
    }

    public MainClientGUI(String currentUser) {
        this.currentUser = currentUser;
        this.port = RelimsProperties.getControllerPort();
        this.IP = RelimsProperties.getControllerIP();
        currentUserMap.put("username", RelimsProperties.getUserID());
        currentUserMap.put("password", RelimsProperties.getPassword());
        initComponents();



        TextAreaAppender consoleAppender = new TextAreaAppender();
        setTitle("Remote Relims");

        /*        StrategyButtons.add(RDBVARMOD);
         StrategyButtons.add(RDBVARDB);
         StrategyButtons.add(RDBSTRAIGHT);

         SourceButtons.add(RDBMSLIMS);
         SourceButtons.add(RDBPRIDE);
         SourceButtons.add(RDBTEXTFILE);
         */
        // add a listener to the jTable 

        DefaultTableModel tableModel = new DefaultTableModel();
        tableModel.addColumn("TaskID");
        tableModel.addColumn("ProjectID");
        tableModel.addColumn("ProjectName");
        tableModel.addColumn("Status");
        String[] freshRow = {"", "", ""};
        //reset the table

        localProjectTable.setModel(tableModel);
        localProjectTable.setShowGrid(true);
        localProjectTable.revalidate();
        localProjectTable.addMouseListener(new TableMouseAdapter(this.IP, this.port));
        //  jScrollPane1.getViewport().add(localProjectTable, null);
        //  jScrollPane1.getViewport().getView().addMouseListener(new TableMouseAdapter(IP, port));
        if (RelimsProperties.getControllerIP() == null) {
            this.IP = "localhost";
        }
        if (RelimsProperties.getControllerPort() < 1000) {
            this.port = 6789;
        }

        //set look and feel
        if (System.getProperty("os.name").lastIndexOf("Windows") != -1) {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (UnsupportedLookAndFeelException e) {
                //handle exception
            } catch (ClassNotFoundException e) {
                // handle exception
            } catch (InstantiationException e) {
                // handle exception
            } catch (IllegalAccessException e) {
                // handle exception
            }
        }

        this.setVisible(true);
        TextAreaAppender.setTextArea(userNotifier);
        MainClientGUI.logger.addAppender(consoleAppender);
        consoleAppender.setImmediateFlush(true);


        loggedInAs1.setText("Tasks for : " + currentUser);

        //Reading default properties to put in fields

        logger.debug("Setting default parameters...");
        logger.debug("Relims located at " + RelimsProperties.getControllerIP() + ":" + RelimsProperties.getControllerPort());


    }

    private class TableMouseAdapter implements MouseListener {

        private String IP = RelimsProperties.getControllerIP();
        private String port = "" + RelimsProperties.getControllerPort();

        public TableMouseAdapter(String IP, int port) {
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                showDetails();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
        }

        @Override
        public void mouseReleased(MouseEvent e) {
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        frameSelectionGroup = new javax.swing.ButtonGroup();
        jTextField1 = new javax.swing.JTextField();
        jFrame1 = new javax.swing.JFrame();
        StrategyButtons = new javax.swing.ButtonGroup();
        SourceButtons = new javax.swing.ButtonGroup();
        SearchparameterOptions = new javax.swing.ButtonGroup();
        jScrollPane3 = new javax.swing.JScrollPane();
        userNotifier = new javax.swing.JTextArea();
        jLabel10 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        FindLocalProjects = new javax.swing.JButton();
        includeFinished = new javax.swing.JCheckBox();
        includeScheduled = new javax.swing.JCheckBox();
        includeRunning = new javax.swing.JCheckBox();
        includeFaulty = new javax.swing.JCheckBox();
        FindLocalProject = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        loggedInAs1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        localProjectTable = new javax.swing.JTable();
        jButton1 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        projects = new javax.swing.JMenu();
        myProjects = new javax.swing.JMenuItem();
        Exit = new javax.swing.JMenuItem();
        searchsettings = new javax.swing.JMenu();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();

        jTextField1.setText("jTextField1");

        javax.swing.GroupLayout jFrame1Layout = new javax.swing.GroupLayout(jFrame1.getContentPane());
        jFrame1.getContentPane().setLayout(jFrame1Layout);
        jFrame1Layout.setHorizontalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jFrame1Layout.setVerticalGroup(
            jFrame1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Command and Control Client");
        setMaximumSize(new java.awt.Dimension(300, 500));
        setMinimumSize(new java.awt.Dimension(300, 500));
        setResizable(false);

        userNotifier.setEditable(false);
        userNotifier.setColumns(20);
        userNotifier.setRows(5);
        jScrollPane3.setViewportView(userNotifier);

        jLabel10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabel10.setText("Activity logger :");
        jLabel10.setToolTipText("");

        FindLocalProjects.setText("Find my projects");
        FindLocalProjects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FindLocalProjectsActionPerformed(evt);
            }
        });

        includeFinished.setText("Finished Projects");
        includeFinished.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeFinishedActionPerformed(evt);
            }
        });

        includeScheduled.setText("Scheduled Projects");
        includeScheduled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeScheduledActionPerformed(evt);
            }
        });

        includeRunning.setText("Running Projects");
        includeRunning.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeRunningActionPerformed(evt);
            }
        });

        includeFaulty.setText("Failed Projects");
        includeFaulty.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeFaultyActionPerformed(evt);
            }
        });

        FindLocalProject.setText("Search");
        FindLocalProject.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FindLocalProjectActionPerformed(evt);
            }
        });

        loggedInAs1.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        loggedInAs1.setText("Logged in as :");

        localProjectTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Project ID", "TaskID", "Status"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        localProjectTable.setFillsViewportHeight(true);
        jScrollPane1.setViewportView(localProjectTable);
        localProjectTable.getColumnModel().getColumn(0).setResizable(false);
        localProjectTable.getColumnModel().getColumn(1).setResizable(false);
        localProjectTable.getColumnModel().getColumn(2).setResizable(false);

        jButton1.setText("Detail");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButton1)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(FindLocalProjects)
                                    .addGap(18, 18, 18)
                                    .addComponent(includeScheduled)
                                    .addGap(8, 8, 8))
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                                    .addComponent(includeFinished)
                                    .addGap(18, 18, 18)))
                            .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(includeRunning)
                                .addGroup(jPanel3Layout.createSequentialGroup()
                                    .addComponent(includeFaulty)
                                    .addGap(83, 83, 83)
                                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 177, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(FindLocalProject))))
                        .addComponent(loggedInAs1)))
                .addContainerGap(105, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loggedInAs1)
                .addGap(18, 18, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(FindLocalProjects)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(includeFaulty)
                        .addComponent(FindLocalProject)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(includeScheduled, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(includeRunning, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(includeFinished))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 384, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addGap(34, 34, 34))
        );

        jTabbedPane1.addTab("My Projects", jPanel3);

        projects.setText("File");

        myProjects.setText("New Project");
        myProjects.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                myProjectsActionPerformed(evt);
            }
        });
        projects.add(myProjects);

        Exit.setText("Exit");
        projects.add(Exit);

        jMenuBar1.add(projects);

        searchsettings.setText("Search Settings");

        jMenu1.setText("Search Options");

        jMenuItem1.setText("New Searchparameters");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Reset Default Searchparameters");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        searchsettings.add(jMenu1);

        jMenuBar1.add(searchsettings);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 591, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        getAccessibleContext().setAccessibleName("ccClient");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void myProjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_myProjectsActionPerformed
        String source = "MSLIMS";
        String strategy = "STRAIGHT";
        /*
         if (RDBMSLIMS.isSelected()) {
         source = "MSLIMS";
         }
         if (RDBPRIDE.isSelected()) {
         source = "PRIDE";
         }
         if (RDBTEXTFILE.isSelected()) {
         source = "TEXTFILE";
         }

         if (RDBSTRAIGHT.isSelected()) {
         strategy = "STRAIGHT";
         }
         if (RDBVARDB.isSelected()) {
         strategy = "VARDB";
         }
         if (RDBVARMOD.isSelected()) {
         strategy = "VARMOD";
         }
         */
        new NewProjectDialog(currentUser, IP, port, source, strategy);
    }//GEN-LAST:event_myProjectsActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        new SearchGUISettingsDialog();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private class BlockedDefaultTableModel extends javax.swing.table.DefaultTableModel {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private void FindLocalProjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FindLocalProjectsActionPerformed

        //DEBUG-LOCAL MODE

        //     DerbyDatabaseService dds = new DerbyDatabaseService();



        List<String[]> myProjects = null;
        boolean update = false;
        // DefaultTableModel tableModel = (DefaultTableModel) localProjectTable.getModel();

        DefaultTableModel tableModel = new BlockedDefaultTableModel();
        tableModel.addColumn("TaskID");
        tableModel.addColumn("ProjectID");
        tableModel.addColumn("ProjectName");
        tableModel.addColumn("Status");
        String[] freshRow = {"", "", ""};
        String queryParameters = "";

        //reset the table
        localProjectTable.setShowGrid(true);
        localProjectTable.setModel(tableModel);
        localProjectTable.revalidate();

        //determine the searchQueryparameters

        if (includeFaulty.isSelected()) {
            queryParameters = queryParameters + "OR TASKSTATE = '%'";
        }

        if (includeFinished.isSelected()) {
            queryParameters = queryParameters + "OR TASKSTATE ='FINISHED' " + "OR TASKSTATE ='PRIDEFAILURE'";
        }

        if (includeRunning.isSelected()) {
            queryParameters = queryParameters + "OR TASKSTATE ='RUNNING' ";
        }

        if (includeScheduled.isSelected()) {
            queryParameters = queryParameters + "OR TASKSTATE ='NEW'";
        }
        if (!queryParameters.equals("")) {
            queryParameters = "(" + queryParameters.substring(3) + ")";

            //REMOTE MODE 

            //try to get the status from the server
            ServerConnector serverConnector = new ServerConnector();
            TaskContainer aSearchQuery = new TaskContainer();
            aSearchQuery.setInstructionMap(currentUserMap);
            aSearchQuery.updateInstruction("instruction", "getTasks");
            aSearchQuery.updateInstruction("queryParameters", queryParameters);
            logger.debug("Reaching server for tasks...");
            try {
                myProjects = (List<String[]>) serverConnector.getFromServer(aSearchQuery);
                //  myProjects = dds.getUserTasks(currentUserMap.get("username"), queryParameters);
                if (!myProjects.isEmpty() && myProjects != null) {
                    update = true;
                } else {
                    update = false;
                }
            } catch (Exception ex) {
                update = false;
                logger.error("Could not retrieve tasks from the database...");
            }

            if (update) {
                for (String[] aRecord : myProjects) {

                    String projectID = aRecord[0];
                    String taskID = aRecord[1];
                    String status = aRecord[2];

                    if (status.equals("PRIDEFAILURE")) {
                        status = "No id's found";
                    }

                    String projectName = aRecord[3];
                    tableModel.addRow(freshRow);
                    int rows = tableModel.getRowCount() - 1;
                    tableModel.setValueAt(taskID, rows, 0);
                    tableModel.setValueAt(projectID, rows, 1);
                    tableModel.setValueAt(projectName, rows, 2);
                    tableModel.setValueAt(status, rows, 3);
                    //prevent the cells from being edited --> using subclass that is blocked, so all cells are not editable
                    localProjectTable.revalidate();

                }
                localProjectTable.setModel(tableModel);
            }
            localProjectTable.revalidate();
        } else {
            logger.error("No searchoptions were selected...");
        }
    }//GEN-LAST:event_FindLocalProjectsActionPerformed

    private void includeFinishedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeFinishedActionPerformed
    }//GEN-LAST:event_includeFinishedActionPerformed

    private void includeScheduledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeScheduledActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_includeScheduledActionPerformed

    private void includeRunningActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeRunningActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_includeRunningActionPerformed

    private void includeFaultyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeFaultyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_includeFaultyActionPerformed

    private void FindLocalProjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FindLocalProjectActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_FindLocalProjectActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed

        File currentParameters = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "configuration" + File.separator + "lastUsedParameters.parameters");
        File defaultParameters = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "configuration" + File.separator + "default_SearchGUI.parameters");
        try {
            FileUtils.copyFile(defaultParameters, currentParameters, true);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MainClientGUI.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            MainClientGUI.setUsingDefault(true);
        }



    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        showDetails();
    }//GEN-LAST:event_jButton1ActionPerformed
    public void close() {
        throw new UnsupportedOperationException("Not supported yet.");




    }

    public void showDetails() {
        int row = localProjectTable.getSelectedRow();
        int column = localProjectTable.getSelectedColumn();
        System.out.println(row);
        System.out.println("c" + column);
        long projectID = Long.parseLong((String) localProjectTable.getValueAt(row, 1));
        long taskID = Long.parseLong((String) localProjectTable.getValueAt(row, 0));
        System.out.println(projectID);
        if (port != -1) {
            new ProjectDetailDialog(currentUserMap, IP, port, projectID, taskID);
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem Exit;
    private javax.swing.JButton FindLocalProject;
    private javax.swing.JButton FindLocalProjects;
    private javax.swing.ButtonGroup SearchparameterOptions;
    private javax.swing.ButtonGroup SourceButtons;
    private javax.swing.ButtonGroup StrategyButtons;
    private javax.swing.ButtonGroup frameSelectionGroup;
    private javax.swing.JCheckBox includeFaulty;
    private javax.swing.JCheckBox includeFinished;
    private javax.swing.JCheckBox includeRunning;
    private javax.swing.JCheckBox includeScheduled;
    private javax.swing.JButton jButton1;
    private javax.swing.JFrame jFrame1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTable localProjectTable;
    private javax.swing.JLabel loggedInAs1;
    private javax.swing.JMenuItem myProjects;
    private javax.swing.JMenu projects;
    private javax.swing.JMenu searchsettings;
    private javax.swing.JTextArea userNotifier;
    // End of variables declaration//GEN-END:variables
//prioritybuttons = easier done myself
}
