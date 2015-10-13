package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParser;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.Node;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree.RetVal;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units.NANOMETER;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MoleculeXYZComparator;
import cz.cuni.lf1.lge.ThunderSTORM.util.WorkerThread;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
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
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DuplicatesFilter extends PostProcessingModule {

    private JPanel filterPanel;
    private JTextField distTextField;
    private JButton applyButton;
    private ParameterKey.String distFormula;

    public DuplicatesFilter() {
        distFormula = params.createStringField("distFormula", null, MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY);
    }

    @Override
    public String getMacroName() {
        return "duplicates";
    }

    @Override
    public String getTabName() {
        return "Remove duplicates";
    }

    @Override
    protected JPanel createUIPanel() {
        filterPanel = new JPanel(new GridBagLayout());
        distTextField = new JTextField();
        distFormula.registerComponent(distTextField);
        InputListener listener = new InputListener();
        distTextField.addKeyListener(listener);
        applyButton = new JButton("Apply");
        applyButton.addActionListener(listener);
        JLabel label = new JLabel("Remove molecules that converged to the same position. Distance threshold [nm]:");
        filterPanel.add(label, new GridBagHelper.Builder().gridxy(0, 0).gridheight(1).fill(GridBagConstraints.BOTH).weightx(1).weighty(1).build());
        filterPanel.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(1, 0).anchor(GridBagConstraints.EAST).build());
        filterPanel.add(distTextField, new GridBagHelper.Builder().gridxy(0, 1).fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
        filterPanel.add(applyButton, new GridBagHelper.Builder().gridxy(1, 1).build());
        params.updateComponents();
        return filterPanel;
    }

    @Override
    protected void runImpl() {
        try {
            if(!applyButton.isEnabled()) {
                return;
            }
            distTextField.setBackground(Color.WHITE);
            final String dist = distFormula.getValue();
            applyButton.setEnabled(false);

            DuplicatesFilter.this.saveStateForUndo();
            final int all = model.getRowCount();
            new WorkerThread<Void>() {
                @Override
                public Void doJob() {
                    applyToModel(model, dist);
                    return null;
                }

                @Override
                public void finishJob(Void nothing) {
                    int filtered = all - model.getRowCount();
                    DuplicatesFilter.this.addOperationToHistory(new DefaultOperation());
                    String be = ((filtered > 1) ? "were" : "was");
                    String item = ((all > 1) ? "items" : "item");
                    table.setStatus(filtered + " out of " + all + " " + item + " " + be + " filtered out");
                    table.showPreview();
                }

                @Override
                public void exCatch(Throwable ex) {
                    distTextField.setBackground(new Color(255, 200, 200));
                    GUI.showBalloonTip(distTextField, ex.getCause().getMessage());
                }

                @Override
                public void exFinally() {
                    applyButton.setEnabled(true);
                }
            }.execute();
        } catch(Exception ex) {
            distTextField.setBackground(new Color(255, 200, 200));
            GUI.showBalloonTip(distTextField, ex.toString());
            IJ.handleException(ex);
        }
    }

    void applyToModel(GenericTableModel model, String formula) {
        if(!model.columnExists(PSFModel.Params.LABEL_X) || !model.columnExists(PSFModel.Params.LABEL_Y)) {
            throw new RuntimeException(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y, model.getColumnNames()));
        }
        if(!model.columnExists(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY)) {
            throw new RuntimeException(String.format("Fitting uncertainty not found in Results table. Looking for: %s. Found: %s.", MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, model.getColumnNames()));
        }
        Node tree = new FormulaParser(formula, FormulaParser.FORMULA_RESULTS_FILTER).parse();
        tree.semanticScan();
        RetVal retval = tree.eval(Units.NANOMETER);
        Double[] dist;
        if(retval.isVector()) {
            if(((Double[]) retval.get()).length != model.getRowCount()) {
                throw new FormulaParserException("Semantic error: result of formula must be either a scalar value, or a numeric vector of the same length as the number of rows in the table!");
            } else {
                dist = (Double[]) retval.get();
            }
        } else {
            dist = new Double[model.getRowCount()];
            Arrays.fill(dist, (Double) retval.get());
        }
        //
        FrameSequence frames = new FrameSequence();
        for(int i = 0, im = model.getRowCount(); i < im; i++) {
            frames.insertMolecule(model.getRow(i), dist[i]);
        }
        model.filterRows(frames.filterDuplicateMolecules());
    }

    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            run();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                run();
            }
        }
    }

    //
    // ===================================================================
    //
    static class FrameSequence {

        // <Frame #, List of Molecules>
        private HashMap<Integer, Vector<Molecule>> detections;
        private HashMap<Integer, Double> radii;
        private Vector<Molecule> molecules;
        private SortedSet frames;
        private Map<Integer, Integer> idToOrder;

        public FrameSequence() {
            detections = new HashMap<Integer, Vector<Molecule>>();
            molecules = new Vector<Molecule>();
            frames = new TreeSet();
            radii = new HashMap<Integer, Double>();
            idToOrder = new HashMap<Integer, Integer>();
        }

        public void insertMolecule(Molecule mol, Double uncertainty) {
            int frame = (int) mol.getParam(MoleculeDescriptor.LABEL_FRAME);
            int id = (int) mol.getParam(MoleculeDescriptor.LABEL_ID);
            radii.put(id, uncertainty);
            idToOrder.put(id, idToOrder.size());
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
         * The method matches molecules in their mutual distance smaller than
         * uncertainty with which their positions were estimated.
         *
         * The method works just in 2D, since the Thompson formula applies only
         * for lateral coordinates. Thus calculating distance {
         *
         * @mathjax (x_1-x_2)^2+(y_1-y_2)^2}.
         */
        public boolean[] filterDuplicateMolecules() {
            molecules.clear();
            Integer[] frno = new Integer[frames.size()];
            frames.toArray(frno);
            boolean[] filter = new boolean[radii.size()]; // zero-based indexing
            Arrays.fill(filter, true);
            //
            for(int fi = 0; fi < frno.length; fi++) {
                Vector<Molecule> frmol = detections.get(frno[fi]);
                Collections.sort(frmol, new MoleculeXYZComparator());
                //
                for(int i = 0, count = frmol.size(); i < count; i++) {
                    Molecule mol = frmol.get(i);
                    int id = (int) mol.getParam(MoleculeDescriptor.LABEL_ID);  // zero-based indexing
                    double uncertainty = sqr(getUncertaintyNm(mol));
                    double radius = sqr(getRadiusNm(mol));
                    //
                    if(filter[idToOrder.get(id)] == false) {
                        continue;
                    }
                    for(int j = i - 1; j >= 0; j--) {
                        if(sqr(frmol.get(j).getX() - mol.getX()) > radius) {
                            break;
                        }
                        if(mol.dist2xy(frmol.get(j), NANOMETER) < radius) {
                            if(uncertainty >= sqr(getUncertaintyNm(frmol.get(j)))) {
                                filter[idToOrder.get(id)] = false;
                                break;
                            }
                        }
                    }
                    //
                    if(filter[idToOrder.get(id)] == false) {
                        continue;
                    }
                    for(int j = i + 1; j < count; j++) {
                        if(sqr(frmol.get(j).getX() - mol.getX()) > radius) {
                            break;
                        }
                        if(mol.dist2xy(frmol.get(j), NANOMETER) < radius) {
                            if(uncertainty >= sqr(getUncertaintyNm(frmol.get(j)))) {
                                filter[idToOrder.get(id)] = false;
                                break;
                            }
                        }
                    }
                }
            }
            return filter;
        }

        private double getRadiusNm(Molecule mol) {
            return radii.get((int) mol.getParam(MoleculeDescriptor.LABEL_ID)).doubleValue();
        }

        private double getUncertaintyNm(Molecule mol) {
            if(mol.hasParam(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY)) {
                return mol.getParam(MoleculeDescriptor.Fitting.LABEL_UNCERTAINTY_XY, Units.NANOMETER);
            } else {
                throw new RuntimeException("Fitting uncertainty not found in Results table.");
            }
        }
    }

    @Override
    public void resetParamsToDefaults() {
        distTextField.setBackground(Color.white);
        super.resetParamsToDefaults();
    }
}
