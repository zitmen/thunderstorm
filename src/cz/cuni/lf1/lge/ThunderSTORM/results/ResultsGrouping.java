package cz.cuni.lf1.lge.ThunderSTORM.results;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
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

class ResultsGrouping {

    private JavaTableWindow table;
    private TripleStateTableModel model;
    private JPanel grouping;
    private JTextField distanceTextField;
    private JButton applyButton;

    public ResultsGrouping(JavaTableWindow table, TripleStateTableModel model) {
        this.table = table;
        this.model = model;
    }

    public JPanel createUIPanel() {
        grouping = new JPanel(new GridBagLayout());
        InputListener listener = new InputListener();
        distanceTextField = new JTextField();
        distanceTextField.addKeyListener(listener);
        JLabel groupThrLabel = new JLabel("Merge molecules in subsequent frames with mutual lateral distance equal or less than: ", SwingConstants.TRAILING);
        groupThrLabel.setLabelFor(distanceTextField);
        applyButton = new JButton("Merge");
        applyButton.addActionListener(listener);
        grouping.add(groupThrLabel);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        grouping.add(distanceTextField, gbc);
        grouping.add(applyButton);
        return grouping;
    }

    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            runGrouping(distanceTextField.getText().isEmpty() ? 0.0 : Double.parseDouble(distanceTextField.getText()));
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyButton.doClick();
            }
        }
    }

    protected void runGrouping(final double dist) {
        if(dist == 0) {
            return;
        }
        distanceTextField.setBackground(Color.WHITE);
        GUI.closeBalloonTip();
        try {
            applyButton.setEnabled(false);
            final OperationsHistoryPanel opHistory = table.getOperationHistoryPanel();
            if(opHistory.getLastOperation() instanceof ResultsGrouping.MergingOperation) {
                model.copyUndoToActual();
                opHistory.removeLastOperation();
            } else {
                model.copyActualToUndo();
            }
            model.setSelectedState(TripleStateTableModel.StateName.ACTUAL);
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
                        opHistory.addOperation(new MergingOperation(dist));

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

    private class MergingOperation extends OperationsHistoryPanel.Operation {

        double threshold;

        public MergingOperation(double threshold) {
            this.threshold = threshold;
        }

        @Override
        protected String getName() {
            return "Merging";
        }

        @Override
        protected boolean isUndoAble() {
            return true;
        }

        @Override
        protected void clicked() {
            if(grouping.getParent() instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) grouping.getParent();
                tabbedPane.setSelectedComponent(grouping);
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

    public static void applyToModel(ResultsTableModel model, double dist) {
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
            int frame = (int)mol.getParam(MoleculeDescriptor.LABEL_FRAME);
            // molecule itself has to be added to the list of detections,
            // because the parameters can change during the merging
            mol.addDetection(mol.clone());
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
         * The method matches molecules at the same positions
         * lasting for more than just 1 frame.
         * 
         * Note: this method makes changes into `detections`!
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
                KdTree<Molecule> tree = new KdTree<Molecule>(2);
                for(Molecule mol : fr2mol) {
                    tree.addPoint(new double[]{mol.getX(), mol.getY()}, mol);
                }
                for(int si = 0, sim = fr1mol.size(); si < sim; si++) {
                    Molecule mol = fr1mol.get(si);
                    nn_mol = tree.findNearestNeighbors(new double[]{mol.getX(), mol.getY()}, 1, dist_fn);
                    if(nn_mol.getMaxKey() < dist2_thr) {
                        nn_mol.getMax().addDetection(mol);
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
