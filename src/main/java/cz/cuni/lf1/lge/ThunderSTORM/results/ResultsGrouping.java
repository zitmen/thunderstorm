package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.WorkerThread;
import ij.IJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;

public class ResultsGrouping extends PostProcessingModule {

    private JTextField distanceTextField;
    private JTextField offFramesTextField;
    private JTextField framesPerMoleculeTextField;
    private JButton applyButton;

    private final ParameterKey.Double distParam;
    private final ParameterKey.Integer offFramesParam;
    private final ParameterKey.Integer framesPerMolecule;
    private final ParameterKey.Double zCoordWeightParam; //hidden param

    public ResultsGrouping() {
        params.setNoGuiParametersAllowed(true);
        distParam = params.createDoubleField("dist", DoubleValidatorFactory.positive(), 20);
        offFramesParam = params.createIntField("offFrames", IntegerValidatorFactory.positive(), 1);
        framesPerMolecule = params.createIntField("framesPerMolecule", IntegerValidatorFactory.positive(), 0);
        zCoordWeightParam = params.createDoubleField("zCoordWeight", DoubleValidatorFactory.positive(), 0.1);
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

        distanceTextField = new JTextField(5);
        distanceTextField.addKeyListener(listener);
        distParam.registerComponent(distanceTextField);
        JLabel groupThrLabel = new JLabel("Maximum distance [units of x,y]: ");

        offFramesTextField = new JTextField(5);
        offFramesTextField.addKeyListener(listener);
        offFramesParam.registerComponent(offFramesTextField);
        JLabel offFramesLabel = new JLabel("Maximum off frames: ");

        framesPerMoleculeTextField = new JTextField(5);
        framesPerMoleculeTextField.addKeyListener(listener);
        framesPerMolecule.registerComponent(framesPerMoleculeTextField);
        JLabel framesPerMoleculeLabel = new JLabel("Max. frames per molecule (0 = unlimited): ");

        applyButton = new JButton("Merge");
        applyButton.addActionListener(listener);

        grouping.add(groupThrLabel, new GridBagHelper.Builder().gridxy(0, 0).weightx(0.2).anchor(GridBagConstraints.EAST).build());
        grouping.add(distanceTextField, new GridBagHelper.Builder().gridxy(1, 0).weightx(0.3).anchor(GridBagConstraints.WEST).build());
        grouping.add(framesPerMoleculeLabel, new GridBagHelper.Builder().gridxy(2, 0).weightx(0.2).anchor(GridBagConstraints.EAST).build());
        grouping.add(framesPerMoleculeTextField, new GridBagHelper.Builder().gridxy(3, 0).weightx(0.3).anchor(GridBagConstraints.WEST).build());
        grouping.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(4, 0).anchor(GridBagConstraints.EAST).build());
        grouping.add(offFramesLabel, new GridBagHelper.Builder().gridxy(0, 1).weightx(0.5).anchor(GridBagConstraints.EAST).build());
        grouping.add(offFramesTextField, new GridBagHelper.Builder().gridxy(1, 1).weightx(0.5).anchor(GridBagConstraints.WEST).build());
        grouping.add(applyButton, new GridBagHelper.Builder().gridxy(4, 1).build());
        params.updateComponents();
        return grouping;
    }

    @Override
    protected void runImpl() {
        final double dist = distParam.getValue();
        final int offFrames = offFramesParam.getValue();
        final int framesPerMol = framesPerMolecule.getValue();
        final double zWeight = zCoordWeightParam.getValue();
        if(!applyButton.isEnabled() || (dist == 0)) {
            return;
        }
        applyButton.setEnabled(false);
        saveStateForUndo();

        final int merged = model.getRowCount();
        new WorkerThread<List<Molecule>>() {
            @Override
            protected List<Molecule> doJob() {
                return getMergedMolecules(model, dist, offFrames, framesPerMol, zWeight);
            }

            @Override
            protected void finishJob(List<Molecule> mergedMolecules) {
                model.reset();
                for(Molecule mol : mergedMolecules) {
                    model.addRow(mol);
                }

                int into = model.getRowCount();
                addOperationToHistory(new DefaultOperation());

                table.setStatus(Integer.toString(merged) + " molecules were merged into " + Integer.toString(into) + " molecules");
                table.showPreview();
            }

            @Override
            public void exCatch(Throwable ex) {
                IJ.handleException(ex);
                GUI.showBalloonTip(distanceTextField, ex.getCause().toString());
            }

            @Override
            public void exFinally() {
                applyButton.setEnabled(true);
            }
        }.execute();
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

    public static List<Molecule> getMergedMolecules(GenericTableModel model, double dist, int offFrames, int framesPerMol, double zWeight) {
        if(!model.columnExists(PSFModel.Params.LABEL_X) || !model.columnExists(PSFModel.Params.LABEL_Y)) {
            throw new RuntimeException(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y, model.getColumnNames()));
        }
        //
        FrameSequence frames = new FrameSequence();
        for(int i = 0, im = model.getRowCount(); i < im; i++) {
            frames.InsertMolecule(model.getRow(i));
        }
        frames.mergeMolecules(sqr(dist), offFrames, new FrameSequence.LastDetection(), framesPerMol, zWeight, null);
        //
        // Set new IDs for the new "macro" molecules
        for(Molecule mol : frames.getAllMolecules()) {
            if(!mol.isSingleMolecule()) {
                mol.setParam(MoleculeDescriptor.LABEL_ID, model.getNewId());
            }
        }
        //
        return frames.getAllMolecules();
    }
}
