package com.compomics.relims.modes.networking.client.GUI;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.ions.PeptideFragmentIon;
import com.compomics.util.experiment.identification.SearchParameters;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.experiment.io.identifications.IdentificationParametersReader;
import com.compomics.util.experiment.massspectrometry.Charge;
import com.compomics.util.preferences.ModificationProfile;
import com.compomics.util.gui.ptm.ModificationsDialog;
import com.compomics.util.gui.ptm.PtmDialogParent;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import eu.isas.searchgui.gui.SearchGUI;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;
import org.xmlpull.v1.XmlPullParserException;
import org.apache.log4j.Logger;

/**
 * The SearchGUI settings dialog.
 *
 * @author Harald Barsnes
 */
public class SearchParameterViewer extends javax.swing.JDialog implements PtmDialogParent {

    private static final Logger logger = Logger.getLogger(SearchParameterViewer.class);
    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;
    /**
     * The sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
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
    /**
     * The post translational modifications factory.
     */
    private PTMFactory ptmFactory = PTMFactory.getInstance();
    /**
     * The parameter file.
     */
    private File parametersFile = null;
    /**
     * The modifications and their usage by the user.
     */
    private HashMap<String, Integer> modificationUse = new HashMap<String, Integer>();
    /**
     * The SearchGUI main frame.
     */
    private SearchGUI searchGUI;
    /*
     * The search parameters.
     */
    private SearchParameters searchParameters;
    /**
     * Reference for the separation of modifications.
     */
    public static final String MODIFICATION_SEPARATOR = "//";
    /**
     * Reference for the separation of modification and its frequency.
     */
    public static final String MODIFICATION_USE_SEPARATOR = "_";
    private File enzymeFile = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "configuration" + File.separator + "searchGUI_enzymes.xml");
    private File searchParametersFile = new File("src" + File.separator + "main" + File.separator + "resources" + File.separator + "configuration" + File.separator + "lastUsedParameters.parameters");
    private long taskID = 0L;

    /**
     * Creates a new SettingsDialog.
     *
     * @param searchGUI
     * @param searchParameters
     * @param modal
     */
    public SearchParameterViewer(File searchParametersFile, long taskID) {
        this.taskID = taskID;
        this.searchParametersFile = searchParametersFile;
        setSearchParametersFromFile();
        initializeEnzymeFactory();
        initComponents();
        setUpGUI();
        formComponentResized(null);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
            }
        });
    }

    public SearchParameterViewer(SearchParameters searchParameters, long taskID) {
        this.taskID = taskID;
        this.searchParameters = searchParameters;
        initializeEnzymeFactory();
        initComponents();
        setUpGUI();
        formComponentResized(null);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setVisible(true);
            }
        });
    }

    private void setSearchParametersFromFile() {
        //set the correct searchParameters file 

        try {
            searchParameters = SearchParameters.getIdentificationParameters(searchParametersFile);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to import search parameters from: " + searchParametersFile.getAbsolutePath() + ".", "Search Parameters",
                    JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }

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

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {

        setScreenProps();
        validateParametersInput(false);

        modificationTypesSplitPane.setDividerLocation(0.5);

        // centrally align the comboboxes
        enzymesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIon1Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIon2Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        precursorIonUnit.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        ((TitledBorder) dataBasePanelSettings.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Database" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);
        ((TitledBorder) modificationsPanel.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Modifications" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);
        ((TitledBorder) proteaseAndFragmentationPanel.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Protease & Fragmentation" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);

        fixedModsJScrollPane.getViewport().setOpaque(false);
        variableModsJScrollPane.getViewport().setOpaque(false);

        fixedModsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        variableModsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());

        fixedModsTable.getColumn(" ").setMaxWidth(35);
        fixedModsTable.getColumn(" ").setMinWidth(35);
        variableModsTable.getColumn(" ").setMaxWidth(35);
        variableModsTable.getColumn(" ").setMinWidth(35);

        fixedModsTable.getColumn("Mass").setMaxWidth(100);
        fixedModsTable.getColumn("Mass").setMinWidth(100);
        variableModsTable.getColumn("Mass").setMaxWidth(100);
        variableModsTable.getColumn("Mass").setMinWidth(100);

        loadModificationsInGUI();
    }

    /**
     * Loads the modifications.
     */
    private void loadModificationsInGUI() {
        loadModificationUse(getModFile());
        updateModificationList();
    }

    private String getModFile() {
        String result = "";

        File configFile = new File("src" + File.separator + "resources" + File.separator + "configuration" + File.separator + "searchGUI_configuration.txt");

        if (configFile.exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(configFile));
                String line;
                while ((line = br.readLine()) != null) {
                    // Skip empty lines and comment ('#') lines.
                    line = line.trim();
                    if (line.equals("") || line.startsWith("#")) {
                    } else if (line.equals("Modification use:")) {
                        result = br.readLine().trim();
                    }
                }
                br.close();
            } catch (IOException ioe) {
                ioe.printStackTrace(); // @TODO: this exception should be thrown to the GUI!
                JOptionPane.showMessageDialog(null, "An error occured when trying to load the modifications preferences.",
                        "Configuration import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return result;
    }

    /**
     * Loads the use of modifications from a line.
     *
     * @param aLine modification use line from the configuration file
     */
    public void loadModificationUse(String aLine) {
        ArrayList<String> modificationUses = new ArrayList<String>();

        // Split the different modifications.
        int start;

        while ((start = aLine.indexOf(MODIFICATION_SEPARATOR)) >= 0) {
            String name = aLine.substring(0, start);
            aLine = aLine.substring(start + 2);
            if (!name.trim().equals("")) {
                modificationUses.add(name);
            }
        }

        for (String name : modificationUses) {
            start = name.indexOf("_");
            String modificationName = name.substring(0, start);
            int number = Integer.parseInt(name.substring(start + 1));
            if (ptmFactory.containsPTM(modificationName)) {
                modificationUse.put(modificationName, number);
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

        modificationOptionsPopupMenu = new javax.swing.JPopupMenu();
        editModificationsMenuItem = new javax.swing.JMenuItem();
        backgroundPanel = new javax.swing.JPanel();
        proteaseAndFragmentationPanel = new javax.swing.JPanel();
        enzymeLabel = new javax.swing.JLabel();
        enzymesCmb = new javax.swing.JComboBox();
        maxMissedCleavagesLabel = new javax.swing.JLabel();
        missedCleavagesTxt = new javax.swing.JTextField();
        precursorIonLbl = new javax.swing.JLabel();
        precursorIonAccuracyTxt = new javax.swing.JTextField();
        precursorIonUnit = new javax.swing.JComboBox();
        fragmentIonLbl = new javax.swing.JLabel();
        fragmentIonAccuracyTxt = new javax.swing.JTextField();
        fragmentIonType1Lbl = new javax.swing.JLabel();
        fragmentIon1Cmb = new javax.swing.JComboBox();
        fragmentIon2Cmb = new javax.swing.JComboBox();
        precursorChargeLbl = new javax.swing.JLabel();
        minPrecursorChargeTxt = new javax.swing.JTextField();
        maxPrecursorChargeTxt = new javax.swing.JTextField();
        precursorChargeRangeLabel = new javax.swing.JLabel();
        dataBasePanelSettings = new javax.swing.JPanel();
        databaseSettingsLbl = new javax.swing.JLabel();
        databaseSettingsTxt = new javax.swing.JTextField();
        targetDecoySettingsButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        SaveConfigurationAs = new javax.swing.JButton();
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
        setStandard = new javax.swing.JButton();

        editModificationsMenuItem.setText("Edit Modifications");
        editModificationsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModificationsMenuItemActionPerformed(evt);
            }
        });
        modificationOptionsPopupMenu.add(editModificationsMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Search Settings");
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        proteaseAndFragmentationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Protease & Fragmentation"));
        proteaseAndFragmentationPanel.setOpaque(false);

        enzymeLabel.setText("Protease");

        enzymesCmb.setModel(new DefaultComboBoxModel(loadEnzymes()));
        enzymesCmb.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enzymesCmbActionPerformed(evt);
            }
        });

        maxMissedCleavagesLabel.setText("Max Missed Cleavages");

        missedCleavagesTxt.setEditable(false);
        missedCleavagesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        missedCleavagesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                missedCleavagesTxtKeyReleased(evt);
            }
        });

        precursorIonLbl.setText("Precursor Mass Tolerance");

        precursorIonAccuracyTxt.setEditable(false);
        precursorIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precursorIonAccuracyTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                precursorIonAccuracyTxtKeyReleased(evt);
            }
        });

        precursorIonUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ppm", "Da" }));

        fragmentIonLbl.setText("Fragment Mass Tolerance (Da)");

        fragmentIonAccuracyTxt.setEditable(false);
        fragmentIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        fragmentIonAccuracyTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                fragmentIonAccuracyTxtKeyReleased(evt);
            }
        });

        fragmentIonType1Lbl.setText("Fragment Ion Types");

        fragmentIon1Cmb.setModel(new DefaultComboBoxModel(forwardIons));

        fragmentIon2Cmb.setModel(new DefaultComboBoxModel(rewindIons));

        precursorChargeLbl.setText("Precursor Charge");

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

        precursorChargeRangeLabel.setText("-");

        javax.swing.GroupLayout proteaseAndFragmentationPanelLayout = new javax.swing.GroupLayout(proteaseAndFragmentationPanel);
        proteaseAndFragmentationPanel.setLayout(proteaseAndFragmentationPanelLayout);
        proteaseAndFragmentationPanelLayout.setHorizontalGroup(
            proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(maxMissedCleavagesLabel)
                    .addComponent(fragmentIonLbl)
                    .addComponent(precursorChargeLbl)
                    .addComponent(fragmentIonType1Lbl)
                    .addComponent(enzymeLabel)
                    .addComponent(precursorIonLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                            .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 136, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(precursorIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(enzymesCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                            .addComponent(fragmentIon1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(fragmentIon2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, proteaseAndFragmentationPanelLayout.createSequentialGroup()
                            .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGap(18, 18, 18)
                            .addComponent(precursorChargeRangeLabel)
                            .addGap(18, 18, 18)
                            .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(missedCleavagesTxt, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 105, Short.MAX_VALUE)
                        .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.Alignment.LEADING)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        proteaseAndFragmentationPanelLayout.setVerticalGroup(
            proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enzymeLabel)
                    .addComponent(enzymesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(precursorIonLbl)
                    .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentIonType1Lbl)
                    .addComponent(fragmentIon2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentIon1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addComponent(maxMissedCleavagesLabel)
                        .addGap(38, 38, 38)
                        .addComponent(precursorChargeLbl))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addComponent(missedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(fragmentIonLbl))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(precursorChargeRangeLabel))))
                .addContainerGap())
        );

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

        cancelButton.setText("Close");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        SaveConfigurationAs.setText("Save Locally");
        SaveConfigurationAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveConfigurationAsActionPerformed(evt);
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
                .addComponent(fixedModificationsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 107, Short.MAX_VALUE)
                .addGap(371, 371, 371))
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addComponent(fixedModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(61, 61, 61))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fixedModificationsLabel)
                .addGap(6, 6, 6)
                .addComponent(fixedModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 99, Short.MAX_VALUE))
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
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                        .addGap(60, 60, 60))))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(variableModificationsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE))
        );

        modificationTypesSplitPane.setRightComponent(jPanel9);

        javax.swing.GroupLayout modificationsPanelLayout = new javax.swing.GroupLayout(modificationsPanel);
        modificationsPanel.setLayout(modificationsPanelLayout);
        modificationsPanelLayout.setHorizontalGroup(
            modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(modificationTypesSplitPane)
                .addGap(37, 37, 37))
        );
        modificationsPanelLayout.setVerticalGroup(
            modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsPanelLayout.createSequentialGroup()
                .addComponent(modificationTypesSplitPane)
                .addContainerGap())
        );

        setStandard.setText("Set as standard");
        setStandard.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setStandardActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modificationsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(proteaseAndFragmentationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addComponent(setStandard)
                        .addGap(96, 96, 96)
                        .addComponent(SaveConfigurationAs, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(proteaseAndFragmentationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setStandard)
                    .addComponent(SaveConfigurationAs)
                    .addComponent(cancelButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
     * Saves the parameters to file.
     *
     * @param evt
     */
    private void SaveConfigurationAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveConfigurationAsActionPerformed
        saveAsPressed();
    }//GEN-LAST:event_SaveConfigurationAsActionPerformed

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void missedCleavagesTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_missedCleavagesTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_missedCleavagesTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void precursorIonAccuracyTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_precursorIonAccuracyTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_precursorIonAccuracyTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void fragmentIonAccuracyTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_fragmentIonAccuracyTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_fragmentIonAccuracyTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void minPrecursorChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_minPrecursorChargeTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_minPrecursorChargeTxtKeyReleased

    /**
     * Validates the parameters.
     *
     * @param evt
     */
    private void maxPrecursorChargeTxtKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_maxPrecursorChargeTxtKeyReleased
        validateParametersInput(false);
    }//GEN-LAST:event_maxPrecursorChargeTxtKeyReleased

    /**
     * Generates a target-decoy database.
     *
     * @param evt
     */
    private void targetDecoySettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetDecoySettingsButtonActionPerformed
        generateTargetDecoyDatabase();
    }//GEN-LAST:event_targetDecoySettingsButtonActionPerformed

    /**
     * Make sure that the fixed and variable modification panels have equal
     * size.
     *
     * @param evt
     */
    private void modificationTypesSplitPaneComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_modificationTypesSplitPaneComponentResized
        modificationTypesSplitPane.setDividerLocation(0.5);
    }//GEN-LAST:event_modificationTypesSplitPaneComponentResized

    /**
     * Save if settings changed.
     *
     * @param evt
     */
    private void enzymesCmbActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enzymesCmbActionPerformed
//        if (defaultConfigsLoaded) {
//            configurationFileTxtSearchTab_parameters.setText(userSettingsTxt); // @TODO: re-add this??
//        }
    }//GEN-LAST:event_enzymesCmbActionPerformed

    /**
     * Open the ModificationsDialog.
     *
     * @param evt
     */
    private void editModificationsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModificationsMenuItemActionPerformed
        new ModificationsDialog(searchGUI, this, true);
    }//GEN-LAST:event_editModificationsMenuItemActionPerformed

    /**
     * Resize the layered panes.
     *
     * @param evt
     */
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
    }//GEN-LAST:event_formComponentResized

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void fixedModsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_fixedModsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_fixedModsTableMouseExited

    /**
     * Changes the cursor to a hand cursor if over the color column.
     *
     * @param evt
     */
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

    /**
     * Opens a file chooser where the color for the ptm can be changed.
     *
     * @param evt
     */
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

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void variableModsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_variableModsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_variableModsTableMouseExited

    /**
     * Changes the cursor to a hand cursor if over the color column.
     *
     * @param evt
     */
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

    /**
     * Opens a file chooser where the color for the ptm can be changed.
     *
     * @param evt
     */
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

    /**
     * Close the window without saving the changes.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

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
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel dataBasePanelSettings;
    private javax.swing.JLabel databaseSettingsLbl;
    private javax.swing.JTextField databaseSettingsTxt;
    private javax.swing.JMenuItem editModificationsMenuItem;
    private javax.swing.JLabel enzymeLabel;
    private javax.swing.JComboBox enzymesCmb;
    private javax.swing.JLabel fixedModificationsLabel;
    private javax.swing.JScrollPane fixedModsJScrollPane;
    private javax.swing.JTable fixedModsTable;
    private javax.swing.JComboBox fragmentIon1Cmb;
    private javax.swing.JComboBox fragmentIon2Cmb;
    private javax.swing.JTextField fragmentIonAccuracyTxt;
    private javax.swing.JLabel fragmentIonLbl;
    private javax.swing.JLabel fragmentIonType1Lbl;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel maxMissedCleavagesLabel;
    private javax.swing.JTextField maxPrecursorChargeTxt;
    private javax.swing.JTextField minPrecursorChargeTxt;
    private javax.swing.JTextField missedCleavagesTxt;
    private javax.swing.JPopupMenu modificationOptionsPopupMenu;
    private javax.swing.JSplitPane modificationTypesSplitPane;
    private javax.swing.JPanel modificationsPanel;
    private javax.swing.JLabel precursorChargeLbl;
    private javax.swing.JLabel precursorChargeRangeLabel;
    private javax.swing.JTextField precursorIonAccuracyTxt;
    private javax.swing.JLabel precursorIonLbl;
    private javax.swing.JComboBox precursorIonUnit;
    private javax.swing.JPanel proteaseAndFragmentationPanel;
    private javax.swing.JButton setStandard;
    private javax.swing.JButton targetDecoySettingsButton;
    private javax.swing.JLabel variableModificationsLabel;
    private javax.swing.JScrollPane variableModsJScrollPane;
    private javax.swing.JTable variableModsTable;
    // End of variables declaration//GEN-END:variables

    /**
     * Loads the implemented enzymes.
     *
     * @return the list of enzyme names
     */
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

    /**
     * This method is called when the user clicks the 'Load' button.
     */
    /**
     * This method is called when the user clicks the 'Save' button.
     */
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

    /**
     * Verifies that the modifications backed-up in the search parameters are
     * loaded and alerts the user in case conflicts are found.
     *
     * @param searchParameters the search parameters to load
     */
    private void loadModifications() {
        ArrayList<String> toCheck = ptmFactory.loadBackedUpModifications(searchParameters, false); // @TODO: have to set the searchparams???
        if (!toCheck.isEmpty()) {
            String message = "The definition of the following PTM(s) seems to have change and was not loaded:\n";
            for (int i = 0; i < toCheck.size(); i++) {
                if (i > 0) {
                    if (i < toCheck.size() - 1) {
                        message += ", ";
                    } else {
                        message += " and ";
                    }
                    message += toCheck.get(i);
                }
            }
            message += ".\nPlease verify the definition of the PTM(s) in the modifications editor.";
            javax.swing.JOptionPane.showMessageDialog(null,
                    message,
                    "PTM definition obsolete", JOptionPane.OK_OPTION);
        }
    }

    /**
     * This method takes the specified search parameters instance and reads the
     * values for (some of) the GUI components from it.
     *
     * @param aSearchParameters searchParameters with the values for the GUI.
     */
    private void setScreenProps() {

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

    /**
     * This method loads the necessary parameters for populating (part of) the
     * GUI from a properties file.
     *
     * @deprecated use SearchParameters instead
     * @param aFile File with the relevant properties file.
     * @return Properties with the loaded properties.
     */
    private Properties loadProperties(File aFile) {
        Properties screenProps = new Properties();
        try {
            FileInputStream fis = new FileInputStream(aFile);
            if (fis != null) {
                screenProps.load(fis);
                fis.close();
            } else {
                throw new IllegalArgumentException("Could not read the file you specified ('" + aFile.getAbsolutePath() + "').");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(this, new String[]{"Unable to read file: " + aFile.getName(), ioe.getMessage()}, "Error Reading File", JOptionPane.WARNING_MESSAGE);
        }
        return screenProps;
    }

    /**
     * This method is called when the user clicks the 'Save As' button.
     */
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

    /**
     * Inspects the parameters validity.
     *
     * @param showMessage if true an error messages are shown to the users
     * @return a boolean indicating if the parameters are valid
     */
    public boolean validateParametersInput(boolean showMessage) {

        boolean valid = true;
        precursorIonLbl.setForeground(Color.BLACK);
        maxMissedCleavagesLabel.setForeground(Color.BLACK);
        fragmentIonLbl.setForeground(Color.BLACK);
        precursorChargeLbl.setForeground(Color.BLACK);
        databaseSettingsLbl.setForeground(Color.BLACK);

        precursorIonLbl.setToolTipText(null);
        maxMissedCleavagesLabel.setToolTipText(null);
        fragmentIonLbl.setToolTipText(null);
        precursorChargeLbl.setToolTipText(null);
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
            maxMissedCleavagesLabel.setForeground(Color.RED);
            maxMissedCleavagesLabel.setToolTipText("Please select the number of allowed missed cleavages");
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
            maxMissedCleavagesLabel.setForeground(Color.RED);
            maxMissedCleavagesLabel.setToolTipText("Please select a positive integer");
        }

        // And it should be zero or more.
        if (missedCleavages < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the allowed missed cleavages.",
                        "Missed Cleavages Error", JOptionPane.WARNING_MESSAGE);
                missedCleavagesTxt.requestFocus();
            }
            valid = false;
            maxMissedCleavagesLabel.setForeground(Color.RED);
            maxMissedCleavagesLabel.setToolTipText("Please select a positive integer");
        }

        // Validate precursor mass tolerances
        if (precursorIonAccuracyTxt.getText() == null || precursorIonAccuracyTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a precursor mass tolerance.",
                        "Precursor Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorIonLbl.setForeground(Color.RED);
            precursorIonLbl.setToolTipText("Please select a precursor mass tolerance");
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
            precursorIonLbl.setForeground(Color.RED);
            precursorIonLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (precursorTolerance < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number (zero or more) for the precursor mass tolerance.",
                        "Precursor Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorIonLbl.setForeground(Color.RED);
            precursorIonLbl.setToolTipText("Please select a positive number");
        }

        // Validate fragment mass tolerances
        if (fragmentIonAccuracyTxt.getText() == null || fragmentIonAccuracyTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a fragment mass tolerance.",
                        "Fragment Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            fragmentIonLbl.setForeground(Color.RED);
            fragmentIonLbl.setToolTipText("Please select the fragment mass tolerance");
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
            fragmentIonLbl.setForeground(Color.RED);
            fragmentIonLbl.setToolTipText("Please select a positive number");
        }

        // And it should be zero or more.
        if (fragmentTolerance < 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive number (zero or more) for the fragment mass tolerance.",
                        "Fragment Mass Tolerance Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            fragmentIonLbl.setForeground(Color.RED);
            fragmentIonLbl.setToolTipText("Please select a positive number");
        }

        // Validate precursor charge lower bound
        if (minPrecursorChargeTxt.getText() == null || minPrecursorChargeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a lower bound for the precursor charge first.",
                        "Lower Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl.setForeground(Color.RED);
            precursorChargeLbl.setToolTipText("Please select a lower bound for the precursor charge");
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
            precursorChargeLbl.setForeground(Color.RED);
            precursorChargeLbl.setToolTipText("Please select positive integers");
        }

        if (chargeLowerBound <= 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the lower bound of the precursor charge.",
                        "Lower Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl.setForeground(Color.RED);
            precursorChargeLbl.setToolTipText("Please select positive integers");
        }

        // Validate precursor charge upper bound
        if (maxPrecursorChargeTxt.getText() == null || maxPrecursorChargeTxt.getText().trim().equals("")) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify an upper bound for the precursor charge.",
                        "Upper Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl.setForeground(Color.RED);
            precursorChargeLbl.setToolTipText("Please select an upper bound for the precursor charge");
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
            precursorChargeLbl.setForeground(Color.RED);
            precursorChargeLbl.setToolTipText("Please select positive integers");
        }

        if (chargeUpperBound <= 0) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "You need to specify a positive integer for the upper bound of the precursor charge.",
                        "Upper Bound Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl.setForeground(Color.RED);
            precursorChargeLbl.setToolTipText("Please select positive integers");
        }

        if (chargeUpperBound < chargeLowerBound) {
            if (showMessage && valid) {
                JOptionPane.showMessageDialog(this, "The minimum precursor charge must be lower than or equal to the maximum precursor charge.",
                        "Precursor Charge Error", JOptionPane.WARNING_MESSAGE);
            }
            valid = false;
            precursorChargeLbl.setForeground(Color.RED);
            precursorChargeLbl.setToolTipText("Minimum precursor charge > Maximum precursor charge!");
        }

        return valid;
    }

    /**
     * Returns a SearchParameters instance based on the user input in the GUI.
     *
     * @return a SearchParameters instance based on the user input in the GUI
     */
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

    /**
     * Updates the modification list (right).
     */
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

    private void updateListToolTip(JList list, java.awt.event.MouseEvent evt) {

        // @TODO: reimplement me??

        String toolTip = null;

        int index = list.locationToIndex(evt.getPoint());
        Rectangle bounds = list.getCellBounds(index, index);

        // update tooltips
        if (index != -1 && bounds.contains(evt.getPoint())) {
            String name = (String) list.getModel().getElementAt(index);

            PTM ptm = ptmFactory.getPTM(name);

            String residuesAsString = "";

            if (ptm.getType() == PTM.MODN) {
                residuesAsString += "protein N-term";
            } else if (ptm.getType() == PTM.MODNP) {
                residuesAsString += "peptide N-term";
            } else if (ptm.getType() == PTM.MODNAA) {
                residuesAsString += "protein starting by " + ptm.getPattern().toString();
            } else if (ptm.getType() == PTM.MODNPAA) {
                residuesAsString += "peptide starting by " + ptm.getPattern().toString();
            }
            if (ptm.getType() == PTM.MODC) {
                residuesAsString += "protein C-term";
            } else if (ptm.getType() == PTM.MODCP) {
                residuesAsString += "peptide C-term";
            } else if (ptm.getType() == PTM.MODCAA) {
                residuesAsString += "protein ending by " + ptm.getPattern().toString();
            } else if (ptm.getType() == PTM.MODCPAA) {
                residuesAsString += "peptide ending by " + ptm.getPattern().toString();
            } else if (ptm.getType() == PTM.MODAA) {
                residuesAsString += ptm.getPattern().toString();
            }


            toolTip = "<html>"
                    + "<table border=\"0\">"
                    + "<tr>"
                    + "<td>Name:</td>"
                    + "<td>" + ptm.getName() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Mass:</td>"
                    + "<td>" + ptm.getMass() + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td>Target:</td>"
                    + "<td>" + residuesAsString + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</html>";
        }

        list.setToolTipText(toolTip);
    }

    /**
     * Updates the modification lists and tables
     */
    public void updateModifications() {
        updateModificationList();
    }

    /**
     * Returns a line with the use of modifications.
     *
     * @return A line containing the use of modifications
     */
    public String getModificationUse() {
        String result = "";
        for (String name : modificationUse.keySet()) {
            result += name + MODIFICATION_USE_SEPARATOR + modificationUse.get(name) + MODIFICATION_SEPARATOR;
        }
        return result;
    }

    /**
     * Adds a decoy database to the current FASTA file.
     */
    public void generateTargetDecoyDatabase() {

        progressDialog = new ProgressDialogX(this, searchGUI,
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui-orange.gif")),
                true);
        progressDialog.setUnstoppable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.setTitle("Creating Decoy. Please Wait...");

        final SearchParameterViewer finalRef = this;

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("DecoyThread") {
            public void run() {

                String fastaInput = databaseSettingsTxt.getText().trim();
                try {
                    progressDialog.setTitle("Importing Database. Please Wait...");
                    progressDialog.setIndeterminate(false);
                    sequenceFactory.loadFastaFile(new File(fastaInput), progressDialog);
                } catch (IOException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(finalRef,
                            new String[]{"FASTA Import Error.", "File " + fastaInput + " not found."},
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (ClassNotFoundException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(finalRef,
                            new String[]{"FASTA Import Error.", "File index of " + fastaInput + " could not be imported. Please contact the developers."},
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (StringIndexOutOfBoundsException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(finalRef,
                            e.getMessage(),
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                }

                if (sequenceFactory.concatenatedTargetDecoy() && !progressDialog.isRunCanceled()) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(finalRef,
                            "The database already contains decoy sequences.",
                            "FASTA File Already Decoy!", JOptionPane.WARNING_MESSAGE);
                    targetDecoySettingsButton.setEnabled(false);
                    return;
                }

                if (!progressDialog.isRunCanceled()) {

                    try {
                        String newFasta = fastaInput.substring(0, fastaInput.lastIndexOf("."));
                        newFasta += searchGUI.getTargetDecoyFileNameTag();
                        progressDialog.setTitle("Appending Decoy Sequences. Please Wait...");
                        sequenceFactory.appendDecoySequences(new File(newFasta), progressDialog);
                        databaseSettingsTxt.setText(newFasta);
                        targetDecoySettingsButton.setEnabled(false);
                    } catch (IllegalArgumentException e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                new String[]{"FASTA File Error.", fastaInput + " already contains decoy sequences."},
                                "FASTA File Error", JOptionPane.WARNING_MESSAGE);
                        targetDecoySettingsButton.setEnabled(false);
                        e.printStackTrace();
                        return;
                    } catch (OutOfMemoryError error) {
                        Runtime.getRuntime().gc();
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                "The task used up all the available memory and had to be stopped.\n"
                                + "Memory boundaries are set in ../resources/conf/JavaOptions.txt.",
                                "Out Of Memory Error",
                                JOptionPane.ERROR_MESSAGE);
                        System.out.println("Ran out of memory!");
                        error.printStackTrace();
                        return;
                    } catch (IOException e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                new String[]{"FASTA Import Error.", "File " + fastaInput + " not found."},
                                "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                        e.printStackTrace();
                        return;
                    } catch (InterruptedException e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                new String[]{"FASTA Import Error.", "File " + fastaInput + " could not be imported."},
                                "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                        e.printStackTrace();
                        return;
                    } catch (ClassNotFoundException e) {
                        progressDialog.setRunFinished();
                        JOptionPane.showMessageDialog(finalRef,
                                new String[]{"FASTA Import Error.", "File " + fastaInput + " could not be imported."},
                                "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                        e.printStackTrace();
                        return;
                    }
                }

                if (!progressDialog.isRunCanceled()) {
                    progressDialog.setRunFinished();
                    targetDecoySettingsButton.setEnabled(false);
                    JOptionPane.showMessageDialog(finalRef, "Concatenated decoy database created and selected.", "Decoy Created", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    progressDialog.setRunFinished();
                }
            }
        }.start();
    }

    /**
     * Copies the content of the FASTA file to a new file and replaces any white
     * space in the file name with '_' instead.
     *
     * @param file
     */
    public void renameFastaFileName(File file) {
        String tempName = file.getName();
        tempName = tempName.replaceAll(" ", "_");

        File renamedFile = new File(file.getParentFile().getAbsolutePath() + File.separator + tempName);

        boolean success = false;

        try {
            success = renamedFile.createNewFile();

            if (success) {

                FileReader r = new FileReader(file);
                BufferedReader br = new BufferedReader(r);

                FileWriter w = new FileWriter(renamedFile);
                BufferedWriter bw = new BufferedWriter(w);

                String line = br.readLine();

                while (line != null) {
                    bw.write(line + "\n");
                    line = br.readLine();
                }

                bw.close();
                w.close();
                br.close();
                r.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Your FASTA file name contained white space and has been renamed to:\n"
                    + file.getParentFile().getAbsolutePath() + File.separator + tempName, "Renamed File", JOptionPane.WARNING_MESSAGE);
            databaseSettingsTxt.setText(file.getParentFile().getAbsolutePath() + File.separator + tempName);
            targetDecoySettingsButton.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Your FASTA file name contains white space and has to been renamed.",
                    "Please Rename File", JOptionPane.WARNING_MESSAGE);
        }
    }
}
