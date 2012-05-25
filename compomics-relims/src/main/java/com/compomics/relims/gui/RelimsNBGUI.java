/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.relims.gui;

import com.compomics.relims.concurrent.RelimsJob;
import com.compomics.relims.conf.RelimsProperties;
import com.compomics.relims.gui.model.ProjectSourceSelectionModel;
import com.compomics.relims.gui.model.RelimsPropertiesTableModel;
import com.compomics.relims.gui.model.StrategySelectionModel;
import org.apache.log4j.Logger;

import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author kennyhelsens
 */
public class RelimsNBGUI extends javax.swing.JFrame {

    private static Logger logger = Logger.getLogger(RelimsNBGUI.class);
    protected StrategySelectionModel iStrategySelectionModel = null;
    protected ProjectSourceSelectionModel iProjectSourceSelectionModel = null;
    private RelimsJob iRelimsJob = null;

    /**
     * Creates new form RelimsNBGUI
     */
    public RelimsNBGUI() {
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
         * Sets an UncaughtExceptionHandler and executes the thread by the ExecutorsService
         */
        Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
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
        splitMain = new javax.swing.JSplitPane();
        scrlLogger = new javax.swing.JScrollPane();
        txtLogger = new javax.swing.JTextArea();
        tabCore = new javax.swing.JTabbedPane();
        jpanMain = new javax.swing.JPanel();
        btnStart = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        rdbVarMOD = new javax.swing.JRadioButton();
        rdbVarDB = new javax.swing.JRadioButton();
        rdbSourceMSLIMS = new javax.swing.JRadioButton();
        jLabel3 = new javax.swing.JLabel();
        btnStop = new javax.swing.JButton();
        rdbSourcePRIDE = new javax.swing.JRadioButton();
        scrlTable = new javax.swing.JScrollPane();
        tblProperties = new javax.swing.JTable();
        jpanHead = new javax.swing.JPanel();
        iconCompomics = new javax.swing.JLabel();
        iconRelims = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jSeparator2 = new javax.swing.JSeparator();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("  ");
        setBackground(new java.awt.Color(255, 255, 255));

        splitMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        splitMain.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        splitMain.setResizeWeight(0.6);

        scrlLogger.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), "Logger", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Lucida Grande", 2, 10))); // NOI18N
        scrlLogger.setViewportBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        txtLogger.setColumns(20);
        txtLogger.setEditable(false);
        txtLogger.setFont(new java.awt.Font("Arial", 0, 10)); // NOI18N
        txtLogger.setRows(5);
        txtLogger.setWrapStyleWord(true);
        scrlLogger.setViewportView(txtLogger);

        splitMain.setRightComponent(scrlLogger);

        jpanMain.setBackground(new java.awt.Color(255, 255, 255));

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

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel2.setText("Strategy");

        btnGroupStrategy.add(rdbVarMOD);
        rdbVarMOD.setSelected(true);
        rdbVarMOD.setText("Variable MODS");
        rdbVarMOD.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbVarMODActionPerformed(evt);
            }
        });

        btnGroupStrategy.add(rdbVarDB);
        rdbVarDB.setText("Variable DB");
        rdbVarDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbVarDBActionPerformed(evt);
            }
        });

        btnGroupSource.add(rdbSourceMSLIMS);
        rdbSourceMSLIMS.setSelected(true);
        rdbSourceMSLIMS.setText("MSLIMS");
        rdbSourceMSLIMS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourceMSLIMSActionPerformed(evt);
            }
        });

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 1, 12)); // NOI18N
        jLabel3.setText("Source");

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

        btnGroupSource.add(rdbSourcePRIDE);
        rdbSourcePRIDE.setText("PRIDE");
        rdbSourcePRIDE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdbSourcePRIDEActionPerformed(evt);
            }
        });

        org.jdesktop.layout.GroupLayout jpanMainLayout = new org.jdesktop.layout.GroupLayout(jpanMain);
        jpanMain.setLayout(jpanMainLayout);
        jpanMainLayout.setHorizontalGroup(
            jpanMainLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jpanMainLayout.createSequentialGroup()
                .addContainerGap()
                .add(jpanMainLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, rdbSourceMSLIMS)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, rdbSourcePRIDE)
                    .add(jpanMainLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(jLabel3)
                        .add(rdbVarMOD)
                        .add(rdbVarDB)
                        .add(jLabel2)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 446, Short.MAX_VALUE)
                .add(btnStart)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(btnStop)
                .addContainerGap())
        );
        jpanMainLayout.setVerticalGroup(
            jpanMainLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jpanMainLayout.createSequentialGroup()
                .addContainerGap()
                .add(jpanMainLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jpanMainLayout.createSequentialGroup()
                        .add(jLabel2)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rdbVarMOD)
                        .add(3, 3, 3)
                        .add(rdbVarDB)
                        .add(23, 23, 23)
                        .add(jLabel3)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(rdbSourceMSLIMS)
                        .add(3, 3, 3)
                        .add(rdbSourcePRIDE))
                    .add(btnStart)
                    .add(btnStop))
                .addContainerGap())
        );

        tabCore.addTab("Main", jpanMain);

        scrlTable.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));

        tblProperties.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        scrlTable.setViewportView(tblProperties);

        tabCore.addTab("Configuration", scrlTable);

        splitMain.setLeftComponent(tabCore);
        tabCore.getAccessibleContext().setAccessibleName("Main");

        jpanHead.setBackground(new java.awt.Color(255, 255, 255));
        jpanHead.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1), " "));

        iconCompomics.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/image/compomics.png"))); // NOI18N

        iconRelims.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/image/logo.png"))); // NOI18N

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 637, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(0, 0, Short.MAX_VALUE)
        );

        org.jdesktop.layout.GroupLayout jpanHeadLayout = new org.jdesktop.layout.GroupLayout(jpanHead);
        jpanHead.setLayout(jpanHeadLayout);
        jpanHeadLayout.setHorizontalGroup(
            jpanHeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jpanHeadLayout.createSequentialGroup()
                .addContainerGap()
                .add(jpanHeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jpanHeadLayout.createSequentialGroup()
                        .add(6, 6, 6)
                        .add(jpanHeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(jpanHeadLayout.createSequentialGroup()
                                .add(iconRelims)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(iconCompomics))
                            .add(jpanHeadLayout.createSequentialGroup()
                                .add(15, 15, 15)
                                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .add(15, 15, 15))))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jpanHeadLayout.createSequentialGroup()
                        .add(24, 24, 24)
                        .add(jSeparator2)))
                .add(82, 82, 82))
        );
        jpanHeadLayout.setVerticalGroup(
            jpanHeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jpanHeadLayout.createSequentialGroup()
                .add(jpanHeadLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jpanHeadLayout.createSequentialGroup()
                        .addContainerGap()
                        .add(iconRelims))
                    .add(jpanHeadLayout.createSequentialGroup()
                        .add(23, 23, 23)
                        .add(iconCompomics)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jSeparator2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(24, 24, 24)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .add(jpanHead, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .add(splitMain, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(jpanHead, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 174, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(splitMain, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                .add(0, 0, 0))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        iRelimsJob.close();
    }

    private void btnStartActionPerformed(ActionEvent aEvt) {
        ExecutorService withinExecutor = Executors.newFixedThreadPool(1);

        String lSearchStrategyID = iStrategySelectionModel.getSelectedItem().toString();
        String lProjectProviderID = iProjectSourceSelectionModel.getSelectedItem().toString();

        iRelimsJob = new RelimsJob( lSearchStrategyID, lProjectProviderID);
        withinExecutor.submit(iRelimsJob);

    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(RelimsNBGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(RelimsNBGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(RelimsNBGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RelimsNBGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new RelimsNBGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup btnGroupSource;
    private javax.swing.ButtonGroup btnGroupStrategy;
    private javax.swing.JButton btnStart;
    private javax.swing.JButton btnStop;
    private javax.swing.JLabel iconCompomics;
    private javax.swing.JLabel iconRelims;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JPanel jpanHead;
    private javax.swing.JPanel jpanMain;
    private javax.swing.JRadioButton rdbSourceMSLIMS;
    private javax.swing.JRadioButton rdbSourcePRIDE;
    private javax.swing.JRadioButton rdbVarDB;
    private javax.swing.JRadioButton rdbVarMOD;
    private javax.swing.JScrollPane scrlLogger;
    private javax.swing.JScrollPane scrlTable;
    private javax.swing.JSplitPane splitMain;
    private javax.swing.JTabbedPane tabCore;
    private javax.swing.JTable tblProperties;
    private javax.swing.JTextArea txtLogger;
    // End of variables declaration//GEN-END:variables
}
