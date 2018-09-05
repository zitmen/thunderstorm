
package cz.cuni.lf1.lge.ThunderSTORM.drift;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;

public class FiducialDriftEstimatorTest {

	@Test
	public void testArtificialData() throws InterruptedException {
		double[] x = { 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30 };
		double[] y = { 20, 21, 22, 23, 24, 25, 24, 23, 22, 21, 20 };
		double[] frame = { 11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111 };

		List<Molecule> mols = new ArrayList<Molecule>();
		for (int i = 0; i < x.length; i++) {
			mols.add(new Molecule(new MoleculeDescriptor(new String[] { PSFModel.Params.LABEL_X,
				PSFModel.Params.LABEL_Y, MoleculeDescriptor.LABEL_FRAME }), new double[] { x[i], y[i],
					frame[i] }));
		}

		DriftResults driftCorrection = new FiducialDriftEstimator().estimateDrift(mols, 50, 0.1, 0.5);

//        ResultsDriftCorrection.showDriftPlot(driftCorrection);
//        Thread.sleep(200000);
		assertEquals(driftCorrection.getMinFrame(), VectorMath.min(frame), 0.00001);
		assertEquals(driftCorrection.getMaxFrame(), VectorMath.max(frame), 0.00001);

		assertArrayEquals(new double[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 }, driftCorrection
			.getDriftDataX(), 0.001);
		assertArrayEquals(new double[] { 0, 1, 2, 3, 4, 5, 4, 3, 2, 1, 0 }, driftCorrection
			.getDriftDataY(), 0.001);

		assertEquals("slope", 0.1, (driftCorrection.getInterpolatedDrift(90).x - driftCorrection
			.getInterpolatedDrift(20).x) / (90 - 20), 0.01);

		assertEquals(0.5, driftCorrection.getInterpolatedDrift(16).x, 0.01);
		assertEquals(0.5, driftCorrection.getInterpolatedDrift(16).y, 0.01);
		assertEquals(8.5, driftCorrection.getInterpolatedDrift(96).x, 0.01);
		assertEquals(1.5, driftCorrection.getInterpolatedDrift(96).y, 0.01);
	}

}
