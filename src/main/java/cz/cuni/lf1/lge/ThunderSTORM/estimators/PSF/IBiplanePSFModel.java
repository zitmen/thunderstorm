package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.SubImage;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

public interface IBiplanePSFModel {
    double[] getInitialParams(SubImage plane1, SubImage plane2);
    MultivariateVectorFunction getValueFunction(double[] xgrid1, double[] ygrid1, double[] xgrid2, double[] ygrid2);
    MultivariateMatrixFunction getJacobianFunction(double[] xgrid1, double[] ygrid1, double[] xgrid2, double[] ygrid2);
    MultivariateFunction getLikelihoodFunction(double[] xgrid1, double[] ygrid1, double[] values1, double[] xgrid2, double[] ygrid2, double[] values2);
    double getChiSquared(double[] xgrid1, double[] ygrid1, double[] values1, double[] xgrid2, double[] ygrid2, double[] values2, double[] params, boolean weighted);
}
