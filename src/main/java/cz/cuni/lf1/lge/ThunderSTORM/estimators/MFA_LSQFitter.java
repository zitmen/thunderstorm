package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedDueToErrorException;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MultiPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import org.apache.commons.math3.distribution.FDistribution;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;

public class MFA_LSQFitter extends MFA_AbstractFitter {

    public LSQFitter lastFitter = null;
    Range expectedIntensity;
    boolean weightedLSQ;
    double pValueThr;
    boolean sameI;
    final static int MODEL_SELECTION_ITERATIONS = 50; // full fitting takes ~20 iterations; this is here to limit max. number of iterations, which is set to 1000
    
    public MFA_LSQFitter(PSFModel basePsfModel, double defaultSigma, int maxN, double pValueThr, boolean sameI, Range expI) {
        super(basePsfModel, defaultSigma, maxN);
        this.sameI = sameI;
        this.pValueThr = pValueThr;
        expectedIntensity = expI;
        if(expectedIntensity!= null){
            this.expectedIntensity.convert(MoleculeDescriptor.Units.PHOTON, MoleculeDescriptor.Units.DIGITAL);
        }
    }

    @Override
    public Molecule fit(SubImage subimage) {
        double[] fittedParams = null;
        MultiPSF model, modelBest = null;
        double chi2, chi2Best = 0.0, pValue;
        if(maxN > 1) {
            // model selection - how many molecules?
            for(int n = 1; n <= maxN; n++) {
                GUI.checkIJEscapePressed();
                model = new MultiPSF(n, defaultSigma, basePsfModel, fittedParams);
                model.setIntensityRange(expectedIntensity);
                model.setFixedIntensities(sameI);
                LSQFitter fitter = new LSQFitter(model, weightedLSQ, MODEL_SELECTION_ITERATIONS, -1);
                fitter.fit(subimage);
                fittedParams = fitter.fittedParameters;
                chi2 = model.getChiSquared(subimage.xgrid, subimage.ygrid, subimage.values, fittedParams, weightedLSQ);
                if(n > 1) {
                    try {
                        pValue = 1.0 - new FDistribution(model.getDoF() - modelBest.getDoF(), subimage.values.length - model.getDoF()).cumulativeProbability(((chi2Best - chi2) / (model.getDoF() - modelBest.getDoF())) / (chi2 / (subimage.values.length - model.getDoF())));
                        if(!Double.isNaN(pValue) && (pValue < pValueThr) ) {//&& !isOutOfRegion(mol, ((double)subimage.size) / 2.0)
                            modelBest = model;
                            chi2Best = chi2;
                        }
                    } catch(NotStrictlyPositiveException ex) {
                        int maxMol = (subimage.values.length - 2) / (int)(basePsfModel.getDoF()-2); // both intensity and offset are estimated for all molecules as a single parameter
                        throw new StoppedDueToErrorException(
                                "F-distribution `F(" + (int)(model.getDoF() - modelBest.getDoF()) + "," +
                                (int)(subimage.values.length - model.getDoF()) + ")` could not be created! " +
                                "There is too many molecules (degrees of freedom) in the model!\n The maximum number of " +
                                "molecules with the current settings (PSF model and fitting radius) is " + maxMol + ".", ex);
                    }
                } else {
                    modelBest = model;
                    chi2Best = chi2;
                }
            }
        } else {
            modelBest = new MultiPSF(1, defaultSigma, basePsfModel, null);
            modelBest.setIntensityRange(expectedIntensity);
            modelBest.setFixedIntensities(sameI);
        }
        // fitting with the selected model
        lastFitter = new LSQFitter(modelBest, weightedLSQ, Params.BACKGROUND);
        Molecule mol = lastFitter.fit(subimage);
        assert (mol != null);    // this is implication of `assert(maxN >= 1)`
        if(!mol.isSingleMolecule()) {
            // copy background value to all molecules
            double bkg = mol.getParam(PSFModel.Params.LABEL_BACKGROUND);
            for(Molecule m : mol.getDetections()) {
                m.setParam(PSFModel.Params.LABEL_BACKGROUND, bkg);
            }
        }
        return eliminateBadFits(mol, ((double) subimage.size_x) / 2.0 - defaultSigma / 2.0, ((double) subimage.size_y) / 2.0 - defaultSigma / 2.0);
    }
}
