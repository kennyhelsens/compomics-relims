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
import com.compomics.util.gui.error_handlers.HelpDialog;
import com.compomics.util.gui.ptm.ModificationsDialog;
import com.compomics.util.gui.ptm.PtmDialogParent;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import eu.isas.searchgui.gui.BareBonesBrowserLaunch;
import eu.isas.searchgui.gui.SearchGUI;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import no.uib.jsparklines.renderers.JSparklinesBarChartTableCellRenderer;
import no.uib.jsparklines.renderers.JSparklinesColorTableCellRenderer;
import org.jfree.chart.plot.PlotOrientation;
import org.xmlpull.v1.XmlPullParserException;

/**
 * The SearchGUI settings dialog.
 *
 * @author Harald Barsnes
 */
public class SearchGUISettingsDialog extends javax.swing.JDialog implements PtmDialogParent {

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

    /**
     * Creates a new SettingsDialog.
     *
     * @param searchGUI
     * @param searchParameters
     * @param modal
     */
    public SearchGUISettingsDialog() {
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
            Logger.getLogger(SearchGUISettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SearchGUISettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
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
        modificationsListCombo.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        enzymesCmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIon1Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        fragmentIon2Cmb.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));
        precursorIonUnit.setRenderer(new com.compomics.util.gui.renderers.AlignedListCellRenderer(SwingConstants.CENTER));

        ((TitledBorder) configurationFilePanelSettings.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Configuration File" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);
        ((TitledBorder) dataBasePanelSettings.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Database" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);
        ((TitledBorder) modificationsPanel.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Modifications" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);
        ((TitledBorder) proteaseAndFragmentationPanel.getBorder()).setTitle(SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING + "Protease & Fragmentation" + SearchGUI.TITLED_BORDER_HORIZONTAL_PADDING);

        fixedModsJScrollPane.getViewport().setOpaque(false);
        variableModsJScrollPane.getViewport().setOpaque(false);
        modificationsJScrollPane.getViewport().setOpaque(false);

        fixedModsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        variableModsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());
        modificationsTable.getColumn(" ").setCellRenderer(new JSparklinesColorTableCellRenderer());

        fixedModsTable.getColumn(" ").setMaxWidth(35);
        fixedModsTable.getColumn(" ").setMinWidth(35);
        variableModsTable.getColumn(" ").setMaxWidth(35);
        variableModsTable.getColumn(" ").setMinWidth(35);
        modificationsTable.getColumn(" ").setMaxWidth(35);
        modificationsTable.getColumn(" ").setMinWidth(35);

        fixedModsTable.getColumn("Mass").setMaxWidth(100);
        fixedModsTable.getColumn("Mass").setMinWidth(100);
        variableModsTable.getColumn("Mass").setMaxWidth(100);
        variableModsTable.getColumn("Mass").setMinWidth(100);
        modificationsTable.getColumn("Mass").setMaxWidth(100);
        modificationsTable.getColumn("Mass").setMinWidth(100);

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
        configurationFilePanelSettings = new javax.swing.JPanel();
        configurationFileLbl2 = new javax.swing.JLabel();
        browseConfigurationButton2 = new javax.swing.JButton();
        configurationFileTxt_parameters = new javax.swing.JTextField();
        saveConfiguration = new javax.swing.JButton();
        SaveConfigurationAs = new javax.swing.JButton();
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
        jCheckBox1 = new javax.swing.JCheckBox();
        dataBasePanelSettings = new javax.swing.JPanel();
        databaseSettingsLbl = new javax.swing.JLabel();
        databaseSettingsTxt = new javax.swing.JTextField();
        browseDatabaseSettings = new javax.swing.JButton();
        targetDecoySettingsButton = new javax.swing.JButton();
        databaseHelpSettingsJLabel = new javax.swing.JLabel();
        modificationsLayeredPane = new javax.swing.JLayeredPane();
        modificationsPanel = new javax.swing.JPanel();
        modificationTypesSplitPane = new javax.swing.JSplitPane();
        jPanel8 = new javax.swing.JPanel();
        fixedModificationsLabel = new javax.swing.JLabel();
        addFixedModification = new javax.swing.JButton();
        removeFixedModification = new javax.swing.JButton();
        fixedModsJScrollPane = new javax.swing.JScrollPane();
        fixedModsTable = new javax.swing.JTable();
        jPanel9 = new javax.swing.JPanel();
        variableModificationsLabel = new javax.swing.JLabel();
        addVariableModification = new javax.swing.JButton();
        removeVariableModification = new javax.swing.JButton();
        variableModsJScrollPane = new javax.swing.JScrollPane();
        variableModsTable = new javax.swing.JTable();
        availableModsPanel = new javax.swing.JPanel();
        modificationsListCombo = new javax.swing.JComboBox();
        modificationsJScrollPane = new javax.swing.JScrollPane();
        modificationsTable = new javax.swing.JTable();
        modificationsHelpJButton = new javax.swing.JButton();
        modificationsOptionsJButton = new javax.swing.JButton();
        contextMenuModificationsBackgroundPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();

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

        configurationFilePanelSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));
        configurationFilePanelSettings.setOpaque(false);

        configurationFileLbl2.setText("Settings File");

        browseConfigurationButton2.setText("Load");
        browseConfigurationButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseConfigurationButton2ActionPerformed(evt);
            }
        });

        configurationFileTxt_parameters.setEditable(false);

        saveConfiguration.setText("Save");
        saveConfiguration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveConfigurationActionPerformed(evt);
            }
        });

        SaveConfigurationAs.setText("Save As");
        SaveConfigurationAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveConfigurationAsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout configurationFilePanelSettingsLayout = new javax.swing.GroupLayout(configurationFilePanelSettings);
        configurationFilePanelSettings.setLayout(configurationFilePanelSettingsLayout);
        configurationFilePanelSettingsLayout.setHorizontalGroup(
            configurationFilePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, configurationFilePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configurationFileLbl2, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(configurationFileTxt_parameters)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(browseConfigurationButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(saveConfiguration)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SaveConfigurationAs, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        configurationFilePanelSettingsLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {SaveConfigurationAs, browseConfigurationButton2, saveConfiguration});

        configurationFilePanelSettingsLayout.setVerticalGroup(
            configurationFilePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(configurationFilePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(configurationFilePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(configurationFileLbl2)
                    .addComponent(configurationFileTxt_parameters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SaveConfigurationAs)
                    .addComponent(saveConfiguration)
                    .addComponent(browseConfigurationButton2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        missedCleavagesTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        missedCleavagesTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                missedCleavagesTxtKeyReleased(evt);
            }
        });

        precursorIonLbl.setText("Precursor Mass Tolerance");

        precursorIonAccuracyTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        precursorIonAccuracyTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                precursorIonAccuracyTxtKeyReleased(evt);
            }
        });

        precursorIonUnit.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "ppm", "Da" }));

        fragmentIonLbl.setText("Fragment Mass Tolerance (Da)");

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

        minPrecursorChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        minPrecursorChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                minPrecursorChargeTxtKeyReleased(evt);
            }
        });

        maxPrecursorChargeTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        maxPrecursorChargeTxt.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                maxPrecursorChargeTxtKeyReleased(evt);
            }
        });

        precursorChargeRangeLabel.setText("-");

        jCheckBox1.setText("Apply settings as new default");

        javax.swing.GroupLayout proteaseAndFragmentationPanelLayout = new javax.swing.GroupLayout(proteaseAndFragmentationPanel);
        proteaseAndFragmentationPanel.setLayout(proteaseAndFragmentationPanelLayout);
        proteaseAndFragmentationPanelLayout.setHorizontalGroup(
            proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addComponent(jCheckBox1)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fragmentIonType1Lbl)
                            .addComponent(enzymeLabel)
                            .addComponent(precursorIonLbl))
                        .addGap(18, 18, 18)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(enzymesCmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proteaseAndFragmentationPanelLayout.createSequentialGroup()
                                .addComponent(fragmentIon1Cmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(18, 18, 18)
                                .addComponent(fragmentIon2Cmb, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, proteaseAndFragmentationPanelLayout.createSequentialGroup()
                                .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 117, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(precursorIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, 99, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(50, 50, 50)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(fragmentIonLbl)
                            .addComponent(maxMissedCleavagesLabel)
                            .addComponent(precursorChargeLbl))
                        .addGap(18, 18, 18)
                        .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(missedCleavagesTxt)
                            .addComponent(fragmentIonAccuracyTxt)
                            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                                .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 62, Short.MAX_VALUE)
                                .addGap(19, 19, 19)
                                .addComponent(precursorChargeRangeLabel)
                                .addGap(18, 18, 18)
                                .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 61, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        proteaseAndFragmentationPanelLayout.setVerticalGroup(
            proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(proteaseAndFragmentationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(missedCleavagesTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(enzymeLabel)
                    .addComponent(enzymesCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxMissedCleavagesLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentIonLbl)
                    .addComponent(precursorIonLbl)
                    .addComponent(precursorIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentIonAccuracyTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorIonUnit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(proteaseAndFragmentationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fragmentIonType1Lbl)
                    .addComponent(fragmentIon2Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(fragmentIon1Cmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorChargeLbl)
                    .addComponent(minPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxPrecursorChargeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(precursorChargeRangeLabel))
                .addGap(18, 18, 18)
                .addComponent(jCheckBox1)
                .addContainerGap())
        );

        dataBasePanelSettings.setBorder(javax.swing.BorderFactory.createTitledBorder("Database"));
        dataBasePanelSettings.setOpaque(false);

        databaseSettingsLbl.setText("Database (FASTA)*");

        databaseSettingsTxt.setEditable(false);

        browseDatabaseSettings.setText("Browse");
        browseDatabaseSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseDatabaseSettingsActionPerformed(evt);
            }
        });

        targetDecoySettingsButton.setText("Decoy");
        targetDecoySettingsButton.setToolTipText("Generate a concatenated Target/Decoy database");
        targetDecoySettingsButton.setEnabled(false);
        targetDecoySettingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                targetDecoySettingsButtonActionPerformed(evt);
            }
        });

        databaseHelpSettingsJLabel.setForeground(new java.awt.Color(0, 0, 255));
        databaseHelpSettingsJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        databaseHelpSettingsJLabel.setText("<html><u><i>Database?</i></u></html>");
        databaseHelpSettingsJLabel.setToolTipText("Open Database Help");
        databaseHelpSettingsJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                databaseHelpSettingsJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                databaseHelpSettingsJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                databaseHelpSettingsJLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout dataBasePanelSettingsLayout = new javax.swing.GroupLayout(dataBasePanelSettings);
        dataBasePanelSettings.setLayout(dataBasePanelSettingsLayout);
        dataBasePanelSettingsLayout.setHorizontalGroup(
            dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataBasePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(databaseSettingsLbl, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(databaseSettingsTxt)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(databaseHelpSettingsJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(browseDatabaseSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(targetDecoySettingsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        dataBasePanelSettingsLayout.setVerticalGroup(
            dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dataBasePanelSettingsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dataBasePanelSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseSettingsLbl)
                    .addComponent(databaseHelpSettingsJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetDecoySettingsButton)
                    .addComponent(browseDatabaseSettings)
                    .addComponent(databaseSettingsTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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

        addFixedModification.setText("<<");
        addFixedModification.setToolTipText("Add as fixed modification");
        addFixedModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFixedModificationActionPerformed(evt);
            }
        });

        removeFixedModification.setText(">>");
        removeFixedModification.setToolTipText("Remove as fixed modification");
        removeFixedModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeFixedModificationActionPerformed(evt);
            }
        });

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
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(fixedModificationsLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 111, Short.MAX_VALUE)
                        .addGap(242, 242, 242))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(fixedModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(7, 7, 7)))
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(removeFixedModification, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addFixedModification, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(fixedModificationsLabel)
                .addGap(6, 6, 6)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(addFixedModification)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeFixedModification)
                        .addContainerGap(59, Short.MAX_VALUE))
                    .addComponent(fixedModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        modificationTypesSplitPane.setLeftComponent(jPanel8);

        jPanel9.setOpaque(false);

        variableModificationsLabel.setFont(variableModificationsLabel.getFont().deriveFont((variableModificationsLabel.getFont().getStyle() | java.awt.Font.ITALIC)));
        variableModificationsLabel.setText("Variable Modifications");

        addVariableModification.setText("<<");
        addVariableModification.setToolTipText("Add as variable modification");
        addVariableModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addVariableModificationActionPerformed(evt);
            }
        });

        removeVariableModification.setText(">>");
        removeVariableModification.setToolTipText("Remove as variable modification");
        removeVariableModification.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeVariableModificationActionPerformed(evt);
            }
        });

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

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(variableModificationsLabel)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(addVariableModification, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(removeVariableModification, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(variableModificationsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(addVariableModification)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeVariableModification)
                        .addContainerGap(59, Short.MAX_VALUE))
                    .addComponent(variableModsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        modificationTypesSplitPane.setRightComponent(jPanel9);

        availableModsPanel.setOpaque(false);

        modificationsListCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Most Used Modifications", "All Modifications" }));
        modificationsListCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificationsListComboActionPerformed(evt);
            }
        });

        modificationsJScrollPane.setPreferredSize(new java.awt.Dimension(100, 60));

        modificationsTable.setModel(new javax.swing.table.DefaultTableModel(
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
        modificationsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent evt) {
                modificationsTableMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                modificationsTableMouseReleased(evt);
            }
        });
        modificationsTable.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                modificationsTableMouseMoved(evt);
            }
        });
        modificationsJScrollPane.setViewportView(modificationsTable);

        javax.swing.GroupLayout availableModsPanelLayout = new javax.swing.GroupLayout(availableModsPanel);
        availableModsPanel.setLayout(availableModsPanelLayout);
        availableModsPanelLayout.setHorizontalGroup(
            availableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(modificationsListCombo, 0, 325, Short.MAX_VALUE)
            .addComponent(modificationsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        availableModsPanelLayout.setVerticalGroup(
            availableModsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(availableModsPanelLayout.createSequentialGroup()
                .addComponent(modificationsListCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationsJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout modificationsPanelLayout = new javax.swing.GroupLayout(modificationsPanel);
        modificationsPanel.setLayout(modificationsPanelLayout);
        modificationsPanelLayout.setHorizontalGroup(
            modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(modificationTypesSplitPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(availableModsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        modificationsPanelLayout.setVerticalGroup(
            modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(modificationsPanelLayout.createSequentialGroup()
                .addGroup(modificationsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(modificationTypesSplitPane)
                    .addComponent(availableModsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        modificationsPanel.setBounds(0, 0, 770, 322);
        modificationsLayeredPane.add(modificationsPanel, javax.swing.JLayeredPane.DEFAULT_LAYER);

        modificationsHelpJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame_grey.png"))); // NOI18N
        modificationsHelpJButton.setToolTipText("Help");
        modificationsHelpJButton.setBorder(null);
        modificationsHelpJButton.setBorderPainted(false);
        modificationsHelpJButton.setContentAreaFilled(false);
        modificationsHelpJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/help_no_frame.png"))); // NOI18N
        modificationsHelpJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                modificationsHelpJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                modificationsHelpJButtonMouseExited(evt);
            }
        });
        modificationsHelpJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modificationsHelpJButtonActionPerformed(evt);
            }
        });
        modificationsHelpJButton.setBounds(760, 0, 10, 19);
        modificationsLayeredPane.add(modificationsHelpJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        modificationsOptionsJButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/contextual_menu_gray.png"))); // NOI18N
        modificationsOptionsJButton.setToolTipText("Modification Details");
        modificationsOptionsJButton.setBorder(null);
        modificationsOptionsJButton.setBorderPainted(false);
        modificationsOptionsJButton.setContentAreaFilled(false);
        modificationsOptionsJButton.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/icons/contextual_menu_black.png"))); // NOI18N
        modificationsOptionsJButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                modificationsOptionsJButtonMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                modificationsOptionsJButtonMouseExited(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                modificationsOptionsJButtonMouseReleased(evt);
            }
        });
        modificationsOptionsJButton.setBounds(745, 5, 10, 19);
        modificationsLayeredPane.add(modificationsOptionsJButton, javax.swing.JLayeredPane.POPUP_LAYER);

        contextMenuModificationsBackgroundPanel.setBackground(backgroundPanel.getBackground());

        javax.swing.GroupLayout contextMenuModificationsBackgroundPanelLayout = new javax.swing.GroupLayout(contextMenuModificationsBackgroundPanel);
        contextMenuModificationsBackgroundPanel.setLayout(contextMenuModificationsBackgroundPanelLayout);
        contextMenuModificationsBackgroundPanelLayout.setHorizontalGroup(
            contextMenuModificationsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );
        contextMenuModificationsBackgroundPanelLayout.setVerticalGroup(
            contextMenuModificationsBackgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 19, Short.MAX_VALUE)
        );

        contextMenuModificationsBackgroundPanel.setBounds(730, 0, 50, 19);
        modificationsLayeredPane.add(contextMenuModificationsBackgroundPanel, javax.swing.JLayeredPane.POPUP_LAYER);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("Deploy");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(configurationFilePanelSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(proteaseAndFragmentationPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(modificationsLayeredPane, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton)))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(configurationFilePanelSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dataBasePanelSettings, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modificationsLayeredPane, javax.swing.GroupLayout.DEFAULT_SIZE, 290, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(proteaseAndFragmentationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton))
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
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Loads settings from a paramters file.
     *
     * @param evt
     */
    private void browseConfigurationButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseConfigurationButton2ActionPerformed
        loadValuesPressed();
    }//GEN-LAST:event_browseConfigurationButton2ActionPerformed

    /**
     * Saves the parameters to file.
     *
     * @param evt
     */
    private void saveConfigurationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveConfigurationActionPerformed
        savePressed();
    }//GEN-LAST:event_saveConfigurationActionPerformed

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
     * Opens a file chooser where the user can select the database file.
     *
     * @param evt
     */
    private void browseDatabaseSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseDatabaseSettingsActionPerformed

        File startLocation = new File(searchGUI.getLastSelectedFolder());

        // First check whether a file has already been selected.
        // If so, start from that file's parent.
        if (databaseSettingsTxt.getText() != null && !databaseSettingsTxt.getText().trim().equals("")) {
            File temp = new File(databaseSettingsTxt.getText());
            startLocation = temp.getParentFile();
        }

        JFileChooser fc = new JFileChooser(startLocation);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File myFile) {

                return myFile.getName().toLowerCase().endsWith(".fasta")
                        || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Database file (.fasta)";
            }
        };
        fc.setFileFilter(filter);
        int result = fc.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            if (file.getName().indexOf(" ") != -1) {
                renameFastaFileName(file);
            } else {
                databaseSettingsTxt.setText(file.getAbsolutePath());
                databaseSettingsTxt.setText(file.getAbsolutePath());
            }

            searchGUI.setLastSelectedFolder(file.getAbsolutePath());
            //targetDecoyButton.setEnabled(true); // @TODO: how to handle this???
            targetDecoySettingsButton.setEnabled(true);

            // check if the database contains decoys
            if (!file.getAbsolutePath().endsWith(searchGUI.getTargetDecoyFileNameTag())) {

                int value = JOptionPane.showConfirmDialog(this,
                        "The selected FASTA file does not seem to contain decoy sequences.\n"
                        + "Decoys are required by PeptideShaker. Add decoys?", "Add Decoy Sequences?", JOptionPane.YES_NO_OPTION);

                if (value == JOptionPane.NO_OPTION) {
                    // do nothing
                } else if (value == JOptionPane.YES_OPTION) {
                    targetDecoySettingsButtonActionPerformed(null);
                }
            }

            validateParametersInput(false);
        }
    }//GEN-LAST:event_browseDatabaseSettingsActionPerformed

    /**
     * Generates a target-decoy database.
     *
     * @param evt
     */
    private void targetDecoySettingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_targetDecoySettingsButtonActionPerformed
        generateTargetDecoyDatabase();
    }//GEN-LAST:event_targetDecoySettingsButtonActionPerformed

    /**
     * Opens the database help web page.
     *
     * @param evt
     */
    private void databaseHelpSettingsJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_databaseHelpSettingsJLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://code.google.com/p/searchgui/wiki/DatabaseHelp");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_databaseHelpSettingsJLabelMouseClicked

    /**
     * Changes the cursor into a hand cursor.
     *
     * @param evt
     */
    private void databaseHelpSettingsJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_databaseHelpSettingsJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_databaseHelpSettingsJLabelMouseEntered

    /**
     * Changes the cursor back to a hand cursor.
     *
     * @param evt
     */
    private void databaseHelpSettingsJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_databaseHelpSettingsJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_databaseHelpSettingsJLabelMouseExited

    /**
     * Add fixed modifications.
     *
     * @param evt
     */
    private void addFixedModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addFixedModificationActionPerformed
        int nSelected = fixedModsTable.getRowCount();
        int nNew = modificationsTable.getSelectedRows().length;
        String[] fixedModifications = new String[nSelected + nNew];
        int cpt = 0;

        for (int i = 0; i < nSelected; i++) {
            fixedModifications[cpt] = (String) fixedModsTable.getValueAt(i, 1);
            cpt++;
        }

        for (int selectedRow : modificationsTable.getSelectedRows()) {
            String name = (String) modificationsTable.getValueAt(selectedRow, 1);
            boolean found = false;
            for (int i = 0; i < fixedModsTable.getModel().getRowCount(); i++) {
                if (((String) fixedModsTable.getValueAt(i, 1)).equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fixedModifications[cpt] = name;
                cpt++;
                if (!modificationUse.containsKey(name)) {
                    modificationUse.put(name, 1);
                } else {
                    modificationUse.put(name, modificationUse.get(name) + 1);
                }
            }
        }

        DefaultTableModel fixedModel = (DefaultTableModel) fixedModsTable.getModel();
        fixedModel.getDataVector().removeAllElements();

        for (String fixedMod : fixedModifications) {
            ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationProfile().getColor(fixedMod), fixedMod, ptmFactory.getPTM(fixedMod).getMass()});
        }
        ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
        fixedModsTable.repaint();

        fixedModificationsLabel.setText("Fixed Modifications (" + fixedModifications.length + ")");
        updateModificationList();
    }//GEN-LAST:event_addFixedModificationActionPerformed

    /**
     * Remove fixed modifications.
     *
     * @param evt
     */
    private void removeFixedModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeFixedModificationActionPerformed
        int nSelected = fixedModsTable.getRowCount();
        int nToRemove = fixedModsTable.getSelectedRows().length;
        String[] fixedModifications = new String[nSelected - nToRemove];
        int cpt = 0;

        for (int i = 0; i < fixedModsTable.getRowCount(); i++) {
            boolean found = false;
            for (int selectedRow : fixedModsTable.getSelectedRows()) {
                if (((String) fixedModsTable.getValueAt(i, 1)).equals((String) fixedModsTable.getValueAt(selectedRow, 1))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                fixedModifications[cpt] = (String) fixedModsTable.getValueAt(i, 1);
                cpt++;
            }
        }

        DefaultTableModel fixedModel = (DefaultTableModel) fixedModsTable.getModel();
        fixedModel.getDataVector().removeAllElements();

        for (String fixedMod : fixedModifications) {
            ((DefaultTableModel) fixedModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationProfile().getColor(fixedMod), fixedMod, ptmFactory.getPTM(fixedMod).getMass()});
        }
        ((DefaultTableModel) fixedModsTable.getModel()).fireTableDataChanged();
        fixedModsTable.repaint();

        fixedModificationsLabel.setText("Fixed Modifications (" + fixedModifications.length + ")");
        updateModificationList();
    }//GEN-LAST:event_removeFixedModificationActionPerformed

    /**
     * Add variable modifications.
     *
     * @param evt
     */
    private void addVariableModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addVariableModificationActionPerformed
        int nSelected = variableModsTable.getRowCount();
        int nNew = modificationsTable.getSelectedRows().length;
        String[] variableModifications = new String[nSelected + nNew];
        int cpt = 0;

        for (int i = 0; i < nSelected; i++) {
            variableModifications[cpt] = (String) variableModsTable.getValueAt(i, 1);
            cpt++;
        }

        for (int selectedRow : modificationsTable.getSelectedRows()) {
            String name = (String) modificationsTable.getValueAt(selectedRow, 1);
            boolean found = false;
            for (int i = 0; i < variableModsTable.getRowCount(); i++) {
                if (((String) variableModsTable.getValueAt(i, 1)).equals(name)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                variableModifications[cpt] = name;
                cpt++;
                if (!modificationUse.containsKey(name)) {
                    modificationUse.put(name, 1);
                } else {
                    modificationUse.put(name, modificationUse.get(name) + 1);
                }
            }
        }

        DefaultTableModel variableModel = (DefaultTableModel) variableModsTable.getModel();
        variableModel.getDataVector().removeAllElements();

        for (String variabledMod : variableModifications) {
            ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationProfile().getColor(variabledMod), variabledMod, ptmFactory.getPTM(variabledMod).getMass()});
        }
        ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
        variableModsTable.repaint();

        variableModificationsLabel.setText("Variable Modifications (" + variableModifications.length + ")");

        if (variableModifications.length > 6) {
            JOptionPane.showMessageDialog(this,
                    "It is not recommended to use more than 6 variable modifications in the same search.", "Warning", JOptionPane.WARNING_MESSAGE);
        }

        updateModificationList();
    }//GEN-LAST:event_addVariableModificationActionPerformed

    /**
     * Remove variable modifications.
     *
     * @param evt
     */
    private void removeVariableModificationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeVariableModificationActionPerformed
        int nSelected = variableModsTable.getRowCount();
        int nToRemove = variableModsTable.getSelectedRows().length;
        String[] variableModifications = new String[nSelected - nToRemove];
        int cpt = 0;

        for (int i = 0; i < variableModsTable.getRowCount(); i++) {
            boolean found = false;
            for (int selectedRow : variableModsTable.getSelectedRows()) {
                if (((String) variableModsTable.getValueAt(i, 1)).equals((String) variableModsTable.getValueAt(selectedRow, 1))) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                variableModifications[cpt] = (String) variableModsTable.getValueAt(i, 1);
                cpt++;
            }
        }

        DefaultTableModel variableModel = (DefaultTableModel) variableModsTable.getModel();
        variableModel.getDataVector().removeAllElements();

        for (String variabledMod : variableModifications) {
            ((DefaultTableModel) variableModsTable.getModel()).addRow(new Object[]{searchParameters.getModificationProfile().getColor(variabledMod), variabledMod, ptmFactory.getPTM(variabledMod).getMass()});
        }
        ((DefaultTableModel) variableModsTable.getModel()).fireTableDataChanged();
        variableModsTable.repaint();

        variableModificationsLabel.setText("Variable Modifications (" + variableModifications.length + ")");
        updateModificationList();
    }//GEN-LAST:event_removeVariableModificationActionPerformed

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
     * Update the modification lists.
     *
     * @param evt
     */
    private void modificationsListComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificationsListComboActionPerformed
        updateModificationList();
    }//GEN-LAST:event_modificationsListComboActionPerformed

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void modificationsHelpJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsHelpJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_modificationsHelpJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void modificationsHelpJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsHelpJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationsHelpJButtonMouseExited

    /**
     * Opens the help dialog.
     *
     * @param evt
     */
    private void modificationsHelpJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modificationsHelpJButtonActionPerformed
        setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        new HelpDialog(this, getClass().getResource("/helpFiles/Modifications.html"),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/help.GIF")),
                Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icons/searchgui.gif")),
                "SearchGUI - Help");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationsHelpJButtonActionPerformed

    /**
     * Change the cursor into a hand cursor.
     *
     * @param evt
     */
    private void modificationsOptionsJButtonMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsOptionsJButtonMouseEntered
        setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_modificationsOptionsJButtonMouseEntered

    /**
     * Change the cursor back to the default cursor.
     *
     * @param evt
     */
    private void modificationsOptionsJButtonMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsOptionsJButtonMouseExited
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationsOptionsJButtonMouseExited

    /**
     * Open the modifications pop up menu.
     *
     * @param evt
     */
    private void modificationsOptionsJButtonMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsOptionsJButtonMouseReleased
        modificationOptionsPopupMenu.show(modificationsOptionsJButton, evt.getX(), evt.getY());
    }//GEN-LAST:event_modificationsOptionsJButtonMouseReleased

    /**
     * Close the window without saving the changes.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Save the changes and then close the dialog.
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed

        // @TODO: check for changes and ask if the user wants to overwrite the file!


        SearchParameters tempSearchParameters = getSearchParameters();
        //transfer the parameters to the MainClientGui as well !
        MainClientGUI.setSearchParameters(tempSearchParameters);
        MainClientGUI.setUsingDefault(false);
        try {
            tempSearchParameters.saveIdentificationParameters(tempSearchParameters, searchParametersFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SearchGUISettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SearchGUISettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(SearchGUISettingsDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
        tempSearchParameters.setParametersFile(null);

        //  searchGUI.setSearchParameters(tempSearchParameters);
        //  searchGUI.setUsedModificationsAsString(getModificationUse());
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

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
        // move the icons
        modificationsLayeredPane.getComponent(0).setBounds(
                modificationsLayeredPane.getWidth() - modificationsLayeredPane.getComponent(0).getWidth() - 10,
                -3,
                modificationsLayeredPane.getComponent(0).getWidth(),
                modificationsLayeredPane.getComponent(0).getHeight());

        modificationsLayeredPane.getComponent(1).setBounds(
                modificationsLayeredPane.getWidth() - modificationsLayeredPane.getComponent(1).getWidth() - 22,
                0,
                modificationsLayeredPane.getComponent(1).getWidth(),
                modificationsLayeredPane.getComponent(1).getHeight());

        modificationsLayeredPane.getComponent(2).setBounds(
                modificationsLayeredPane.getWidth() - modificationsLayeredPane.getComponent(2).getWidth() - 5,
                -3,
                modificationsLayeredPane.getComponent(2).getWidth(),
                modificationsLayeredPane.getComponent(2).getHeight());

        // resize the plot area
        modificationsLayeredPane.getComponent(3).setBounds(0, 0, modificationsLayeredPane.getWidth(), modificationsLayeredPane.getHeight());
        modificationsLayeredPane.revalidate();
        modificationsLayeredPane.repaint();
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

        enableAddRemoveButtons();
    }//GEN-LAST:event_fixedModsTableMouseReleased

    /**
     * Changes the cursor back to the default cursor.
     *
     * @param evt
     */
    private void modificationsTableMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsTableMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_modificationsTableMouseExited

    /**
     * Changes the cursor to a hand cursor if over the color column.
     *
     * @param evt
     */
    private void modificationsTableMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsTableMouseMoved
        int row = modificationsTable.rowAtPoint(evt.getPoint());
        int column = modificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == modificationsTable.getColumn(" ").getModelIndex()) {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_modificationsTableMouseMoved

    /**
     * Opens a file chooser where the color for the ptm can be changed.
     *
     * @param evt
     */
    private void modificationsTableMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_modificationsTableMouseReleased
        int row = modificationsTable.rowAtPoint(evt.getPoint());
        int column = modificationsTable.columnAtPoint(evt.getPoint());

        if (row != -1) {
            if (column == modificationsTable.getColumn(" ").getModelIndex()) {
                Color newColor = JColorChooser.showDialog(this, "Pick a Color", (Color) modificationsTable.getValueAt(row, column));

                if (newColor != null) {
                    ptmFactory.setColor((String) modificationsTable.getValueAt(row, 1), newColor);
                    modificationsTable.setValueAt(newColor, row, 0);
                    ((DefaultTableModel) modificationsTable.getModel()).fireTableDataChanged();
                    modificationsTable.repaint();
                }
            }
        }

        enableAddRemoveButtons();
    }//GEN-LAST:event_modificationsTableMouseReleased

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

        enableAddRemoveButtons();
    }//GEN-LAST:event_variableModsTableMouseReleased
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton SaveConfigurationAs;
    private javax.swing.JButton addFixedModification;
    private javax.swing.JButton addVariableModification;
    private javax.swing.JPanel availableModsPanel;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton browseConfigurationButton2;
    private javax.swing.JButton browseDatabaseSettings;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel configurationFileLbl2;
    private javax.swing.JPanel configurationFilePanelSettings;
    private javax.swing.JTextField configurationFileTxt_parameters;
    private javax.swing.JPanel contextMenuModificationsBackgroundPanel;
    private javax.swing.JPanel dataBasePanelSettings;
    private javax.swing.JLabel databaseHelpSettingsJLabel;
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
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JLabel maxMissedCleavagesLabel;
    private javax.swing.JTextField maxPrecursorChargeTxt;
    private javax.swing.JTextField minPrecursorChargeTxt;
    private javax.swing.JTextField missedCleavagesTxt;
    private javax.swing.JPopupMenu modificationOptionsPopupMenu;
    private javax.swing.JSplitPane modificationTypesSplitPane;
    private javax.swing.JButton modificationsHelpJButton;
    private javax.swing.JScrollPane modificationsJScrollPane;
    private javax.swing.JLayeredPane modificationsLayeredPane;
    private javax.swing.JComboBox modificationsListCombo;
    private javax.swing.JButton modificationsOptionsJButton;
    private javax.swing.JPanel modificationsPanel;
    private javax.swing.JTable modificationsTable;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel precursorChargeLbl;
    private javax.swing.JLabel precursorChargeRangeLabel;
    private javax.swing.JTextField precursorIonAccuracyTxt;
    private javax.swing.JLabel precursorIonLbl;
    private javax.swing.JComboBox precursorIonUnit;
    private javax.swing.JPanel proteaseAndFragmentationPanel;
    private javax.swing.JButton removeFixedModification;
    private javax.swing.JButton removeVariableModification;
    private javax.swing.JButton saveConfiguration;
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
    public void loadValuesPressed() {
        // First check whether a file has already been selected.
        // If so, start from that file's parent.
        File startLocation = new File(this.getClass().getClassLoader().toString());
        if (configurationFileTxt_parameters.getText() != null && !configurationFileTxt_parameters.getText().trim().equals("")) {
            File temp = new File(configurationFileTxt_parameters.getText());
            startLocation = temp.getParentFile();
        }
        JFileChooser fc = new JFileChooser(startLocation);

        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File myFile) {

                return myFile.getName().toLowerCase().endsWith(".properties")
                        || myFile.getName().toLowerCase().endsWith(".parameters")
                        || myFile.isDirectory();
            }

            @Override
            public String getDescription() {
                return "SearchGUI search parameters";
            }
        };
        fc.setFileFilter(filter);
        int result = fc.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            searchGUI.setLastSelectedFolder(file.getAbsolutePath());
            try {
                searchParameters = SearchParameters.getIdentificationParameters(file);
                loadModifications();
                setScreenProps();
            } catch (Exception e) {
                try {
                    // Old school format, overwrite old file
                    Properties props = loadProperties(file);
                    searchParameters = IdentificationParametersReader.getSearchParameters(props, searchGUI.getSearchHandler().getUserModificationsFile(searchGUI.getJarFilePath()));
                    setScreenProps();
                    String fileName = file.getName();
                    if (fileName.endsWith(".properties")) {
                        String newName = fileName.substring(0, fileName.lastIndexOf(".")) + ".parameters";
                        try {
                            file.delete();
                        } catch (Exception deleteException) {
                            deleteException.printStackTrace();
                        }
                        file = new File(file.getParentFile(), newName);
                    }
                    SearchParameters.saveIdentificationParameters(searchParameters, file);
                } catch (Exception saveException) {
                    e.printStackTrace();
                    saveException.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Error occured while reading " + file + ". Please verify the search paramters.", "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            parametersFile = file;
            configurationFileTxt_parameters.setText(parametersFile.getAbsolutePath());
            searchParameters.setParametersFile(parametersFile);
            validateParametersInput(false);
        }
    }

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

        if (searchParameters.getParametersFile() != null) {
            configurationFileTxt_parameters.setText(searchParameters.getParametersFile().getAbsolutePath());
        }

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

            if (configurationFileTxt_parameters.getText() != null && !configurationFileTxt_parameters.getText().trim().equals("")) {
                File temp = new File(configurationFileTxt_parameters.getText());
                startLocation = temp.getParentFile();
            }

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
            configurationFileTxt_parameters.setText(parametersFile.getPath());
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

        okButton.setEnabled(valid);

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
        if (modificationsListCombo.getSelectedIndex() == 0) {
            for (String name : modificationUse.keySet()) {
                if (modificationUse.get(name) >= 6) {
                    allModificationsList.add(name);
                }
            }
        } else {
            allModificationsList = ptmFactory.getPTMs();
        }

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

        DefaultTableModel modsModel = (DefaultTableModel) modificationsTable.getModel();
        modsModel.getDataVector().removeAllElements();

        for (String mod : allModificationsAsArray) {
            ((DefaultTableModel) modificationsTable.getModel()).addRow(new Object[]{ptmFactory.getColor(mod), mod, ptmFactory.getPTM(mod).getMass()});
        }
        ((DefaultTableModel) modificationsTable.getModel()).fireTableDataChanged();
        modificationsTable.repaint();

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

        modificationsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) modificationsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);
        fixedModsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) fixedModsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);
        variableModsTable.getColumn("Mass").setCellRenderer(new JSparklinesBarChartTableCellRenderer(PlotOrientation.HORIZONTAL, minMass, maxMass));
        ((JSparklinesBarChartTableCellRenderer) variableModsTable.getColumn("Mass").getCellRenderer()).showNumberAndChart(true, 50);

        if (modificationsTable.getRowCount() > 0) {
            modificationsTable.setRowSelectionInterval(0, 0);
        }

        // enable/disable the add/remove ptm buttons
        enableAddRemoveButtons();
    }

    /**
     * Enable/disable the add/remove ptm buttons.
     */
    private void enableAddRemoveButtons() {
        removeVariableModification.setEnabled(variableModsTable.getSelectedRow() != -1);
        addVariableModification.setEnabled(modificationsTable.getSelectedRow() != -1);
        removeFixedModification.setEnabled(fixedModsTable.getSelectedRow() != -1);
        addFixedModification.setEnabled(modificationsTable.getSelectedRow() != -1);
    }

    /**
     * Updates the tooltip to the selected modification.
     *
     * @param list the list to update the tooltip for
     * @param evt the mouse event used to locate the item the mouse is hovering
     * over
     */
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

        final SearchGUISettingsDialog finalRef = this;

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
