package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.abs;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MultiPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import org.apache.commons.math3.distribution.FDistribution;

public class MFA_LSQFitter extends MFA_AbstractFitter {
    
    double pValueThr;
    final static int MODEL_SELECTION_ITERATIONS = 50; // full fitting takes ~20 iterations; this is here to limit max. number of iterations, which is set to 1000
    
    public MFA_LSQFitter(PSFModel basePsfModel, double defaultSigma, int maxN, double pValueThr) {
        super(basePsfModel, defaultSigma, maxN);
        this.pValueThr = pValueThr;
    }

    @Override
    public Molecule fit(OneLocationFitter.SubImage subimage) {
        Molecule mol;
        double[] fittedParams = null;
        MultiPSF model = null, modelPrev;
        double chi2 = 0.0, chi2Prev, pValue, prevDoF = 0.0;
        if(maxN > 1) {
            // model selection - how many molecules?
            for(int n = 1; n <= maxN; n++) {
                modelPrev = model;
                model = new MultiPSF(n, defaultSigma, basePsfModel, fittedParams);
                LSQFitter fitter = new LSQFitter(model, MODEL_SELECTION_ITERATIONS);
                mol = fitter.fit(subimage);
                fittedParams = fitter.fittedParameters;
                chi2Prev = chi2;
                chi2 = model.getChiSquared(subimage.xgrid, subimage.ygrid, subimage.values, fittedParams);
                pValue = 1.0 - new FDistribution(model.getDoF() - prevDoF, subimage.values.length - (model.getDoF() + 1)).cumulativeProbability((chi2Prev - chi2) / (chi2 / (double)(subimage.values.length - model.getDoF() - 1)));
                if(n > 1) {
                    if(Double.isNaN(pValue) || (pValue > pValueThr) || isOutOfRegion(mol, ((double)subimage.size) / 2.0)) {
                        model = modelPrev;
                        break;
                    }
                }
                prevDoF = model.getDoF();
            }
        } else {
            model = new MultiPSF(1, defaultSigma, basePsfModel, null);
        }
        // fitting with the selected model
        LSQFitter fitter = new LSQFitter(model);
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
