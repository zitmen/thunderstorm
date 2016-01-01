package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.optim.nonlinear.vector.jacobian.LevenbergMarquardtOptimizer;

import static cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.sub;

public class LSQFitter implements IOneLocationFitter, IOneLocationBiplaneFitter {

    public final static int MAX_ITERATIONS = 1000;

    public double[] fittedParameters;
    public PSFModel psfModel;
    public boolean useWeighting;

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
        this.fittedParameters = null;
    }

    @Override
    public Molecule fit(SubImage img) {
        return fit(new LsqMleSinglePlaneFunctions(psfModel, img));
    }

    @Override
    public Molecule fit(SubImage plane1, SubImage plane2) {
        return fit(new LsqMleBiplaneFunctions(psfModel, plane1, plane2));
    }

    protected Molecule fit(ILsqFunctions functions) {
        // init
        double[] weights = functions.calcWeights(useWeighting);
        double[] observations = functions.getObservations();

        // fit
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
                new SimplePointChecker<PointVectorValuePair>(10e-10, 10e-10, maxIter));

        PointVectorValuePair pv;
        pv = optimizer.optimize(
                MaxEval.unlimited(),
                new MaxIter(MAX_ITERATIONS + 1),
                new ModelFunction(functions.getValueFunction()),
                new ModelFunctionJacobian(functions.getJacobianFunction()),
                new Target(observations),
                new InitialGuess(psfModel.transformParametersInverse(functions.getInitialParams())),
                new Weight(weights));

        // estimate background and return an instance of the `Molecule`
        fittedParameters = pv.getPointRef();
        if (bkgStdColumn >= 0) {
            fittedParameters[bkgStdColumn] = VectorMath.stddev(sub(observations, functions.getValueFunction().value(fittedParameters)));
        }
        return psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters), functions.getImageUnits(), true);
    }
}
