package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import static org.apache.commons.math3.util.FastMath.log;

/**
 * Representation of PSFModel model.
 *
 * <strong>Note:</strong> in a future release the PSFModel will be more abstract
 * to allow easily work with any possible PSFModel out there, but right we use
 * strictly the symmetric 2D Gaussian model.
 *
 * <strong>This class and its children need to be refactored!</strong>
 */
public abstract class PSFModel {

    /**
     * This class allows to maintain the variables inside of the PSF,
     * keeps their order in check just by changing the ids, and
     * makes the conversion between names and indices into the `params` array.
     * 
     * If a PSF doesn't use any of the parameters, it can be simply ignored.
     * Allocating few extra bytes doesn't hurt the speed much and it simplifies
     * the code a bit.
     */
    public static class Params {

        public static final int X = 0;
        public static final int Y = 1;
        public static final int Z = 2;
        public static final int SIGMA = 3;
        public static final int INTENSITY = 4;
        public static final int OFFSET = 5;
        public static final int BACKGROUND = 6;
        public static final int ANGLE = 7;
        public static final int SIGMA1 = 8;
        public static final int SIGMA2 = 9;
        public static final int PARAMS_LENGTH = 10;  // <stop>
        
        private static String [] all_names = null;
        
        public static final String LABEL_X = "x";
        public static final String LABEL_Y = "y";
        public static final String LABEL_Z = "z";
        public static final String LABEL_SIGMA = "sigma";
        public static final String LABEL_INTENSITY = "intensity";
        public static final String LABEL_OFFSET = "offset";
        public static final String LABEL_BACKGROUND = "bkgstd";
        public static final String LABEL_SIGMA1 = "sigma1";
        public static final String LABEL_SIGMA2 = "sigma2";
        public static final String LABEL_ANGLE = "angle";

        // automatically generates the corresponding names,
        // even if the order have been changed
        private void initNames() {
            Params.all_names = new String[PARAMS_LENGTH];
            Params.all_names[X] = LABEL_X;
            Params.all_names[Y] = LABEL_Y;
            Params.all_names[Z] = LABEL_Z;
            Params.all_names[SIGMA] = LABEL_SIGMA;
            Params.all_names[INTENSITY] = LABEL_INTENSITY;
            Params.all_names[OFFSET] = LABEL_OFFSET;
            Params.all_names[BACKGROUND] = LABEL_BACKGROUND;
            Params.all_names[SIGMA1] = LABEL_SIGMA1;
            Params.all_names[SIGMA2] = LABEL_SIGMA2;
            Params.all_names[ANGLE] = LABEL_ANGLE;
        }
        
        // these arrays can be used for "communicating" with the PSF classes
        // from outside world without need of any additonal information about
        // the implementation of PSF
        public int [] indices;
        public String [] names;
        public double [] values;
        
        private HashSet<Integer> params_int;
        private HashMap<String,Integer> params_str;
        
        // if fullVector == true, then values are of length = PARAMS_LENGTH,
        // otherwise they are as long as variables.length
        public Params(int [] variables, double [] values, boolean fullVector) {
            assert(variables != null);
            assert(values != null);
            //
            if(Params.all_names == null) {  // init
                initNames();
            }
            this.indices = variables;
            this.names = new String[indices.length];
            this.params_int = new HashSet<Integer>();
            this.params_str = new HashMap<String,Integer>();
            if(fullVector) {
                this.values = values;
            } else {
                this.values = new double[Params.PARAMS_LENGTH];
                Arrays.fill(this.values, 0.0);
            }
            for(int i = 0; i < indices.length; i++) {
                this.names[i] = Params.all_names[indices[i]];
                this.params_int.add(indices[i]);
                this.params_str.put(names[i], indices[i]);
                if(!fullVector) {
                    this.values[indices[i]] = values[i];
                }
            }
        }
        
        public boolean hasParam(int param) {
            return params_int.contains(param);
        }

        public boolean hasParam(String name) {
            return params_str.containsKey(name);
        }

        public double getParam(int param) {
            if(!hasParam(param)) throw new IllegalArgumentException("Parameter does not exist!");
            return values[param];
        }

        public void setParam(int param, double value) {
            if(!hasParam(param)) throw new IllegalArgumentException("Parameter does not exist!");
            values[param] = value;
        }

        public double getParamAt(int i) {
            if(i < 0 || i >= indices.length) throw new ArrayIndexOutOfBoundsException("Parameter index is out of bouds!");
            return values[indices[i]];
        }

        public void setParamAt(int i, double value) {
            if(i < 0 || i >= indices.length) throw new ArrayIndexOutOfBoundsException("Parameter index is out of bouds!");
            values[indices[i]] = value;
        }

        public String getParamNameAt(int i) {
            if(i < 0 || i >= indices.length) throw new ArrayIndexOutOfBoundsException("Parameter index is out of bouds!");
            return names[i];
        }

        public void setParam(String name, double value) {
            if(!hasParam(name)) throw new IllegalArgumentException("Parameter `" + name + "` does not exist!");
            values[params_str.get(name).intValue()] = value;
        }

        public double getParam(String name) {
            if(!hasParam(name)) throw new IllegalArgumentException("Parameter `" + name + "` does not exist!");
            return values[params_str.get(name).intValue()];
        }

        public int getParamsCount() {
            return indices.length;
        }
    }
    
    public double[] transformParameters(double[] params) {
        return params;
    }

    public double[] transformParametersInverse(double[] params) {
        return params;
    }

    abstract public double getValue(double[] params, double x, double y);

    public MultivariateVectorFunction getValueFunction(final int[] xgrid, final int[] ygrid) {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] params) throws IllegalArgumentException {
                double[] transformedParams = transformParameters(params);
                double[] retVal = new double[xgrid.length];
                for (int i = 0; i < xgrid.length; i++) {
                    retVal[i] = getValue(transformedParams, xgrid[i], ygrid[i]);
                }
                return retVal;
            }
        };
    }

    /**
     * Default implementation with numeric gradients. You can override it with
     * analytical jacobian.
     */
    public MultivariateMatrixFunction getJacobianFunction(final int[] xgrid, final int[] ygrid) {
        final MultivariateVectorFunction valueFunction = getValueFunction(xgrid, ygrid);
        return new MultivariateMatrixFunction() {
            static final double step = 0.01;

            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {
                double[][] retVal = new double[xgrid.length][point.length];

                for (int i = 0; i < point.length; i++) {
                    double[] newPoint = point.clone();
                    newPoint[i] = newPoint[i] + step;
                    double[] f1 = valueFunction.value(newPoint);
                    double[] f2 = valueFunction.value(point);
                    for (int j = 0; j < f1.length; j++) {
                        retVal[j][i] = (f1[j] - f2[j]) / step;
                    }
                }
                return retVal;
            }
        };
    }

    public MultivariateFunction getLikelihoodFunction(final int[] xgrid, final int[] ygrid, final double[] imageValues) {
        return new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double[] newPoint = transformParameters(point);


                double logLikelihood = 0;
                for (int i = 0; i < xgrid.length; i++) {
                    double expectedValue = getValue(newPoint, xgrid[i], ygrid[i]);
                    double log = log(expectedValue);
                    if (log < -1e6) {
                        log = -1e6;
                    }
                    logLikelihood += expectedValue - imageValues[i] * log;
                }
//        IJ.log("likelihood:" + logLikelihood);
//        IJ.log("point: " + Arrays.toString(point));
                return logLikelihood;
            }
        };
    }

    /**
     * first step of nelder-mead simplex algorithm. Used in mle estimator.
     */
    public abstract double[] getInitialSimplex();

    public abstract double[] getInitialParams(OneLocationFitter.SubImage subImage);

    public abstract Molecule newInstanceFromParams(double [] params);
}
