package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MultiPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import org.apache.commons.math3.distribution.FDistribution;

public class MFA_LSQFitter extends MFA_AbstractFitter {

    Range expectedIntensity;
    double pValueThr;
    boolean sameI;
    final static int MODEL_SELECTION_ITERATIONS = 50; // full fitting takes ~20 iterations; this is here to limit max. number of iterations, which is set to 1000
    
    public MFA_LSQFitter(PSFModel basePsfModel, double defaultSigma, int maxN, double pValueThr, boolean sameI, Range expI) {
        super(basePsfModel, defaultSigma, maxN);
        this.sameI = sameI;
        this.pValueThr = pValueThr;
        this.expectedIntensity = expI;
    }

    @Override
    public Molecule fit(OneLocationFitter.SubImage subimage) {
        Molecule mol;
        double[] fittedParams = null;
        MultiPSF model, modelBest = null;
        double chi2, chi2Best = 0.0, pValue;
        if(maxN > 1) {
            // model selection - how many molecules?
            for(int n = 1; n <= maxN; n++) {
                model = new MultiPSF(n, defaultSigma, basePsfModel, fittedParams);
                model.setIntensityRange(expectedIntensity);
                model.setFixedIntensities(sameI);
                LSQFitter fitter = new LSQFitter(model, MODEL_SELECTION_ITERATIONS);
                mol = fitter.fit(subimage);
                fittedParams = fitter.fittedParameters;
                chi2 = model.getChiSquared(subimage.xgrid, subimage.ygrid, subimage.values, fittedParams);
                if(n > 1) {
                    pValue = 1.0 - new FDistribution(model.getDoF() - modelBest.getDoF(), subimage.values.length - (model.getDoF() + 1)).cumulativeProbability((chi2Best - chi2) / (chi2 / (double)(subimage.values.length - model.getDoF() - 1)));
                    if(!Double.isNaN(pValue) && (pValue < pValueThr) && !isOutOfRegion(mol, ((double)subimage.size) / 2.0)) {
                        modelBest = model;
                        chi2Best = chi2;
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
        LSQFitter fitter = new LSQFitter(modelBest);
        mol = fitter.fit(subimage);
        assert (mol != null);    // this is implication of `assert(maxN >= 1)`
        if(!mol.isSingleMolecule()) {
            // copy background value to all molecules
            double bkg = mol.getParam(PSFModel.Params.LABEL_BACKGROUND);
            for(Molecule m : mol.detections) {
                m.setParam(PSFModel.Params.LABEL_BACKGROUND, bkg);
            }
        }
        return eliminateBadFits(mol, ((double)subimage.size) / 2.0 - defaultSigma / 2.0);
    }
}
