package cz.cuni.lf1.lge.ThunderSTORM.util;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt;
import java.util.List;

public class MoleculeMatcher {

    public List<Molecule> detections;
    public List<Molecule> groundTruth;
    public MoleculeDescriptor.Units distUnits;
    public double dist2Thr;

    public MoleculeMatcher(double dist2Thr, MoleculeDescriptor.Units distUnits) {
        this.dist2Thr = dist2Thr;
        this.distUnits = distUnits;
    }

    /**
     * The method matches detected molecules to the ground-truth data.
     * @param det [in] List of detected molecules.
     * @param gt [in] List of ground-truth molecules.
     * @param TP [out] List of true-positive pairs &lt;ground-truth,detection&gt; (the container must be allocated by caller).
     * @param FP [out] List of false-positive detections (the container must be allocated by caller).
     * @param FN [out] List of false-negative items from ground-truth (the container must be allocated by caller).
     */
    public void matchMolecules(List<Molecule> det, List<Molecule> gt, List<Pair<Molecule, Molecule>> TP, List<Molecule> FP, List<Molecule> FN) {
        if(det == null || gt == null || TP == null || FP == null || FN == null) {
            return;
        }
        this.detections = det;
        this.groundTruth = gt;
        //
        SquareEuclideanDistanceFunction dist_fn = new SquareEuclideanDistanceFunction();
        MaxHeap<Molecule> nn_mol;
        //
        KdTree<Molecule> tree = new KdTree<Molecule>(3);
        for(Molecule mol : gt) {
            tree.addPoint(new double[]{mol.getX(distUnits), mol.getY(distUnits), mol.getZ(distUnits)}, mol);
            FN.add(mol);
            mol.setStatus(Molecule.DetectionStatus.FALSE_NEGATIVE);
        }
        for(Molecule mol : det) {
            nn_mol = tree.findNearestNeighbors(new double[]{mol.getX(distUnits), mol.getY(distUnits), mol.getZ(distUnits)}, 1, dist_fn);
            if(nn_mol.getMaxKey() < dist2Thr) {
                TP.add(new Pair(nn_mol.getMax(), mol));
                FN.remove(nn_mol.getMax());
                mol.setStatus(Molecule.DetectionStatus.TRUE_POSITIVE);
                nn_mol.getMax().setStatus(Molecule.DetectionStatus.TRUE_POSITIVE);
                //
                mol.setParam(MoleculeDescriptor.LABEL_GROUND_TRUTH_ID, nn_mol.getMax().getParam(MoleculeDescriptor.LABEL_ID));
                mol.setParam(MoleculeDescriptor.LABEL_DISTANCE_TO_GROUND_TRUTH, sqrt(nn_mol.getMaxKey()));
            } else {
                FP.add(mol);
                mol.setStatus(Molecule.DetectionStatus.FALSE_POSITIVE);
                //
                mol.setParam(MoleculeDescriptor.LABEL_GROUND_TRUTH_ID, 0);
                mol.setParam(MoleculeDescriptor.LABEL_DISTANCE_TO_GROUND_TRUTH, Double.POSITIVE_INFINITY);
            }
        }
    }

}
