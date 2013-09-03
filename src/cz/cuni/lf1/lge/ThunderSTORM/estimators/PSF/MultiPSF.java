package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.abs;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;
import java.util.Arrays;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

/**
 * Representation of multi-molecule model.
 */
public class MultiPSF extends PSFModel {

    private int nmol;
    private double defaultSigma;
    private PSFModel psf;
    private double [] n_1_params;   // params fitted in model with nmol=nmol-1
    
    public MultiPSF(int nmol, double defaultSigma, PSFModel psf) {
        this.psf = psf;
        this.nmol = nmol;
        this.n_1_params = null;
    }
    
    public MultiPSF(int nmol, double defaultSigma, PSFModel psf, double [] n_1_params) {
        this.psf = psf;
        this.nmol = nmol;
        this.n_1_params = n_1_params;
    }
    
    private double [] fixParams(double [] params) {
        // values of Intensity and offset must be the same for each `multi`-molecule
        double I = params[Params.INTENSITY];
        double off = params[Params.OFFSET];
        for(int base = 0; base < params.length; base += Params.PARAMS_LENGTH) {
            params[base+Params.INTENSITY] = I;
            params[base+Params.OFFSET] = off;
        }
        return params;
    }
    
    @Override
    public double getValue(double[] params, double x, double y) {
        fixParams(params);
        //
        double value = 0.0;
        for(int i = 0; i < nmol; i++) {
            double [] tmp = Arrays.copyOfRange(params, i*Params.PARAMS_LENGTH, (i+1)*Params.PARAMS_LENGTH);
            value += psf.getValue(tmp, x, y);
        }
        return value;
    }

    @Override
    public double[] transformParameters(double[] params) {
        double[] transformed = new double[params.length];
        for(int i = 0; i < nmol; i++) {
            double [] tmp = Arrays.copyOfRange(params, i*Params.PARAMS_LENGTH, (i+1)*Params.PARAMS_LENGTH);
            System.arraycopy(psf.transformParameters(tmp), 0, transformed, i*Params.PARAMS_LENGTH, Params.PARAMS_LENGTH);
        }
        return transformed;  // values of Intensity and offset must be the same for each `multi`-molecule
    }

    @Override
    public double[] transformParametersInverse(double[] params) {
        double[] transformed = new double[params.length];
        for(int i = 0; i < nmol; i++) {
            double [] tmp = Arrays.copyOfRange(params, i*Params.PARAMS_LENGTH, (i+1)*Params.PARAMS_LENGTH);
            System.arraycopy(psf.transformParametersInverse(tmp), 0, transformed, i*Params.PARAMS_LENGTH, Params.PARAMS_LENGTH);
        }
        return transformed;
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction(final int[] xgrid, final int[] ygrid) {
        return new MultivariateMatrixFunction() {
            @Override
            public double[][] value(double[] point) throws IllegalArgumentException {
                fixParams(point);
                //
                double[][] retVal = new double[xgrid.length][point.length];
                for(int i = 0; i < nmol; i++) {
                    double [] tmp = Arrays.copyOfRange(point, i*Params.PARAMS_LENGTH, (i+1)*Params.PARAMS_LENGTH);
                    double [][] J = psf.getJacobianFunction(xgrid, ygrid).value(tmp);
                    for(int j = 0; j < J.length; j++) {
                        for(int k = 0, l = i*Params.PARAMS_LENGTH; k < J[j].length; k++, l++) {
                            retVal[j][l] = J[j][k];
                        }
                    }
                }
                return retVal;
            }
        };
    }

    /**
     * Value function overriden for speed. When calculating for the whole
     * subimage, some values can be reused. But can only be used for a square
     * grid where xgrid values are same in each column and ygrid values are the
     * same in each row.
     *
     * @param xgrid
     * @param ygrid
     * @return
     */
    @Override
    public MultivariateVectorFunction getValueFunction(final int[] xgrid, final int[] ygrid) {
        return new MultivariateVectorFunction() {
            @Override
            public double[] value(double[] point) throws IllegalArgumentException {
                fixParams(point);
                //
                double[] retVal = new double[xgrid.length];
                Arrays.fill(retVal, 0.0);
                for(int i = 0; i < nmol; i++) {
                    double [] tmp = Arrays.copyOfRange(point, i*Params.PARAMS_LENGTH, (i+1)*Params.PARAMS_LENGTH);
                    double [] values = psf.getValueFunction(xgrid, ygrid).value(tmp);
                    for(int j = 0; j < values.length; j++) {
                        retVal[j] += values[j];
                    }
                }
                return retVal;
            }
        };
    }

    @Override
    public double[] getInitialSimplex() {
        double[] steps = new double[nmol*PSFModel.Params.PARAMS_LENGTH];
        double[] init = psf.getInitialSimplex();
        for(int i = 0; i < nmol; i++) {
            for(int j = 0, k = i*Params.PARAMS_LENGTH; j < Params.PARAMS_LENGTH; j++, k++) {
                steps[k] = init[j];
            }
        }
        return steps;
    }

    @Override
    public double[] getInitialParams(OneLocationFitter.SubImage subImage) {
        if(n_1_params == null) {
            assert(nmol == 1);
            return psf.getInitialParams(subImage);
        } else {
            assert(nmol > 1);
            //
            double[] guess = new double[nmol*PSFModel.Params.PARAMS_LENGTH];
            Arrays.fill(guess, 0);
            // copy parameters of N-1 molecules from previous model
            System.arraycopy(n_1_params, 0, guess, 0, n_1_params.length);
            // subtract fitted model from the subimage
            nmol -= 1;  // change size to get the values of simpler model
            double [] residual = subImage.subtract(getValueFunction(subImage.xgrid, subImage.ygrid).value(n_1_params));
            nmol += 1;  // change it back
            // find maximum
            int max_i = 0;
            for(int i = 1; i < residual.length; i++) {
                if(residual[i] > residual[max_i]) {
                    max_i = i;
                }
            }
            OneLocationFitter.SubImage img = new OneLocationFitter.SubImage(subImage.size, subImage.xgrid, subImage.ygrid, residual, subImage.xgrid[max_i], subImage.ygrid[max_i]);
            // get the initial guess for Nth molecule
            System.arraycopy(psf.getInitialParams(img), 0, guess, (nmol-1)*PSFModel.Params.PARAMS_LENGTH, PSFModel.Params.PARAMS_LENGTH);
            // perform push&pull adjustment -- to close to the boundary? push out; else pull in;
            double x, y, sig_2 = defaultSigma / 2.0;
            for(int i = 0, base = 0; i < nmol; i++, base += PSFModel.Params.PARAMS_LENGTH) {
                x = guess[base+PSFModel.Params.X];
                y = guess[base+PSFModel.Params.Y];
                if((subImage.size/2 - abs(x)) < defaultSigma) {
                    guess[base+PSFModel.Params.X] += (x > 0 ? sig_2 : -sig_2);
                } else {
                    guess[base+PSFModel.Params.X] -= (x > 0 ? sig_2 : -sig_2);
                }
                if((subImage.size/2 - abs(y)) < defaultSigma) {
                    guess[base+PSFModel.Params.Y] += (y > 0 ? sig_2 : -sig_2);
                } else {
                    guess[base+PSFModel.Params.Y] -= (y > 0 ? sig_2 : -sig_2);
                }
            }
            return guess;
        }
    }

    @Override
    public Molecule newInstanceFromParams(double[] params) {    // returns one `macro-molecule`
        double [] tmp = Arrays.copyOfRange(params, 0, Params.PARAMS_LENGTH);
        Molecule macroMol = psf.newInstanceFromParams(tmp);  // init...if there is more than one molecule, the values of macro-molecule are ignored anyway
        macroMol.detections.add(macroMol);  // i = 0
        for(int i = 1; i < nmol; i++) {
            tmp = Arrays.copyOfRange(params, i*Params.PARAMS_LENGTH, (i+1)*Params.PARAMS_LENGTH);
            macroMol.detections.add(psf.newInstanceFromParams(tmp));
        }
        return macroMol;
    }

    @Override
    public double getDoF() {
        return psf.getDoF() + (nmol-1)*(psf.getDoF()-2);    // both intensity and offset are estimated for all molecules as a single parameter (see method `fixParams`)
    }
}