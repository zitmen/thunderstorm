package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParser;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.RetVal;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.NANOMETER;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MoleculeXYZComparator;
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
import javax.swing.SwingWorker;

class DuplicatesFilter {

    private JPanel filterPanel;
    private JTextField distTextField;
    private JButton applyButton;
    private ResultsTableWindow table;
    private TripleStateTableModel model;

    public DuplicatesFilter(ResultsTableWindow table, TripleStateTableModel model) {
        this.table = table;
        this.model = model;
    }

    public DuplicatesFilter() {
    }

    public JPanel createUIPanel() {
        filterPanel = new JPanel(new GridBagLayout());
        distTextField = new JTextField(MoleculeDescriptor.Fitting.LABEL_THOMPSON);
        InputListener listener = new InputListener();
        distTextField.addKeyListener(listener);
        applyButton = new JButton("Apply");
        applyButton.addActionListener(listener);
        JLabel label = new JLabel("Remove molecules that converged to the same position. Distance threshold [nm]:");
        filterPanel.add(label, new GridBagHelper.Builder().gridxy(0, 0).gridheight(1).fill(GridBagConstraints.BOTH).weightx(1).weighty(1).build());
        filterPanel.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(1, 0).anchor(GridBagConstraints.EAST).build());
        filterPanel.add(distTextField, new GridBagHelper.Builder().gridxy(0, 1).fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
        filterPanel.add(applyButton, new GridBagHelper.Builder().gridxy(1, 1).build());
        return filterPanel;
    }

    protected void runFilter() {
        if(!applyButton.isEnabled()) {
            return;
        }
        distTextField.setBackground(Color.WHITE);
        GUI.closeBalloonTip();
        try {
            applyButton.setEnabled(false);
            final OperationsHistoryPanel opHistory = table.getOperationHistoryPanel();
            if(opHistory.getLastOperation() instanceof DuplicatesRemovalOperation) {
                model.copyUndoToActual();
                opHistory.removeLastOperation();
            } else {
                model.copyActualToUndo();
            }
            model.setActualState();
            final int all = model.getRowCount();
            new SwingWorker() {

                @Override
                protected Object doInBackground() throws Exception {
                    applyToModel(model);
                    return null;
                }
                
                @Override
                protected void done() {
                    try {
                        get();  // throws an exception if doInBackground hasn't finished
                        int filtered = all - model.getRowCount();
                        opHistory.addOperation(new DuplicatesRemovalOperation());
                        String be = ((filtered > 1) ? "were" : "was");
                        String item = ((all > 1) ? "items" : "item");
                        table.setStatus(filtered + " out of " + all + " " + item + " " + be + " filtered out");
                        table.showPreview();
                    } catch(ExecutionException ex) {
                        distTextField.setBackground(new Color(255, 200, 200));
                        GUI.showBalloonTip(distTextField, ex.getCause().getMessage());
                    } catch(Exception ex) {
                        IJ.handleException(ex);
                    } finally {
                        applyButton.setEnabled(true);
                    }
                }
            }.execute();
            
            TableHandlerPlugin.recordRemoveDuplicates();
        } catch(Exception ex) {
            distTextField.setBackground(new Color(255, 200, 200));
            GUI.showBalloonTip(distTextField, ex.toString());
            IJ.handleException(ex);
        }
    }

    void applyToModel(GenericTableModel model) {
        if(!model.columnExists(PSFModel.Params.LABEL_X) || !model.columnExists(PSFModel.Params.LABEL_Y)) {
            throw new RuntimeException(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y, model.getColumnNames()));
        }
        Node tree = new FormulaParser(distTextField.getText(), FormulaParser.FORMULA_RESULTS_FILTER).parse();
        tree.semanticScan();
        RetVal retval = tree.eval(Units.NANOMETER);
        Double [] dist;
        if(retval.isVector()) {
            if(((Double [])retval.get()).length != model.getRowCount()) {
                throw new FormulaParserException("Semantic error: result of formula must be either a scalar value, or a numeric vector of the same length as the number of rows in the table!");
            } else {
                dist = (Double[])retval.get();
            }
        } else {
            dist = new Double[model.getRowCount()];
            Arrays.fill(dist, (Double)retval.get());
        }
        //
        FrameSequence frames = new FrameSequence();
        for(int i = 0, im = model.getRowCount(); i < im; i++) {
            frames.insertMolecule(model.getRow(i), dist[i]);
        }
        model.filterRows(frames.filterDuplicateMolecules());
    }

    class DuplicatesRemovalOperation extends OperationsHistoryPanel.Operation {
        final String name = "Remove duplicates";

        public DuplicatesRemovalOperation() {
            //
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
            if(filterPanel.getParent() instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) filterPanel.getParent();
                tabbedPane.setSelectedComponent(filterPanel);
            }
        }

        @Override
        protected void undo() {
            model.swapUndoAndActual();
            table.setStatus("Remove duplicates: Undo.");
            table.showPreview();
        }

        @Override
        protected void redo() {
            model.swapUndoAndActual();
            table.setStatus("Remove duplicates: Redo.");
            table.showPreview();
        }
    }
    
    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            runFilter();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                runFilter();
            }
        }
    }
    
    //
    // ===================================================================
    //
    
    static class FrameSequence {

        // <Frame #, List of Molecules>
        private HashMap<Integer, Vector<Molecule>> detections;
        private HashMap<Integer,Double> uncertainties;
        private Vector<Molecule> molecules;
        private SortedSet frames;
        private int maxId;

        public FrameSequence() {
            detections = new HashMap<Integer, Vector<Molecule>>();
            molecules = new Vector<Molecule>();
            frames = new TreeSet();
            maxId = 0;
            uncertainties = new HashMap<Integer,Double>();
        }

        public void insertMolecule(Molecule mol, Double uncertainty) {
            int frame = (int)mol.getParam(MoleculeDescriptor.LABEL_FRAME);
            uncertainties.put((int)mol.getParam(MoleculeDescriptor.LABEL_ID), uncertainty);
            //
            if(!detections.containsKey(frame)) {
                detections.put(frame, new Vector<Molecule>());
            }
            detections.get(frame).add(mol);
            frames.add(frame);
            //
            int id = (int)mol.getParam(MoleculeDescriptor.LABEL_ID);
            if(id > maxId) {
                maxId = id;
            }
        }

        public Vector<Molecule> getAllMolecules() {
            Collections.sort(molecules);
            return molecules;
        }

        /**
         * The method matches molecules in their mutual distance smaller than
         * uncertainty with which their positions were estimated.
         * 
         * The method works just in 2D, since the Thompson formula applies only for
         * lateral coordinates. Thus calculating distance
         * {@mathjax (x_1-x_2)^2+(y_1-y_2)^2}.
         */
        public boolean [] filterDuplicateMolecules() {
            molecules.clear();
            Integer[] frno = new Integer[frames.size()];
            frames.toArray(frno);
            boolean [] filter = new boolean[maxId]; // zero-based indexing
            Arrays.fill(filter, true);
            //
            for(int fi = 0; fi < frno.length; fi++) {
                Vector<Molecule> frmol = detections.get(frno[fi]);
                Collections.sort(frmol, new MoleculeXYZComparator());
                //
                for(int i = 0, count = frmol.size(); i < count; i++) {
                    Molecule mol = frmol.get(i);
                    int id = (int)mol.getParam(MoleculeDescriptor.LABEL_ID)-1;  // zero-based indexing
                    double uncertainty = sqr(getUncertaintyNm(mol));
                    //
                    if(filter[id] == false) continue;
                    for(int j = i - 1; j >= 0; j--) {
                        if(sqr(frmol.get(j).getX() - mol.getX()) > uncertainty) {
                            break;
                        }
                        if(mol.dist2xy(frmol.get(j), NANOMETER) < uncertainty) {
                            if(uncertainty >= sqr(getUncertaintyNm(frmol.get(j)))) {
                                filter[id] = false;
                                break;
                            }
                        }
                    }
                    //
                    if(filter[id] == false) continue;
                    for(int j = i + 1; j < count; j++) {
                        if(sqr(frmol.get(j).getX() - mol.getX()) > uncertainty) {
                            break;
                        }
                        if(mol.dist2xy(frmol.get(j), NANOMETER) < uncertainty) {
                            if(uncertainty >= sqr(getUncertaintyNm(frmol.get(j)))) {
                                filter[id] = false;
                                break;
                            }
                        }
                    }
                }
            }
            return filter;
        }

        private double getUncertaintyNm(Molecule mol) {
            return uncertainties.get((int)mol.getParam(MoleculeDescriptor.LABEL_ID)).doubleValue();
        }
    }

}
