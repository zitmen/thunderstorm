
package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;

public interface ILsqFunctions {

	// TODO: the following three methods could be optimized by passing
	// a pre-allocated array to avoid multiple reallocations
	double[] getInitialParams();

	double[] getObservations();

	double[] calcWeights(boolean useWeighting);

	MoleculeDescriptor.Units getImageUnits();

	MultivariateVectorFunction getValueFunction();

	MultivariateMatrixFunction getJacobianFunction();
}
