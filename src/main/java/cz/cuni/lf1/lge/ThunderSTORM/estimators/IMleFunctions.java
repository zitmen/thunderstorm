
package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;

public interface IMleFunctions {

	// TODO: the following two methods could be optimized by passing
	// a pre-allocated array to avoid multiple reallocations
	double[] getInitialParams();

	double[] getObservations();

	MoleculeDescriptor.Units getImageUnits();

	MultivariateVectorFunction getValueFunction();

	MultivariateFunction getLikelihoodFunction();
}
