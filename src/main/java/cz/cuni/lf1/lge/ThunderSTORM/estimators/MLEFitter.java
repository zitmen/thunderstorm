package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.optimizers.NelderMead;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;

public class MLEFitter implements IOneLocationFitter, IOneLocationBiplaneFitter {

    public final static int MAX_ITERATIONS = 50000;

    public double[] fittedParameters;
    public PSFModel psfModel;

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
        this.maxIter = maxIter;
        this.fittedParameters = null;
    }

    @Override
    public Molecule fit(SubImage subimage) {
        subimage.convertTo(MoleculeDescriptor.Units.PHOTON);
        return fit(new LsqMleSinglePlaneFunctions(psfModel, subimage));
    }

    @Override
    public Molecule fit(SubImage plane1, SubImage plane2) {
        plane1.convertTo(MoleculeDescriptor.Units.PHOTON);
        plane2.convertTo(MoleculeDescriptor.Units.PHOTON);
        return fit(new LsqMleBiplaneFunctions(psfModel, plane1, plane2));
    }

    public Molecule fit(IMleFunctions functions) {
        // init
        double[] observations = functions.getObservations();

        // fit
        NelderMead nm = new NelderMead();
        nm.optimize(functions.getLikelihoodFunction(), NelderMead.Objective.MAXIMIZE,
                psfModel.transformParametersInverse(functions.getInitialParams()),
                1e-8, psfModel.getInitialSimplex(), 10, maxIter);
        fittedParameters = nm.xmin;

        // estimate background and return an instance of the `Molecule`
        fittedParameters[Params.BACKGROUND] = VectorMath.stddev(VectorMath.sub(observations, functions.getValueFunction().value(fittedParameters)));

        Molecule mol = psfModel.newInstanceFromParams(psfModel.transformParameters(fittedParameters), functions.getImageUnits(), true);

        if(mol.isSingleMolecule()) {
            convertMoleculeToDigitalUnits(mol);
        } else {
            for(Molecule detection : mol.getDetections()) {
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
