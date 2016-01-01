package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

public interface IMleFunctions {
    // TODO: the following two methods could be optimized by passing
    //       a pre-allocated array to avoid multiple reallocations
    double[] getInitialParams();
    double[] getObservations();

    MoleculeDescriptor.Units getImageUnits();

    MultivariateVectorFunction getValueFunction();
    MultivariateFunction getLikelihoodFunction();
}
