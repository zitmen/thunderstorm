package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import java.util.Arrays;

public class LsqMleSinglePlaneFunctions implements ILsqFunctions, IMleFunctions {

    PSFModel psfModel;
    SubImage subimage;

    public LsqMleSinglePlaneFunctions(PSFModel psfModel, SubImage subimage) {
        assert (psfModel != null);
        assert (subimage != null);
        this.psfModel = psfModel;
        this.subimage = subimage;
    }

    @Override
    public double[] getInitialParams() {
        return psfModel.getInitialParams(subimage);
    }

    @Override
    public MultivariateVectorFunction getValueFunction() {
        return psfModel.getValueFunction(subimage.xgrid, subimage.ygrid);
    }

    @Override
    public MultivariateFunction getLikelihoodFunction() {
        return psfModel.getLikelihoodFunction(subimage.xgrid, subimage.ygrid, subimage.values);
    }

    @Override
    public MultivariateMatrixFunction getJacobianFunction() {
        return psfModel.getJacobianFunction(subimage.xgrid, subimage.ygrid);
    }

    @Override
    public double[] calcWeights(boolean useWeighting) {
        double[] weights = new double[subimage.values.length];
        if(!useWeighting){
            Arrays.fill(weights, 1);
        } else {
            double minWeight = 1.0 / subimage.getMax();
            double maxWeight = 1000 * minWeight;
            for (int i = 0; i < subimage.values.length; i++) {
                double weight = 1 / subimage.values[i];
                if (Double.isInfinite(weight) || Double.isNaN(weight) || weight > maxWeight) {
                    weight = maxWeight;
                }
                weights[i] = weight;
            }
        }
        return weights;
    }

    @Override
    public double[] getObservations() {
        return subimage.values;
    }

    @Override
    public MoleculeDescriptor.Units getImageUnits() {
        return subimage.units;
    }
}
