
package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.awt.Color;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import net.java.balloontip.BalloonTip;
import net.java.balloontip.TableCellBalloonTip;
import net.java.balloontip.positioners.RightBelowPositioner;
import net.java.balloontip.styles.RoundedBalloonStyle;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;

class MergedMoleculesPopUp {

	// Bug: if row == 0, then the balloon does not show up! This is probably a bug
	// in the BalloonTip library.
	public MergedMoleculesPopUp(JTable parent, int row, int col, List<Molecule> molecules) {
		GenericTableModel model = new GenericTableModel();
		for (Molecule mol : molecules) {
			model.addRow(mol);
		}
		MoleculeDescriptor header = model.cloneDescriptor();
		header.removeParam(MoleculeDescriptor.LABEL_DETECTIONS);
		model.setDescriptor(header);
		//
		JComponent mergedMoleculesTable = new JScrollPane(new JTable(model));
		mergedMoleculesTable.setPreferredSize(new Dimension(450, 250));
		//
		new TableCellBalloonTip(parent, mergedMoleculesTable, row, col, new RoundedBalloonStyle(5, 10,
			Color.LIGHT_GRAY, Color.BLUE), new RightBelowPositioner(10, 10), BalloonTip
				.getDefaultCloseButton());
	}
}
