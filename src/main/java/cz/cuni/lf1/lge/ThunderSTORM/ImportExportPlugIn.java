package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExport.IImportExport;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.MeasurementProtocol;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.PluginCommands;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.ValidatorException;
import ij.IJ;
import ij.WindowManager;
import ij.plugin.PlugIn;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

@SuppressWarnings("Since15")
public class ImportExportPlugIn implements PlugIn {

    public static final String IMPORT = "import";
    public static final String EXPORT = "export";
    private List<IImportExport> modules = null;
    private String[] moduleNames = null;
    private String[] moduleExtensions = null;

    private String path = null;

    public ImportExportPlugIn() {
        super();
        this.path = null;
    }

    public ImportExportPlugIn(String path) {
        super();
        this.path = path;
    }

    @Override
    public void run(String command) {
        GUI.setLookAndFeel();
        //Valid command strings:
        //"import;results"
        //"export;results"
        //"import;ground-truth"
        //"export;ground-truth"
        String[] commands = command.split(";");
        if(commands.length != 2) {
            throw new IllegalArgumentException("Malformed argument for Import/Export plug-in!");
        }
        //
        try {
            //get table
            GenericTable table;
            boolean groundTruth = IJGroundTruthTable.IDENTIFIER.equals(commands[1]);
            if(groundTruth) {
                table = IJGroundTruthTable.getGroundTruthTable();
            } else {
                table = IJResultsTable.getResultsTable();
            }

            setupModules();

            if(EXPORT.equals(commands[0])) {
                runExport(table, groundTruth);
            } else if(IMPORT.equals(commands[0])) {
                runImport(table, groundTruth);
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    private void runImport(GenericTable table, boolean groundTruth) {
        ImportDialog dialog = new ImportDialog(IJ.getInstance(), groundTruth);
        if(MacroParser.isRanFromMacro()) {
            dialog.getParams().readMacroOptions();
            dialog.getFileParams().readMacroOptions();
        } else {
            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return;
            }
        }

        path = dialog.getFilePath();
        boolean resetFirst = !dialog.append.getValue();
        int startingFrame = dialog.startingFrame.getValue();
        IImportExport importer = getModuleByName(dialog.getFileFormat());
        table.forceHide();
        if(resetFirst) table.reset();
        if(groundTruth) {
            callImporter(importer, table, path, startingFrame);
        } else {    // IJResultsTable
            IJResultsTable ijrt = (IJResultsTable) table;
            try {
                ijrt.setAnalyzedImage(WindowManager.getImage(dialog.rawImageStack.getValue()));
            } catch(ArrayIndexOutOfBoundsException ex) {
                if(resetFirst) {
                    ijrt.setAnalyzedImage(null);
                }
            }
            importMeasurementProtocol();
            callImporter(importer, table, path, startingFrame);
            AnalysisPlugIn.setDefaultColumnsWidth(ijrt);
            ijrt.setLivePreview(dialog.showPreview.getValue());
            ijrt.showPreview();
        }
        table.forceShow();
    }

    private void runExport(GenericTable table, boolean groundTruth) {
        String[] colNames = (String[]) table.getColumnNames().toArray(new String[0]);

        ExportDialog dialog = new ExportDialog(IJ.getInstance(), groundTruth, colNames);
        if(MacroParser.isRanFromMacro()) {
            dialog.getParams().readMacroOptions();
            dialog.getFileParams().readMacroOptions();
        } else {
            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return;
            }
        }

        // eliminate the need to list all columns when running from macro
        // - if nothing is mentioned, assume user wants to export everything
        if (MacroParser.isRanFromMacro()) {
            boolean all = true;
            for (int i = 0; i < dialog.exportColumns.length; i++) {
                if (dialog.exportColumns[i].getValue()) {
                    all = false;
                    break;
                }
            }
            if (all) {
                for (int i = 0; i < dialog.exportColumns.length; i++) {
                    dialog.exportColumns[i].setValue(true);
                }
            }
        }

        List<String> columns = new ArrayList<String>();
        for(int i = 0; i < colNames.length; i++) {
            if(dialog.exportColumns[i].getValue()) {
                columns.add(colNames[i]);
            }
        }
        path = dialog.getFilePath();
        //save protocol
        if(!groundTruth && dialog.saveProtocol.getValue()) {
            IJResultsTable ijrt = (IJResultsTable) table;
            if(ijrt.getMeasurementProtocol() != null) {
                ijrt.getMeasurementProtocol().exportToFile(getProtocolFilePath(path));
            }
        }

        //export
        IImportExport exporter = getModuleByName(dialog.getFileFormat());
        callExporter(exporter, table, path, dialog.getFloatPrecision(), columns);
    }

    private void importMeasurementProtocol() {
        // Create an empty protocol first
        IJResultsTable.getResultsTable().setMeasurementProtocol(new MeasurementProtocol());
        //
        // Automatically load protocol, if available -> this allows for (re)calculation of
        // fitting uncertainties and other values that depend on the data about analysis.
        File protoFile = new File(getProtocolFilePath(path));
        if(protoFile.exists() && protoFile.isFile()) {
            int dialogResult = JOptionPane.showConfirmDialog(null,
                    "We detected a measurement protocol for the selected data.\nDo you with to load the information?",
                    "Measurement protocol", JOptionPane.YES_NO_OPTION);
            if (dialogResult == JOptionPane.YES_OPTION) {
                // Parse the protocol and attach it to the results table.
                MeasurementProtocol protocol = MeasurementProtocol.importFromFile(getProtocolFilePath(path));
                IJResultsTable.getResultsTable().setMeasurementProtocol(protocol);
                // Also, update the available camera settings!
                if (!protocol.cameraSettings.isEmpty()) {
                    CameraSetupPlugIn.params.setNoGuiParametersAllowed(true);   // allow setting parameters even before UI is initialized
                    if (protocol.cameraSettings.containsKey(CameraSetupPlugIn.EM_GAIN_ENABLED)) {
                        CameraSetupPlugIn.setIsEmGain(((Boolean) protocol.cameraSettings.get(CameraSetupPlugIn.EM_GAIN_ENABLED)).booleanValue());
                    }
                    if (protocol.cameraSettings.containsKey(CameraSetupPlugIn.EM_GAIN)) {
                        CameraSetupPlugIn.setGain(((Double) protocol.cameraSettings.get(CameraSetupPlugIn.EM_GAIN)).doubleValue());
                    }
                    if (protocol.cameraSettings.containsKey(CameraSetupPlugIn.BASELINE_OFFSET)) {
                        CameraSetupPlugIn.setOffset(((Double) protocol.cameraSettings.get(CameraSetupPlugIn.BASELINE_OFFSET)).doubleValue());
                    }
                    if (protocol.cameraSettings.containsKey(CameraSetupPlugIn.READOUT_NOISE)) {
                        CameraSetupPlugIn.setReadoutNoise(((Double) protocol.cameraSettings.get(CameraSetupPlugIn.READOUT_NOISE)).doubleValue());
                    }
                    if (protocol.cameraSettings.containsKey(CameraSetupPlugIn.PHOTONS_TO_ADU)) {
                        CameraSetupPlugIn.setPhotons2ADU(((Double) protocol.cameraSettings.get(CameraSetupPlugIn.PHOTONS_TO_ADU)).doubleValue());
                    }
                    if (protocol.cameraSettings.containsKey(CameraSetupPlugIn.PIXEL_SIZE)) {
                        CameraSetupPlugIn.setPixelSize(((Double) protocol.cameraSettings.get(CameraSetupPlugIn.PIXEL_SIZE)).doubleValue());
                    }
                    if (protocol.cameraSettings.containsKey(CameraSetupPlugIn.QUANTUM_EFFICIENCY)) {
                        CameraSetupPlugIn.setQuantumEfficiency(((Double) protocol.cameraSettings.get(CameraSetupPlugIn.QUANTUM_EFFICIENCY)).doubleValue());
                    }
                    CameraSetupPlugIn.params.setNoGuiParametersAllowed(false);   // back to the default setting
                }
            }
        }
    }

    private void setupModules() {
        modules = ModuleLoader.getModules(IImportExport.class);
        moduleNames = new String[modules.size()];
        moduleExtensions = new String[modules.size()];
        for(int i = 0; i < moduleNames.length; i++) {
            moduleNames[i] = modules.get(i).getName();
            moduleExtensions[i] = modules.get(i).getSuffix();
        }
    }

    private String getProtocolFilePath(String fpath) {
        int dotpos = fpath.lastIndexOf('.');
        if(dotpos < 0) {
            return fpath + "-protocol.txt";
        } else {
            return fpath.substring(0, dotpos) + "-protocol.txt";
        }
    }

    private void callExporter(IImportExport exporter, GenericTable table, String fpath, int floatPrecision, List<String> columns) {
        IJ.showStatus("ThunderSTORM is exporting your results...");
        IJ.showProgress(0.0);
        try {
            exporter.exportToFile(fpath, floatPrecision, table, columns);
            IJ.showStatus("ThunderSTORM has exported your results.");
        } catch(IOException ex) {
            IJ.showStatus("");
            IJ.showMessage("Exception", ex.getMessage());
        } catch(Exception ex) {
            IJ.showStatus("");
            IJ.handleException(ex);
        }
        IJ.showProgress(1.0);
    }

    private void callImporter(IImportExport importer, GenericTable table, String fpath, int startingFrame) {
        IJ.showStatus("ThunderSTORM is importing your file...");
        IJ.showProgress(0.0);
        try {
            table.setOriginalState();
            importer.importFromFile(fpath, table, startingFrame);
            table.convertAllColumnsToAnalogUnits();
            IJ.showStatus("ThunderSTORM has imported your file.");
        } catch(IOException ex) {
            IJ.showStatus("");
            IJ.showMessage("Exception", ex.getMessage());
        } catch(Exception ex) {
            IJ.showStatus("");
            IJ.handleException(ex);
        }
        IJ.showProgress(1.0);
    }

    public IImportExport getModuleByName(String name) {
        for(int i = 0; i < moduleNames.length; i++) {
            if(moduleNames[i].equals(name)) {
                return modules.get(i);
            }
        }
        throw new RuntimeException("No module found for name " + name + ".");
    }

    //---------------GUI-----------------------
    abstract class IODialog extends DialogStub {

        public ParameterTracker fileParams;

        public IODialog(ParameterTracker params, ParameterTracker fileParams, Window owner, String title) {
            super(params, owner, title);
            this.fileParams = fileParams;
        }

        @Override
        protected JPanel createButtonsPanel() {
            JPanel buttons = new JPanel(new GridBagLayout());
            GridBagConstraints glueConstraints = new GridBagConstraints();
            glueConstraints.fill = GridBagConstraints.HORIZONTAL;
            glueConstraints.weightx = 1;

            JButton okButton = createOKButton();
            okButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    try {
                        fileParams.readDialogOptions();
                        dispose();
                        result = JOptionPane.OK_OPTION;
                        fileParams.recordMacroOptions();
                        if (fileParams.isPrefsSavingEnabled()) {
                            fileParams.savePrefs();
                        }
                    } catch (ValidatorException ex) {
                        handleValidationException(ex);
                    }
                }
            });
            JButton defaultsButton = createDefaultsButton();
            defaultsButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fileParams.resetToDefaults(true);
                }
            });

            buttons.add(defaultsButton);
            buttons.add(Box.createHorizontalGlue(), glueConstraints);
            buttons.add(Help.createHelpButton(ImportExportPlugIn.class));
            buttons.add(okButton);
            buttons.add(createCancelButton());
            return buttons;
        }

        ParameterTracker getFileParams() {
            return fileParams;
        }
    }

    class ImportDialog extends IODialog {

        ParameterKey.String fileFormat;
        ParameterKey.String filePath;
        ParameterKey.Integer startingFrame;
        ParameterKey.Boolean showPreview;
        ParameterKey.Boolean append;
        ParameterKey.String rawImageStack;

        private boolean groundTruth;

        public ImportDialog(Window owner, boolean groundTruth) {
            super(new ParameterTracker("thunderstorm.io"), new ParameterTracker("thunderstorm.io." + (groundTruth ? "gt" : "res")), owner, "Import" + (groundTruth ? " ground-truth" : ""));
            assert moduleNames != null && moduleNames.length > 0;
            fileFormat = fileParams.createStringField("fileFormat", StringValidatorFactory.isMember(moduleNames), moduleNames[0]);
            filePath = fileParams.createStringField("filePath", StringValidatorFactory.fileExists(), "");
            startingFrame = params.createIntField("startingFrame", IntegerValidatorFactory.positiveNonZero(), 1);
            append = params.createBooleanField("append", null, false);
            this.groundTruth = groundTruth;
            if(!groundTruth) {
                showPreview = params.createBooleanField("livePreview", null, true);
                rawImageStack = params.createStringField("rawImageStack", StringValidatorFactory.openImages(true), "");
            }
        }

        ParameterTracker getParams() {
            return params;
        }

        @Override
        protected void layoutComponents() {
            JTextField startingFrameTextField = new JTextField(20);

            JPanel cameraPanel = new JPanel(new BorderLayout());
            cameraPanel.setBorder(new TitledBorder("Camera"));
            JButton cameraSetup = new JButton("Camera setup");
            cameraSetup.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MacroParser.runNestedWithRecording(PluginCommands.CAMERA_SETUP, null);
                }
            });
            cameraPanel.add(cameraSetup);
            add(cameraPanel, GridBagHelper.twoCols());

            JPanel filePanel = new JPanel(new GridBagLayout());
            filePanel.setBorder(new TitledBorder("Input File"));
            JComboBox<String> fileFormatCBox = new JComboBox<String>(moduleNames);
            JTextField filePathTextField = new JTextField(20);
            filePathTextField.getDocument().addDocumentListener(createDocListener(filePathTextField, fileFormatCBox));
            fileFormatCBox.addItemListener(createItemListener(filePathTextField, fileFormatCBox));
            JButton browseButton = createBrowseButton(filePathTextField, true, new FileNameExtensionFilter(createFilterString(moduleExtensions), moduleExtensions));
            fileFormat.registerComponent(fileFormatCBox);
            filePath.registerComponent(filePathTextField);
            JPanel filePathPanel = new JPanel(new BorderLayout());
            filePathPanel.add(filePathTextField);
            filePathPanel.add(browseButton, BorderLayout.EAST);
            filePathPanel.setPreferredSize(startingFrameTextField.getPreferredSize());
            filePanel.add(new JLabel("File format:"), GridBagHelper.leftCol());
            filePanel.add(fileFormatCBox, GridBagHelper.rightCol());
            filePanel.add(new JLabel("File path:"), GridBagHelper.leftCol());
            filePanel.add(filePathPanel, GridBagHelper.rightCol());
            add(filePanel, GridBagHelper.twoCols());

            JPanel concatenationPanel = new JPanel(new GridBagLayout());
            concatenationPanel.setBorder(new TitledBorder("Results concatenation"));
            JCheckBox appendCheckBox = new JCheckBox();
            startingFrame.registerComponent(startingFrameTextField);
            append.registerComponent(appendCheckBox);
            concatenationPanel.add(new JLabel("Append to current table:"), GridBagHelper.leftCol());
            concatenationPanel.add(appendCheckBox, GridBagHelper.rightCol());
            concatenationPanel.add(new JLabel("Starting frame number:"), GridBagHelper.leftCol());
            concatenationPanel.add(startingFrameTextField, GridBagHelper.rightCol());
            add(concatenationPanel, GridBagHelper.twoCols());

            if(!groundTruth) {
                JCheckBox showPreviewCheckBox = new JCheckBox();
                JComboBox<String> rawImageComboBox = createOpenImagesComboBox(true);
                rawImageComboBox.setPreferredSize(startingFrameTextField.getPreferredSize());
                showPreview.registerComponent(showPreviewCheckBox);
                rawImageStack.registerComponent(rawImageComboBox);
                JPanel previewPanel = new JPanel(new GridBagLayout());
                previewPanel.setBorder(new TitledBorder("Visualization"));
                previewPanel.add(new JLabel("Live preview:"), GridBagHelper.leftCol());
                previewPanel.add(showPreviewCheckBox, GridBagHelper.rightCol());
                previewPanel.add(new JLabel("Raw image sequence for overlay:"), GridBagHelper.leftCol());
                previewPanel.add(rawImageComboBox, GridBagHelper.rightCol());
                add(previewPanel, GridBagHelper.twoCols());
            }

            add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
            add(createButtonsPanel(), GridBagHelper.twoCols());

            params.loadPrefs();
            fileParams.loadPrefs();
            if(path != null && !path.isEmpty()) {
                filePath.setValue(path);
            }

            getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            pack();
            setLocationRelativeTo(null);
            setModal(true);
        }

        public String getFilePath() {
            return filePath.getValue();
        }

        public String getFileFormat() {
            return fileFormat.getValue();
        }
    }

    class ExportDialog extends IODialog {

        ParameterKey.String fileFormat;
        ParameterKey.String filePath;
        ParameterKey.Integer floatPrecision;
        ParameterKey.Boolean[] exportColumns;
        ParameterKey.Boolean saveProtocol;

        private boolean groundTruth;
        private String[] columnHeaders;

        public ExportDialog(Window owner, boolean groundTruth, String[] columnHeaders) {
            super(new ParameterTracker("thunderstorm.io"), new ParameterTracker("thunderstorm.io." + (groundTruth ? "gt" : "res")), owner, "Export" + (groundTruth ? " ground-truth" : ""));
            assert moduleNames != null && moduleNames.length > 0;
            this.columnHeaders = columnHeaders;
            this.groundTruth = groundTruth;
            fileFormat = fileParams.createStringField("fileFormat", StringValidatorFactory.isMember(moduleNames), moduleNames[0]);
            filePath = fileParams.createStringField("filePath", null, "");
            floatPrecision = fileParams.createIntField("floatPrecision", IntegerValidatorFactory.positive(), 5);
            exportColumns = new ParameterKey.Boolean[columnHeaders.length];
            for(int i = 0; i < columnHeaders.length; i++) {
                exportColumns[i] = params.createBooleanField(columnHeaders[i], null, i != 0);
            }
            if(!groundTruth) {
                saveProtocol = params.createBooleanField("saveProtocol", null, true);
            }
        }

        ParameterTracker getParams() {
            return params;
        }

        @Override
        protected void layoutComponents() {
            JPanel pane = new JPanel();
            pane.setLayout(new GridBagLayout());
            GridBagConstraints componentConstraints = new GridBagConstraints();
            componentConstraints.gridx = 0;
            componentConstraints.fill = GridBagConstraints.BOTH;
            componentConstraints.weightx = 1;

            JPanel filePanel = new JPanel(new GridBagLayout());
            filePanel.setBorder(new TitledBorder("Output File"));
            JComboBox<String> fileFormatCBox = new JComboBox<String>(moduleNames);
            JTextField filePathTextField = new JTextField(20);
            filePathTextField.getDocument().addDocumentListener(createDocListener(filePathTextField, fileFormatCBox));
            fileFormatCBox.addItemListener(createItemListener(filePathTextField, fileFormatCBox));
            JButton browseButton = createBrowseButton(filePathTextField, true, new FileNameExtensionFilter(createFilterString(moduleExtensions), moduleExtensions));
            JTextField floatPrecisionTextField = new JTextField(5);
            fileFormat.registerComponent(fileFormatCBox);
            filePath.registerComponent(filePathTextField);
            floatPrecision.registerComponent(floatPrecisionTextField);
            JPanel filePathPanel = new JPanel(new BorderLayout());
            filePathPanel.setPreferredSize(filePathTextField.getPreferredSize());
            filePathPanel.add(filePathTextField);
            filePathPanel.add(browseButton, BorderLayout.EAST);
            filePanel.add(new JLabel("File format:"), GridBagHelper.leftCol());
            filePanel.add(fileFormatCBox, GridBagHelper.rightCol());
            filePanel.add(new JLabel("File path:"), GridBagHelper.leftCol());
            filePanel.add(filePathPanel, GridBagHelper.rightCol());
            filePanel.add(new JLabel("Float precision:"), GridBagHelper.leftCol());
            filePanel.add(floatPrecisionTextField, GridBagHelper.rightCol());
            pane.add(filePanel, componentConstraints);

            if(!groundTruth) {
                JPanel protocolPanel = new JPanel(new GridBagLayout());
                protocolPanel.setBorder(new TitledBorder("Protocol"));
                protocolPanel.add(Box.createHorizontalStrut(filePathTextField.getPreferredSize().width / 2), GridBagHelper.leftCol());
                protocolPanel.add(Box.createHorizontalStrut(filePathTextField.getPreferredSize().width), GridBagHelper.rightCol());
                JCheckBox saveProtocolCheckBox = new JCheckBox();
                saveProtocol.registerComponent(saveProtocolCheckBox);
                protocolPanel.add(new JLabel("Save measurement protocol:"), GridBagHelper.leftCol());
                protocolPanel.add(saveProtocolCheckBox, GridBagHelper.rightCol());
                pane.add(protocolPanel, componentConstraints);
            }

            JPanel columnsPanel = new JPanel(new GridBagLayout());
            columnsPanel.setBorder(new TitledBorder("Columns to export"));
            columnsPanel.add(Box.createHorizontalStrut(0), GridBagHelper.leftCol());
            columnsPanel.add(Box.createHorizontalStrut(filePathTextField.getPreferredSize().width), GridBagHelper.rightCol());
            for(int i = 0; i < columnHeaders.length; i++) {
                columnsPanel.add(new JLabel(columnHeaders[i]), GridBagHelper.leftCol());
                JCheckBox colCheckBox = new JCheckBox();
                exportColumns[i].registerComponent(colCheckBox);
                columnsPanel.add(colCheckBox, GridBagHelper.rightCol());
            }
            pane.add(columnsPanel, componentConstraints);

            pane.add(Box.createVerticalStrut(10), componentConstraints);
            pane.add(createButtonsPanel(), componentConstraints);
            pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

            JScrollPane scrollPane = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setBorder(BorderFactory.createEmptyBorder());
            add(scrollPane);

            params.loadPrefs();
            fileParams.loadPrefs();

            int maxScreenHeight = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
            if(getHeight() > maxScreenHeight) {
                setSize(getWidth(), maxScreenHeight);
            }
        }

        @Override
        public int showAndGetResult() {
            setLocationRelativeTo(null);
            setModal(true);
            setResizable(true);
            layoutComponents();
            pack();
            setVisible(true);
            return result;
        }

        public String getFilePath() {
            return filePath.getValue();
        }

        public String getFileFormat() {
            return fileFormat.getValue();
        }

        public int getFloatPrecision() {
            return floatPrecision.getValue();
        }
    }

    private String createFilterString(String[] moduleExtensions) {
        StringBuilder sb = new StringBuilder();
        sb.append("Known formats(");
        for(String ext : moduleExtensions) {
            sb.append(ext);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }

    private ItemListener createItemListener(final JTextField filePathTextField, final JComboBox<String> fileFormatCBox) {
        return new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                final String fp = filePathTextField.getText();
                if(!fp.isEmpty()) {
                    if(fp.endsWith("\\") || fp.endsWith("/")) {
                        filePathTextField.setText(fp + "results." + moduleExtensions[fileFormatCBox.getSelectedIndex()]);
                    } else {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                int dotpos = fp.lastIndexOf('.');
                                if(dotpos > 0) {
                                    filePathTextField.setText(fp.substring(0, dotpos + 1) + moduleExtensions[fileFormatCBox.getSelectedIndex()]);
                                }
                            }
                        });
                    }
                }
            }
        };
    }

    private DocumentListener createDocListener(final JTextField filePathTextField, final JComboBox<String> fileFormatCBox) {
        return new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                handle();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handle();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }

            private void handle() {
                String fname = new File(filePathTextField.getText()).getName().trim();
                if(fname.isEmpty()) {
                    return;
                }
                int dotpos = fname.lastIndexOf('.');
                if(dotpos >= 0) {
                    String type = fname.substring(dotpos + 1).trim();
                    for(int i = 0; i < moduleExtensions.length; i++) {
                        if(type.equals(moduleExtensions[i])) {
                            //found correct suffix, adjust type combobox and return
                            fileFormatCBox.setSelectedIndex(i);
                            return;
                        }
                    }
                } else {
                    //no suffix found
                    if(!filePathTextField.isFocusOwner()) {
                        //user is not writting text at the moment
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                String selectedTypeSuffix = moduleExtensions[fileFormatCBox.getSelectedIndex()];
                                filePathTextField.setText(filePathTextField.getText() + "." + selectedTypeSuffix);
                            }
                        });
                    }
                }
            }
        };
    }
}
