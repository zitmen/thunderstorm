package cz.cuni.lf1.lge.ThunderSTORM.results;

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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;

class DuplicatesFilter implements ActionListener {

    private JPanel filterPanel;
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
        applyButton = new JButton("Apply");
        applyButton.addActionListener(this);
        JLabel label = new JLabel("Remove molecules that converged to the same position.");
        filterPanel.add(label, new GridBagHelper.Builder().gridxy(0, 0).gridheight(1).fill(GridBagConstraints.BOTH).weightx(1).weighty(1).build());
        filterPanel.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(1, 0).anchor(GridBagConstraints.EAST).build());
        filterPanel.add(applyButton, new GridBagHelper.Builder().gridxy(1, 1).build());
        return filterPanel;
    }

    protected void runFilter() {
        if(!applyButton.isEnabled()) {
            return;
        }
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
                        int filtered = all - model.getRowCount();
                        opHistory.addOperation(new DuplicatesRemovalOperation());
                        String be = ((filtered > 1) ? "were" : "was");
                        String item = ((all > 1) ? "items" : "item");
                        table.setStatus(filtered + " out of " + all + " " + item + " " + be + " filtered out");
                        table.showPreview();
                    } catch(Exception ex) {
                        IJ.handleException(ex);
                    } finally {
                        applyButton.setEnabled(true);
                    }
                }
            }.execute();
            
            TableHandlerPlugin.recordRemoveDuplicates();
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    static void applyToModel(GenericTableModel model) {
        if(!model.columnExists(PSFModel.Params.LABEL_X) || !model.columnExists(PSFModel.Params.LABEL_Y)) {
            throw new RuntimeException(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y, model.getColumnNames()));
        }
        if(!model.columnExists(MoleculeDescriptor.Fitting.LABEL_CCD_THOMPSON) && !model.columnExists(MoleculeDescriptor.Fitting.LABEL_EMCCD_THOMPSON)) {
            throw new RuntimeException(String.format("Fitting uncertainty not found in Results table. Looking for: %s or %s. Found: %s.", MoleculeDescriptor.Fitting.LABEL_CCD_THOMPSON, MoleculeDescriptor.Fitting.LABEL_EMCCD_THOMPSON, model.getColumnNames()));
        }
        //
        FrameSequence frames = new FrameSequence();
        for(int i = 0, im = model.getRowCount(); i < im; i++) {
            frames.InsertMolecule(model.getRow(i));
        }
        model.filterRows(frames.filterDuplicateMolecules());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == applyButton) {
            runFilter();
        }
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
    
    //
    // ===================================================================
    //

    static class FrameSequence {

        // <Frame #, List of Molecules>
        private HashMap<Integer, Vector<Molecule>> detections;
        private Vector<Molecule> molecules;
        private SortedSet frames;
        private int maxId;

        public FrameSequence() {
            detections = new HashMap<Integer, Vector<Molecule>>();
            molecules = new Vector<Molecule>();
            frames = new TreeSet();
            maxId = 0;
        }

        public void InsertMolecule(Molecule mol) {
            int frame = (int)mol.getParam(MoleculeDescriptor.LABEL_FRAME);
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
                            if(uncertainty > sqr(getUncertaintyNm(frmol.get(j)))) {
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
                            if(uncertainty > sqr(getUncertaintyNm(frmol.get(j)))) {
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
            double uncertainty;
            if(mol.hasParam(MoleculeDescriptor.Fitting.LABEL_CCD_THOMPSON)) {
                uncertainty = mol.getParam(MoleculeDescriptor.Fitting.LABEL_CCD_THOMPSON, Units.NANOMETER);
            } else if(mol.hasParam(MoleculeDescriptor.Fitting.LABEL_EMCCD_THOMPSON)) {
                uncertainty = mol.getParam(MoleculeDescriptor.Fitting.LABEL_EMCCD_THOMPSON, Units.NANOMETER);
            } else {
                throw new RuntimeException("Fitting uncertainty not found in Results table.");
            }
            return uncertainty;
        }
    }

}
