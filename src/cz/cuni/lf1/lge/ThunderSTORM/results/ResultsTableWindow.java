package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.RenderingPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_DETECTIONS;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.ASHRenderingUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.max;
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
import java.util.Vector;
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

class ResultsTableWindow extends GenericTableWindow {
    
    private JButton io_import;
    private JButton io_export;
    private JButton showHist;
    private JButton render;
    private JCheckBox preview;
    private JLabel status;
    private RenderingQueue previewRenderer;
    private boolean livePreview;
    private JButton setCamera;
    private JButton resetButton;
    private JTabbedPane tabbedPane;
    private OperationsHistoryPanel operationsStackPanel;
    ResultsFilter resultsFilter;
    DuplicatesFilter removeDuplicates;
    ResultsGrouping resultsGrouping;
    ResultsDriftCorrection resultsDriftCorrection;
    ResultsStageOffset resultsStageOffset;
    
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
        setCamera = new JButton("Camera setup...");
        showHist = new JButton("Plot histogram...");
        io_import = new JButton("Import...");
        io_export = new JButton("Export...");
        render = new JButton("Render...");
        setCamera.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CameraSetupPlugIn().run(null);
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
                new ImportExportPlugIn().run(ImportExportPlugIn.IMPORT + ";" + IJResultsTable.IDENTIFIER);
            }
        });
        io_export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ImportExportPlugIn().run(ImportExportPlugIn.EXPORT + ";" + IJResultsTable.IDENTIFIER);
            }
        });
        render.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RenderingPlugIn().run(IJResultsTable.IDENTIFIER);
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
        buttons.add(setCamera);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(showHist);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(render);
        buttons.add(Box.createHorizontalStrut(5));
        buttons.add(io_import);
        buttons.add(Box.createHorizontalStrut(3));
        buttons.add(io_export);
        //
        resultsFilter = new ResultsFilter(this, model);
        removeDuplicates = new DuplicatesFilter(this, model);
        resultsGrouping = new ResultsGrouping(this, model);
        resultsDriftCorrection = new ResultsDriftCorrection();
        resultsStageOffset = new ResultsStageOffset(this, model);
        JPanel grouping = resultsGrouping.createUIPanel();
        JPanel duplicates = removeDuplicates.createUIPanel();
        JPanel filter = resultsFilter.createUIPanel();
        JPanel drift = resultsDriftCorrection.createUIPanel();
        JPanel offset = resultsStageOffset.createUIPanel();

        //fill tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Filter", filter);
        tabbedPane.addTab("Remove duplicates", duplicates);
        tabbedPane.addTab("Merging", grouping);
        tabbedPane.addTab("Drift correction", drift);
        tabbedPane.addTab("Z-stage offset", offset);

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
        if(livePreview) {
            if(!rt.columnExists(LABEL_X) || !rt.columnExists(LABEL_Y)) {
                IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X, LABEL_Y, rt.getColumnNames()));
                return;
            }
            if(rt.isEmpty()) {
                IJ.error("Results were empty.");
                return;
            }
            //
            if(previewRenderer == null) {
                IRendererUI renderer = new ASHRenderingUI();
            ImagePlus analyzedImage = rt.getAnalyzedImage();
            if(analyzedImage != null) {
                renderer.setSize(analyzedImage.getWidth(), analyzedImage.getHeight());
            }else{
                renderer.setSize((int)Math.ceil(max(rt.getColumnAsDoubles(LABEL_X, MoleculeDescriptor.Units.PIXEL))) + 1,
                                 (int)Math.ceil(max(rt.getColumnAsDoubles(LABEL_Y, MoleculeDescriptor.Units.PIXEL))) + 1);
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

    public void setStatus(String text) {
        if(text == null) {
            text = " ";
        }
        this.status.setText(text);
    }
    
    public String getFilterFormula() {
        return resultsFilter.getFilterFormula();
    }
    
    public void setFilterFormula(String formula) {
        resultsFilter.setFilterFormula(formula);
    }

    public OperationsHistoryPanel getOperationHistoryPanel() {
        return operationsStackPanel;
    }

    public ResultsFilter getFilter(){
        return resultsFilter;
    }
    
    public DuplicatesFilter getDuplicatesFilter() {
        return removeDuplicates;
    }
    
    public ResultsGrouping getGrouping(){
        return resultsGrouping;
    }
    
    public ResultsDriftCorrection getDriftCorrection(){
        return resultsDriftCorrection;
    }
    
    public ResultsStageOffset getStageOffset(){
        return resultsStageOffset;
    }
    
    @Override
    protected void tableMouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)) {
            if (e.getClickCount() == 2) {
                IJResultsTable rt = IJResultsTable.getResultsTable();
                int row = table.getSelectedRow();
                int rowIndex = rt.convertViewRowIndexToModel(row);
                Molecule mol = rt.getRow(rowIndex);
                if(mol.hasParam(LABEL_DETECTIONS)) {
                    if(mol.getParam(LABEL_DETECTIONS) > 1) {
                        Vector<Molecule> detections = mol.getDetections();
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
