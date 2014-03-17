package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import ij.IJ;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

public class ResultsGrouping extends PostProcessingModule {

    private JTextField distanceTextField;
    private JTextField offFramesTextField;
    private JButton applyButton;

    private final ParameterKey.Double distParam;
    private final ParameterKey.Integer offFramesParam;
    private final ParameterKey.Double zCoordWeightParam; //hidden param

    public ResultsGrouping() {
        params.setNoGuiParametersAllowed(true);
        distParam = params.createDoubleField("dist", DoubleValidatorFactory.positive(), 20);
        offFramesParam = params.createIntField("offFrames", IntegerValidatorFactory.positive(), 1);
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

        distanceTextField = new JTextField(15);
        distanceTextField.addKeyListener(listener);
        distParam.registerComponent(distanceTextField);
        JLabel groupThrLabel = new JLabel("Maximum distance [current units of x,y]:");

        offFramesTextField = new JTextField(15);
        offFramesTextField.addKeyListener(listener);
        offFramesParam.registerComponent(offFramesTextField);
        JLabel offFramesLabel = new JLabel("Maximum off frames:");

        applyButton = new JButton("Merge");
        applyButton.addActionListener(listener);

        grouping.add(groupThrLabel, new GridBagHelper.Builder().gridxy(0, 0).weightx(0.5).anchor(GridBagConstraints.EAST).build());
        grouping.add(distanceTextField, new GridBagHelper.Builder().gridxy(1, 0).weightx(0.5).anchor(GridBagConstraints.WEST).build());
        grouping.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(2, 0).anchor(GridBagConstraints.EAST).build());
        grouping.add(offFramesLabel, new GridBagHelper.Builder().gridxy(0, 1).weightx(0.5).anchor(GridBagConstraints.EAST).build());
        grouping.add(offFramesTextField, new GridBagHelper.Builder().gridxy(1, 1).weightx(0.5).anchor(GridBagConstraints.WEST).build());
        grouping.add(applyButton, new GridBagHelper.Builder().gridxy(2, 1).build());
        params.updateComponents();
        return grouping;
    }

    @Override
    protected void runImpl() {
        final double dist = distParam.getValue();
        final int offFrames = offFramesParam.getValue();
        final double zWeight = zCoordWeightParam.getValue();
        if(!applyButton.isEnabled() || (dist == 0)) {
            return;
        }
        distanceTextField.setBackground(Color.WHITE);
        GUI.closeBalloonTip();
        applyButton.setEnabled(false);
        ResultsGrouping.this.saveStateForUndo(MergingOperation.class);

        final int merged = model.getRowCount();
        new SwingWorker<List<Molecule>, Object>() {
            @Override
            protected List<Molecule> doInBackground() {
                return getMergedMolecules(model, dist, offFrames, zWeight);
            }

            @Override
            protected void done() {
                try {
                    List<Molecule> mergedMolecules = get();
                    model.reset();
                    for(Molecule mol : mergedMolecules) {
                        model.addRow(mol);
                    }

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

    public static List<Molecule> getMergedMolecules(GenericTableModel model, double dist, int offFrames, double zWeight) {
        if(!model.columnExists(PSFModel.Params.LABEL_X) || !model.columnExists(PSFModel.Params.LABEL_Y)) {
            throw new RuntimeException(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y, model.getColumnNames()));
        }
        //
        FrameSequence frames = new FrameSequence();
        for(int i = 0, im = model.getRowCount(); i < im; i++) {
            frames.InsertMolecule(model.getRow(i));
        }
        frames.matchMolecules(sqr(dist),
                new FrameSequence.Fixed(offFrames),
                new FrameSequence.LastDetection(),
                zWeight);
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
