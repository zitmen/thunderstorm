package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import ij.IJ;
import ij.WindowManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class GenericTableWindow {

    protected JFrame frame;
    protected ColoredTable table;
    protected final TripleStateTableModel model;
    protected JScrollPane tableScrollPane;

    public GenericTableWindow(String frameTitle) {
        frame = new JFrame(frameTitle);
        frame.setIconImage(IJ.getInstance().getIconImage());
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //
        model = new TripleStateTableModel();
        table = new ColoredTable(model);
        TableRowSorter<TripleStateTableModel> sorter = new TableRowSorter<TripleStateTableModel>(model);
        table.setRowSorter(sorter);
        //
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableHeaderMouseClicked(e);
            }
        });
        table.setDropTarget(new TableDropTarget());
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setDropTarget(new TableDropTarget());
        table.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                tableMouseDragged(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                tableMouseMoved(e);
            }
        });
        table.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                tableMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                tableMouseReleased(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                tableMouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                tableMouseExited(e);
            }
        });
    }

    protected void packFrame() {
        Container contentPane = frame.getContentPane();
        contentPane.add(tableScrollPane);
        frame.setContentPane(contentPane);
        frame.pack();
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
        GUI.runOnUIThreadAndWait(new Runnable() {
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

    private class UnitsContextMenu implements ActionListener {

        private int column;

        public UnitsContextMenu(MouseEvent e, int column) {
            this.column = column;
            Units selected = model.getColumnUnits(column);
            JPopupMenu popup = new JPopupMenu();
            //
            if(MoleculeDescriptor.LABEL_ID.equals(model.getColumnRealName(column))) {
                JMenuItem item;
                item = new JMenuItem("convert all to digital units");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        model.convertAllColumnsToDigitalUnits();
                        if(GenericTableWindow.this instanceof ResultsTableWindow) {
                            TableHandlerPlugin.recordChangeAllUnits(false);
                        }
                    }
                });
                popup.add(item);
                item = new JMenuItem("convert all to analog units");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        model.convertAllColumnsToAnalogUnits();
                        if(GenericTableWindow.this instanceof ResultsTableWindow) {
                            TableHandlerPlugin.recordChangeAllUnits(true);
                        }
                    }
                });
                popup.add(item);
            } else if(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY.equals(model.getColumnRealName(column))) {
                JMenuItem item = new JMenuItem("recalculate");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            model.calculateUncertaintyXY();
                        } catch (MoleculeDescriptor.Fitting.UncertaintyNotApplicableException ex) {
                            IJ.log("Cannot calculate lateral uncertainty: " + ex.getMessage());
                        } catch (NullPointerException ex) {
                            IJ.log("Measurement protocol wasn't set properly to calculate uncertainty!");
                        }
                    }
                });
                popup.add(item);
                popup.add(new JSeparator());
            } else if(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_Z.equals(model.getColumnRealName(column))) {
                JMenuItem item = new JMenuItem("recalculate");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
                            model.calculateUncertaintyZ();
                        } catch (MoleculeDescriptor.Fitting.UncertaintyNotApplicableException ex) {
                            IJ.log("Cannot calculate axial uncertainty: " + ex.getMessage());
                        } catch (NullPointerException ex) {
                            IJ.log("Measurement protocol wasn't set properly to calculate uncertainty!");
                        }
                    }
                });
                popup.add(item);
                popup.add(new JSeparator());
            }
            //
            JRadioButtonMenuItem menuItem;
            for(Units unit : Units.getCompatibleUnits(selected)) {
                if(unit == Units.UNITLESS) {
                    continue;
                }
                if((PSFModel.Params.LABEL_Z.equals(model.getColumnRealName(column)))
                        || (PSFModel.Params.LABEL_Z_REL.equals(model.getColumnRealName(column)))) {
                    if(unit == Units.PIXEL) {
                        continue;   // z-position can't be converted to pixels
                    }
                }
                menuItem = new JRadioButtonMenuItem(unit.getLabel(), unit == selected);
                menuItem.addActionListener(this);
                popup.add(menuItem);
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Units target = Units.fromString(e.getActionCommand());
            if(model.getColumnUnits(column) == target) {
                return;    // nothing to do here
            }
            String colName = model.getColumnRealName(column);
            if(PSFModel.Params.LABEL_X.equals(colName) || PSFModel.Params.LABEL_Y.equals(colName)) {
                // ensure that X and Y are always in same units!
                model.setColumnUnits(PSFModel.Params.LABEL_X, target);
                model.setColumnUnits(PSFModel.Params.LABEL_Y, target);
            } else {
                model.setColumnUnits(column, target);
            }
            if(GenericTableWindow.this instanceof ResultsTableWindow) {
                TableHandlerPlugin.recordChangeColumnUnits(colName, target);
            }
        }
    }

    private class TableDropTarget extends DropTarget {

        @Override
        public synchronized void drop(DropTargetDropEvent dtde) {
            dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
            Transferable t = dtde.getTransferable();
            List fileList;
            try {
                fileList = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
            } catch(UnsupportedFlavorException ex) {
                return;
            } catch(IOException ex) {
                return;
            }
            File f = (File) fileList.get(0);
            dropFile(f);
        }
    }

    protected void dropFile(File f) {
        new ImportExportPlugIn(f.getAbsolutePath()).run(ImportExportPlugIn.IMPORT + IJResultsTable.IDENTIFIER);
    }

    protected void tableHeaderMouseClicked(MouseEvent e) {
        if(SwingUtilities.isRightMouseButton(e)) {
            new UnitsContextMenu(e, table.convertColumnIndexToModel(table.columnAtPoint(e.getPoint())));
        }
    }

    protected void tableMouseDragged(MouseEvent e) {
        //
    }

    protected void tableMouseMoved(MouseEvent e) {
        //
    }

    protected void tableMouseClicked(MouseEvent e) {
        //
    }

    protected void tableMousePressed(MouseEvent e) {
        //
    }

    protected void tableMouseReleased(MouseEvent e) {
        //
    }

    protected void tableMouseEntered(MouseEvent e) {
        //
    }

    protected void tableMouseExited(MouseEvent e) {
        //
    }

    // =============================================================
    public class ColoredTable extends JTable {

        public final Color LIGHT_ORANGE = new Color(255, 222, 200);
        public final Color LIGHT_RED = new Color(255, 222, 222);
        public final Color LIGHT_GREEN = new Color(222, 255, 222);

        public ColoredTable(TableModel dm) {
            super(dm);
        }

        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            final Component c = super.prepareRenderer(renderer, row, column);
            if (column < 0 || column >= getColumnCount()) return c;
            if (row < 0 || row >= getRowCount()) return c;

            NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);
            ((DecimalFormat) formatter).setGroupingUsed(false);
            ((JLabel) c).setText(formatter.format(getValueAt(row, column)));
            if(!isCellSelected(row, column)) {
                c.setBackground(null);
                Molecule mol = ((GenericTableModel) super.getModel()).getRow(convertRowIndexToModel(row));
                switch(mol.getStatus()) {
                    case FALSE_POSITIVE:
                        c.setBackground(LIGHT_RED);
                        break;

                    case FALSE_NEGATIVE:
                        c.setBackground(LIGHT_ORANGE);
                        break;

                    case TRUE_POSITIVE:
                        c.setBackground(LIGHT_GREEN);
                        break;

                    default:
                        c.setBackground(Color.WHITE);
                }
            }
            return c;
        }
    }
}
