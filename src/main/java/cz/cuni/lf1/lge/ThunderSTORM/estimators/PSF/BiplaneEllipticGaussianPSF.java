package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DoubleDefocusCalibration;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import java.util.Arrays;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.exp;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt;
import static java.lang.Math.abs;

public class BiplaneEllipticGaussianPSF extends BiplaneSymmetricGaussianPSF {

    private DoubleDefocusCalibration<DaostormCalibration> calibration;
    private double fi1, fi2, sinfi1, sinfi2, cosfi1, cosfi2;

    public BiplaneEllipticGaussianPSF(DoubleDefocusCalibration<DaostormCalibration> calibration, boolean numericalDerivatives) {
        super(numericalDerivatives);
        assert(calibration != null);
        assert(calibration.homography != null);
        assert(calibration.cal1 != null);
        assert(calibration.cal2 != null);
        this.calibration = calibration;
        this.fi1 = calibration.cal1.angle;
        this.sinfi1 = Math.sin(fi1);
        this.cosfi1 = Math.cos(fi1);
        this.fi2 = calibration.cal2.angle;
        this.sinfi2 = Math.sin(fi2);
        this.cosfi2 = Math.cos(fi2);
    }

    @Override
    public double[] transformParameters(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.SIGMA1] = calibration.cal1.getSigma1Squared(parameters[Params.Z]);
        transformed[Params.SIGMA2] = calibration.cal1.getSigma2Squared(parameters[Params.Z]);
        transformed[Params.SIGMA3] = calibration.cal2.getSigma1Squared(parameters[Params.Z]);
        transformed[Params.SIGMA4] = calibration.cal2.getSigma2Squared(parameters[Params.Z]);
        transformed[Params.INTENSITY] = sqr(parameters[Params.INTENSITY]);
        transformed[Params.OFFSET1] = sqr(parameters[Params.OFFSET1]);
        transformed[Params.OFFSET2] = sqr(parameters[Params.OFFSET2]);
        return transformed;
    }

    @Override
    public double[] transformParametersInverse(double[] parameters) {
        double [] transformed = Arrays.copyOf(parameters, parameters.length);
        transformed[Params.SIGMA1] = calibration.cal1.getSigma1(parameters[Params.Z]);
        transformed[Params.SIGMA2] = calibration.cal1.getSigma2(parameters[Params.Z]);
        transformed[Params.SIGMA3] = calibration.cal2.getSigma1(parameters[Params.Z]);
        transformed[Params.SIGMA4] = calibration.cal2.getSigma2(parameters[Params.Z]);
        transformed[Params.INTENSITY] = sqrt(abs(parameters[Params.INTENSITY]));
        transformed[Params.OFFSET1] = sqrt(abs(parameters[Params.OFFSET1]));
        transformed[Params.OFFSET2] = sqrt(abs(parameters[Params.OFFSET2]));
        return transformed;
    }

    @Override
    public Molecule newInstanceFromParams(double[] params, MoleculeDescriptor.Units subImageUnits, boolean afterFitting) {
        if (afterFitting) {
            params[Params.SIGMA1] = calibration.cal1.getSigma1(params[Params.Z]);
            params[Params.SIGMA2] = calibration.cal1.getSigma2(params[Params.Z]);
            params[Params.SIGMA3] = calibration.cal2.getSigma1(params[Params.Z]);
            params[Params.SIGMA4] = calibration.cal2.getSigma2(params[Params.Z]);
        }
        Molecule mol = new Molecule(new Params(new int[] { Params.X, Params.Y, Params.Z,
                Params.SIGMA1, Params.SIGMA2, Params.SIGMA3, Params.SIGMA4, Params.INTENSITY,
                Params.OFFSET1, Params.OFFSET2, Params.BACKGROUND }, params, true));
        MoleculeDescriptor descriptor = mol.descriptor;
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_INTENSITY));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET1));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_OFFSET2));
        descriptor.setColumnUnits(subImageUnits, descriptor.getParamColumn(Params.LABEL_BACKGROUND));
        return mol;
    }

    @Override
    public MultivariateVectorFunction getValueFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] params) throws IllegalArgumentException {
                double[] tparams = transformParameters(params);
                double[] retVal = new double[xgrid1.length + xgrid2.length];

                params[Params.SIGMA1] = calibration.cal1.getSigma1(tparams[Params.Z]);
                params[Params.SIGMA2] = calibration.cal1.getSigma2(tparams[Params.Z]);
                params[Params.SIGMA3] = calibration.cal2.getSigma1(tparams[Params.Z]);
                params[Params.SIGMA4] = calibration.cal2.getSigma2(tparams[Params.Z]);

                int index = 0;
                for(int i = 0; i < xgrid1.length; i++, index++) {
                    double dx = ((xgrid1[i] - tparams[Params.X])*cosfi1 - (ygrid1[i] - tparams[Params.Y])*sinfi1);
                    double dy = ((xgrid1[i] - tparams[Params.X])*sinfi1 + (ygrid1[i] - tparams[Params.Y])*cosfi1);
                    retVal[index] = exp(-0.5 * (sqr(dx/params[Params.SIGMA1]) + sqr(dy/params[Params.SIGMA2])))
                                  * (tparams[Params.INTENSITY] / 2.0) / (2*Math.PI*params[Params.SIGMA1]*params[Params.SIGMA2])
                                  + tparams[Params.OFFSET1];
                }
                for(int i = 0; i < xgrid2.length; i++, index++) {
                    double dx = ((xgrid2[i] - tparams[Params.X])*cosfi2 - (ygrid2[i] - tparams[Params.Y])*sinfi2);
                    double dy = ((xgrid2[i] - tparams[Params.X])*sinfi2 + (ygrid2[i] - tparams[Params.Y])*cosfi2);
                    retVal[index] = exp(-0.5 * (sqr(dx/params[Params.SIGMA3]) + sqr(dy/params[Params.SIGMA4])))
                            * (tparams[Params.INTENSITY] / 2.0) / (2*Math.PI*params[Params.SIGMA3]*params[Params.SIGMA4])
                            + tparams[Params.OFFSET2];
                }
                return retVal;
            }
        };
    }

    @Override
    protected MultivariateMatrixFunction getAnalyticJacobianFunction(final double[] xgrid1, final double[] ygrid1, final double[] xgrid2, final double[] ygrid2) {
        return new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[] transformedPoint = transformParameters(point);
                double[][] retVal = new double[xgrid1.length + xgrid2.length][transformedPoint.length];

                int index = 0;
                for (int i = 0; i < xgrid1.length; i++, index++) {
                    double xd = xgrid1[i] - transformedPoint[Params.X];
                    double yd = ygrid1[i] - transformedPoint[Params.Y];
                    double cosfiXd = cosfi1 * xd, cosfiYd = cosfi1 * yd;
                    double sinfiYd = sinfi1 * yd, sinfiXd = sinfi1 * xd;
                    double first = cosfiXd - sinfiYd, second = sinfiXd + cosfiYd;
                    double expVal = exp(-0.5 * (sqr(first)/transformedPoint[Params.SIGMA1] + sqr(second)/transformedPoint[Params.SIGMA2]));
                    double oneDivPISS2 = 1 / (Math.PI * point[Params.SIGMA1] * point[Params.SIGMA2]);
                    // diff(psf, x0)
                    double pom1 = first*cosfi1/transformedPoint[Params.SIGMA1] + second*sinfi1/transformedPoint[Params.SIGMA2];
                    retVal[index][Params.X] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom1 * expVal;
                    // diff(psf, y0)
                    double pom2 = first*sinfi1/transformedPoint[Params.SIGMA1] + second*cosfi1/transformedPoint[Params.SIGMA2];
                    retVal[index][Params.Y] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom2 * expVal;
                    // diff(psf, z0)
                    double pom4 = (transformedPoint[Params.Z] - calibration.cal1.c1) / sqr(calibration.cal1.d1
                                * (1 + sqr((transformedPoint[Params.Z] - calibration.cal1.c1) / calibration.cal1.d1)));
                    double pom5 = (transformedPoint[Params.Z] - calibration.cal1.c2) / sqr(calibration.cal1.d2
                                * (1 + sqr((transformedPoint[Params.Z] - calibration.cal1.c2) / calibration.cal1.d2)));
                    double pom3 = sqr(first) * pom4 + sqr(second) * pom5;
                    retVal[index][Params.Z] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom3
                                            - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom4
                                            - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom5;
                    // diff(psf, I)
                    retVal[index][Params.INTENSITY] = point[Params.INTENSITY] * expVal * oneDivPISS2;
                    // diff(psf, off1)
                    retVal[index][Params.OFFSET1] = 2 * point[Params.OFFSET1];
                }
                for (int i = 0; i < xgrid2.length; i++, index++) {
                    double xd = xgrid2[i] - transformedPoint[Params.X];
                    double yd = ygrid2[i] - transformedPoint[Params.Y];
                    double cosfiXd = cosfi2 * xd, cosfiYd = cosfi2 * yd;
                    double sinfiYd = sinfi2 * yd, sinfiXd = sinfi2 * xd;
                    double first = cosfiXd - sinfiYd, second = sinfiXd + cosfiYd;
                    double expVal = exp(-0.5 * (sqr(first)/transformedPoint[Params.SIGMA3] + sqr(second)/transformedPoint[Params.SIGMA4]));
                    double oneDivPISS2 = 1 / (Math.PI * point[Params.SIGMA3] * point[Params.SIGMA4]);
                    // diff(psf, x0)
                    double pom1 = first*cosfi2/transformedPoint[Params.SIGMA3] + second*sinfi2/transformedPoint[Params.SIGMA4];
                    retVal[index][Params.X] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom1 * expVal;
                    // diff(psf, y0)
                    double pom2 = first*sinfi2/transformedPoint[Params.SIGMA3] + second*cosfi2/transformedPoint[Params.SIGMA4];
                    retVal[index][Params.Y] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * pom2 * expVal;
                    // diff(psf, z0)
                    double pom4 = (transformedPoint[Params.Z] - calibration.cal2.c1) / sqr(calibration.cal2.d1
                            * (1 + sqr((transformedPoint[Params.Z] - calibration.cal2.c1) / calibration.cal2.d1)));
                    double pom5 = (transformedPoint[Params.Z] - calibration.cal2.c2) / sqr(calibration.cal2.d2
                            * (1 + sqr((transformedPoint[Params.Z] - calibration.cal2.c2) / calibration.cal2.d2)));
                    double pom3 = sqr(first) * pom4 + sqr(second) * pom5;
                    retVal[index][Params.Z] = oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom3
                            - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom4
                            - oneDivPISS2 * 0.5 * transformedPoint[Params.INTENSITY] * expVal * pom5;
                    // diff(psf, I)
                    retVal[index][Params.INTENSITY] = point[Params.INTENSITY] * expVal * oneDivPISS2;
                    // diff(psf, off2)
                    retVal[index][Params.OFFSET2] = 2 * point[Params.OFFSET2];
                }

                return retVal;
            }
        };
    }
}
