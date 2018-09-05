
package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingWorker;

import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import ij.IJ;
import ij.ImagePlus;

final class TableRowsPopUpMenu implements ActionListener {

	private final IJResultsTable rt;
	private final ResultsTableWindow tableWindow;
	private final GenericTableModel tableModel;
	private final JTable jtable;
	private final JMenuItem highlightMoleculeMenuItem;
	private final JMenuItem deleteMoleculeMenuItem;
	private final JMenuItem mergedMoleculesMenuItem;

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
		if (rt.getAnalyzedImage() != null) {
			popup.add(highlightMoleculeMenuItem);
		}
		popup.add(deleteMoleculeMenuItem);
		if (jtable.getSelectedRowCount() == 1) {
			int rowIndex = jtable.convertRowIndexToModel(jtable.getSelectedRow());
			if (tableModel.findColumn(
				MoleculeDescriptor.LABEL_DETECTIONS) != GenericTableModel.COLUMN_NOT_FOUND)
			{
				if (tableModel.getValueAt(rowIndex, MoleculeDescriptor.LABEL_DETECTIONS) > 1) {
					popup.add(mergedMoleculesMenuItem);
				}
			}
		}
		popup.show(evt.getComponent(), evt.getX(), evt.getY());
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		if (evt.getSource() == highlightMoleculeMenuItem) {
			new SwingWorker() {

				@Override
				protected Object doInBackground() throws Exception {
					try {
						highlightMolecules();
					}
					catch (Exception e) {
						IJ.handleException(e);
					}
					return null;
				}
			}.execute();
		}
		else if (evt.getSource() == deleteMoleculeMenuItem) {
			filterMolecules();
		}
		else if (evt.getSource() == mergedMoleculesMenuItem) {
			showMergedMolecules();
		}
	}

	private void highlightMolecules() {
		ImagePlus imp = rt.getAnalyzedImage();
		if (imp != null) {
			IJ.showStatus("Building new overlay...");
			imp.setOverlay(null);
			//
			int[] rows = jtable.getSelectedRows();
			HashSet<Integer> rowIndices = new HashSet<Integer>();
			for (int r = 0; r < rows.length; r++) {
				rowIndices.add(jtable.convertRowIndexToModel(rows[r]));
			}
			List<Molecule> selectedMolecules = new ArrayList<Molecule>();
			List<Molecule> notSelectedMolecules = new ArrayList<Molecule>();

			for (int r = 0, rm = rt.getRowCount(); r < rm; r++) {
				Molecule mol = rt.getRow(r);
				if (rowIndices.contains(r)) {
					selectedMolecules.add(mol);
					if (!mol.isSingleMolecule()) {
						selectedMolecules.addAll(mol.getDetections());
					}
				}
				else {
					notSelectedMolecules.add(mol);
					if (!mol.isSingleMolecule()) {
						notSelectedMolecules.addAll(mol.getDetections());
					}
				}
			}

			RenderingOverlay.showPointsInImage(selectedMolecules.toArray(new Molecule[0]), imp, null,
				Color.GREEN, RenderingOverlay.MARKER_CIRCLE);
			RenderingOverlay.showPointsInImage(notSelectedMolecules.toArray(new Molecule[0]), imp, null,
				Color.RED, RenderingOverlay.MARKER_CROSS);
			//
			IJ.showProgress(1.0);
			IJ.showStatus("");
		}
	}

	private void filterMolecules() {
		int[] rows = jtable.getSelectedRows();
		Vector<Integer> rowIds = new Vector<Integer>();
		for (int r = 0; r < rows.length; r++) {
			int rowIndex = jtable.convertRowIndexToModel(rows[r]);
			rowIds.add(tableModel.getValueAt(rowIndex, MoleculeDescriptor.LABEL_ID).intValue());
		}
		Collections.sort(rowIds);
		int start, end;
		StringBuilder sb = new StringBuilder();
		for (int r = 0, rm = rowIds.size(); r < rm; r++) {
			if (r > 0) {
				sb.append("&");
			}
			start = rowIds.get(r);
			if ((r + 1) >= rm) {
				sb.append("(id!=").append(start).append(")");
			}
			else {
				end = rowIds.get(r + 1);
				if ((end - start) > 1) {
					sb.append("(id!=").append(start).append(")");
				}
				else {
					sb.append("(id<").append(start).append("|");
					while (((r + 1) < rm) && ((end - start) <= 1)) {
						start = rowIds.get(r);
						end = rowIds.get(r + 1);
						r++;
					}
					if ((end - start) > 1) {
						sb.append("id>").append(start).append(")");
					}
					else {
						sb.append("id>").append(end).append(")");
					}
				}
			}
		}
		//
		String formula = sb.toString();
		ResultsFilter filter = tableWindow.getFilter();
		filter.setFilterFormula(formula);
		filter.run();
	}

	private void showMergedMolecules() {
		int row = jtable.getSelectedRow();
		int rowIndex = jtable.convertRowIndexToModel(row);
		Molecule mol = tableModel.getRow(rowIndex);
		List<Molecule> detections = mol.getDetections();
		Collections.sort(detections);
		new MergedMoleculesPopUp(jtable, row, 0, detections);
	}
}
