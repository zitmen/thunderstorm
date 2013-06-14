package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
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

  public void testFitter(OneLocationFitter fitter) {
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

    PSFInstance fit = fitter.fit(new OneLocationFitter.SubImage(xgrid, ygrid, values, 0.5, 0.5));
    System.out.println(fit.toString());


    double[] groundTruth = {1, 0, 1, 1.5, 0};
    assertEquals(groundTruth[0], fit.getX(), 10e-3);
    assertEquals(groundTruth[1], fit.getY(), 10e-3);
    assertEquals(groundTruth[2], fit.getParam(PSFInstance.INTENSITY), 10e-3);
    assertEquals(groundTruth[3], fit.getParam(PSFInstance.SIGMA), 10e-3);
    assertEquals(groundTruth[4], fit.getParam(PSFInstance.BACKGROUND), 10e-3);
  }
}
