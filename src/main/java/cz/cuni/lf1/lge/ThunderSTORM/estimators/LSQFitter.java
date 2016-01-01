package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.BiplaneEllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import static cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.sub;
import java.util.Arrays;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

public class LSQFitter implements OneLocationFitter, OneLocationBiplaneFitter {

    boolean useWeighting;
    double[] weights = null;
    double[] observations = null;
    double[] fittedModelValues = null;
    double[] fittedParameters = null;
    public PSFModel psfModel;
    final static int MAX_ITERATIONS = 1000;
    private int maxIter;    // after `maxIter` iterations the algorithm converges
    private int bkgStdColumn;

    public LSQFitter(PSFModel psfModel, boolean useWeighting) {
        this(psfModel, useWeighting, MAX_ITERATIONS + 1, -1 );
    }
    
    public LSQFitter(PSFModel psfModel, boolean useWeighting, int bkgStdIndex) {
        this(psfModel, useWeighting, MAX_ITERATIONS + 1, Params.BACKGROUND );
    }

    public LSQFitter(PSFModel psfModel, boolean useWeighting, int maxIter, int bkgStdIndex) {// throws an exception after `MAX_ITERATIONS` iterations
        this.psfModel = psfModel;
        this.maxIter = maxIter;
        this.useWeighting = useWeighting;
        this.bkgStdColumn = bkgStdIndex;
    }

    @Override
    public Molecule fit(SubImage subimage) {
        // init
        computeWeights(subimage);
        if((fittedModelValues == null) || (fittedModelValues.length != subimage.values.length)) {
            fittedModelValues = new double[subimage.values.length];
        }

        // fit
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
                new SimplePointChecker<PointVectorValuePair>(10e-10, 10e-10, maxIter));

        PointVectorValuePair pv;
        pv = optimizer.optimize(
                MaxEval.unlimited(),
                new MaxIter(MAX_ITERATIONS + 1),
                new ModelFunction(psfModel.getValueFunction(subimage.xgrid, subimage.ygrid)),
                new ModelFunctionJacobian(psfModel.getJacobianFunction(subimage.xgrid, subimage.ygrid)),
                new Target(subimage.values),
                new InitialGuess(psfModel.transformParametersInverse(psfModel.getInitialParams(subimage))),
                new Weight(weights));

        // estimate background and return an instance of the `Molecule`
        fittedParameters = pv.getPointRef();
        if(bkgStdColumn >= 0){
            fittedParameters[bkgStdColumn] = VectorMath.stddev(sub(fittedModelValues, subimage.values,
                    psfModel.getValueFunction(subimage.xgrid, subimage.ygrid).value(fittedParameters)));
        }
        return psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters), subimage.units, true);
    }

    @Override
    public Molecule fit(SubImage plane1, SubImage plane2) throws Exception {
        // init
        computeWeights(plane1, plane2);
        copyObservations(plane1, plane2);
        if((fittedModelValues == null) || (fittedModelValues.length != plane1.values.length + plane2.values.length)) {
            fittedModelValues = new double[plane1.values.length + plane2.values.length];
        }

        // fit
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
                new SimplePointChecker<PointVectorValuePair>(10e-10, 10e-10, maxIter));

        PointVectorValuePair pv;
        pv = optimizer.optimize(
                MaxEval.unlimited(),
                new MaxIter(MAX_ITERATIONS + 1),
                new ModelFunction(getValueFunction(plane1, plane2)),
                new ModelFunctionJacobian(getJacobianFunction(plane1, plane2)),
                new Target(observations),
                new InitialGuess(psfModel.transformParametersInverse(getInitialParams(plane1, plane2))),
                new Weight(weights));

        // estimate background and return an instance of the `Molecule`
        fittedParameters = pv.getPointRef();
        if (bkgStdColumn >= 0) {
            fittedParameters[bkgStdColumn] = VectorMath.stddev(sub(fittedModelValues, observations,
                    getValueFunction(plane1, plane2).value(fittedParameters)));
        }
        return psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters), plane1.units, true);
    }

    public double[] getInitialParams(SubImage plane1, SubImage plane2) throws Exception {
        BiplaneEllipticGaussianPSF model = (BiplaneEllipticGaussianPSF) psfModel;
        if (model == null) throw new Exception("Unknown PSF model for biplane fitting!");
        return model.getInitialParams(plane1, plane2);
    }

    public MultivariateVectorFunction getValueFunction(SubImage plane1, SubImage plane2) throws Exception {
        BiplaneEllipticGaussianPSF model = (BiplaneEllipticGaussianPSF) psfModel;
        if (model == null) throw new Exception("Unknown PSF model for biplane fitting!");
        return model.getValueFunction(plane1.xgrid, plane1.ygrid, plane2.xgrid, plane2.ygrid);
    }

    public MultivariateMatrixFunction getJacobianFunction(SubImage plane1, SubImage plane2) throws Exception {
        BiplaneEllipticGaussianPSF model = (BiplaneEllipticGaussianPSF) psfModel;
        if (model == null) throw new Exception("Unknown PSF model for biplane fitting!");
        return model.getJacobianFunction(plane1.xgrid, plane1.ygrid, plane2.xgrid, plane2.ygrid);
    }

    private void copyObservations(SubImage ... subimages) {
        int len = 0;
        for (SubImage subimage : subimages) {
            len += subimage.values.length;
        }
        if((observations == null) || (observations.length != len)) {
            observations = new double[len];
        }
        int index = 0;
        for (SubImage subimage : subimages) {
            for (int i = 0; i < subimage.values.length; i++, index++) {
                observations[index] = subimage.values[i];
            }
        }
    }

    private void computeWeights(SubImage ... subimages) {
        int len = 0;
        double max = 0.0;
        for (SubImage subimage : subimages) {
            len += subimage.values.length;
            max = Math.max(max, subimage.getMax());
        }
        if ((weights == null) || (weights.length != len)) {
            weights = new double[len];
            if(!useWeighting){
                Arrays.fill(weights, 1);
            }
        }
        if(useWeighting) {
            double minWeight = 1.0 / max;
            double maxWeight = 1000 * minWeight;
            int index = 0;
            for (SubImage subimage : subimages) {
                for (int i = 0; i < subimage.values.length; i++, index++) {
                    double weight = 1 / subimage.values[i];
                    if (Double.isInfinite(weight) || Double.isNaN(weight) || weight > maxWeight) {
                        weight = maxWeight;
                    }
                    weights[index] = weight;
                }
            }
        }
    }
}
