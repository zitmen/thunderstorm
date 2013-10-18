package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import ij.IJ;
import ij.gui.Overlay;
import ij.gui.Roi;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.HashSet;
import java.util.Vector;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingWorker;

final class TableRowsPopUpMenu implements ActionListener {

    private IJResultsTable rt;
    private ResultsTableWindow tableWindow;
    private GenericTableModel tableModel;
    private JTable jtable;
    private JMenuItem highlightMoleculeMenuItem;
    private JMenuItem deleteMoleculeMenuItem;
    private JMenuItem mergedMoleculesMenuItem;

    public TableRowsPopUpMenu(MouseEvent evt, ResultsTableWindow table) {
        tableWindow = table;
        tableModel = table.getModel();
        jtable = table.getView();
        rt = IJResultsTable.getResultsTable();
        //
        highlightMoleculeMenuItem = new JMenuItem("highlight selected molecules in overlay");
        deleteMoleculeMenuItem = new JMenuItem("filter out selected molecules");
        mergedMoleculesMenuItem = new JMenuItem("show list of merged molecules");
        //
        highlightMoleculeMenuItem.addActionListener(this);
        deleteMoleculeMenuItem.addActionListener(this);
        mergedMoleculesMenuItem.addActionListener(this);
        //
        JPopupMenu popup = new JPopupMenu();
        if(rt.getAnalyzedImage() != null) {
            popup.add(highlightMoleculeMenuItem);
        }
        popup.add(deleteMoleculeMenuItem);
        if(jtable.getSelectedRowCount() == 1) {
            int rowIndex = jtable.convertRowIndexToModel(jtable.getSelectedRow());
            if(tableModel.findColumn(MoleculeDescriptor.LABEL_DETECTIONS) != GenericTableModel.COLUMN_NOT_FOUND) {
                if(tableModel.getValueAt(rowIndex, MoleculeDescriptor.LABEL_DETECTIONS) > 1) {
                    popup.add(mergedMoleculesMenuItem);
                }
            }
        }
        popup.show(evt.getComponent(), evt.getX(), evt.getY());
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        if(evt.getSource() == highlightMoleculeMenuItem) {
            new SwingWorker() {
                @Override
                protected Object doInBackground() throws Exception {
                    try {
                        highlightMolecules();
                    } catch(Exception e) {
                        IJ.handleException(e);
                    }
                    return null;
                }
            }.execute();
        } else if(evt.getSource() == deleteMoleculeMenuItem) {
            filterMolecules();
        } else if(evt.getSource() == mergedMoleculesMenuItem) {
            showMergedMolecules();
        }
    }

    private void highlightMolecules() {
        if(rt.getAnalyzedImage() != null) {
            rt.getAnalyzedImage().setOverlay(null);
            //
            int[] rows = jtable.getSelectedRows();
            HashSet<Integer> rowIndices = new HashSet<Integer>();
            for(int r = 0; r < rows.length; r++) {
                rowIndices.add(jtable.convertRowIndexToModel(rows[r]));
            }
            //
            Overlay overlay = new Overlay();
            Rectangle rect;
            Roi roi = rt.getAnalyzedImage().getRoi();
            if(roi != null) {
                rect = roi.getBounds();
            } else {
                rect = new Rectangle(0, 0, rt.getAnalyzedImage().getWidth(), rt.getAnalyzedImage().getHeight());
            }
            Units pixels = MoleculeDescriptor.Units.PIXEL;
            Units unitsX = rt.getColumnUnits(PSFModel.Params.LABEL_X);
            Units unitsY = rt.getColumnUnits(PSFModel.Params.LABEL_Y);
            IJ.showStatus("Building new overlay...");
            for(int r = 0, rm = rt.getRowCount(); r < rm; r++) {
                IJ.showProgress((double) r / (double) rm);
                int id = rt.getValue(r, MoleculeDescriptor.LABEL_ID).intValue();
                double xCoord = rect.x + unitsX.convertTo(pixels, rt.getValue(r, PSFModel.Params.LABEL_X).doubleValue());
                double yCoord = rect.y + unitsY.convertTo(pixels, rt.getValue(r, PSFModel.Params.LABEL_Y).doubleValue());
                int slice = rt.getValue(r, MoleculeDescriptor.LABEL_FRAME).intValue();
                if(rowIndices.contains(r)) {
                    for(int frame = slice, max = slice + rt.getRow(r).getDetectionsCount(); frame < max; frame++) {
                        RenderingOverlay.drawCircle(id, xCoord, yCoord, frame, overlay, Color.GREEN, 2.5);
                    }
                } else {
                    for(int frame = slice, max = slice + rt.getRow(r).getDetectionsCount(); frame < max; frame++) {
                        RenderingOverlay.drawCross(id, xCoord, yCoord, frame, overlay, Color.RED);
                    }
                }
            }
            IJ.showProgress(1.0);
            IJ.showStatus("");
            //
            rt.getAnalyzedImage().setOverlay(overlay);
        }
    }

    private void filterMolecules() {
        int[] rows = jtable.getSelectedRows();
        Vector<Integer> rowIds = new Vector<Integer>();
        for(int r = 0; r < rows.length; r++) {
            int rowIndex = jtable.convertRowIndexToModel(rows[r]);
            rowIds.add(tableModel.getValueAt(rowIndex, MoleculeDescriptor.LABEL_ID).intValue());
        }
        Collections.sort(rowIds);
        int start, end;
        StringBuilder sb = new StringBuilder();
        for(int r = 0, rm = rowIds.size(); r < rm; r++) {
            if(r > 0) {
                sb.append("&");
            }
            start = rowIds.get(r);
            if((r + 1) >= rm) {
                sb.append("(id!=").append(start).append(")");
            } else {
                end = rowIds.get(r + 1);
                if((end - start) > 1) {
                    sb.append("(id!=").append(start).append(")");
                } else {
                    sb.append("(id<").append(start).append("|");
                    while(((r + 1) < rm) && ((end - start) <= 1)) {
                        start = rowIds.get(r);
                        end = rowIds.get(r + 1);
                        r++;
                    }
                    if((end - start) > 1) {
                        sb.append("id>").append(start).append(")");
                    } else {
                        sb.append("id>").append(end).append(")");
                    }
                }
            }
        }
        //
        String formula = sb.toString();
        tableWindow.resultsFilter.setFilterFormula(formula);
        tableWindow.resultsFilter.runFilter(formula);
    }

    private void showMergedMolecules() {
        int row = jtable.getSelectedRow();
        int rowIndex = jtable.convertRowIndexToModel(row);
        Molecule mol = tableModel.getRow(rowIndex);
        Vector<Molecule> detections = mol.getDetections();
        Collections.sort(detections);
        new MergedMoleculesPopUp(jtable, row, 0, detections);
    }
}
