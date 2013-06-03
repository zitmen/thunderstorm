package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.max;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.min;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public interface OneLocationFitter {

  public static class SubImage {

    public int[] xgrid;
    public int[] ygrid;
    public double[] values;
    public double detectorX;
    public double detectorY;

    public SubImage() {
    }

    public SubImage(int[] xgrid, int[] ygrid, double[] values, double detectorX, double detectorY) {
      this.xgrid = xgrid;
      this.ygrid = ygrid;
      this.values = values;
      this.detectorX = detectorX;
      this.detectorY = detectorY;
    }

    public double getMax() {
      return max(values);
    }

    public double getMin() {
      return min(values);
    }
  }

  public PSFInstance fit(SubImage img);
}
