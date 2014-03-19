package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import ij.IJ;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import net.sf.javaml.core.kdtree.KDTree;
import net.sf.javaml.core.kdtree.KeyDuplicateException;
import net.sf.javaml.core.kdtree.KeySizeException;

//
// ===================================================================
public class FrameSequence {

    // <Frame #, List of Molecules>
    private final HashMap<Integer, List<Molecule>> detections;
    private final List<Molecule> molecules;
    private final SortedSet<Integer> frames;

    public FrameSequence() {
        detections = new HashMap<Integer, List<Molecule>>();
        molecules = new ArrayList<Molecule>();
        frames = new TreeSet<Integer>();
    }

    public void InsertMolecule(Molecule mol) {
        int frame = (int) mol.getParam(MoleculeDescriptor.LABEL_FRAME);
        // molecule itself has to be added to the list of detections,
        // because the parameters can change during the merging
        mol.addDetection(mol.clone(mol.descriptor));
        mol.updateParameters();
        mol.addParam(MoleculeDescriptor.LABEL_DETECTIONS, MoleculeDescriptor.Units.UNITLESS, 1);
        //
        if(!detections.containsKey(frame)) {
            detections.put(frame, new ArrayList<Molecule>());
        }
        detections.get(frame).add(mol);
        frames.add(frame);
    }

    public List<Molecule> getAllMolecules() {
        Collections.sort(molecules);
        return molecules;
    }

    /**
     * The method matches molecules at the same positions lasting for more than
     * just 1 frame. Maximum number of frames between two detections to be still
     * considered as one molecule can be specifed(offFramesThr).
     *
     * The method works in 3D - calculating weighted squared euclidean distance.
     * The weight of z in the distance is specified in zCoordWeight parameter. X
     * and Y weights are not adjustable. Note: this method makes changes into
     * `detections`!
     */
    public void matchMolecules(double dist2_thr,
            OffFramesThresholdStrategy off_thr,
            ActiveMoleculePositionStrategy positionStrategy,
            double zCoordWeight) {
        molecules.clear();
        List<Molecule> activeMolecules = new ArrayList<Molecule>();
        List<Molecule> activeMoleculesTemp = new ArrayList<Molecule>();
        for(int frame : frames) {
            List<Molecule> fr2mol = detections.get(frame);
            //
            KDTree<Molecule> tree = new KDTree<Molecule>(3);
            try {
                //build tree from active detections
                for(Molecule mol : activeMolecules) {
                    try {
                        Molecule lastMol = positionStrategy.getActiveMoleculePosition(mol);
                        //key in the tree is the coords of the molecule from last frame, but the object stored is the parent molecule
                        tree.insert(new double[]{lastMol.getX(), lastMol.getY(), zCoordWeight * lastMol.getZ()}, mol);
                    } catch(KeyDuplicateException ex) {
                        IJ.handleException(ex); // almost never happens...if it does, somethin is wrong with fitting/detection
                    }
                }
                boolean emptyTree = activeMolecules.isEmpty();
                Molecule nn_mol;
                for(Molecule mol : fr2mol) {
                    if(!emptyTree) {
                        nn_mol = tree.nearest(new double[]{mol.getX(), mol.getY(), zCoordWeight * mol.getZ()});
                        Molecule lastAddedMol = positionStrategy.getActiveMoleculePosition(nn_mol);
                        if(squareDistWeightedZ(mol, lastAddedMol, zCoordWeight) < dist2_thr) {
                            nn_mol.addDetection(mol.getDetections().get(0));
                            nn_mol.updateParameters();
                            continue;
                        }
                    }
                    activeMolecules.add(mol);
                    molecules.add(mol);
                }
                //remove from activeMolecules those, that were off for more than offFramesThr
                activeMoleculesTemp.clear();
                for(Molecule mol : activeMolecules) {
                    if(frame - getLastAddedChildMolecule(mol).getParam(MoleculeDescriptor.LABEL_FRAME) <= off_thr.getMaxOffFrames(mol)) {
                        activeMoleculesTemp.add(mol);
                    }
                }
                List<Molecule> pom = activeMolecules;
                activeMolecules = activeMoleculesTemp;
                activeMoleculesTemp = pom;
            } catch(KeySizeException ex) {
                // never happens
            }
        }
    }

    /**
     * return the molecule that was last added to the input molecule or the
     * input molecule if it is a single molecule
     */
    private static Molecule getLastAddedChildMolecule(Molecule parent) {
        if(parent.isSingleMolecule()) {
            return parent;
        } else {
            return parent.getDetections().get(parent.getDetectionsCount() - 1);
        }
    }

    private static double squareDistWeightedZ(Molecule m1, Molecule m2, double zWeight) {
        return MathProxy.sqr(m1.getX() - m2.getX()) + MathProxy.sqr(m1.getY() - m2.getY()) + MathProxy.sqr(zWeight) * MathProxy.sqr(m1.getZ() - m2.getZ());
    }

    public interface ActiveMoleculePositionStrategy {

        Molecule getActiveMoleculePosition(Molecule mol);
    }

    public static class LastFewDetectionsMean implements ActiveMoleculePositionStrategy {

        int few;
        private static final MoleculeDescriptor simpleMolDesc = new MoleculeDescriptor(new String[]{PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y, PSFModel.Params.LABEL_Z});

        public LastFewDetectionsMean(int few) {
            this.few = few;
        }

        @Override
        public Molecule getActiveMoleculePosition(Molecule macroMolecule) {
            List<Molecule> detections = macroMolecule.getDetections();
            double sumX = 0;
            double sumY = 0;
            double sumZ = 0;
            int count = 0;
            for(int i = detections.size() - 1; i >= 0 && i > (detections.size() - 1 - few); i--) {
                Molecule detection = detections.get(i);
                sumX += detection.getX();
                sumY += detection.getY();
                sumZ += detection.getZ();
                count++;
            }
            double x = sumX / count;
            double y = sumY / count;
            double z = sumZ / count;
            return new Molecule(simpleMolDesc, new double[]{x, y, z});
        }
    }

    public static class LastDetection implements ActiveMoleculePositionStrategy {

        @Override
        public Molecule getActiveMoleculePosition(Molecule mol) {
            return getLastAddedChildMolecule(mol);
        }
    }

    public static class Centroid implements ActiveMoleculePositionStrategy {

        @Override
        public Molecule getActiveMoleculePosition(Molecule mol) {
            mol.updateParameters();
            return mol;
        }
    }

    public interface OffFramesThresholdStrategy {

        double getMaxOffFrames(Molecule activeMolecule);
    }

    public static class Fixed implements OffFramesThresholdStrategy {

        double threshold;

        public Fixed(double threshold) {
            this.threshold = threshold;
        }

        @Override
        public double getMaxOffFrames(Molecule activeMolecule) {
            return threshold;
        }
    }

    public static class RelativeToDetectionCount implements OffFramesThresholdStrategy {

        double ratio;

        public RelativeToDetectionCount(double ratio) {
            this.ratio = ratio;
        }

        @Override
        public double getMaxOffFrames(Molecule activeMolecule) {
            return activeMolecule.getDetectionsCount() * ratio;
        }

    }
}
