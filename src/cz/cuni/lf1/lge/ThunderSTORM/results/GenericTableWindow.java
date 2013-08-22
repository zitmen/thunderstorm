package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import ij.IJ;
import ij.WindowManager;
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
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

class GenericTableWindow {

    protected JFrame frame;
    protected JTable table;
    protected final TripleStateTableModel model;
    protected JScrollPane tableScrollPane;

    public GenericTableWindow(String frameTitle) {
        frame = new JFrame(frameTitle);
        frame.setIconImage(IJ.getInstance().getIconImage());
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        //
        model = new TripleStateTableModel();
        table = new JTable(model);
        TableRowSorter<TripleStateTableModel> sorter = new TableRowSorter<TripleStateTableModel>(model);
        table.setRowSorter(sorter);
        //
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                tableHeaderMouseClicked(e);
            }
        });
        table.setDropTarget(new ResultsTableDropTarget());
        tableScrollPane = new JScrollPane(table);
        tableScrollPane.setDropTarget(new ResultsTableDropTarget());
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

    private class UnitsContextMenu implements ActionListener {
        
        private int column;

        public UnitsContextMenu(MouseEvent e, int column) {
            this.column = column;
            Units selected = model.getColumnUnits(column);
            JPopupMenu popup = new JPopupMenu();
            //
            if(MoleculeDescriptor.LABEL_ID.equals(model.getColumnName(column))) {
                JMenuItem item;
                item = new JMenuItem("convert all to digital units");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        model.convertAllColumnsToDigitalUnits();
                    }
                });
                popup.add(item);
                item = new JMenuItem("convert all to analog units");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        model.convertAllColumnsToAnalogUnits();
                    }
                });
                popup.add(item);
            } else if(MoleculeDescriptor.Fitting.LABEL_CCD_THOMPSON.equals(model.getColumnName(column))) {
                JMenuItem item = new JMenuItem("recalculate");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        model.convertAllColumnsToAnalogUnits(); // ensure that the units are correct!
                        model.calculateThompsonFormula();
                    }
                });
                popup.add(item);
                popup.add(new JSeparator());
            } else if(MoleculeDescriptor.Fitting.LABEL_EMCCD_THOMPSON.equals(model.getColumnName(column))) {
                JMenuItem item = new JMenuItem("recalculate");
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        model.convertAllColumnsToAnalogUnits(); // ensure that the units are correct!
                        model.calculateThompsonFormula();
                    }
                });
                popup.add(item);
                popup.add(new JSeparator());
            }
            //
            JRadioButtonMenuItem menuItem;
            for(Units unit : Units.getCompatibleUnits(selected)) {
                if(unit == Units.UNITLESS) continue;
                menuItem = new JRadioButtonMenuItem(unit.getLabel(), unit == selected);
                menuItem.addActionListener(this);
                popup.add(menuItem);
            }
            popup.show(e.getComponent(), e.getX(), e.getY());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Units target = Units.fromString(e.getActionCommand());
            if(model.getColumnUnits(column) == target) return;    // nothing to do here
            String colName = model.getColumnName(column);
            if(PSFModel.Params.LABEL_X.equals(colName) || PSFModel.Params.LABEL_Y.equals(colName)) {
                // ensure that X and Y are always in same units!
                model.setColumnUnits(PSFModel.Params.LABEL_X, target);
                model.setColumnUnits(PSFModel.Params.LABEL_Y, target);
            } else {
                model.setColumnUnits(column, target);
            }
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
}
