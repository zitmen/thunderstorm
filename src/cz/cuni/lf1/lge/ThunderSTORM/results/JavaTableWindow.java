package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_DETECTIONS;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import ij.Executer;
import ij.IJ;
import ij.WindowManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

class JavaTableWindow {

    private JTable table;
    private JFrame frame;
    private JButton io_import;
    private JButton io_export;
    private JButton render;
    private JCheckBox preview;
    private JLabel status;
    private RenderingQueue previewRenderer;
    private boolean livePreview;
    private JButton setCamera;
    private JButton showHist;
    private JButton resetButton;
    private JTabbedPane tabbedPane;
    private OperationsHistoryPanel operationsStackPanel;
    private final TripleStateTableModel model;

    public JavaTableWindow() {
        frame = new JFrame("ThunderSTORM: Results");
        frame.setIconImage(IJ.getInstance().getIconImage());
        //
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        frame.setPreferredSize(new Dimension(600, 750));
        //
        model = new TripleStateTableModel();
        table = new JTable(model);
        TableRowSorter<TripleStateTableModel> sorter = new TableRowSorter<TripleStateTableModel>(model);
        table.setRowSorter(sorter);
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(SwingUtilities.isRightMouseButton(e)) {
                    new UnitsContextMenu(e, table.columnAtPoint(e.getPoint()));
                }
            }
        });
        table.setDropTarget(new ResultsTableDropTarget());
        JScrollPane tableScrollPane = new JScrollPane(table);
        tableScrollPane.setDropTarget(new ResultsTableDropTarget());
        table.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // drag&drop is not supported here
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                //
                IJResultsTable rt = IJResultsTable.getResultsTable();
                Molecule mol = rt.getRow(table.rowAtPoint(e.getPoint()));
                if(mol.hasParam(LABEL_DETECTIONS)) {
                    if(mol.getParam(LABEL_DETECTIONS) > 1) {
                        table.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        return;
                    }
                }
                table.setCursor(Cursor.getDefaultCursor()); // reset
            }
        });
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    IJResultsTable rt = IJResultsTable.getResultsTable();
                    int row = table.getSelectedRow();
                    Molecule mol = rt.getRow(row);
                    if(mol.hasParam(LABEL_DETECTIONS)) {
                        if(mol.getParam(LABEL_DETECTIONS) > 1) {
                            new MergedMoleculesPopUp(table, row, 0, mol.getDetections());
                        }
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                // nothing
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // nothing
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // nothing
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // nothing
            }
        });
        //
        status = new JLabel(" ");
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        status.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        //
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
                new IJDistribution().run(null);
            }
        });
        io_import.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ImportExportPlugIn().run("import");
            }
        });
        io_export.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ImportExportPlugIn().run("export");
            }
        });
        render.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Executer("Rendering").run();
            }
        });
        livePreview = true;
        preview = new JCheckBox("Preview", livePreview);
        preview.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                livePreview = (e.getStateChange() == ItemEvent.SELECTED);
                showPreview();
            }
        });
        preview.setEnabled(false);
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
        JPanel grouping = new ResultsGrouping(this, model).createUIPanel();
        JPanel filter = new ResultsFilter(this, model).createUIPanel();
        JPanel drift = new ResultsDriftCorrection().createUIPanel();

        //fill tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("filter", filter);
        tabbedPane.addTab("merging", grouping);
        tabbedPane.addTab("drift correction", drift);

        //history pane
        JPanel historyPane = new JPanel(new GridBagLayout());
        operationsStackPanel = new OperationsHistoryPanel();
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                model.copyOriginalToActual();
                operationsStackPanel.removeAllOperations();
                setStatus("Results reset.");
                showPreview();
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

    public void showPreview() {
        if(livePreview == false || previewRenderer == null) {
            return;
        }
        //
        IJResultsTable rt = IJResultsTable.getResultsTable();
        if(!rt.columnExists(LABEL_X) || !rt.columnExists(LABEL_Y)) {
            IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X, LABEL_Y, rt.getColumnNames()));
            return;
        }
        if(rt.isEmpty()) {
            IJ.error("results were empty");
            return;
        }
        previewRenderer.resetLater();
        previewRenderer.renderLater(rt.getData());
        previewRenderer.repaintLater();
    }

    public TripleStateTableModel getModel() {
        return (TripleStateTableModel) table.getModel();
    }

    public JTable getView() {
        return table;
    }

    public void show(String title) {
        frame.setTitle(title);
        show();
    }

    public void show() {
        WindowManager.addWindow(frame); // ImageJ's own Window Manager
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                frame.setVisible(true);
            }
        });
        WindowManager.setWindow(frame); // ImageJ's own Window Manager
    }

    public void hide() {
        frame.setVisible(false);
        WindowManager.removeWindow(frame); // ImageJ's own Window Manager
    }

    public boolean isVisible() {
        return frame.isVisible();
    }

    public void setPreviewRenderer(RenderingQueue renderer) {
        preview.setEnabled(renderer != null);
        previewRenderer = renderer;
    }

    public void setStatus(String text) {
        if(text == null) {
            text = " ";
        }
        this.status.setText(text);
    }

    public OperationsHistoryPanel getOperationHistoryPanel() {
        return operationsStackPanel;
    }

    private class UnitsContextMenu implements ActionListener {
        
        private IJResultsTable rt;
        private int column;

        public UnitsContextMenu(MouseEvent e, int column) {
            this.column = column;
            this.rt = IJResultsTable.getResultsTable();
            Units selected = rt.getColumnUnits(column);
            JPopupMenu popup = new JPopupMenu();
            //
            if(MoleculeDescriptor.LABEL_ID.equals(rt.getColumnName(column))) {
                JMenuItem item;
                item = new JMenuItem("convert all to digital units");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        AnalysisPlugIn.convertAllColumnsToDigitalUnits(rt);
                    }
                });
                popup.add(item);
                item = new JMenuItem("convert all to analog units");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        AnalysisPlugIn.convertAllColumnsToAnalogUnits(rt);
                    }
                });
                popup.add(item);
                popup.add(new JSeparator());
            } else if(MoleculeDescriptor.Fitting.LABEL_CCD_THOMPSON.equals(rt.getColumnName(column))) {
                JMenuItem item = new JMenuItem("recalculate");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        AnalysisPlugIn.convertAllColumnsToAnalogUnits(rt); // ensure that the units are correct!
                        AnalysisPlugIn.calculateThompsonFormula(rt);
                    }
                });
                popup.add(item);
                popup.add(new JSeparator());
            } else if(MoleculeDescriptor.Fitting.LABEL_EMCCD_THOMPSON.equals(rt.getColumnName(column))) {
                JMenuItem item = new JMenuItem("recalculate");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        AnalysisPlugIn.convertAllColumnsToAnalogUnits(rt); // ensure that the units are correct!
                        AnalysisPlugIn.calculateThompsonFormula(rt);
                    }
                });
                popup.add(item);
                popup.add(new JSeparator());
            }
            //
            JRadioButtonMenuItem menuItem;
            for(Units unit : Units.getCompatibleUnits(selected)) {
                menuItem = new JRadioButtonMenuItem(unit.getLabel(), unit == selected);
                menuItem.addActionListener(this);
                popup.add(menuItem);
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Units target = Units.fromString(e.getActionCommand());
            if(rt.getColumnUnits(column) == target) return;    // nothing to do here
            rt.setColumnUnits(column, target);
        }
    }

    private class ResultsTableDropTarget extends DropTarget {

        @Override
        public synchronized void drop(DropTargetDropEvent dtde) {
            dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
            Transferable t = dtde.getTransferable();
            List fileList;
            try {
                fileList = (List)t.getTransferData(DataFlavor.javaFileListFlavor);
            } catch(UnsupportedFlavorException ex) {
                return;
            } catch(IOException ex) {
                return;
            }
            File f = (File)fileList.get(0);
            new ImportExportPlugIn(f.getAbsolutePath()).run("import");
        }
    }
}
