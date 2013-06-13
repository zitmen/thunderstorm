package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.*;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class PolynomialCalibration implements CylindricalLensCalibration {

  double angle;
  // polynom = b + a*(z-c)^2
  double a1;
  double b1;
  double c1;
  double a2;
  double b2;
  double c2;

  public PolynomialCalibration() {
  }

  public PolynomialCalibration(double angle, double[] sigma1Params, double[] sigma2Params) {
    this.angle = angle;
    c1 = sigma1Params[0];
    a1 = sigma1Params[1];
    b1 = sigma1Params[2];
    c2 = sigma2Params[0];
    a2 = sigma2Params[1];
    b2 = sigma2Params[2];
  }

  @Override
  public double getZ(double sigma1, double sigma2) {
    return getZDiff(sigma1, sigma2);
  }

  public double getZSeparate(double sigma1, double sigma2) {

    double sqrt = Math.sqrt((sigma1 - b1) / a1);
    if (Double.isNaN(sqrt)) {
      sqrt = 0;
    }
    double sqrt2 = Math.sqrt((sigma2 - b2) / a2);
    if (Double.isNaN(sqrt2)) {
      sqrt2 = 0;
    }
    //two solutions for each sigma
    double z1s1 = -sqrt + c1;
    double z2s1 = sqrt + c1;
    double z1s2 = -sqrt2 + c2;
    double z2s2 = sqrt2 + c2;


    double d11 = Math.abs(z1s1 - z1s2);
    double d12 = Math.abs(z1s1 - z2s2);
    double d22 = Math.abs(z2s1 - z2s2);

    double z;
    if (d11 < d12) {
      if (d11 < d22) {
        z = (z1s1 + z1s2) / 2;
      } else {
        z = (z2s1 + z2s2) / 2;
      }
    } else {
      if (d12 < d22) {
        z = (z1s1 + z2s2) / 2;
      } else {
        z = (z2s1 + z2s2) / 2;
      }
    }

    return z;
  }

  public double getZDiff(double sigma1, double sigma2) {
    double d = sigma1 - sigma2;
    double x;
    if (Math.abs(a1 - a2) < 1e-6) {
      x = (-a1 * sqr(c1) + a1 * sqr(c2) - b1 + b2 + d) / (2 * a1 * c2 - 2 * a1 * c1);
    } else {
      double sqrt = Math.sqrt(sqr(2 * a2 * c2 - 2 * a1 * c1) - 4 * (a1 - a2) * (a1 * sqr(c1) - a2 * sqr(c2) + b1 - b2 - d));
      double x1 = (-sqrt + 2 * a1 * c1 - 2 * a2 * c2) / (2 * (a1 - a2));
      double x2 = (sqrt + 2 * a1 * c1 - 2 * a2 * c2) / (2 * (a1 - a2));
      x = (Math.abs(x1) < Math.abs(x2)) ? x1 : x2;
    }

    return x;
  }

  @Override
  public double getAngle() {
    return angle;
  }

  public void setAngle(double angle) {
    this.angle = angle;
  }

  public double getA1() {
    return a1;
  }

  public void setA1(double a1) {
    this.a1 = a1;
  }

  public double getB1() {
    return b1;
  }

  public void setB1(double b1) {
    this.b1 = b1;
  }

  public double getC1() {
    return c1;
  }

  public void setC1(double c1) {
    this.c1 = c1;
  }

  public double getA2() {
    return a2;
  }

  public void setA2(double a2) {
    this.a2 = a2;
  }

  public double getB2() {
    return b2;
  }

  public void setB2(double b2) {
    this.b2 = b2;
  }

  public double getC2() {
    return c2;
  }

  public void setC2(double c2) {
    this.c2 = c2;
  }
}
