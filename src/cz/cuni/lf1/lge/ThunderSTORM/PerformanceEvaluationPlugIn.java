package cz.cuni.lf1.lge.ThunderSTORM;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqrt;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.max;
import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class PerformanceEvaluationPlugIn implements PlugIn {
    
    private int processingFrame;
    private int frames;
    
    @Override
    public void run(String command) {
        GUI.setLookAndFeel();
        //
        if("showGroundTruthTable".equals(command)) {
            IJGroundTruthTable.getGroundTruthTable().show();
            return;
        }
        if(!IJResultsTable.isResultsWindow() || !IJGroundTruthTable.isGroundTruthWindow()) {
            IJ.error("Requires `" + IJResultsTable.IDENTIFIER + "` and `" + IJGroundTruthTable.IDENTIFIER + "` windows open!");
            return;
        }
        if(IJResultsTable.getResultsTable().isEmpty() || IJGroundTruthTable.getGroundTruthTable().isEmpty()) {
            IJ.error("Neither `" + IJResultsTable.IDENTIFIER + "` or `" + IJGroundTruthTable.IDENTIFIER + "` table can't be empty!");
            return;
        }
        //
        try {
            // Create and show the dialog
            GenericDialogPlus gd = new GenericDialogPlus("ThunderSTORM: Performance evaluation");
            gd.addNumericField("Tolerance radius [nm]: ", 50, 0);
            gd.showDialog();
            
            if(!gd.wasCanceled()) {
                double dist = readParams(gd);
                runEvaluation(dist*dist, Units.NANOMETER);
            }
        } catch (Exception ex) {
            IJ.handleException(ex);
        }
    }
    
    private double readParams(GenericDialogPlus gd) {
        return gd.getNextNumber();
    }
    
    private void prepareResultsTable(Units units) {
        try {
            // insert the new columns before parallel processing starts
            IJResultsTable rt = IJResultsTable.getResultsTable();
            MoleculeDescriptor descriptor = rt.getDescriptor();
            int lastIndex = rt.getRow(0).values.size();
            descriptor.addParam(MoleculeDescriptor.LABEL_GROUND_TRUTH_ID, lastIndex, Units.UNITLESS);
            descriptor.addParam(MoleculeDescriptor.LABEL_DISTANCE_TO_GROUND_TRUTH, lastIndex+1, units);
        } catch(Exception ex) {
            //
        }
    }

    private void runEvaluation(double dist2Tol, Units distUnits) {
        //
        int cores = Runtime.getRuntime().availableProcessors();
        MoleculeMatcher [] matchers = new MoleculeMatcher[cores];
        Thread [] threads = new Thread[cores];
        processingFrame = 1;
        frames = getFrameCount();
        prepareResultsTable(distUnits);
        try {
            // prepare the workers and allocate resources for all the threads
            for(int c = 0, f_start = 0, f_end, f_inc = frames / cores; c < cores; c++) {
                if((c+1) < cores) {
                    f_end = f_start + f_inc;
                } else {
                    f_end = frames;
                }
                matchers[c] = new MoleculeMatcher(f_start, f_end, dist2Tol, distUnits);
                threads[c] = new Thread(matchers[c]);
                f_start = f_end + 1;
            }
            // start all the workers
            for(int c = 0; c < cores; c++) {
                threads[c].start();
            }
            // wait for all the workers to finish
            int wait = 1000 / cores;    // max 1s
            boolean finished = false;
            while(!finished) {
                finished = true;
                for(int c = 0; c < cores; c++) {
                    threads[c].join(wait);
                    finished &= !threads[c].isAlive();   // all threads must not be alive to finish!
                }
                if(IJ.escapePressed()) {    // abort?
                    // stop the workers
                    for(int ci = 0; ci < cores; ci++) {
                        threads[ci].interrupt();
                    }
                    // wait so the message below is not overwritten by any of the threads
                    for(int ci = 0; ci < cores; ci++) {
                        threads[ci].join();
                    }
                    // show info and exit the plugin
                    IJ.showProgress(1.0);
                    IJ.showStatus("Operation has been aborted by user!");
                    return;
                }
            }
        } catch(InterruptedException ex) {
            //
        }
        //
        IJ.showProgress(1.0);
        IJ.showStatus("Gathering results...");
        //
        Vector<Pair<Molecule,Molecule>> TP = new Vector<Pair<Molecule,Molecule>>();
        double tp = 0.0, fp = 0.0, fn = 0.0;
        for(int i = 0; i < matchers.length; i++) {
            tp += (double)matchers[i].TP.size();
            fp += (double)matchers[i].FP.size();
            fn += (double)matchers[i].FN.size();
            TP.addAll(matchers[i].TP);
        }
        double jaccard = tp / (tp + fp + fn);
        double precision = tp / (tp + fp);
        double recall = tp / (tp + fn);
        double F1 = 2 * precision * recall / (precision + recall);
        double RMSExy = calcRMSExy(TP, distUnits);
        double RMSEz = calcRMSEz(TP, distUnits);
        double RMSExyz = calcRMSExyz(TP, distUnits);
        //
        ResultsTable rt = ResultsTable.getResultsTable();
        rt.incrementCounter();
        rt.addValue("Distance radius [" + distUnits.getLabel()+ "]", sqrt(dist2Tol));
        rt.addValue("# of TP", tp);
        rt.addValue("# of FP", fp);
        rt.addValue("# of FN", fn);
        rt.addValue("Jaccard index", jaccard);
        rt.addValue("precision", precision);
        rt.addValue("recall", recall);
        rt.addValue("F1-measure", F1);
        rt.addValue("RMSE lateral [" + distUnits.getLabel() + "]", RMSExy);
        rt.addValue("RMSE axial [" + distUnits.getLabel() + "]", RMSEz);
        rt.addValue("RMSE total [" + distUnits.getLabel() + "]", RMSExyz);
        rt.show("Results");
        //
        IJResultsTable.getResultsTable().fireStructureChanged();
        IJResultsTable.getResultsTable().fireDataChanged();
        IJGroundTruthTable.getGroundTruthTable().fireDataChanged();
        //
        IJ.showStatus("");
    }

    private double calcRMSExyz(Vector<Pair<Molecule, Molecule>> pairs, Units units) {
        double rmse = 0.0;
        for(Pair<Molecule,Molecule> pair : pairs) {
            rmse += pair.first.dist2xyz(pair.second, units);
        }
        return sqrt(rmse / (double)pairs.size());
    }
    
    private double calcRMSExy(Vector<Pair<Molecule, Molecule>> pairs, Units units) {
        double rmse = 0.0;
        for(Pair<Molecule,Molecule> pair : pairs) {
            rmse += pair.first.dist2xy(pair.second, units);
        }
        return sqrt(rmse / (double)pairs.size());
    }
    
    private double calcRMSEz(Vector<Pair<Molecule, Molecule>> pairs, Units units) {
        double rmse = 0.0;
        for(Pair<Molecule,Molecule> pair : pairs) {
            rmse += pair.first.dist2z(pair.second, units);
        }
        return sqrt(rmse / (double)pairs.size());
    }
    
    private synchronized void processingNewFrame(String message) {
        IJ.showStatus(String.format(message, processingFrame, frames));
        IJ.showProgress((double)(processingFrame) / (double)frames);
        processingFrame++;
    }

    private int getFrameCount() {
        return (int) max(max(IJResultsTable.getResultsTable().getColumnAsDoubles(MoleculeDescriptor.LABEL_FRAME)),
            max(IJGroundTruthTable.getGroundTruthTable().getColumnAsDoubles(MoleculeDescriptor.LABEL_FRAME)));
    }
    
    // ------------------------------------------------------------------------
    
    final class MoleculeMatcher implements Runnable {

        // <Frame #, List of Molecules>
        public HashMap<Integer, Vector<Molecule>> detections;
        public HashMap<Integer, Vector<Molecule>> groundTruth;
        public Vector<Pair<Molecule, Molecule>> TP; // <ground-truth, detection>
        public Vector<Molecule> FP, FN;
        public SortedSet<Integer> frames;
        private double dist2Thr;
        private Units distUnits;

        public MoleculeMatcher(int frameStart, int frameStop, double dist2Thr, Units distUnits) {
            this.detections = new HashMap<Integer, Vector<Molecule>>();
            this.groundTruth = new HashMap<Integer, Vector<Molecule>>();
            this.frames = new TreeSet<Integer>();
            this.TP = new Vector<Pair<Molecule, Molecule>>();
            this.FP = new Vector<Molecule>();
            this.FN = new Vector<Molecule>();
            //
            this.dist2Thr = dist2Thr;
            this.distUnits = distUnits;
            //
            fillDetections(frameStart, frameStop);
            fillGroundTruth(frameStart, frameStop);
        }
        
        @Override
        public void run() {
            matchMolecules(dist2Thr, distUnits);
        }

        private void fillDetections(int frameStart, int frameStop) {
            IJResultsTable rt = IJResultsTable.getResultsTable();
            for(int i = 0, im = rt.getRowCount(); i < im; i++) {
                Molecule mol = rt.getRow(i);
                int frame = (int)mol.getParam(MoleculeDescriptor.LABEL_FRAME);
                if((frame < frameStart) || (frame > frameStop)) continue;
                if(!detections.containsKey(frame)) {
                    detections.put(frame, new Vector<Molecule>());
                    frames.add(frame);
                }
                detections.get(frame).add(mol);
            }
        }
        
        private void fillGroundTruth(int frameStart, int frameStop) {
            IJGroundTruthTable rt = IJGroundTruthTable.getGroundTruthTable();
            for(int i = 0, im = rt.getRowCount(); i < im; i++) {
                Molecule mol = rt.getRow(i);
                int frame = (int)mol.getParam(MoleculeDescriptor.LABEL_FRAME);
                if((frame < frameStart) || (frame > frameStop)) continue;
                if(!groundTruth.containsKey(frame)) {
                    groundTruth.put(frame, new Vector<Molecule>());
                    frames.add(frame);
                }
                groundTruth.get(frame).add(mol);
            }
        }

        /**
         * The method matches detected molecules to the ground-truth data.
         */
        private void matchMolecules(double dist2_thr, Units distUnits) {
            SquareEuclideanDistanceFunction dist_fn = new SquareEuclideanDistanceFunction();
            MaxHeap<Molecule> nn_mol;
            for(Integer frame : frames) {
                if(Thread.interrupted()) {
                    return;
                }
                processingNewFrame("ThunderSTORM is evaluating frame %d out of %d...");
                //
                Vector<Molecule> det = detections.get(frame);
                Vector<Molecule> gt = groundTruth.get(frame);
                //
                if(det == null || gt == null) continue;
                //
                KdTree<Molecule> tree = new KdTree<Molecule>(3);
                for(Molecule mol : gt) {
                    tree.addPoint(new double[]{mol.getX(distUnits), mol.getY(distUnits), mol.getZ(distUnits)}, mol);
                    FN.add(mol);
                    mol.setStatus(Molecule.DetectionStatus.FALSE_NEGATIVE);
                }
                for(Molecule mol : det) {
                    nn_mol = tree.findNearestNeighbors(new double[]{mol.getX(distUnits), mol.getY(distUnits), mol.getZ(distUnits)}, 1, dist_fn);
                    if(nn_mol.getMaxKey() < dist2_thr) {
                        TP.add(new Pair(nn_mol.getMax(), mol));
                        FN.remove(nn_mol.getMax());
                        mol.setStatus(Molecule.DetectionStatus.TRUE_POSITIVE);
                        nn_mol.getMax().setStatus(Molecule.DetectionStatus.TRUE_POSITIVE);
                        tree.removePoint(nn_mol);
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
    }

}
