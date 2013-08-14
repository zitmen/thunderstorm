package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
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
    PSFModel psfModel;
    final static int MAX_ITERATIONS = 3000;

    public LSQFitter(PSFModel psfModel) {
        this.psfModel = psfModel;
    }

    /**
     *
     * @param values
     * @param initialGuess for example: {A, x, y, sigma, b}
     */
    @Override
    public Molecule fit(OneLocationFitter.SubImage subimage) {
        if (weights == null) {
            weights = new double[subimage.values.length];
            Arrays.fill(weights, 1);
        }

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(new SimplePointChecker(10e-10, 10e-10));
        PointVectorValuePair pv;

        pv = optimizer.optimize(
                MaxEval.unlimited(),
                new MaxIter(MAX_ITERATIONS),
                new ModelFunction(psfModel.getValueFunction(subimage.xgrid, subimage.ygrid)),
                new ModelFunctionJacobian(psfModel.getJacobianFunction(subimage.xgrid, subimage.ygrid)),
                new Target(subimage.values),
                new InitialGuess(psfModel.transformParametersInverse(psfModel.getInitialParams(subimage))),
                new Weight(weights));
        double[] point = pv.getPointRef();
        //IJ.log("iterations:" + optimizer.getIterations());
        return psfModel.newInstanceFromParams(psfModel.transformParameters(point));

    }
}
