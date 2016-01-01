package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_INTENSITY;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_OFFSET;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import org.junit.Test;
import static org.junit.Assert.*;

public class OneLocationFittersTest {

    @Test
    public void testLSQSymmetric() {
        System.out.println("testLSQSymmetric");
        testFitter(new LSQFitter(new SymmetricGaussianPSF(1.5), false, Params.BACKGROUND));
        testFitter(new LSQFitter(new SymmetricGaussianPSF(1.5), true, Params.BACKGROUND));
    }

    @Test
    public void testLSQIntSymmetric() {
        System.out.println("testLSQIntSymmetric");
        testFitter(new LSQFitter(new IntegratedSymmetricGaussianPSF(1.2), false, Params.BACKGROUND));
        testFitter(new LSQFitter(new IntegratedSymmetricGaussianPSF(1.2), true, Params.BACKGROUND));
    }

    @Test
    public void testLSQEllipticWAngle() {
        System.out.println("testLSQEllipticWAngle");
        testFitter(new LSQFitter(new EllipticGaussianWAnglePSF(1.2, 0), false, Params.BACKGROUND));
        testFitter(new LSQFitter(new IntegratedSymmetricGaussianPSF(1.2), true, Params.BACKGROUND));
    }

    @Test
    public void testLSQElliptic() {
        System.out.println("testLSQElliptic");
        testFitter(new LSQFitter(new EllipticGaussianPSF(1.2, 0), false, Params.BACKGROUND));
        testFitter(new LSQFitter(new EllipticGaussianPSF(1.2, 0), true, Params.BACKGROUND));
    }

    @Test
    public void testMLESymmetric() {
        System.out.println("testMLESymmetric");
        testFitter(new MLEFitter(new SymmetricGaussianPSF(1.2), Params.BACKGROUND));
    }

    public void testMLEIntSymmetric() {
        System.out.println("testMLEIntSymmetric");
        testFitter(new MLEFitter(new IntegratedSymmetricGaussianPSF(1.2), Params.BACKGROUND));
    }

    @Test
    public void testMLEElliptic() {
        System.out.println("testMLEElliptic");
        testFitter(new MLEFitter(new EllipticGaussianPSF(1.2, 0), Params.BACKGROUND));
    }

    @Test
    public void testMLEEllipticWAngle() {
        System.out.println("testMLEEllipticWAngle");
        testFitter(new MLEFitter(new EllipticGaussianWAnglePSF(1.2, 0), Params.BACKGROUND));
    }

    @Test
    public void testRadialSymmetry() {
        System.out.println("testRadialSymmetry");
        Molecule psf = fitTestData(new RadialSymmetryFitter());
        System.out.println(psf.toString());

        assertEquals(1, psf.getX(), 1e-3);
        assertEquals(0, psf.getY(), 1e-3);
    }

    public void testFitter(IOneLocationFitter fitter) {

        Molecule fit = fitTestData(fitter);
        System.out.println(fit.toString());

        double[] groundTruth = {1, 0, 1, 1.5, 0};
        assertEquals(groundTruth[0], fit.getX(), 10e-3);
        assertEquals(groundTruth[1], fit.getY(), 10e-3);
        assertEquals(groundTruth[2], fit.getParam(LABEL_INTENSITY), 10e-3);
        assertEquals(groundTruth[4], fit.getParam(LABEL_OFFSET), 10e-3);
        if(fit.hasParam(LABEL_SIGMA)) {
            assertEquals(groundTruth[3], fit.getParam(LABEL_SIGMA), 0.1);   // symmetric PSF
        } else {
            assertEquals(groundTruth[3], fit.getParam(LABEL_SIGMA1), 0.1);  // eliptic PSF
        }
    }

    private Molecule fitTestData(IOneLocationFitter fitter) {
        double[] values = {
            1.4175035112951352E-7, 1.5056067251874795E-6, 1.0387350450247402E-5, 4.6596555689039867E-5, 1.3603475690306496E-4, 2.5864120226361595E-4, 3.203992064538993E-4, 2.5864120226361595E-4, 1.3603475690306496E-4, 4.6596555689039867E-5, 1.0387350450247402E-5,
            9.779516450051282E-7, 1.0387350450247402E-5, 7.166350121265529E-5, 3.2147488824231735E-4, 9.385191163983373E-4, 0.0017843947983501222, 0.002210470228208766, 0.0017843947983501222, 9.385191163983373E-4, 3.2147488824231735E-4, 7.166350121265529E-5,
            4.3869876640759965E-6, 4.6596555689039867E-5, 3.2147488824231735E-4, 0.00144210235366173, 0.004210097510616101, 0.008004606371066857, 0.00991593569322976, 0.008004606371066857, 0.004210097510616101, 0.00144210235366173, 3.2147488824231735E-4,
            1.2807444490144886E-5, 1.3603475690306496E-4, 9.385191163983373E-4, 0.004210097510616101, 0.01229102844461037, 0.02336878049655781, 0.02894874699531102, 0.02336878049655781, 0.01229102844461037, 0.004210097510616101, 9.385191163983373E-4,
            2.4350635942371895E-5, 2.5864120226361595E-4, 0.0017843947983501222, 0.008004606371066857, 0.02336878049655781, 0.04443077358068976, 0.05503989493087988, 0.04443077358068976, 0.02336878049655781, 0.008004606371066857, 0.0017843947983501222,
            3.0165048585846606E-5, 3.203992064538993E-4, 0.002210470228208766, 0.00991593569322976, 0.02894874699531102, 0.05503989493087988, 0.06818224824514224, 0.05503989493087988, 0.02894874699531102, 0.00991593569322976, 0.002210470228208766,
            2.4350635942371895E-5, 2.5864120226361595E-4, 0.0017843947983501222, 0.008004606371066857, 0.02336878049655781, 0.04443077358068976, 0.05503989493087988, 0.04443077358068976, 0.02336878049655781, 0.008004606371066857, 0.0017843947983501222,
            1.2807444490144886E-5, 1.3603475690306496E-4, 9.385191163983373E-4, 0.004210097510616101, 0.01229102844461037, 0.02336878049655781, 0.02894874699531102, 0.02336878049655781, 0.01229102844461037, 0.004210097510616101, 9.385191163983373E-4,
            4.3869876640759965E-6, 4.6596555689039867E-5, 3.2147488824231735E-4, 0.00144210235366173, 0.004210097510616101, 0.008004606371066857, 0.00991593569322976, 0.008004606371066857, 0.004210097510616101, 0.00144210235366173, 3.2147488824231735E-4,
            9.779516450051282E-7, 1.0387350450247402E-5, 7.166350121265529E-5, 3.2147488824231735E-4, 9.385191163983373E-4, 0.0017843947983501222, 0.002210470228208766, 0.0017843947983501222, 9.385191163983373E-4, 3.2147488824231735E-4, 7.166350121265529E-5,
            1.4175035112951352E-7, 1.5056067251874795E-6, 1.0387350450247402E-5, 4.6596555689039867E-5, 1.3603475690306496E-4, 2.5864120226361595E-4, 3.203992064538993E-4, 2.5864120226361595E-4, 1.3603475690306496E-4, 4.6596555689039867E-5, 1.0387350450247402E-5};

        double[] xgrid = new double[values.length];
        double[] ygrid = new double[values.length];
        int idx = 0;
        for(int i = -5; i <= 5; i++) {
            for(int j = -5; j <= 5; j++) {
                xgrid[idx] = j;
                ygrid[idx] = i;
                idx++;
            }
        }
        Molecule fit = fitter.fit(new SubImage(11, 11, xgrid, ygrid, values, 0.5, 0.5));
        return fit;
    }
}
