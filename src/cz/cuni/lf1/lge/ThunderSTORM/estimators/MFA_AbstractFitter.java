package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.abs;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import java.util.Vector;

abstract public class MFA_AbstractFitter implements OneLocationFitter {

    double defaultSigma;
    PSFModel basePsfModel;
    int maxN;

    public MFA_AbstractFitter(PSFModel basePsfModel, double defaultSigma, int maxN) {
        assert(maxN >= 1);
        //
        this.defaultSigma = defaultSigma;
        this.basePsfModel = basePsfModel;
        this.maxN = maxN;
    }

    // get rid of the molecules close to the fiting region boundary
    protected Molecule eliminateBadFits(Molecule mol, double maxXY) {
        Vector<Molecule> detections = new Vector<Molecule>();
        for(Molecule m : mol.detections) {
            if((abs(m.getX()) <= maxXY) || (abs(m.getY()) <= maxXY)) {
                detections.add(m);
            }
        }
        mol.detections = detections;
        return mol;
    }
    
    protected boolean isOutOfRegion(Molecule mol, double maxXY) {
        for(Molecule m : mol.detections) {
            if((abs(m.getX()) > maxXY) || (abs(m.getY()) > maxXY)) {
                return true;
            }
        }
        return false;
    }
}
