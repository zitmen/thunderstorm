package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sub;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.stddev;
import java.util.Arrays;
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

public class LSQFitter implements OneLocationFitter {

    private double[] weights;
    boolean useWeighting;
    double[] fittedModelValues;
    double[] fittedParameters;
    PSFModel psfModel;
    final static int MAX_ITERATIONS = 1000;
    private int maxIter;    // after `maxIter` iterations the algorithm converges

    public LSQFitter(PSFModel psfModel, boolean useWeighting) {
        this(psfModel, useWeighting, MAX_ITERATIONS + 1 );
    }

    public LSQFitter(PSFModel psfModel, boolean useWeighting, int maxIter) {// throws an exception after `MAX_ITERATIONS` iterations
        this.psfModel = psfModel;
        this.fittedModelValues = null;
        this.fittedParameters = null;
        this.maxIter = maxIter;
        this.useWeighting = useWeighting;
    }

    /**
     *
     * @param values
     * @param initialGuess for example: {A, x, y, sigma, b}
     */
    @Override
    public Molecule fit(OneLocationFitter.SubImage subimage) {
        computeWeights(subimage);
        
        if((fittedModelValues == null) || (fittedModelValues.length < subimage.values.length)) {
            fittedModelValues = new double[subimage.values.length];
        }

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(new SimplePointChecker(10e-10, 10e-10, maxIter));
        PointVectorValuePair pv;

        pv = optimizer.optimize(
                MaxEval.unlimited(),
                new MaxIter(MAX_ITERATIONS),
                new ModelFunction(psfModel.getValueFunction(subimage.xgrid, subimage.ygrid)),
                new ModelFunctionJacobian(psfModel.getJacobianFunction(subimage.xgrid, subimage.ygrid)),
                new Target(subimage.values),
                new InitialGuess(psfModel.transformParametersInverse(psfModel.getInitialParams(subimage))),
                new Weight(weights));

        // estimate background and return an instance of the `Molecule`
        fittedParameters = pv.getPointRef();
        fittedParameters[PSFModel.Params.BACKGROUND] = stddev(sub(fittedModelValues, subimage.values, psfModel.getValueFunction(subimage.xgrid, subimage.ygrid).value(fittedParameters)));
        return psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters));
    }

    private void computeWeights(SubImage subimage) {
        if(weights == null) {
            weights = new double[subimage.values.length];
            if(!useWeighting){
                Arrays.fill(weights, 1);
            }
        }
        if(useWeighting) {
            double minWeight = 1.0 / subimage.getMax();
            double maxWeight = 1000 * minWeight;
            
            for(int i = 0; i < weights.length; i++) {
                double weight = 1 / subimage.values[i];
                if(Double.isInfinite(weight) || Double.isNaN(weight) || weight > maxWeight) {
                    weight = maxWeight;
                }
                weights[i] = weight;
            }
        }
    }

}
