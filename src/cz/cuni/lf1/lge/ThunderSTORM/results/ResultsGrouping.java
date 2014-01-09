package cz.cuni.lf1.lge.ThunderSTORM.results;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import ij.IJ;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

class ResultsGrouping extends PostProcessingModule {

    private JTextField distanceTextField;
    private JButton applyButton;
    private JLabel groupThrLabel;

    private ParameterKey.Double distParam;
    
    public ResultsGrouping(ResultsTableWindow table, TripleStateTableModel model) {
        this.table = table;
        this.model = model;
        distParam = params.createDoubleField("dist", DoubleValidatorFactory.positive(), 0);
    }

    @Override
    public String getMacroName() {
        return "merge";
    }

    @Override
    public String getTabName() {
        return "Merging";
    }

    @Override
    protected JPanel createUIPanel() {
        JPanel grouping = new JPanel(new GridBagLayout());
        InputListener listener = new InputListener();
        distanceTextField = new JTextField();
        distanceTextField.addKeyListener(listener);
        distParam.registerComponent(distanceTextField);
        groupThrLabel = new JLabel("Merge molecules in subsequent frames with mutual lateral distance \u2264 [current units of x,y]: ", SwingConstants.TRAILING);
        groupThrLabel.setLabelFor(distanceTextField);
        applyButton = new JButton("Merge");
        applyButton.addActionListener(listener);
        grouping.add(groupThrLabel, new GridBagHelper.Builder().gridxy(0, 0).anchor(GridBagConstraints.WEST).build());
        grouping.add(distanceTextField, new GridBagHelper.Builder().gridxy(0, 1).fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
        grouping.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(1, 0).anchor(GridBagConstraints.EAST).build());
        grouping.add(applyButton, new GridBagHelper.Builder().gridxy(1, 1).build());
        return grouping;
    }

    @Override
    protected void runImpl() {
        final double dist = distParam.getValue();
        if(!applyButton.isEnabled() || (dist == 0)) {
            return;
        }
        distanceTextField.setBackground(Color.WHITE);
        GUI.closeBalloonTip();
        try {
            applyButton.setEnabled(false);
            ResultsGrouping.this.saveStateForUndo(MergingOperation.class);
            
            final int merged = model.getRowCount();
            new SwingWorker() {
                @Override
                protected Object doInBackground() {
                    applyToModel(model, dist);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        int into = model.getRowCount();
                        ResultsGrouping.this.addOperationToHistory(new MergingOperation(dist));

                        table.setStatus(Integer.toString(merged) + " molecules were merged into " + Integer.toString(into) + " molecules");
                        table.showPreview();
                    } catch(InterruptedException ex) {
                    } catch(ExecutionException ex) {
                        IJ.handleException(ex);
                        distanceTextField.setBackground(new Color(255, 200, 200));
                        GUI.showBalloonTip(distanceTextField, ex.getCause().toString());
                    } finally {
                        applyButton.setEnabled(true);
                    }
                }
            }.execute();
        } catch(Exception ex) {
            IJ.handleException(ex);
            distanceTextField.setBackground(new Color(255, 200, 200));
            GUI.showBalloonTip(distanceTextField, ex.toString());
        }
    }
    
    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            run();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyButton.doClick();
            }
        }
    }

    class MergingOperation extends OperationsHistoryPanel.Operation {

        final String name = "Merging";
        double threshold;

        public MergingOperation(double threshold) {
            this.threshold = threshold;
        }

        @Override
        protected String getName() {
            return name;
        }

        @Override
        protected boolean isUndoAble() {
            return true;
        }

        @Override
        protected void clicked() {
            if(uiPanel.getParent() instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) uiPanel.getParent();
                tabbedPane.setSelectedComponent(uiPanel);
            }
            distanceTextField.setText(Double.toString(threshold));
        }

        @Override
        protected void undo() {
            model.swapUndoAndActual();
            table.setStatus("Merging: Undo.");
            table.showPreview();
        }

        @Override
        protected void redo() {
            model.swapUndoAndActual();
            table.setStatus("Merging: Redo.");
            table.showPreview();
        }
    }

    public static void applyToModel(GenericTableModel model, double dist) {
        if(!model.columnExists(PSFModel.Params.LABEL_X) || !model.columnExists(PSFModel.Params.LABEL_Y)) {
            throw new RuntimeException(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y, model.getColumnNames()));
        }
        //
        FrameSequence frames = new FrameSequence();
        for(int i = 0, im = model.getRowCount(); i < im; i++) {
            frames.InsertMolecule(model.getRow(i));
        }
        frames.matchMolecules(sqr(dist));
        //
        // Set new IDs for the new "macro" molecules
        for(Molecule mol : frames.getAllMolecules()) {
            if(!mol.isSingleMolecule()) {
                mol.setParam(MoleculeDescriptor.LABEL_ID, model.getNewId());
            }
        }
        //
        model.reset();
        for(Molecule mol : frames.getAllMolecules()) {
            model.addRow(mol);
        }
    }

    //
    // ===================================================================
    //
    static class FrameSequence {

        // <Frame #, List of Molecules>
        private HashMap<Integer, Vector<Molecule>> detections;
        private Vector<Molecule> molecules;
        private SortedSet frames;

        public FrameSequence() {
            detections = new HashMap<Integer, Vector<Molecule>>();
            molecules = new Vector<Molecule>();
            frames = new TreeSet();
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
                detections.put(frame, new Vector<Molecule>());
            }
            detections.get(frame).add(mol);
            frames.add(frame);
        }

        public Vector<Molecule> getAllMolecules() {
            Collections.sort(molecules);
            return molecules;
        }

        /**
         * The method matches molecules at the same positions lasting for more
         * than just 1 frame.
         *
         * The method works in 3D, thus calculating distance {
         *
         * @mathjax (x_1-x_2)^2+(y_1-y_2)^2+(z_1-z_2)^2}. Note: this method
         * makes changes into `detections`!
         */
        public void matchMolecules(double dist2_thr) {
            molecules.clear();
            Integer[] frno = new Integer[frames.size()];
            frames.toArray(frno);
            SquareEuclideanDistanceFunction dist_fn = new SquareEuclideanDistanceFunction();
            MaxHeap<Molecule> nn_mol;
            for(int fi = 1; fi < frno.length; fi++) {
                Vector<Molecule> fr1mol = detections.get(frno[fi - 1]);
                Vector<Molecule> fr2mol = detections.get(frno[fi]);
                //
                boolean[] selected = new boolean[fr1mol.size()];
                Arrays.fill(selected, false);
                //
                KdTree<Molecule> tree = new KdTree<Molecule>(3);
                for(Molecule mol : fr2mol) {
                    tree.addPoint(new double[]{mol.getX(), mol.getY(), mol.getZ()}, mol);
                }
                for(int si = 0, sim = fr1mol.size(); si < sim; si++) {
                    Molecule mol = fr1mol.get(si);
                    nn_mol = tree.findNearestNeighbors(new double[]{mol.getX(), mol.getY(), mol.getZ()}, 1, dist_fn);
                    if(nn_mol.getMaxKey() < dist2_thr) {
                        nn_mol.getMax().addDetection(mol);
                        nn_mol.getMax().updateParameters();
                        selected[si] = true;
                    }
                }
                // store the not-selected molecules as real ones
                for(int si = 0; si < selected.length; si++) {
                    if(selected[si] == false) {
                        molecules.add(fr1mol.get(si));
                    }
                }
            }
            // at the end store all the molecules from the last frame
            for(Molecule mol : detections.get(frno[frno.length - 1])) {
                molecules.add(mol);
            }
        }
    }
}
