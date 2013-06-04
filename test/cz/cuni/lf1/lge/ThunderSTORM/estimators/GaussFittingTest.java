package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.SymmetricGaussianPSF;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class GaussFittingTest {

  @Test
  public void testFit() {
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

    
    LSQFitter fitter = new LSQFitter(new SymmetricGaussianPSF(1.2));
    LSQFitter fitter2 = new LSQFitter(new EllipticGaussianPSF(1.2, 0));
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
    PSFInstance fit2 = fitter2.fit(new OneLocationFitter.SubImage(xgrid, ygrid, values, 0.5, 0.5));
    System.out.println(fit.toString());
    System.out.println(fit2.toString());
    
        
    double[] groundTruth = {1, 0, 1, 1.5, 0};
    double[] groundTruth2 = {1, 0, 1, 1.5, 1.5, 0};
    assertArrayEquals("fittin results", groundTruth, fit.getParamArray(), 10e-3);
    assertArrayEquals("fittin results", groundTruth2, fit2.getParamArray(), 10e-3);
  }
}
