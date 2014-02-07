package cz.cuni.lf1.lge.ThunderSTORM.calibration;

public interface CylindricalLensCalibration {

    public double getAngle();
    public double getZ(double sigma1, double sigma2);
    public double getSigma1(double z);
    public double getSigma2(double z);
}
