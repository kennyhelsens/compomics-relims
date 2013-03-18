package com.compomics.relims.modes.networking.client.GUI;


import com.compomics.relims.modes.networking.client.connectivity.connectors.ServerConnector;
import com.compomics.relims.modes.networking.controller.connectivity.taskobjects.TaskContainer;
import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.preferences.ModificationProfile;
import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jfree.chart.plot.PlotOrientation;
import org.xmlpull.v1.XmlPullParserException;

/**
 * The SearchGUI settings dialog.
 *
 * @author Harald Barsnes
 */
public class ProjectDetailDialog extends javax.swing.JDialog {

    private Logger logger = Logger.getLogger(ProjectDetailDialog.class);
    private SearchParameters searchParameters;
    private javax.swing.JRadioButton selectedStrategy;
    private javax.swing.JRadioButton selectedSource;
    Map<String, String> currentUserMap = null;
    Boolean includeFinishedProjects = true;
    public static final String MODIFICATION_SEPARATOR = "//";
    public static SearchParameters loadedSearchParameters;
    private int port = 0;
    private String controllerIP = null;
    private long taskID = 0L;
    private File enzymeFile = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "configuration" + File.separator + "searchGUI_enzymes.xml");
    private File searchParametersFile = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "configuration" + File.separator + "lastUsedParameters.parameters");
    private File parametersFile = null;
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    private BlockedDefaultTableModel tableModelProject = new BlockedDefaultTableModel();
    private BlockedDefaultTableModel tableModelTask = new BlockedDefaultTableModel();
    private BlockedDefaultTableModel tableModelComputing = new BlockedDefaultTableModel();
    /**
     * The enzyme factory.
     */
    private EnzymeFactory enzymeFactory = EnzymeFactory.getInstance();
    /**
     * Convenience array for forward ion type selection.
     */
    private String[] forwardIons = {"a", "b", "c"};
    /**
     * Convenience array for rewind ion type selection.
     */
    private String[] rewindIons = {"x", "y", "z"};
    private HashMap<String, Object> myProjectInfo;

    public ProjectDetailDialog(Map<String, String> currentUser, String IP, int port, long projectID, long taskID) {
        String enzymeFileName = "Z:/remote_relims/Files/Utils/SearchGUI-1.11.0-beta/resources/conf/searchGUI_enzymes.xml";
        enzymeFileName.replace("/", File.separator);
        enzymeFile = new File(enzymeFileName);

        this.port = port;
        this.controllerIP = IP;
        this.taskID = taskID;
        currentUserMap = currentUser;
        initializeEnzymeFactory();
        getProjectInfo();
        initComponents();
        setScreenProps();
        this.setTitle("Detail for task " + taskID);
        setStatisticLabels();


        formComponentResized(null);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
            }
        });
    }

    private class BlockedDefaultTableModel extends javax.swing.table.DefaultTableModel {

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    private void setToStatisticsTable(DefaultTableModel tableModel, String parameterName, String parameterKeyName) {
        String[] freshRow = {"", ""};
        try {
            String parameterValue = myProjectInfo.get(parameterKeyName).toString();
            //for timeunits...
            if (parameterKeyName.equals("TASKTIME")) {
                parameterValue = parameterValue.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) myProjectInfo.get(parameterKeyName)),
                        TimeUnit.MILLISECONDS.toSeconds((long) myProjectInfo.get(parameterKeyName))
                        - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long) myProjectInfo.get(parameterKeyName))));
            }
            tableModel.addRow(freshRow);
            int rows = tableModel.getRowCount() - 1;
            tableModel.setValueAt(parameterName, rows, 0);
            tableModel.setValueAt(parameterValue, rows, 1);
        } catch (Exception ex) {
            tableModel.addRow(freshRow);
            int rows = tableModel.getRowCount() - 1;
            tableModel.setValueAt(parameterName, rows, 0);
            tableModel.setValueAt("Could not be determined due to an error", rows, 1);
        }
    }

    private void setToStatisticsTable(DefaultTableModel tableModel, String parameterName, Object value, boolean calculated) {
        String[] freshRow = {"", ""};
        try {
            if (calculated) {
                String parameterValue = value.toString();
                if (parameterName.equals("TIMESTAMP")) {
                    parameterValue = parameterValue.replace(" ", " at ");
                }
                tableModel.addRow(freshRow);
                int rows = tableModel.getRowCount() - 1;
                tableModel.setValueAt(parameterName, rows, 0);
                tableModel.setValueAt(parameterValue, rows, 1);
            }
        } catch (Exception ex) {
            tableModel.addRow(freshRow);
            int rows = tableModel.getRowCount() - 1;
            tableModel.setValueAt(parameterName, rows, 0);
            tableModel.setValueAt("Could not be determined", rows, 1);
        }
    }

    private void setStatisticLabels() {




        //get the project parameters file from the NAS storage

        String defaultParamFileName = "Z:/remote_relims/Files/Utils/SearchGUI-1.11.0-beta/resources/conf/searchGUI_enzymes.xml";
        defaultParamFileName.replace("/", File.separator);
        searchParametersFile = new File(defaultParamFileName);

        tableModelProject.addColumn("Parameter");
        tableModelProject.addColumn("Value");
        statisticsTableProject.setModel(tableModelProject);

        tableModelTask.addColumn("Parameter");
        tableModelTask.addColumn("Value");
        statisticsTableTask.setModel(tableModelTask);

        tableModelComputing.addColumn("Parameter");
        tableModelComputing.addColumn("Value");
        statisticsTableComputing.setModel(tableModelComputing);
        statisticsTableComputing.revalidate();
        //NICE WAY
        //Getting the project parameters

        //Project
        setToStatisticsTable(tableModelProject, "ProjectID", "PROJECTID");
        setToStatisticsTable(tableModelProject, "Project Name", "PROJECTNAME");
        setToStatisticsTable(tableModelProject, "Source", "SOURCEID");
        setToStatisticsTable(tableModelProject, "Strategy", "STRATEGYID");

        //Task
        setToStatisticsTable(tableModelTask, "TaskID", "TASKID");
        setToStatisticsTable(tableModelTask, "State", "TASKSTATE");
        //---date without ms
        String dateNoMillis = myProjectInfo.get("TIMESTAMP").toString().replace(" ", " at ");
        dateNoMillis = dateNoMillis.substring(0, dateNoMillis.length() - 4);
        setToStatisticsTable(tableModelTask, "Date of run", dateNoMillis, true);
        setToStatisticsTable(tableModelTask, "Length of run", "TASKTIME");

        //Computing
        setToStatisticsTable(tableModelComputing, "Operating System", "OSNAME");
        setToStatisticsTable(tableModelComputing, "Version", "OSVERSION");
        setToStatisticsTable(tableModelComputing, "Arch", "OSARCH");
        setToStatisticsTable(tableModelComputing, "Java runtime", "JAVAVERSION");
        setToStatisticsTable(tableModelComputing, "Amount of CPU's", "CORES");
        // -- cpu load (system)
        DecimalFormat twoDForm = new DecimalFormat("#.####");
        double cpuUsage = (double) myProjectInfo.get("SYSTEMCPULOAD");
        cpuUsage = Double.valueOf(twoDForm.format(cpuUsage));
        setToStatisticsTable(tableModelComputing, "Average system CPU Usage", cpuUsage * 100 + "%", true);
        // -- cpu load (process)

        cpuUsage = (double) myProjectInfo.get("PROCESSCPULOAD");
        if (cpuUsage != -1) {
            cpuUsage = Double.valueOf(twoDForm.format(cpuUsage));
            setToStatisticsTable(tableModelComputing, "Process system CPU Usage", cpuUsage * 100 + "%", true);
        } else {
            setToStatisticsTable(tableModelComputing, "Process system CPU Usage", "Could not be determined", true);
        }

        //-- used physical memory
        long usedMemory = (long) myProjectInfo.get("TOTALPHYSICALMEMORYSIZE") - (long) myProjectInfo.get("FREEPHYSICALMEMORYSIZE");
        setToStatisticsTable(tableModelComputing, "Used physical memory", FileUtils.byteCountToDisplaySize(usedMemory), true);

        statisticsTableProject.revalidate();
        statisticsTableTask.revalidate();
        statisticsTableComputing.revalidate();

    }

    private void setScreenProps() {

        fixedModsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        fixedModsTable.getColumn(" ").setWidth(port);
        variableModsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        fixedModsTable.getColumn(" ").setMaxWidth(35);
        fixedModsTable.getColumn(" ").setMinWidth(35);
        variableModsTable.getColumn(" ").setMaxWidth(35);
        variableModsTable.getColumn(" ").setMinWidth(35);
        fixedModsTable.getColumn("Mass").setMaxWidth(100);
        fixedModsTable.getColumn("Mass").setMinWidth(100);
        variableModsTable.getColumn("Mass").setMaxWidth(100);
        variableModsTable.getColumn("Mass").setMinWidth(100);

        File fastaFile = searchParameters.getFastaFile();
        if (fastaFile != null) {
            String fastaPath = fastaFile.getAbsolutePath();
            databaseSettingsTxt.setText(fastaPath);
        }

        ArrayList<String> missingPtms = new ArrayList<String>();
        ModificationProfile modificationProfile = searchParameters.getModificationProfile();
        if (modificationProfile != null) {
            ArrayList<String> fixedMods = modificationProfile.getFixedModifications();

            for (String ptmName : fixedMods) {
                if (!ptmFactory.containsPTM(ptmName)) {
                    missingPtms.add(ptmName);
                }
            }

            for (String missing : missingPtms) {
                fixedMods.remove(missing);
            }

            if (!missingPtms.isEmpty()) {
                if (missingPtms.size() == 1) {
                    JOptionPane.showMessageDialog(this, "The following modification is currently not recognized by SearchGUI: "
                            + missingPtms.get(0) + ".\nPlease import it in the Modification Editor.", "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                } else {

                    String output = "The following modifications are currently not recognized by SearchGUI:\n";
                    boolean first = true;

                    for (String ptm : missingPtms) {
                        if (first) {
                            first = false;
                        } else {
                            output += ", ";
                        }
                        output += ptm;
                    }

                    output += ".\nPlease import them in the Modification Editor.";
                    JOptionPane.showMessageDialog(this, output, "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }

            DefaultTableModel fixedModel = (DefaultTableModel) fixedModsTable.getModel();
            fixedModel.getDataVector().removeAllElements();

            for (String fixedMod : fixedMods) {
                ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationProfile().getColor(fixedMod), fixedMod, ptmFactory.getPTM(fixedMod).getMass()});
            }
            ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
            fixedModsTable.repaint();
            fixedModificationsLabel.setText("Fixed Modifications (" + fixedMods.size() + ")");

            ArrayList<String> variableMods = modificationProfile.getVariableModifications();

            for (String ptmName : variableMods) {
                if (!ptmFactory.containsPTM(ptmName)) {
                    missingPtms.add(ptmName);
                }
            }

            for (String missing : missingPtms) {
                variableMods.remove(missing);
            }

            if (!missingPtms.isEmpty()) {
                if (missingPtms.size() == 1) {
                    JOptionPane.showMessageDialog(this, "The following modification is currently not recognized by SearchGUI: "
                            + missingPtms.get(0) + ".\nPlease import it in the Modification Editor.", "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                } else {
                    String output = "The following modifications are currently not recognized by SearchGUI:\n";
                    boolean first = true;

                    for (String ptm : missingPtms) {
                        if (first) {
                            first = false;
                        } else {
                            output += ", ";
                        }
                        output += ptm;
                    }

                    output += ".\nPlease import them in the Modification Editor.";
                    JOptionPane.showMessageDialog(this, output, "Modification Not Found", JOptionPane.WARNING_MESSAGE);
                }
            }
            DefaultTableModel variableModel = (DefaultTableModel) variableModsTable.getModel();
            variableModel.getDataVector().removeAllElements();
            for (String variableMod : variableMods) {
                ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationProfile().getColor(variableMod), variableMod, ptmFactory.getPTM(variableMod).getMass()});
            }
            ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
            variableModsTable.repaint();
            variableModificationsLabel.setText("Variable Modifications (" + variableMods.size() + ")");

            updateModificationList();
        }

        Enzyme enzyme = searchParameters.getEnzyme();
        if (enzyme != null) {
            String enzymeName = enzyme.getName();

            if (!enzymeFactory.enzymeLoaded(enzymeName)) {
                enzymeFactory.addEnzyme(searchParameters.getEnzyme());
            }
            enzymesCmb.setSelectedItem(enzymeName);
        }

        if (searchParameters.getIonSearched1() != null) {
            fragmentIon1Cmb.setSelectedItem(PeptideFragmentIon.getSubTypeAsString(searchParameters.getIonSearched1()));
        }

        if (searchParameters.getIonSearched2() != null) {
            fragmentIon2Cmb.setSelectedItem(PeptideFragmentIon.getSubTypeAsString(searchParameters.getIonSearched2()));
        }

        if (searchParameters.getnMissedCleavages() != null) {
            missedCleavagesTxt.setText(searchParameters.getnMissedCleavages() + "");
        }

        if (searchParameters.getPrecursorAccuracy() != null) {
            precursorIonAccuracyTxt.setText(searchParameters.getPrecursorAccuracy() + "");
        }

        if (searchParameters.getPrecursorAccuracyType() != null) {
            if (searchParameters.getPrecursorAccuracyType() == SearchParameters.PrecursorAccuracyType.PPM) {
                precursorIonUnit.setSelectedItem("ppm");
            } else if (searchParameters.getPrecursorAccuracyType() == SearchParameters.PrecursorAccuracyType.DA) {
                precursorIonUnit.setSelectedItem("Da");
            }
        }

        if (searchParameters.getFragmentIonAccuracy() != null) {
            fragmentIonAccuracyTxt.setText(searchParameters.getFragmentIonAccuracy() + "");
        }

        if (searchParameters.getMinChargeSearched() != null) {
            minPrecursorChargeTxt.setText(searchParameters.getMinChargeSearched().value + "");
        }

        if (searchParameters.getMaxChargeSearched() != null) {
            maxPrecursorChargeTxt.setText(searchParameters.getMaxChargeSearched().value + "");
        }
    }

    private void updateModificationList() {
        ArrayList<String> allModificationsList = new ArrayList<String>();

        allModificationsList = ptmFactory.getPTMs();


        int nFixed = fixedModsTable.getRowCount();
        int nVariable = variableModsTable.getRowCount();
        ArrayList<String> allModifications = new ArrayList<String>();

        for (String name : allModificationsList) {
            boolean found = false;
            for (int j = 0; j < nFixed; j++) {
                if (((String) fixedModsTable.getValueAt(j, 1)).equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                for (int j = 0; j < nVariable; j++) {
                    if (((String) variableModsTable.getValueAt(j, 1)).equals(name)) {
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                allModifications.add(name);
            }
        }

        String[] allModificationsAsArray = new String[allModifications.size()];

        for (int i = 0; i < allModifications.size(); i++) {
            allModificationsAsArray[i] = allModifications.get(i);
        }

        Arrays.sort(allModificationsAsArray);


        // get the min and max values for the mass sparklines
        double maxMass = Double.MIN_VALUE;
        double minMass = Double.MAX_VALUE;

        for (String ptm : ptmFactory.getPTMs()) {
            if (ptmFactory.getPTM(ptm).getMass() > maxMass) {
                maxMass = ptmFactory.getPTM(ptm).getMass();
            }
            if (ptmFactory.getPTM(ptm).getMass() < minMass) {
                minMass = ptmFactory.getPTM(ptm).getMass();
            }
        }

        fixedModsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) fixedModsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);
        variableModsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) variableModsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);



    }

    private void initializeEnzymeFactory() {
        try {
            enzymeFactory.importEnzymes(enzymeFile);
        } catch (XmlPullParserException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        statisticsTableProject = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        statisticsTableComputing = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        statisticsTableTask = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        SaveConfigurationAs = new javax.swing.JButton();
        dataBasePanelSettings = new javax.swing.JPanel();
        databaseSettingsLbl = new javax.swing.JLabel();
        databaseSettingsTxt = new javax.swing.JTextField();
        targetDecoySettingsButton = new javax.swing.JButton();
        setStandard = new javax.swing.JButton();
        modificationsPanel = new javax.swing.JPanel();
        modificationTypesSplitPane = new javax.swing.JSplitPane();
        jPanel8 = new javax.swing.JPanel();
        fixedModificationsLabel = new javax.swing.JLabel();
        fixedModsJScrollPane = new javax.swing.JScrollPane();
        fixedModsTable = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        variableModsJScrollPane = new javax.swing.JScrollPane();
        variableModsTable = new javax.swing.JTable();
        variableModificationsLabel = new javax.swing.JLabel();
        proteaseAndFragmentationPanel1 = new javax.swing.JPanel();
        enzymeLabel1 = new javax.swing.JLabel();
        enzymesCmb = new javax.swing.JComboBox();
        maxMissedCleavagesLabel1 = new javax.swing.JLabel();
        missedCleavagesTxt = new javax.swing.JTextField();
        precursorIonLbl1 = new javax.swing.JLabel();
        precursorIonAccuracyTxt = new javax.swing.JTextField();
        precursorIonUnit = new javax.swing.JComboBox();
        fragmentIonLbl1 = new javax.swing.JLabel();
        fragmentIonAccuracyTxt = new javax.swing.JTextField();
        fragmentIonType1Lbl1 = new javax.swing.JLabel();
        fragmentIon1Cmb = new javax.swing.JComboBox();
        fragmentIon2Cmb = new javax.swing.JComboBox();
        precursorChargeLbl1 = new javax.swing.JLabel();
        minPrecursorChargeTxt = new javax.swing.JTextField();
        maxPrecursorChargeTxt = new javax.swing.JTextField();
        precursorChargeRangeLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search Settings");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Running Details"));

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Project Details"));

        statisticsTableProject.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane1.setViewportView(statisticsTableProject);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder("Worker details"));

        statisticsTableComputing.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane3.setViewportView(statisticsTableComputing);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel5.setBorder(javax.swing.BorderFactory.createTitledBorder("Task details"));

        statisticsTableTask.setModel(new javax.swing.table.DefaultTableModel(
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
        jScrollPane2.setViewportView(statisticsTableTask);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 452, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel5Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder("Used Searchparameters"));

        SaveConfigurationAs.setText("Save Locally");
        SaveConfigurationAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveConfigurationAsActionPerformed(evt);
            }
        });

        dataBasePanelSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Database"));
        dataBasePanelSettings.setOpaque(false);

        databaseSettingsLbl.setText("Used Database (FASTA)*");

        databaseSettingsTxt.setEditable(false);

        targetDecoySettingsButton.setText("Decoy");
        targetDecoySettingsButton.setToolTipText("Generate a concatenated Target/Decoy database");
        targetDecoySettingsButton.setEnabled(false);
        targetDecoySettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetDecoySettingsButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dataBasePanelSettingsLayout = new javax.swing.GroupLayout(dataBasePanelSettings);
        dataBasePanelSettings.setLayout(dataBasePanelSettingsLayout);
        dataBasePanelSettingsLayout.setHorizontalGroup(
            dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataBasePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(databaseSettingsLbl)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(databaseSettingsTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(targetDecoySettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        dataBasePanelSettingsLayout.setVerticalGroup(
            dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataBasePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseSettingsLbl)
                    .addComponent(targetDecoySettingsButton)
                    .addComponent(databaseSettingsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setStandard.setText("Set Default");
        setStandard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setStandardActionPerformed(evt);
            }
        });

        modificationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Modifications"));
        modificationsPanel.setOpaque(false);

        modificationTypesSplitPane.setBorder(null);
        modificationTypesSplitPane.setDividerSize(0);
        modificationTypesSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        modificationTypesSplitPane.setResizeWeight(0.5);
        modificationTypesSplitPane.setOpaque(false);
        modificationTypesSplitPane.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                modificationTypesSplitPaneComponentResized(evt);
            }
        });

        jPanel8.setOpaque(false);

        fixedModificationsLabel.setFont(fixedModificationsLabel.getFont().deriveFont((fixedModificationsLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        fixedModificationsLabel.setText("Fixed Modifications");

        fixedModsJScrollPane.setPreferredSize(new java.awt.Dimension(100, 60));

        fixedModsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Name", "Mass"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Double.class
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
        fixedModsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseReleased(evt);
            }
        });
        fixedModsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                fixedModsTableMouseMoved(evt);
            }
        });
        fixedModsJScrollPane.setViewportView(fixedModsTable);

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(fixedModificationsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                .addGap(371, 371, 371))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(fixedModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fixedModificationsLabel)
                .addGap(6, 6, 6)
                .addComponent(fixedModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
        );

        modificationTypesSplitPane.setLeftComponent(jPanel8);

        jPanel9.setOpaque(false);

        variableModsJScrollPane.setPreferredSize(new java.awt.Dimension(100, 60));

        variableModsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                " ", "Name", "Mass"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.String.class, java.lang.Double.class
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
        variableModsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                variableModsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                variableModsTableMouseReleased(evt);
            }
        });
        variableModsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                variableModsTableMouseMoved(evt);
            }
        });
        variableModsJScrollPane.setViewportView(variableModsTable);

        variableModificationsLabel.setFont(variableModificationsLabel.getFont().deriveFont((variableModificationsLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        variableModificationsLabel.setText("Variable Modifications");

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(variableModificationsLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 523, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(variableModificationsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE))
        );

        modificationTypesSplitPane.setRightComponent(jPanel9);

        javax.swing.GroupLayout modificationsPanelLayout = new javax.swing.GroupLayout(modificationsPanel);
        modificationsPanel.setLayout(modificationsPanelLayout);
        modificationsPanelLayout.setHorizontalGroup(
            modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(modificationTypesSplitPane)
                .addGap(0, 0, 0))
        );
        modificationsPanelLayout.setVerticalGroup(
            modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsPanelLayout.createSequentialGroup()
                .addComponent(modificationTypesSplitPane)
                .addContainerGap())
        );

        proteaseAndFragmentationPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Protease & Fragmentation"));
        proteaseAndFragmentationPanel1.setOpaque(false);

        enzymeLabel1.setText("Protease");

        enzymesCmb.setModel(new DefaultComboBoxModel(loadEnzymes()));
        enzymesCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enzymesCmbActionPerformed(evt);
            }
        });

        maxMissedCleavagesLabel1.setText("Max Missed Cleavages");

        missedCleavagesTxt.setEditable(false);
        missedCleavagesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        missedCleavagesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                missedCleavagesTxtKeyReleased(evt);
            }
        });

        precursorIonLbl1.setText("Precursor Mass Tolerance");

        precursorIonAccuracyTxt.setEditable(false);
        precursorIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precursorIonAccuracyTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                precursorIonAccuracyTxtKeyReleased(evt);
            }
        });

        precursorIonUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ppm", "Da" }));

        fragmentIonLbl1.setText("Fragment Mass Tolerance (Da)");

        fragmentIonAccuracyTxt.setEditable(false);
        fragmentIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        fragmentIonAccuracyTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fragmentIonAccuracyTxtKeyReleased(evt);
            }
        });

        fragmentIonType1Lbl1.setText("Fragment Ion Types");

        fragmentIon1Cmb.setModel(new DefaultComboBoxModel(forwardIons));

        fragmentIon2Cmb.setModel(new DefaultComboBoxModel(rewindIons));

        precursorChargeLbl1.setText("Precursor Charge");

        minPrecursorChargeTxt.setEditable(false);
        minPrecursorChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecursorChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecursorChargeTxtKeyReleased(evt);
            }
        });

        maxPrecursorChargeTxt.setEditable(false);
        maxPrecursorChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPrecursorChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPrecursorChargeTxtKeyReleased(evt);
            }
        });

        precursorChargeRangeLabel1.setText("-");

        javax.swing.GroupLayout proteaseAndFragmentationPanel1Layout = new javax.swing.GroupLayout(proteaseAndFragmentationPanel1);
        proteaseAndFragmentationPanel1.setLayout(proteaseAndFragmentationPanel1Layout);
        proteaseAndFragmentationPanel1Layout.setHorizontalGroup(
            proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxMissedCleavagesLabel1)
                    .addComponent(fragmentIonLbl1)
                    .addComponent(precursorChargeLbl1)
                    .addComponent(fragmentIonType1Lbl1)
                    .addComponent(enzymeLabel1)
                    .addComponent(precursorIonLbl1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(proteaseAndFragmentationPanel1Layout.createSequentialGroup()
                            .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(precursorIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(enzymesCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(proteaseAndFragmentationPanel1Layout.createSequentialGroup()
                            .addComponent(fragmentIon1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(fragmentIon2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, proteaseAndFragmentationPanel1Layout.createSequentialGroup()
                            .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(precursorChargeRangeLabel1)
                            .addGap(18, 18, 18)
                            .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(missedCleavagesTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                        .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.Alignment.LEADING)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        proteaseAndFragmentationPanel1Layout.setVerticalGroup(
            proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enzymeLabel1)
                    .addComponent(enzymesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(precursorIonLbl1)
                    .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentIonType1Lbl1)
                    .addComponent(fragmentIon2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentIon1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proteaseAndFragmentationPanel1Layout.createSequentialGroup()
                        .addComponent(maxMissedCleavagesLabel1)
                        .addGap(38, 38, 38)
                        .addComponent(precursorChargeLbl1))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proteaseAndFragmentationPanel1Layout.createSequentialGroup()
                        .addComponent(missedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fragmentIonLbl1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(precursorChargeRangeLabel1))))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modificationsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(setStandard, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(SaveConfigurationAs, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(proteaseAndFragmentationPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(proteaseAndFragmentationPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SaveConfigurationAs)
                    .addComponent(setStandard))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Resize the layered panes.
     *
     * @param evt
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
    }//GEN-LAST:event_formComponentResized

    private void enzymesCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enzymesCmbActionPerformed
        //        if (defaultConfigsLoaded) {
        //            configurationFileTxtSearchTab_parameters.setText(userSettingsTxt); // @TODO: re-add this??
        //        }
    }//GEN-LAST:event_enzymesCmbActionPerformed

    private String[] loadEnzymes() {

        ArrayList<String> tempEnzymes = new ArrayList<String>();

        for (int i = 0; i < enzymeFactory.getEnzymes().size(); i++) {
            tempEnzymes.add(enzymeFactory.getEnzymes().get(i).getName());
        }

        Collections.sort(tempEnzymes);

        String[] enzymes = new String[tempEnzymes.size()];

        for (int i = 0; i < tempEnzymes.size(); i++) {
            enzymes[i] = tempEnzymes.get(i);
        }

        return enzymes;
    }

    public boolean validateParametersInput(boolean showMessage) {

        boolean valid = true;
        precursorIonLbl1.setForeground(Color.BLACK);
        maxMissedCleavagesLabel1.setForeground(Color.BLACK);
        fragmentIonLbl1.setForeground(Color.BLACK);
        precursorChargeLbl1.setForeground(Color.BLACK);
        databaseSettingsLbl.setForeground(Color.BLACK);

        precursorIonLbl1.setToolTipText(null);
        maxMissedCleavagesLabel1.setToolTipText(null);
        fragmentIonLbl1.setToolTipText(null);
        precursorChargeLbl1.setToolTipText(null);
        databaseSettingsLbl.setToolTipText(null);

        if (databaseSettingsTxt.getText() == null || databaseSettingsTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a search database.", "Search Database Not Found", JOptionPane.WARNING_MESSAGE);
            }
            databaseSettingsLbl.setForeground(Color.RED);
            databaseSettingsLbl.setToolTipText("Please select a valid '.fasta' or '.fas' database file");
            valid = false;
        } else {
            File test = new File(databaseSettingsTxt.getText().trim());
            if (!test.exists()) {
                if (showMessage && valid) {
                    JOptionPane.showMessageDialog(this, "Database file could not be found.", "Search Database Not Found!", JOptionPane.WARNING_MESSAGE);
                }
                databaseSettingsLbl.setForeground(Color.RED);
                databaseSettingsLbl.setToolTipText("Database file could not be found!");
                valid = false;
            }
        }

        // Validate missed cleavages (multi-step validation).
        if (missedCleavagesTxt.getText() == null || missedCleavagesTxt.getText().trim().equals("")) {
            if (showMessage) {
                JOptionPane.showMessageDialog(this, "You need to specify a number of allowed missed cleavages.", "Missed Cleavages Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxMissedCleavagesLabel1.setForeground(Color.RED);
            maxMissedCleavagesLabel1.setToolTipText("Please select the number of allowed missed cleavages");
        }

        // OK, see if it is an integer.
        int missedCleavages = -1;

        try {
            missedCleavages = Integer.parseInt(missedCleavagesTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable integer!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the allowed missed cleavages.",
                        "Missed Cleavages Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            maxMissedCleavagesLabel1.setForeground(Color.RED);
            maxMissedCleavagesLabel1.setToolTipText("Please select a positive integer");
        }

        // And it should be zero or more.
        if (missedCleavages < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the allowed missed cleavages.",
                        "Missed Cleavages Error", JOptionPane.WARNING_MESSAGE);
                missedCleavagesTxt.requestFocus();
            }
            valid = false;
            maxMissedCleavagesLabel1.setForeground(Color.RED);
            maxMissedCleavagesLabel1.setToolTipText("Please select a positive integer");
        }

        // Validate precursor mass tolerances
        if (precursorIonAccuracyTxt.getText() == null || precursorIonAccuracyTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a precursor mass tolerance.",
                        "Precursor Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorIonLbl1.setForeground(Color.RED);
            precursorIonLbl1.setToolTipText("Please select a precursor mass tolerance");
        }

        // OK, see if it is a number.
        float precursorTolerance = -1;

        try {
            precursorTolerance = Float.parseFloat(precursorIonAccuracyTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number (zero or more) for the precursor mass tolerance.",
                        "Precursor Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorIonLbl1.setForeground(Color.RED);
            precursorIonLbl1.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (precursorTolerance < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number (zero or more) for the precursor mass tolerance.",
                        "Precursor Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorIonLbl1.setForeground(Color.RED);
            precursorIonLbl1.setToolTipText("Please select a positive number");
        }

        // Validate fragment mass tolerances
        if (fragmentIonAccuracyTxt.getText() == null || fragmentIonAccuracyTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a fragment mass tolerance.",
                        "Fragment Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            fragmentIonLbl1.setForeground(Color.RED);
            fragmentIonLbl1.setToolTipText("Please select the fragment mass tolerance");
        }

        // OK, see if it is a number.
        float fragmentTolerance = -1;

        try {
            fragmentTolerance = Float.parseFloat(fragmentIonAccuracyTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number (zero or more) for the fragment mass tolerance.",
                        "Fragment Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            fragmentIonLbl1.setForeground(Color.RED);
            fragmentIonLbl1.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (fragmentTolerance < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number (zero or more) for the fragment mass tolerance.",
                        "Fragment Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            fragmentIonLbl1.setForeground(Color.RED);
            fragmentIonLbl1.setToolTipText("Please select a positive number");
        }

        // Validate precursor charge lower bound
        if (minPrecursorChargeTxt.getText() == null || minPrecursorChargeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a lower bound for the precursor charge first.",
                        "Lower Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl1.setForeground(Color.RED);
            precursorChargeLbl1.setToolTipText("Please select a lower bound for the precursor charge");
        }

        // OK, see if it is an integer.
        int chargeLowerBound = -1;

        try {
            chargeLowerBound = Integer.parseInt(minPrecursorChargeTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the lower bound of the precursor charge.",
                        "Lower Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl1.setForeground(Color.RED);
            precursorChargeLbl1.setToolTipText("Please select positive integers");
        }

        if (chargeLowerBound <= 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the lower bound of the precursor charge.",
                        "Lower Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl1.setForeground(Color.RED);
            precursorChargeLbl1.setToolTipText("Please select positive integers");
        }

        // Validate precursor charge upper bound
        if (maxPrecursorChargeTxt.getText() == null || maxPrecursorChargeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify an upper bound for the precursor charge.",
                        "Upper Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl1.setForeground(Color.RED);
            precursorChargeLbl1.setToolTipText("Please select an upper bound for the precursor charge");
        }

        // OK, see if it is an integer.
        int chargeUpperBound = -1;

        try {
            chargeUpperBound = Integer.parseInt(maxPrecursorChargeTxt.getText().trim());
        } catch (NumberFormatException nfe) {
            // Unparseable number!
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the upper bound of the precursor charge.",
                        "Upper Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl1.setForeground(Color.RED);
            precursorChargeLbl1.setToolTipText("Please select positive integers");
        }

        if (chargeUpperBound <= 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the upper bound of the precursor charge.",
                        "Upper Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl1.setForeground(Color.RED);
            precursorChargeLbl1.setToolTipText("Please select positive integers");
        }

        if (chargeUpperBound < chargeLowerBound) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "The minimum precursor charge must be lower than or equal to the maximum precursor charge.",
                        "Precursor Charge Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl1.setForeground(Color.RED);
            precursorChargeLbl1.setToolTipText("Minimum precursor charge > Maximum precursor charge!");
        }

        return valid;
    }
    private void missedCleavagesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_missedCleavagesTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_missedCleavagesTxtKeyReleased

    private void precursorIonAccuracyTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precursorIonAccuracyTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_precursorIonAccuracyTxtKeyReleased

    private void fragmentIonAccuracyTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fragmentIonAccuracyTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_fragmentIonAccuracyTxtKeyReleased

    private void minPrecursorChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecursorChargeTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_minPrecursorChargeTxtKeyReleased

    private void maxPrecursorChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPrecursorChargeTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_maxPrecursorChargeTxtKeyReleased

    private void targetDecoySettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetDecoySettingsButtonActionPerformed
    }//GEN-LAST:event_targetDecoySettingsButtonActionPerformed

    private void SaveConfigurationAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveConfigurationAsActionPerformed
        saveAsPressed();
    }//GEN-LAST:event_SaveConfigurationAsActionPerformed

    private void fixedModsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fixedModsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_fixedModsTableMouseExited
    public void saveAsPressed() {

        if (validateParametersInput(true)) {

            // First check whether a file has already been selected.
            // If so, start from that file's parent.
            File startLocation = new File(this.getClass().getClassLoader().toString());

            boolean complete = false;

            while (!complete) {
                JFileChooser fc = new JFileChooser(startLocation);
                FileFilter filter = new FileFilter() {
                    @Override
                    public boolean accept(File myFile) {

                        return myFile.getName().toLowerCase().endsWith(".parameters")
                                || myFile.isDirectory();
                    }

                    @Override
                    public String getDescription() {
                        return "SearchGUI search parameters";
                    }
                };
                fc.setFileFilter(filter);
                int result = fc.showSaveDialog(this);

                if (result == JFileChooser.APPROVE_OPTION) {
                    File selected = fc.getSelectedFile();
                    //searchGUI.setLastSelectedFolder(selected.getAbsolutePath());
                    // Make sure the file is appended with '.parameters'
                    if (!selected.getName().toLowerCase().endsWith(".parameters")) {
                        selected = new File(selected.getParentFile(), selected.getName() + ".parameters");
                        parametersFile = selected;
                    } else {
                        selected = new File(selected.getParentFile(), selected.getName());
                        parametersFile = selected;
                    }
                    complete = true;
                    if (selected.exists()) {
                        int choice = JOptionPane.showConfirmDialog(this,
                                new String[]{"The file " + selected.getName() + " already exists.", "Overwrite?"},
                                "File Already Exists", JOptionPane.YES_NO_OPTION);
                        if (choice == JOptionPane.NO_OPTION) {
                            complete = false;
                        }
                    }
                } else {
                    return;
                }
            }

            savePressed();
            searchParameters.setParametersFile(parametersFile);
        }
    }

    public void savePressed() {
        if (parametersFile == null) {
            saveAsPressed();
        } else if (validateParametersInput(true)) {
            try {
                searchParameters = getSearchParameters();
                SearchParameters.saveIdentificationParameters(searchParameters, parametersFile);
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, new String[]{"An error occurred while witing: " + parametersFile.getName(), e.getMessage()}, "Error Saving File", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public SearchParameters getSearchParameters() {

        String dbPath = databaseSettingsTxt.getText().trim();
        if (!dbPath.equals("")) {
            File fastaFile = new File(databaseSettingsTxt.getText().trim());
            searchParameters.setFastaFile(fastaFile);
        }

        Enzyme enzyme = enzymeFactory.getEnzyme(enzymesCmb.getSelectedItem().toString());
        searchParameters.setEnzyme(enzyme);

        ModificationProfile modificationProfile = new ModificationProfile();
        for (int i = 0; i < fixedModsTable.getRowCount(); i++) {
            String modName = (String) fixedModsTable.getValueAt(i, 1);
            modificationProfile.addFixedModification(ptmFactory.getPTM(modName));
        }

        for (int i = 0; i < variableModsTable.getRowCount(); i++) {
            String modName = (String) variableModsTable.getValueAt(i, 1);
            modificationProfile.addVariableModification(ptmFactory.getPTM(modName));
        }
        searchParameters.setModificationProfile(modificationProfile);

        searchParameters.setnMissedCleavages(new Integer(missedCleavagesTxt.getText().trim()));
        searchParameters.setPrecursorAccuracy(new Double(precursorIonAccuracyTxt.getText().trim()));
        if (precursorIonUnit.getSelectedIndex() == 0) {
            searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.PPM);
        } else {
            searchParameters.setPrecursorAccuracyType(SearchParameters.PrecursorAccuracyType.DA);
        }
        searchParameters.setFragmentIonAccuracy(new Double(fragmentIonAccuracyTxt.getText().trim()));
        searchParameters.setIonSearched1(fragmentIon1Cmb.getSelectedItem().toString().trim());
        searchParameters.setIonSearched2(fragmentIon2Cmb.getSelectedItem().toString().trim());
        int charge = new Integer(minPrecursorChargeTxt.getText().trim());
        searchParameters.setMinChargeSearched(new Charge(Charge.PLUS, charge));
        charge = new Integer(maxPrecursorChargeTxt.getText().trim());
        searchParameters.setMaxChargeSearched(new Charge(Charge.PLUS, charge));

        return searchParameters;
    }

    private void fixedModsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fixedModsTableMouseReleased
        int row = fixedModsTable.rowAtPoint(evt.getPoint());
        int column = fixedModsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == fixedModsTable.getColumn(" ").getModelIndex()) {
                Color newColor = JColorChooser.showDialog(this, "Pick a Color", (Color) fixedModsTable.getValueAt(row, column));

                if (newColor != null) {
                    searchParameters.getModificationProfile().setColor((String) fixedModsTable.getValueAt(row, 1), newColor);
                    fixedModsTable.setValueAt(newColor, row, 0);
                    ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
                    fixedModsTable.repaint();
                }
            }
        }

    }//GEN-LAST:event_fixedModsTableMouseReleased

    private void fixedModsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fixedModsTableMouseMoved
        int row = fixedModsTable.rowAtPoint(evt.getPoint());
        int column = fixedModsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == fixedModsTable.getColumn(" ").getModelIndex()) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_fixedModsTableMouseMoved

    private void variableModsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_variableModsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_variableModsTableMouseExited

    private void variableModsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_variableModsTableMouseReleased
        int row = variableModsTable.rowAtPoint(evt.getPoint());
        int column = variableModsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == variableModsTable.getColumn(" ").getModelIndex()) {
                Color newColor = JColorChooser.showDialog(this, "Pick a Color", (Color) variableModsTable.getValueAt(row, column));

                if (newColor != null) {
                    searchParameters.getModificationProfile().setColor((String) variableModsTable.getValueAt(row, 1), newColor);
                    variableModsTable.setValueAt(newColor, row, 0);
                    ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
                    variableModsTable.repaint();
                }
            }
        }

    }//GEN-LAST:event_variableModsTableMouseReleased

    private void variableModsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_variableModsTableMouseMoved
        int row = variableModsTable.rowAtPoint(evt.getPoint());
        int column = variableModsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == variableModsTable.getColumn(" ").getModelIndex()) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_variableModsTableMouseMoved

    private void modificationTypesSplitPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_modificationTypesSplitPaneComponentResized
        modificationTypesSplitPane.setDividerLocation(0.5);
    }//GEN-LAST:event_modificationTypesSplitPaneComponentResized

    private void setStandardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setStandardActionPerformed

        SearchParameters tempSearchParameters = getSearchParameters();
        //transfer the parameters to the MainClientGui as well !
        MainClientGUI.setSearchParameters(tempSearchParameters);
        MainClientGUI.setUsingDefault(false);
        try {
            tempSearchParameters.saveIdentificationParameters(tempSearchParameters, searchParametersFile);
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        } catch (ClassNotFoundException ex) {
            logger.error(ex);
        }
        tempSearchParameters.setParametersFile(null);
        setStandard.setEnabled(false);
        logger.info("The searchparameters for task " + taskID + " have been set as the default parameters.");
        //  searchGUI.setSearchParameters(tempSearchParameters);
        //  searchGUI.setUsedModificationsAsString(getModificationUse());
    }//GEN-LAST:event_setStandardActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton SaveConfigurationAs;
    private javax.swing.JPanel dataBasePanelSettings;
    private javax.swing.JLabel databaseSettingsLbl;
    private javax.swing.JTextField databaseSettingsTxt;
    private javax.swing.JLabel enzymeLabel1;
    private javax.swing.JComboBox enzymesCmb;
    private javax.swing.JLabel fixedModificationsLabel;
    private javax.swing.JScrollPane fixedModsJScrollPane;
    private javax.swing.JTable fixedModsTable;
    private javax.swing.JComboBox fragmentIon1Cmb;
    private javax.swing.JComboBox fragmentIon2Cmb;
    private javax.swing.JTextField fragmentIonAccuracyTxt;
    private javax.swing.JLabel fragmentIonLbl1;
    private javax.swing.JLabel fragmentIonType1Lbl1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel maxMissedCleavagesLabel1;
    private javax.swing.JTextField maxPrecursorChargeTxt;
    private javax.swing.JTextField minPrecursorChargeTxt;
    private javax.swing.JTextField missedCleavagesTxt;
    private javax.swing.JSplitPane modificationTypesSplitPane;
    private javax.swing.JPanel modificationsPanel;
    private javax.swing.JLabel precursorChargeLbl1;
    private javax.swing.JLabel precursorChargeRangeLabel1;
    private javax.swing.JTextField precursorIonAccuracyTxt;
    private javax.swing.JLabel precursorIonLbl1;
    private javax.swing.JComboBox precursorIonUnit;
    private javax.swing.JPanel proteaseAndFragmentationPanel1;
    private javax.swing.JButton setStandard;
    private javax.swing.JTable statisticsTableComputing;
    private javax.swing.JTable statisticsTableProject;
    private javax.swing.JTable statisticsTableTask;
    private javax.swing.JButton targetDecoySettingsButton;
    private javax.swing.JLabel variableModificationsLabel;
    private javax.swing.JScrollPane variableModsJScrollPane;
    private javax.swing.JTable variableModsTable;
    // End of variables declaration//GEN-END:variables

    private void getProjectInfo() {
        //try to get the status from the server

        ServerConnector serverConnector = new ServerConnector();
        TaskContainer aSearchQuery = new TaskContainer();
        aSearchQuery.setInstructionMap(currentUserMap);
        aSearchQuery.updateInstruction("instruction", "getSpecificTask");
        aSearchQuery.updateInstruction("taskID", "" + taskID);
        logger.debug("Reaching server for task...");
        try {
            myProjectInfo = (HashMap<String, Object>) serverConnector.getSpecificFromServer(aSearchQuery);
            for (String aString : myProjectInfo.keySet()) {
                try {
                    System.out.println(aString + " : " + myProjectInfo.get(aString).toString());
                    // this.searchParameters = (SearchParameters) myProjectInfo.get("SearchParameters");
                    String projectId = (String) myProjectInfo.get("PROJECTID").toString();
                    StringBuilder myRepFiles = new StringBuilder().append("Z:/remote_relims/Repository/");
                    if (myProjectInfo.get("SOURCEID").equals("pride")) {
                        //get the project parameters file from the NAS storage
                        myRepFiles.append("/PRIDE/").append(projectId);
                    } else {
                        myRepFiles.append("/MSLIMS/").append(projectId);
                    }
                    searchParametersFile = new File(myRepFiles.toString());
                } catch (NullPointerException e) {
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (IOException ex) {
            logger.error("Could not retrieve tasks from the server...");
        }
    }
}