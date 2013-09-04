package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sub;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.stddev;
import java.util.Comparator;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.util.LocalizedFormats;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleValueChecker;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.AbstractSimplex;
import org.apache.commons.math3.optim.nonlinear.scalar.noderiv.NelderMeadSimplex;

public class MLEFitter implements OneLocationFitter {

    PSFModel psfModel;
    public double[] fittedModelValues;
    public double[] fittedParameters;
    public final static int MAX_ITERATIONS = 50000;
    private int maxIter;

    public MLEFitter(PSFModel psfModel) {
        this.psfModel = psfModel;
        this.fittedModelValues = null;
        this.fittedParameters = null;
        this.maxIter = MAX_ITERATIONS + 1;    // throws an exception after `MAX_ITERATIONS` iterations
    }

    public MLEFitter(PSFModel psfModel, int maxIter) {
        this.psfModel = psfModel;
        this.fittedModelValues = null;
        this.fittedParameters = null;
        this.maxIter = maxIter;
    }

    @Override
    public Molecule fit(SubImage subimage) {

        if((fittedModelValues == null) || (fittedModelValues.length < subimage.values.length)) {
            fittedModelValues = new double[subimage.values.length];
        }

        SimplexOptimizer optimizer = new SimplexOptimizer(new SimpleValueChecker(1e-10, 1e-10, maxIter));
        PointValuePair pv;

        pv = optimizer.optimize(
                MaxEval.unlimited(),
                new MaxIter(MAX_ITERATIONS),
                new ObjectiveFunction(psfModel.getLikelihoodFunction(subimage.xgrid, subimage.ygrid, subimage.values)),
                new InitialGuess(psfModel.transformParametersInverse(psfModel.getInitialParams(subimage))),
                GoalType.MAXIMIZE,
                new NelderMeadSimplex(psfModel.getInitialSimplex()));

        // estimate background and return an instance of the `Molecule`
        fittedParameters = pv.getPointRef();
        fittedParameters[PSFModel.Params.BACKGROUND] = stddev(sub(fittedModelValues, subimage.values, psfModel.getValueFunction(subimage.xgrid, subimage.ygrid).value(fittedParameters)));
        return psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters));
    }

    //
    // Note: This is almost exact copy of SimplexOptimizer from ApacheCommons Math 3.2.
    //       Unfortunately there is a stupid bug on line 163; variable `iterations` is always zero
    //       which disallow us to check number of iterations in convergence checker!
    //
    public class SimplexOptimizer extends MultivariateOptimizer {

        /**
         * Simplex update rule.
         */
        private AbstractSimplex simplex;

        /**
         * @param checker Convergence checker.
         */
        public SimplexOptimizer(ConvergenceChecker<PointValuePair> checker) {
            super(checker);
        }

        /**
         * @param rel Relative threshold.
         * @param abs Absolute threshold.
         */
        public SimplexOptimizer(double rel, double abs) {
            this(new SimpleValueChecker(rel, abs));
        }

        /**
         * {@inheritDoc}
         *
         * @param optData Optimization data. In addition to those documented in          {@link MultivariateOptimizer#parseOptimizationData(OptimizationData[])
     * MultivariateOptimizer}, this method will register the following data:
         * <ul>
         * <li>{@link AbstractSimplex}</li>
         * </ul>
         * @return {@inheritDoc}
         */
        @Override
        public PointValuePair optimize(OptimizationData... optData) {
            // Set up base class and perform computation.
            return super.optimize(optData);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected PointValuePair doOptimize() {
            checkParameters();

            // Indirect call to "computeObjectiveValue" in order to update the
            // evaluations counter.
            final MultivariateFunction evalFunc = new MultivariateFunction() {
                @Override
                public double value(double[] point) {
                    return computeObjectiveValue(point);
                }
            };

            final boolean isMinim = getGoalType() == GoalType.MINIMIZE;
            final Comparator<PointValuePair> comparator = new Comparator<PointValuePair>() {
                @Override
                public int compare(final PointValuePair o1,
                        final PointValuePair o2) {
                    final double v1 = o1.getValue();
                    final double v2 = o2.getValue();
                    return isMinim ? Double.compare(v1, v2) : Double.compare(v2, v1);
                }
            };

            // Initialize search.
            simplex.build(getStartPoint());
            simplex.evaluate(evalFunc, comparator);

            PointValuePair[] previous = null;
            final ConvergenceChecker<PointValuePair> checker = getConvergenceChecker();
            while(true) {
                if(getIterations() > 0) {
                    boolean converged = true;
                    for(int i = 0; i < simplex.getSize(); i++) {
                        PointValuePair prev = previous[i];
                        converged = converged
                                && checker.converged(getIterations(), prev, simplex.getPoint(i));
                    }
                    if(converged) {
                        // We have found an optimum.
                        return simplex.getPoint(0);
                    }
                }

                // We still need to search.
                previous = simplex.getPoints();
                simplex.iterate(evalFunc, comparator);

                incrementIterationCount();
            }
        }

        /**
         * Scans the list of (required and optional) optimization data that
         * characterize the problem.
         *
         * @param optData Optimization data. The following data will be looked
         * for:
         * <ul>
         * <li>{@link AbstractSimplex}</li>
         * </ul>
         */
        @Override
        protected void parseOptimizationData(OptimizationData... optData) {
            // Allow base class to register its own data.
            super.parseOptimizationData(optData);

            // The existing values (as set by the previous call) are reused if
            // not provided in the argument list.
            for(OptimizationData data : optData) {
                if(data instanceof AbstractSimplex) {
                    simplex = (AbstractSimplex) data;
                    // If more data must be parsed, this statement _must_ be
                    // changed to "continue".
                    break;
                }
            }
        }

        /**
         * @throws MathUnsupportedOperationException if bounds were passed to
         * the {@link #optimize(OptimizationData[]) optimize} method.
         * @throws NullArgumentException if no initial simplex was passed to the
         * {@link #optimize(OptimizationData[]) optimize} method.
         */
        private void checkParameters() {
            if(simplex == null) {
                throw new NullArgumentException();
            }
            if(getLowerBound() != null
                    || getUpperBound() != null) {
                throw new MathUnsupportedOperationException(LocalizedFormats.CONSTRAINT);
            }
        }
    }
}
