
package cz.cuni.lf1.lge.ThunderSTORM.results;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_FRAME;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_ID;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;

public class ResultsGroupingTest {

	@Test
	public void testGetMergedMolecules() {
		System.out.println("getMergedMolecules");
		//
		assertEquals(5, ResultsGrouping.getMergedMolecules(generateTable(), 10.0, 0, 3, 0).size()); // radius
																																																// =
																																																// 10nm,
																																																// off_frames
																																																// =
																																																// 0,
																																																// max_frames
																																																// =
																																																// 3
		assertEquals(3, ResultsGrouping.getMergedMolecules(generateTable(), 10.0, 2, 0, 0).size()); // radius
																																																// =
																																																// 10nm,
																																																// off_frames
																																																// =
																																																// 2,
																																																// max_frames
																																																// =
																																																// 0
		assertEquals(2, ResultsGrouping.getMergedMolecules(generateTable(), 50.0, 2, 5, 0).size()); // radius
																																																// =
																																																// 10nm,
																																																// off_frames
																																																// =
																																																// 2,
																																																// max_frames
																																																// =
																																																// 5
	}

	private GenericTableModel generateTable() {
		MoleculeDescriptor molDesc = new MoleculeDescriptor(new String[] { LABEL_ID, LABEL_FRAME,
			LABEL_X, LABEL_Y }, new Units[] { Units.UNITLESS, Units.UNITLESS, Units.NANOMETER,
				Units.NANOMETER });
		List<Molecule> molecules;
		molecules = new ArrayList<Molecule>();
		molecules.add(new Molecule(molDesc, new double[] { 1, 1, 5.0, 5.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 2, 2, 10.0, 5.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 3, 3, 8.0, 7.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 4, 4, 20.0, 20.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 5, 5, 30.0, 30.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 6, 6, 5.0, 5.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 7, 7, 5.0, 5.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 8, 8, 5.0, 5.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 9, 9, 5.0, 5.0 }));
		molecules.add(new Molecule(molDesc, new double[] { 10, 10, 5.0, 5.0 }));
		//
		GenericTableModel model = new GenericTableModel();
		for (Molecule mol : molecules) {
			model.addRow(mol);
		}
		return model;
	}
}
