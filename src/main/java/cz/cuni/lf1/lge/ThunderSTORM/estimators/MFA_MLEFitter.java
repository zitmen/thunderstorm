package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MultiPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class MFA_MLEFitter extends MFA_AbstractFitter {

    Range expectedIntensity;
    double pValueThr;
    boolean sameI;
    final static int MODEL_SELECTION_ITERATIONS = 5000; // full fitting takes ~50000 iterations (--> this is 10%)
    
    public MFA_MLEFitter(PSFModel basePsfModel, double defaultSigma, int maxN, double pValueThr, boolean sameI, Range expI) {
        super(basePsfModel, defaultSigma, maxN);
        this.sameI = sameI;
        this.pValueThr = pValueThr;
        this.expectedIntensity = expI;
    }

    @Override
    public Molecule fit(SubImage subimage) {
        double[] fittedParams = null;
        MultiPSF model, modelBest = null;
        double logLik, logLikPrevBest = 0.0, pValue;
        if(maxN > 1) {
            // model selection - how many molecules?
            for(int n = 1; n <= maxN; n++) {
                GUI.checkIJEscapePressed();
                model = new MultiPSF(n, defaultSigma, basePsfModel, fittedParams);
                model.setIntensityRange(expectedIntensity);
                model.setFixedIntensities(sameI);
                MLEFitter fitter = new MLEFitter(model, MODEL_SELECTION_ITERATIONS, Params.BACKGROUND);
                fitter.fit(subimage);
                fittedParams = fitter.fittedParameters;
                logLik = model.getLikelihoodFunction(subimage.xgrid, subimage.ygrid, subimage.values).value(fittedParams);
                if(n > 1) {
                    pValue = 1.0 - new ChiSquaredDistribution(model.getDoF() - modelBest.getDoF()).cumulativeProbability(-2 * (logLikPrevBest - logLik));
                    if(!Double.isNaN(pValue) && (pValue < pValueThr) ) {//&& !isOutOfRegion(mol, ((double)subimage.size) / 2.0)
                        logLikPrevBest = logLik;
                        modelBest = model;
                    }
                } else {
                    logLikPrevBest = logLik;
                    modelBest = model;
                }
            }
        } else {
            modelBest = new MultiPSF(1, defaultSigma, basePsfModel, null);
            modelBest.setIntensityRange(expectedIntensity);
            modelBest.setFixedIntensities(sameI);
        }
        // fitting with the selected model
        MLEFitter fitter = new MLEFitter(modelBest, MLEFitter.MAX_ITERATIONS - MODEL_SELECTION_ITERATIONS, Params.BACKGROUND);
        Molecule mol = fitter.fit(subimage);
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
