package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_INTENSITY;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_OFFSET;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.MoleculeXYZComparator;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Note: some of the estimators have too many free parameters to converge to these test cases.
 * This in particular applies to the elliptic Gaussians, especially the on with free angle.
 * 
 * Maximum-likelihood estimators perform better on a single-molecule data, but for the purpose of
 * crowded-field estimation they are completely useless. This is not just a matter of setting
 * a propriate p-value, because when you check the estimates for 2 molecules, the guesses are completely
 * wrong. This might be caused by the surface of likelihood function being too flat, because values of
 * the likelihood function are almost static. Especially for x,y coordinates, the likelihood function
 * is very insensitive.
 * 
 * On the other hand, Least-Squares perform much better at this task and work almost flawlessly, however,
 * for more than 2 molecules it is very important to fix the intensity of molecules to a specific value,
 * otherwise it will find only 2 molecules, which is why the test for 3 molecules does not pass.
 * I have verified that it will pass if the intensity is fixed to 1.0 in the `fixParams` method of `MultiPSF` class.
 */
public class CrowdedFieldEstimatorsTest {
    
    public static final int FITRADIUS = 6;
    public static final int MAX_N = 5;
    public static final double ANGLE = 0.0;
    public static final double P_VAL = 1e-6;
    public static final double SIGMA = 1.6;
    
    @Ignore
    @Test
    public void testLSQSym() {
        testEstimator(new MFA_LSQFitter(new SymmetricGaussianPSF(SIGMA), SIGMA, MAX_N, P_VAL, true, null));
    }

    @Ignore
    @Test
    public void testLSQIntSym() {
        testEstimator(new MFA_LSQFitter(new IntegratedSymmetricGaussianPSF(SIGMA), SIGMA, MAX_N, P_VAL, true, null));
    }
    
    @Ignore
    @Test
    public void testMLEIntSym() {
        testEstimator(new MFA_MLEFitter(new IntegratedSymmetricGaussianPSF(SIGMA), SIGMA, MAX_N, P_VAL, true, null));
    }
    
    @Ignore
    @Test
    public void testMLESym() {
        testEstimator(new MFA_MLEFitter(new SymmetricGaussianPSF(SIGMA), SIGMA, MAX_N, P_VAL, true, null));
    }

    @Ignore
    @Test
    public void testLSQEllipticAngle() {
        testEstimator(new MFA_LSQFitter(new EllipticGaussianWAnglePSF(SIGMA, ANGLE), SIGMA, MAX_N, P_VAL, true, null));
    }

    @Ignore
    @Test
    public void testMLEEllipticAngle() {
        testEstimator(new MFA_MLEFitter(new EllipticGaussianWAnglePSF(SIGMA, ANGLE), SIGMA, MAX_N, P_VAL, true, null));
    }

    @Ignore
    @Test
    public void testLSQElliptic() {
        testEstimator(new MFA_LSQFitter(new EllipticGaussianPSF(SIGMA, ANGLE), SIGMA, MAX_N, P_VAL, true, null));
    }

    @Ignore
    @Test
    public void testMLEElliptic() {
        testEstimator(new MFA_MLEFitter(new EllipticGaussianPSF(SIGMA, ANGLE), SIGMA, MAX_N, P_VAL, true, null));
    }
    
    public void testEstimator(IOneLocationFitter fitter) {
        Molecule fit;
        double [][] tol = new double[][] {
            { 0.001, 0.1, 0.1 },
            { 0.025, 0.100, 0.1 },
            { 0.050, 0.100, 0.1 }
        };
        double [][] groundTruth = new double[][] {
            { 0.0, 0.0, 1.0, 0.0, 1.6 },
            { 0.4,-0.6, 1.0, 0.0, 1.6 },
            { 0.5, 0.5, 1.0, 0.0, 1.6 },
            { 0.5, 0.5, 1.0, 0.0, 1.6 }
        };
        for(int mol = 1; mol <= 3; mol++) {
            fit = fitTestData(fitter, mol);
            System.out.println(fit.toString());
            assertEquals(mol, fit.getDetections().size());
            List<Molecule> detections = fit.getDetections();
            Collections.sort(detections, new MoleculeXYZComparator());
            for(int i = 0; i < mol; i++) {
                assertEquals(groundTruth[i][0], detections.get(i).getX(), tol[mol-1][0]);
                assertEquals(groundTruth[i][1], detections.get(i).getY(), tol[mol-1][0]);
                assertEquals(groundTruth[i][2], detections.get(i).getParam(LABEL_INTENSITY), tol[mol-1][1]);
                assertEquals(groundTruth[i][3], detections.get(i).getParam(LABEL_OFFSET), tol[mol-1][1]);
                assertEquals(groundTruth[i][4], detections.get(i).getParam(fit.hasParam(LABEL_SIGMA) ? LABEL_SIGMA : LABEL_SIGMA1), tol[mol-1][2]);
            }
        }
    }

    private Molecule fitTestData(IOneLocationFitter fitter, int dataset) throws FormulaParserException {
        double [][] values = new double[][] { {
            0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0001, 0.0001, 0.0001, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0000, 0.0001, 0.0002, 0.0004, 0.0005, 0.0004, 0.0002, 0.0001, 0.0000, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0001, 0.0005, 0.0014, 0.0024, 0.0029, 0.0024, 0.0014, 0.0005, 0.0001, 0.0000, 0.0000,
            0.0000, 0.0001, 0.0005, 0.0020, 0.0052, 0.0091, 0.0110, 0.0091, 0.0052, 0.0020, 0.0005, 0.0001, 0.0000,
            0.0000, 0.0002, 0.0014, 0.0052, 0.0133, 0.0234, 0.0283, 0.0234, 0.0133, 0.0052, 0.0014, 0.0002, 0.0000,
            0.0001, 0.0004, 0.0024, 0.0091, 0.0234, 0.0412, 0.0498, 0.0412, 0.0234, 0.0091, 0.0024, 0.0004, 0.0001,
            0.0001, 0.0005, 0.0029, 0.0110, 0.0283, 0.0498, 0.0602, 0.0498, 0.0283, 0.0110, 0.0029, 0.0005, 0.0001,
            0.0001, 0.0004, 0.0024, 0.0091, 0.0234, 0.0412, 0.0498, 0.0412, 0.0234, 0.0091, 0.0024, 0.0004, 0.0001,
            0.0000, 0.0002, 0.0014, 0.0052, 0.0133, 0.0234, 0.0283, 0.0234, 0.0133, 0.0052, 0.0014, 0.0002, 0.0000,
            0.0000, 0.0001, 0.0005, 0.0020, 0.0052, 0.0091, 0.0110, 0.0091, 0.0052, 0.0020, 0.0005, 0.0001, 0.0000,
            0.0000, 0.0000, 0.0001, 0.0005, 0.0014, 0.0024, 0.0029, 0.0024, 0.0014, 0.0005, 0.0001, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0000, 0.0001, 0.0002, 0.0004, 0.0005, 0.0004, 0.0002, 0.0001, 0.0000, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0001, 0.0001, 0.0001, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000
        }, {
            0.0000, 0.0000, 0.0000, 0.0000, 0.0001, 0.0002, 0.0003, 0.0003, 0.0002, 0.0001, 0.0000, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0001, 0.0003, 0.0008, 0.0015, 0.0020, 0.0019, 0.0012, 0.0005, 0.0002, 0.0000, 0.0000,
            0.0000, 0.0001, 0.0003, 0.0013, 0.0036, 0.0071, 0.0095, 0.0087, 0.0055, 0.0024, 0.0007, 0.0001, 0.0000,
            0.0000, 0.0002, 0.0011, 0.0043, 0.0120, 0.0231, 0.0306, 0.0280, 0.0176, 0.0076, 0.0023, 0.0005, 0.0001,
            0.0000, 0.0004, 0.0024, 0.0098, 0.0272, 0.0521, 0.0686, 0.0622, 0.0389, 0.0167, 0.0050, 0.0010, 0.0001,
            0.0001, 0.0007, 0.0039, 0.0156, 0.0430, 0.0816, 0.1065, 0.0958, 0.0594, 0.0253, 0.0074, 0.0015, 0.0002,
            0.0001, 0.0008, 0.0044, 0.0173, 0.0472, 0.0886, 0.1147, 0.1024, 0.0629, 0.0266, 0.0078, 0.0016, 0.0002,
            0.0001, 0.0006, 0.0034, 0.0133, 0.0359, 0.0669, 0.0858, 0.0759, 0.0463, 0.0194, 0.0056, 0.0011, 0.0002,
            0.0000, 0.0003, 0.0018, 0.0070, 0.0189, 0.0350, 0.0445, 0.0390, 0.0236, 0.0098, 0.0028, 0.0006, 0.0001,
            0.0000, 0.0001, 0.0007, 0.0026, 0.0069, 0.0127, 0.0160, 0.0139, 0.0083, 0.0034, 0.0010, 0.0002, 0.0000,
            0.0000, 0.0000, 0.0002, 0.0007, 0.0017, 0.0032, 0.0040, 0.0034, 0.0020, 0.0008, 0.0002, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0000, 0.0001, 0.0003, 0.0005, 0.0007, 0.0006, 0.0003, 0.0001, 0.0000, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0000, 0.0000, 0.0000, 0.0001, 0.0001, 0.0001, 0.0000, 0.0000, 0.0000, 0.0000, 0.0000
        }, {
            0.0000, 0.0000, 0.0000, 0.0000, 0.0001, 0.0002, 0.0003, 0.0003, 0.0002, 0.0001, 0.0000, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0001, 0.0003, 0.0008, 0.0016, 0.0022, 0.0021, 0.0013, 0.0006, 0.0002, 0.0000, 0.0000,
            0.0000, 0.0001, 0.0003, 0.0014, 0.0040, 0.0079, 0.0107, 0.0100, 0.0064, 0.0028, 0.0009, 0.0002, 0.0000,
            0.0000, 0.0002, 0.0012, 0.0049, 0.0138, 0.0269, 0.0363, 0.0337, 0.0215, 0.0095, 0.0029, 0.0006, 0.0001,
            0.0001, 0.0005, 0.0028, 0.0116, 0.0329, 0.0641, 0.0862, 0.0798, 0.0509, 0.0224, 0.0068, 0.0014, 0.0002,
            0.0001, 0.0008, 0.0048, 0.0195, 0.0551, 0.1073, 0.1440, 0.1333, 0.0851, 0.0374, 0.0113, 0.0024, 0.0003,
            0.0001, 0.0009, 0.0056, 0.0230, 0.0648, 0.1262, 0.1695, 0.1571, 0.1004, 0.0442, 0.0134, 0.0028, 0.0004,
            0.0001, 0.0008, 0.0046, 0.0189, 0.0535, 0.1044, 0.1406, 0.1307, 0.0838, 0.0370, 0.0113, 0.0024, 0.0003,
            0.0001, 0.0004, 0.0027, 0.0109, 0.0310, 0.0607, 0.0820, 0.0766, 0.0493, 0.0219, 0.0067, 0.0014, 0.0002,
            0.0000, 0.0002, 0.0011, 0.0044, 0.0126, 0.0247, 0.0336, 0.0315, 0.0204, 0.0091, 0.0028, 0.0006, 0.0001,
            0.0000, 0.0000, 0.0003, 0.0012, 0.0036, 0.0071, 0.0096, 0.0091, 0.0059, 0.0027, 0.0008, 0.0002, 0.0000,
            0.0000, 0.0000, 0.0001, 0.0002, 0.0007, 0.0014, 0.0019, 0.0018, 0.0012, 0.0005, 0.0002, 0.0000, 0.0000,
            0.0000, 0.0000, 0.0000, 0.0000, 0.0001, 0.0002, 0.0003, 0.0003, 0.0002, 0.0001, 0.0000, 0.0000, 0.0000
        } };

        double[] xgrid = new double[(int)sqr(2*FITRADIUS+1)];
        double[] ygrid = new double[(int)sqr(2*FITRADIUS+1)];
        int idx = 0;
        for (int i = -FITRADIUS; i <= FITRADIUS; i++) {
            for (int j = -FITRADIUS; j <= FITRADIUS; j++) {
                xgrid[idx] = j;
                ygrid[idx] = i;
                idx++;
            }
        }

        return ((MFA_AbstractFitter) fitter).eliminateBadFits(
            fitter.fit(new SubImage(2 * FITRADIUS + 1, 2 * FITRADIUS + 1,
                xgrid, ygrid, values[dataset - 1], 0.0, 0.0, MoleculeDescriptor.Units.DIGITAL)),
                FITRADIUS, FITRADIUS);
    }

}
