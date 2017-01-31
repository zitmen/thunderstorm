package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_DETECTIONS;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.ASHRenderingUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.RendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.PluginCommands;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import ij.ImagePlus;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Collections;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class ResultsTableWindow extends GenericTableWindow {

    private JButton io_import;
    private JButton io_export;
    private JButton showHist;
    private JButton render;
    private JButton defaultsButton;
    private JCheckBox preview;
    private JLabel status;
    private RenderingQueue previewRenderer;
    private boolean livePreview;
    private JButton resetButton;
    private JTabbedPane tabbedPane;
    private OperationsHistoryPanel operationsStackPanel;
    private PostProcessingModule[] postProcessingModules;

    public ResultsTableWindow(String frameTitle) {
        super(frameTitle);
    }

    @Override
    protected void packFrame() {
        frame.setPreferredSize(new Dimension(600, 750));
        //
        status = new JLabel(" ");
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        //buttons
        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        defaultsButton = new JButton("Defaults");
        showHist = new JButton("Plot histogram");
        io_import = new JButton("Import");
        io_export = new JButton("Export");
        render = new JButton("Visualization");
        defaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                for(PostProcessingModule module : postProcessingModules){
                    module.resetParamsToDefaults();
                }
            }
        });
        showHist.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new IJDistribution().run(IJResultsTable.IDENTIFIER);
            }
        });
        io_import.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MacroParser.runNestedWithRecording(PluginCommands.IMPORT_RESULTS.getValue(), null);
            }
        });
        io_export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MacroParser.runNestedWithRecording(PluginCommands.EXPORT_RESULTS.getValue(), null);
            }
        });
        render.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MacroParser.runNestedWithRecording(PluginCommands.RENDERING.getValue(), null);
            }
        });
        livePreview = false;
        preview = new JCheckBox("Preview", livePreview);
        preview.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                livePreview = (e.getStateChange() == ItemEvent.SELECTED);
                showPreview();
            }
        });
        buttons.add(preview);
        buttons.add(Box.createHorizontalGlue());
        buttons.add(defaultsButton);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(showHist);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(render);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(io_import);
        buttons.add(Box.createHorizontalStrut(3));
        buttons.add(io_export);
        //
        postProcessingModules = PostProcessingModulesFactory.createAllPostProcessingModules();

        //fill tabbed pane
        tabbedPane = new JTabbedPane();
        for(PostProcessingModule module : postProcessingModules) {
            module.setModel(model);
            module.setTable(this);
            tabbedPane.addTab(module.getTabName(), module.getUIPanel());
        }

        //history pane
        JPanel historyPane = new JPanel(new GridBagLayout());
        operationsStackPanel = new OperationsHistoryPanel();
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.copyOriginalToActual();
                model.convertAllColumnsToAnalogUnits();
                operationsStackPanel.removeAllOperations();
                setStatus("Results reset.");
                showPreview();
                TableHandlerPlugin.recordReset();
            }
        });
        historyPane.add(operationsStackPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        historyPane.add(resetButton);

        Container contentPane = frame.getContentPane();
        JPanel controlsPane = new JPanel();
        controlsPane.setLayout(new BoxLayout(controlsPane, BoxLayout.PAGE_AXIS));

        contentPane.add(tableScrollPane, BorderLayout.CENTER);
        contentPane.add(controlsPane, BorderLayout.SOUTH);

        controlsPane.add(tabbedPane);
        controlsPane.add(historyPane);
        controlsPane.add(new JSeparator(SwingConstants.HORIZONTAL));
        controlsPane.add(buttons);
        controlsPane.add(new JSeparator(SwingConstants.HORIZONTAL));
        controlsPane.add(status);
        //
        frame.setContentPane(contentPane);
        frame.pack();
    }

    public void setLivePreview(boolean enabled) {
        livePreview = enabled;
        preview.setSelected(enabled);
    }

    public void showPreview() {
        IJResultsTable rt = IJResultsTable.getResultsTable();
        if(livePreview && !rt.isEmpty()) {
            if(!rt.columnExists(LABEL_X) || !rt.columnExists(LABEL_Y)) {
                IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X, LABEL_Y, rt.getColumnNames()));
                return;
            }
            if(previewRenderer == null) {
                RendererUI renderer = new ASHRenderingUI(0.0, 0.0);
                ImagePlus analyzedImage = rt.getAnalyzedImage();
                if(analyzedImage != null) {
                    renderer.setSize(analyzedImage.getWidth(), analyzedImage.getHeight());
                } else {
                    double[] xpos = rt.getColumnAsDoubles(LABEL_X, MoleculeDescriptor.Units.PIXEL);
                    double[] ypos = rt.getColumnAsDoubles(LABEL_Y, MoleculeDescriptor.Units.PIXEL);
                    int left = Math.max((int) Math.floor(VectorMath.min(xpos)) - 1, 0);
                    int top = Math.max((int) Math.floor(VectorMath.min(ypos)) - 1, 0);
                    int right = (int) Math.ceil(VectorMath.max(xpos)) + 1;
                    int bottom = (int) Math.ceil(VectorMath.max(ypos)) + 1;
                    renderer.setSize(left, top, right - left + 1, bottom - top + 1);
                }
                IncrementalRenderingMethod rendererImplementation = renderer.getImplementation();
                previewRenderer = new RenderingQueue(rendererImplementation,
                        new RenderingQueue.DefaultRepaintTask(rendererImplementation.getRenderedImage()),
                        renderer.getRepaintFrequency());
            }
            //
            previewRenderer.resetLater();
            previewRenderer.renderLater(rt.getData());
            previewRenderer.repaintLater();
        }
        rt.repaintAnalyzedImageOverlay();
    }

    public void setPreviewRenderer(RenderingQueue renderer) {
        previewRenderer = renderer;
        livePreview = (renderer != null);
        preview.setSelected(livePreview);
    }

    public ImagePlus getPreviewImage() {
        if(!livePreview || previewRenderer == null) {
            return null;
        }
        return previewRenderer.method.getRenderedImage();
    }

    public void setStatus(String text) {
        if(text == null) {
            text = " ";
        }
        this.status.setText(text);
    }

    public ResultsFilter getFilter() {
        if(postProcessingModules != null) {
            for(PostProcessingModule module : postProcessingModules) {
                if(module instanceof ResultsFilter) {
                    return (ResultsFilter) module;
                }
            }
        }
        return null;
    }

    public OperationsHistoryPanel getOperationHistoryPanel() {
        return operationsStackPanel;
    }

    public PostProcessingModule[] getPostProcessingModules() {
        return postProcessingModules;
    }

    @Override
    protected void tableMouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)) {
            if(e.getClickCount() == 2) {
                IJResultsTable rt = IJResultsTable.getResultsTable();
                int row = table.getSelectedRow();
                int rowIndex = rt.convertViewRowIndexToModel(row);
                Molecule mol = rt.getRow(rowIndex);
                if(mol.hasParam(LABEL_DETECTIONS)) {
                    if(mol.getParam(LABEL_DETECTIONS) > 1) {
                        List<Molecule> detections = mol.getDetections();
                        Collections.sort(detections);
                        new MergedMoleculesPopUp(table, row, 0, detections);
                    }
                }
            }
        } else if(SwingUtilities.isRightMouseButton(e)) {
            if(table.getSelectedRowCount() > 0) {
                new TableRowsPopUpMenu(e, this);
            }
        }
    }

    @Override
    protected void tableMouseMoved(MouseEvent e) {
        IJResultsTable rt = IJResultsTable.getResultsTable();
        int rowIndex = rt.convertViewRowIndexToModel(table.rowAtPoint(e.getPoint()));
        Molecule mol = rt.getRow(rowIndex);
        if(mol.hasParam(LABEL_DETECTIONS)) {
            if(mol.getParam(LABEL_DETECTIONS) > 1) {
                table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                return;
            }
        }
        table.setCursor(Cursor.getDefaultCursor()); // reset
    }

    @Override
    protected void dropFile(File f) {
        new ImportExportPlugIn(f.getAbsolutePath()).run(ImportExportPlugIn.IMPORT + ";" + IJResultsTable.IDENTIFIER);
    }

}
