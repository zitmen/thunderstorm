package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.results.GenericTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.max;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt;
import cz.cuni.lf1.lge.ThunderSTORM.util.MoleculeMatcher;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.DialogStub;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.StringValidatorFactory;
import cz.vutbr.web.css.TermNumeric;
import ij.IJ;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.*;

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
            PerformanceEvaluationDialog dialog = new PerformanceEvaluationDialog();
            if(MacroParser.isRanFromMacro()) {
                dialog.getParams().readMacroOptions();
            } else {
                if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                    return;
                }
            }
            runEvaluation(dialog.getEvaluationSpace().equals("xyz"), sqr(dialog.getToleranceRadius()), Units.NANOMETER);
        } catch (Exception ex) {
            IJ.handleException(ex);
        }
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

    private void runEvaluation(boolean threeD, double dist2Tol, Units distUnits) {
        //
        int cores = Runtime.getRuntime().availableProcessors();
        MoleculeMatcherWorker [] matchers = new MoleculeMatcherWorker[cores];
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
                matchers[c] = new MoleculeMatcherWorker(f_start, f_end, threeD, dist2Tol, distUnits);
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
        for(MoleculeMatcherWorker matcher : matchers) {
            tp += (double) matcher.TP.size();
            fp += (double) matcher.FP.size();
            fn += (double) matcher.FN.size();
            TP.addAll(matcher.TP);
        }
        double jaccard = tp / (tp + fp + fn);
        double precision = tp / (tp + fp);
        double recall = tp / (tp + fn);
        double F1 = 2 * precision * recall / (precision + recall);
        double calcRMSEx = calcRMSEx(TP, distUnits);
        double calcRMSEy = calcRMSEy(TP, distUnits);
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
        rt.addValue("RMSE x [" + distUnits.getLabel() + "]", calcRMSEx);
        rt.addValue("RMSE y [" + distUnits.getLabel() + "]", calcRMSEy);
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

    private double calcRMSExyz(List<Pair<Molecule, Molecule>> pairs, Units units) {
        double rmse = 0.0;
        for(Pair<Molecule,Molecule> pair : pairs) {
            rmse += pair.first.dist2xyz(pair.second, units);
        }
        return sqrt(rmse / (double)pairs.size());
    }
    
    private double calcRMSExy(List<Pair<Molecule, Molecule>> pairs, Units units) {
        double rmse = 0.0;
        for(Pair<Molecule,Molecule> pair : pairs) {
            rmse += pair.first.dist2xy(pair.second, units);
        }
        return sqrt(rmse / (double)pairs.size());
    }
    
    private double calcRMSEz(List<Pair<Molecule, Molecule>> pairs, Units units) {
        double rmse = 0.0;
        for(Pair<Molecule,Molecule> pair : pairs) {
            rmse += pair.first.dist2z(pair.second, units);
        }
        return sqrt(rmse / (double)pairs.size());
    }
    
    private double calcRMSEx(List<Pair<Molecule, Molecule>> pairs, Units units){
        double residualSumOfSquares = 0;
        for(Pair<Molecule,Molecule> pair : pairs) {
            residualSumOfSquares += sqr(pair.first.getX(units) - pair.second.getX(units));
        }
        return sqrt(residualSumOfSquares / (double)pairs.size());
    }
    
    private double calcRMSEy(List<Pair<Molecule, Molecule>> pairs, Units units){
        double residualSum = 0;
        for(Pair<Molecule,Molecule> pair : pairs) {
            residualSum += sqr(pair.first.getY(units) - pair.second.getY(units));
        }
        return sqrt(residualSum / (double)pairs.size());
    }
    
    private synchronized void processingNewFrame(String message) {
        IJ.showStatus(String.format(message, processingFrame, frames));
        IJ.showProgress((double)(processingFrame) / (double)frames);
        processingFrame++;
    }

    private int getFrameCount() {
        return (int) max(VectorMath.max(IJResultsTable.getResultsTable().getColumnAsDoubles(MoleculeDescriptor.LABEL_FRAME)),
            VectorMath.max(IJGroundTruthTable.getGroundTruthTable().getColumnAsDoubles(MoleculeDescriptor.LABEL_FRAME)));
    }
    
    // ------------------------------------------------------------------------
    
    final class MoleculeMatcherWorker implements Runnable {

        // <Frame #, List of Molecules>
        public Map<Integer, List<Molecule>> detections;
        public Map<Integer, List<Molecule>> groundTruth;
        private final MoleculeMatcher matcher;
        public SortedSet<Integer> frames;
        public Vector<Pair<Molecule, Molecule>> TP; // <ground-truth, detection>
        public Vector<Molecule> FP, FN;

        public MoleculeMatcherWorker(int frameStart, int frameStop, boolean threeD, double dist2Thr, Units distUnits) {
            this.frames = new TreeSet<Integer>();
            this.detections = fillWithData(frameStart, frameStop, IJResultsTable.getResultsTable());
            this.groundTruth = fillWithData(frameStart, frameStop, IJGroundTruthTable.getGroundTruthTable());
            this.matcher = new MoleculeMatcher(threeD, dist2Thr, distUnits);
            //
            this.TP = new Vector<Pair<Molecule, Molecule>>();
            this.FP = new Vector<Molecule>();
            this.FN = new Vector<Molecule>();
        }
        
        @Override
        public void run() {
            for(Integer frame : frames) {
                if(Thread.interrupted()) {
                    return;
                }
                processingNewFrame("ThunderSTORM is evaluating frame %d out of %d...");
                matcher.matchMolecules(detections.get(frame), groundTruth.get(frame), TP, FP, FN);
            }
        }
        
        private Map<Integer, List<Molecule>> fillWithData(int frameStart, int frameStop, GenericTable table) {
            Map<Integer, List<Molecule>> framesMolList = new HashMap<Integer, List<Molecule>>();
            for(int i = 0, im = table.getRowCount(); i < im; i++) {
                Molecule mol = table.getRow(i);
                int frame = (int)mol.getParam(MoleculeDescriptor.LABEL_FRAME);
                if((frame < frameStart) || (frame > frameStop)) continue;
                if(!framesMolList.containsKey(frame)) {
                    framesMolList.put(frame, new Vector<Molecule>());
                    frames.add(frame);
                }
                framesMolList.get(frame).add(mol);
            }
            return framesMolList;
        }
    }
    
    //---------------GUI-----------------------
    class PerformanceEvaluationDialog extends DialogStub {

        ParameterKey.Double toleranceRadius;
        ParameterKey.String evaluationSpace;
        
        public PerformanceEvaluationDialog() {
            super(new ParameterTracker("thunderstorm.evaluation"), IJ.getInstance(), "ThunderSTORM: Performance evaluation");
            toleranceRadius = params.createDoubleField("toleranceRadius", DoubleValidatorFactory.positiveNonZero(), 50.0);
            evaluationSpace = params.createStringField("evaluationSpace", null, "xy");
        }

        public ParameterTracker getParams() {
            return params;
        }
        
        public double getToleranceRadius() {
            return toleranceRadius.getValue();
        }

        public String getEvaluationSpace() {
            return evaluationSpace.getValue();
        }

        @Override
        protected void layoutComponents() {
            JTextField toleranceTextField = new JTextField(20);
            toleranceRadius.registerComponent(toleranceTextField);

            add(new JLabel("Pair molecules with tolerance in:"), GridBagHelper.leftCol());
            ButtonGroup btnGroup = new ButtonGroup();
            JRadioButton rbXY = new JRadioButton("xy");
            JRadioButton rbXYZ = new JRadioButton("xyz");
            btnGroup.add(rbXY);
            btnGroup.add(rbXYZ);
            params.registerComponent(evaluationSpace, btnGroup);
            add(rbXY, GridBagHelper.rightCol());
            add(Box.createHorizontalGlue(), GridBagHelper.leftCol());
            add(rbXYZ, GridBagHelper.rightCol());

            add(new JLabel("Tolerance radius [nm]:"), GridBagHelper.leftCol());
            add(toleranceTextField, GridBagHelper.rightCol());
            add(Box.createVerticalStrut(10), GridBagHelper.twoCols());

            JPanel buttons = new JPanel(new GridBagLayout());
            buttons.add(createDefaultsButton());
            buttons.add(Box.createHorizontalGlue(), new GridBagHelper.Builder()
                    .fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
            buttons.add(Help.createHelpButton(PerformanceEvaluationPlugIn.class));
            buttons.add(createOKButton());
            buttons.add(createCancelButton());
            add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
            add(buttons, GridBagHelper.twoCols());

            params.loadPrefs();
            getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            setLocationRelativeTo(null);
            setModal(true);
        }
    }

}
