package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.BiplaneEllipticGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import java.util.Arrays;

public class LsqMleBiplaneFunctions implements ILsqFunctions, IMleFunctions {

    BiplaneEllipticGaussianPSF psfModel;
    SubImage plane1;
    SubImage plane2;

    public LsqMleBiplaneFunctions(PSFModel psfModel, SubImage plane1, SubImage plane2) {
        assert (psfModel != null);
        assert (plane1 != null);
        assert (plane2 != null);
        if (!(psfModel instanceof BiplaneEllipticGaussianPSF)) {
            throw new ClassCastException("Can't use this PSF model!");
        }
        this.psfModel = (BiplaneEllipticGaussianPSF) psfModel;
        this.plane1 = plane1;
        this.plane2 = plane2;
    }

    @Override
    public double[] getInitialParams() {
        return psfModel.getInitialParams(plane1, plane2);
    }

    @Override
    public MultivariateVectorFunction getValueFunction() {
        return psfModel.getValueFunction(plane1.xgrid, plane1.ygrid, plane2.xgrid, plane2.ygrid);
    }

    @Override
    public MultivariateFunction getLikelihoodFunction() {
        return psfModel.getLikelihoodFunction(plane1.xgrid, plane1.ygrid, plane1.values, plane2.xgrid, plane2.ygrid, plane2.values);
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction() {
         return psfModel.getJacobianFunction(plane1.xgrid, plane1.ygrid, plane2.xgrid, plane2.ygrid);
    }

    @Override
    public double[] calcWeights(boolean useWeighting) {
        double[] weights = new double[plane1.values.length + plane2.values.length];
        if(!useWeighting){
            Arrays.fill(weights, 1);
        } else {
            double minWeight = 1.0 / Math.max(plane1.getMax(), plane2.getMax());
            double maxWeight = 1000 * minWeight;
            int index = 0;
            for (int i = 0; i < plane1.values.length; i++, index++) {
                double weight = 1 / plane1.values[i];
                if (Double.isInfinite(weight) || Double.isNaN(weight) || weight > maxWeight) {
                    weight = maxWeight;
                }
                weights[index] = weight;
            }
            for (int i = 0; i < plane2.values.length; i++, index++) {
                double weight = 1 / plane2.values[i];
                if (Double.isInfinite(weight) || Double.isNaN(weight) || weight > maxWeight) {
                    weight = maxWeight;
                }
                weights[index] = weight;
            }
        }
        return weights;
    }

    @Override
    public double[] getObservations() {
        double[] observations = new double[plane1.values.length + plane2.values.length];
        int index = 0;
        for (int i = 0; i < plane1.values.length; i++, index++) {
            observations[index] = plane1.values[i];
        }
        for (int i = 0; i < plane2.values.length; i++, index++) {
            observations[index] = plane2.values[i];
        }
        return observations;
    }

    @Override
    public MoleculeDescriptor.Units getImageUnits() {
        return plane1.units;
    }
}
