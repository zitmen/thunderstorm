package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.optimizers.NelderMead;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sub;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.stddev;

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
        //convert to photons
        subimage.convertTo(MoleculeDescriptor.Units.PHOTON);

        if((fittedModelValues == null) || (fittedModelValues.length < subimage.values.length)) {
            fittedModelValues = new double[subimage.values.length];
        }
        
        NelderMead nm = new NelderMead();
        double[] guess = psfModel.transformParametersInverse(psfModel.getInitialParams(subimage));
        nm.optimize(psfModel.getLikelihoodFunction(subimage.xgrid, subimage.ygrid, subimage.values),
                NelderMead.Objective.MAXIMIZE, guess, 1e-8, psfModel.getInitialSimplex(), 10, maxIter);
        fittedParameters = nm.xmin;

        // estimate background and return an instance of the `Molecule`
        fittedParameters[Params.BACKGROUND] = stddev(sub(fittedModelValues, subimage.values,
                psfModel.getValueFunction(subimage.xgrid, subimage.ygrid).value(fittedParameters)));

        Molecule mol = psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters), subimage.units);
        
        if(mol.isSingleMolecule()){
            convertMoleculeToDigitalUnits(mol);
        }else{
            for(Molecule detection : mol.getDetections()){
                convertMoleculeToDigitalUnits(detection);
            }
        }
        return mol;
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
}