package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IBiplanePSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.optimizers.NelderMead;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;

import static cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath.sub;

public class MLEFitter implements OneLocationFitter, OneLocationBiplaneFitter {

    PSFModel psfModel;
    public double[] fittedModelValues;
    public double[] fittedParameters;
    public final static int MAX_ITERATIONS = 50000;
    private double[] observations = null;
    private int maxIter;
    private int bkgStdColumn;

    public MLEFitter(PSFModel psfModel) {
        this(psfModel, MAX_ITERATIONS + 1, -1);
    }

    public MLEFitter(PSFModel psfModel, int bkgStdIndex) {
        this(psfModel, MAX_ITERATIONS + 1, bkgStdIndex);
    }

    public MLEFitter(PSFModel psfModel, int maxIter, int bkgStdIndex) {
        this.psfModel = psfModel;
        this.fittedModelValues = null;
        this.fittedParameters = null;
        this.maxIter = maxIter;
    }

    @Override
    public Molecule fit(SubImage subimage) {
        // init
        subimage.convertTo(MoleculeDescriptor.Units.PHOTON);
        if((fittedModelValues == null) || (fittedModelValues.length < subimage.values.length)) {
            fittedModelValues = new double[subimage.values.length];
        }

        // fit
        NelderMead nm = new NelderMead();
        double[] guess = psfModel.transformParametersInverse(psfModel.getInitialParams(subimage));
        nm.optimize(psfModel.getLikelihoodFunction(subimage.xgrid, subimage.ygrid, subimage.values),
                NelderMead.Objective.MAXIMIZE, guess, 1e-8, psfModel.getInitialSimplex(), 10, maxIter);
        fittedParameters = nm.xmin;

        // estimate background and return an instance of the `Molecule`
        fittedParameters[Params.BACKGROUND] = VectorMath.stddev(VectorMath.sub(fittedModelValues, subimage.values,
                psfModel.getValueFunction(subimage.xgrid, subimage.ygrid).value(fittedParameters)));

        Molecule mol = psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters), subimage.units, true);

        if(mol.isSingleMolecule()) {
            convertMoleculeToDigitalUnits(mol);
        } else {
            for(Molecule detection : mol.getDetections()) {
                convertMoleculeToDigitalUnits(detection);
            }
        }
        return mol;
    }

    @Override
    public Molecule fit(SubImage plane1, SubImage plane2) throws Exception {
        // init
        plane1.convertTo(MoleculeDescriptor.Units.PHOTON);
        plane2.convertTo(MoleculeDescriptor.Units.PHOTON);
        copyObservations(plane1, plane2);
        if((fittedModelValues == null) || (fittedModelValues.length != plane1.values.length + plane2.values.length)) {
            fittedModelValues = new double[plane1.values.length + plane2.values.length];
        }

        // fit
        NelderMead nm = new NelderMead();
        double[] guess = psfModel.transformParametersInverse(getInitialParams(plane1, plane2));
        nm.optimize(getLikelihoodFunction(plane1, plane2),
                NelderMead.Objective.MAXIMIZE, guess, 1e-8, psfModel.getInitialSimplex(), 10, maxIter);
        fittedParameters = nm.xmin;
        // estimate background and return an instance of the `Molecule`
        fittedParameters[Params.BACKGROUND] = VectorMath.stddev(sub(fittedModelValues, observations,
                getValueFunction(plane1, plane2).value(fittedParameters)));

        Molecule mol = psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters), MoleculeDescriptor.Units.PHOTON, true);
        if(mol.isSingleMolecule()) {
            convertMoleculeToDigitalUnits(mol);
        } else {
            for(Molecule detection : mol.getDetections()) {
                convertMoleculeToDigitalUnits(detection);
            }
        }
        return mol;
    }

    public double[] getInitialParams(SubImage plane1, SubImage plane2) throws Exception {
        IBiplanePSFModel model = (IBiplanePSFModel) psfModel;
        if (model == null) throw new Exception("Unknown PSF model for biplane fitting!");
        return model.getInitialParams(plane1, plane2);
    }

    public MultivariateVectorFunction getValueFunction(SubImage plane1, SubImage plane2) throws Exception {
        IBiplanePSFModel model = (IBiplanePSFModel) psfModel;
        if (model == null) throw new Exception("Unknown PSF model for biplane fitting!");
        return model.getValueFunction(plane1.xgrid, plane1.ygrid, plane2.xgrid, plane2.ygrid);
    }

    public MultivariateFunction getLikelihoodFunction(SubImage plane1, SubImage plane2) throws Exception {
        IBiplanePSFModel model = (IBiplanePSFModel) psfModel;
        if (model == null) throw new Exception("Unknown PSF model for biplane fitting!");
        return model.getLikelihoodFunction(plane1.xgrid, plane1.ygrid, plane1.values, plane2.xgrid, plane2.ygrid, plane2.values);
    }

    private void convertMoleculeToDigitalUnits(Molecule mol) {
        for(String param : mol.descriptor.names) {
            MoleculeDescriptor.Units paramUnits = mol.getParamUnits(param);
            MoleculeDescriptor.Units digitalUnits = MoleculeDescriptor.Units.getDigitalUnits(paramUnits);
            if(!digitalUnits.equals(paramUnits)) {
                mol.setParam(param, digitalUnits, mol.getParam(param, digitalUnits));
            }
        }
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
}
