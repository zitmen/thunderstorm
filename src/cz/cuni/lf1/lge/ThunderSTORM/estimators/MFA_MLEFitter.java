package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MultiPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import org.apache.commons.math3.distribution.ChiSquaredDistribution;

public class MFA_MLEFitter extends MFA_AbstractFitter {
    
    double pValueThr;
    final static int MODEL_SELECTION_ITERATIONS = 1500; // full fitting takes ~15000 iterations (--> this is 10%)
    
    public MFA_MLEFitter(PSFModel basePsfModel, double defaultSigma, int maxN, double pValueThr) {
        super(basePsfModel, defaultSigma, maxN);
        this.pValueThr = pValueThr;
    }

    @Override
    public Molecule fit(OneLocationFitter.SubImage subimage) {
        Molecule mol;
        double[] fittedParams = null;
        MultiPSF model = null, modelPrev;
        double logLik = 0.0, logLikPrev, pValue;
        if(maxN > 1) {
            // model selection - how many molecules?
            for(int n = 1; n <= maxN; n++) {
                modelPrev = model;
                model = new MultiPSF(n, defaultSigma, basePsfModel, fittedParams);
                MLEFitter fitter = new MLEFitter(model, MODEL_SELECTION_ITERATIONS);
                mol = fitter.fit(subimage);
                fittedParams = fitter.fittedParameters;
                logLikPrev = logLik;
                logLik = model.getLikelihoodFunction(subimage.xgrid, subimage.ygrid, subimage.values).value(fittedParams);
                pValue = new ChiSquaredDistribution(model.getDoF() - modelPrev.getDoF()).density(2 * (logLikPrev - logLik));
                if(n > 1) {
                    if(Double.isNaN(pValue) || (pValue > pValueThr)) {
                        model = modelPrev;
                        break;
                    }
                }
            }
        } else {
            model = new MultiPSF(1, defaultSigma, basePsfModel, null);
        }
        // fitting with the selected model
        MLEFitter fitter = new MLEFitter(model);
        mol = fitter.fit(subimage);
        assert (mol != null);    // this is implication of `assert(maxN >= 1)`
        if(!mol.isSingleMolecule()) {
            // copy background value to all molecules
            double bkg = mol.getParam(PSFModel.Params.LABEL_BACKGROUND);
            for(Molecule m : mol.detections) {
                m.setParam(PSFModel.Params.LABEL_BACKGROUND, bkg);
            }
        }
        return eliminateBadFits(mol, ((double) subimage.size) / 2.0 - defaultSigma / 2.0);
    }
}
