package cz.cuni.lf1.lge.ThunderSTORM.calibration;

/**
 *
 */
public class InterpolatedCalibration implements CylindricalLensCalibration{

  double angle;
  double[][] values;

  public InterpolatedCalibration(double angle, double[][] values) {
    this.angle = angle;
    this.values = values;
  }

  public InterpolatedCalibration() {
  }

  @Override
  public double getAngle() {
    return angle;
  }

  public void setAngle(double angle) {
    this.angle = angle;
  }

  public double[][] getValues() {
    return values;
  }

  public void setValues(double[][] values) {
    this.values = values;
  }

  @Override
  public double getZ(double sigma1, double sigma2) {
    double ratio = sigma1 / sigma2;
    if (ratio < values[0][0] || ratio > values[values.length - 1][0]) {
      return Double.NaN;
    }

    int lowerIndex = 0;
    int higherIndex = values.length - 1;

    while (higherIndex - lowerIndex > 1) {
      int newIndex = ((higherIndex - lowerIndex) / 2) + lowerIndex;
      if (values[newIndex][0] < ratio) {
        lowerIndex = newIndex;
      } else {
        higherIndex = newIndex;
      }
    }
    //linear interploation
    double slope = (values[higherIndex][1] - values[lowerIndex][1]) / (values[higherIndex][0] - values[lowerIndex][0]);
    double interpolatedValue = (ratio - values[lowerIndex][0]) * slope + values[lowerIndex][1];

    return interpolatedValue;
  }
}
