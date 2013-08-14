package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_INTENSITY;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_OFFSET;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import org.junit.Test;
import static org.junit.Assert.*;

public class OneLocationFittersTest {

    @Test
    public void testFitters() {
        testFitter(new LSQFitter(new SymmetricGaussianPSF(1.2)));
        testFitter(new LSQFitter(new EllipticGaussianWAnglePSF(1.2, 0)));
        testFitter(new LSQFitter(new EllipticGaussianPSF(1.2, 0)));
        testFitter(new MLEFitter(new SymmetricGaussianPSF(1.2)));
        testFitter(new MLEFitter(new EllipticGaussianPSF(1.2, 0)));
        testFitter(new MLEFitter(new EllipticGaussianWAnglePSF(1.2, 0)));
    }

    @Test
    public void testRadialSymmetry() {
        Molecule psf = fitTestData(new RadialSymmetryFitter());
        System.out.println(psf.toString());

        assertEquals(1, psf.getX(), 1e-3);
        assertEquals(0, psf.getY(), 1e-3);
    }

    public void testFitter(OneLocationFitter fitter) {

        Molecule fit = fitTestData(fitter);
        System.out.println(fit.toString());

        double[] groundTruth = {1, 0, 1, 1.5, 0};
        assertEquals(groundTruth[0], fit.getX(), 10e-3);
        assertEquals(groundTruth[1], fit.getY(), 10e-3);
        assertEquals(groundTruth[2], fit.getParam(LABEL_INTENSITY), 10e-3);
        assertEquals(groundTruth[4], fit.getParam(LABEL_OFFSET), 10e-3);
        if (fit.hasParam(LABEL_SIGMA)) {
            assertEquals(groundTruth[3], fit.getParam(LABEL_SIGMA), 10e-3);   // symmetric PSF
        } else {
            assertEquals(groundTruth[3], fit.getParam(LABEL_SIGMA1), 10e-3);  // eliptic PSF
        }
    }

    private Molecule fitTestData(OneLocationFitter fitter) {
        double[] values = {0.0000, 0.0000, 0.0000, 0.0000, 0.0001, 0.0002, 0.0003, 0.0002, 0.0001, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0001, 0.0003, 0.0008, 0.0016, 0.0020, 0.0016, 0.0008, 0.0003, 0.0001,
            0.0000, 0.0000, 0.0003, 0.0013, 0.0039, 0.0077, 0.0096, 0.0077, 0.0039, 0.0013, 0.0003,
            0.0000, 0.0001, 0.0008, 0.0039, 0.0120, 0.0233, 0.0291, 0.0233, 0.0120, 0.0039, 0.0008,
            0.0000, 0.0002, 0.0016, 0.0077, 0.0233, 0.0454, 0.0566, 0.0454, 0.0233, 0.0077, 0.0016,
            0.0000, 0.0003, 0.0020, 0.0096, 0.0291, 0.0566, 0.0707, 0.0566, 0.0291, 0.0096, 0.0020,
            0.0000, 0.0002, 0.0016, 0.0077, 0.0233, 0.0454, 0.0566, 0.0454, 0.0233, 0.0077, 0.0016,
            0.0000, 0.0001, 0.0008, 0.0039, 0.0120, 0.0233, 0.0291, 0.0233, 0.0120, 0.0039, 0.0008,
            0.0000, 0.0000, 0.0003, 0.0013, 0.0039, 0.0077, 0.0096, 0.0077, 0.0039, 0.0013, 0.0003,
            0.0000, 0.0000, 0.0001, 0.0003, 0.0008, 0.0016, 0.0020, 0.0016, 0.0008, 0.0003, 0.0001,
            0.0000, 0.0000, 0.0000, 0.0000, 0.0001, 0.0002, 0.0003, 0.0002, 0.0001, 0.0000, 0.0000};
        int[] xgrid = new int[values.length];
        int[] ygrid = new int[values.length];
        int idx = 0;
        for (int i = -5; i <= 5; i++) {
            for (int j = -5; j <= 5; j++) {
                xgrid[idx] = j;
                ygrid[idx] = i;
                idx++;
            }
        }
        Molecule fit = fitter.fit(new OneLocationFitter.SubImage(11, xgrid, ygrid, values, 0.5, 0.5));
        return fit;
    }
}
