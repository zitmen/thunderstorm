package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class ResultsStageOffset extends PostProcessingModule {

    private JButton applyButton;
    JTextField framesPerStagePositionTextField;
    JTextField stagePositionsTextField;
    JTextField stageStepTextField;
    JTextField firstPositionOffsetTextField;

    ParameterKey.Integer framesPerStagePositionParam;
    ParameterKey.Integer stagePositionsParam;
    ParameterKey.Double stageStepParam;
    ParameterKey.Double firstPositionOffsetParam;

    public ResultsStageOffset() {
        framesPerStagePositionParam = params.createIntField("framesPerStagePos", IntegerValidatorFactory.positiveNonZero(), 1);
        stagePositionsParam = params.createIntField("stagePositions", IntegerValidatorFactory.positiveNonZero(), 1);
        stageStepParam = params.createDoubleField("stageStep", null, 0);
        firstPositionOffsetParam = params.createDoubleField("firstPosOffset", null, 0);
    }

    @Override
    public String getMacroName() {
        return "offset";
    }

    @Override
    public String getTabName() {
        return "Z-stage offset";
    }

    @Override
    protected JPanel createUIPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        InputListener listener = new InputListener();
        framesPerStagePositionTextField = new JTextField("1");
        stagePositionsTextField = new JTextField("1");
        stageStepTextField = new JTextField("0");
        firstPositionOffsetTextField = new JTextField("0");

        framesPerStagePositionTextField.addKeyListener(listener);
        stagePositionsTextField.addKeyListener(listener);
        stageStepTextField.addKeyListener(listener);
        firstPositionOffsetTextField.addKeyListener(listener);

        framesPerStagePositionParam.registerComponent(framesPerStagePositionTextField);
        stagePositionsParam.registerComponent(stagePositionsTextField);
        stageStepParam.registerComponent(stageStepTextField);
        firstPositionOffsetParam.registerComponent(firstPositionOffsetTextField);

        applyButton = new JButton("Apply");
        applyButton.addActionListener(listener);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        panel.add(new JLabel("Frames per one Z-stage position:", SwingConstants.RIGHT), new GridBagHelper.Builder().gridxy(0, 0).fill(GridBagConstraints.HORIZONTAL).weightx(0.1).build());
        panel.add(framesPerStagePositionTextField, new GridBagHelper.Builder().gridxy(1, 0).fill(GridBagConstraints.HORIZONTAL).weightx(0.2).build());
        panel.add(new JLabel("Number of Z-stage positions:", SwingConstants.RIGHT), new GridBagHelper.Builder().gridxy(0, 1).fill(GridBagConstraints.HORIZONTAL).weightx(0.1).build());
        panel.add(stagePositionsTextField, new GridBagHelper.Builder().gridxy(1, 1).fill(GridBagConstraints.HORIZONTAL).weightx(0.2).build());
        panel.add(new JLabel("Z-stage step [nm]:", SwingConstants.RIGHT), new GridBagHelper.Builder().gridxy(2, 0).insets(new Insets(0, 20, 0, 0)).fill(GridBagConstraints.HORIZONTAL).build());
        panel.add(stageStepTextField, new GridBagHelper.Builder().gridxy(3, 0).fill(GridBagConstraints.HORIZONTAL).weightx(0.2).build());
        panel.add(new JLabel("Initial Z-stage offset [nm]:", SwingConstants.RIGHT), new GridBagHelper.Builder().gridxy(2, 1).insets(new Insets(0, 20, 0, 0)).fill(GridBagConstraints.HORIZONTAL).build());
        panel.add(firstPositionOffsetTextField, new GridBagHelper.Builder().gridxy(3, 1).fill(GridBagConstraints.HORIZONTAL).weightx(0.1).build());
        panel.add(Box.createGlue(), new GridBagHelper.Builder().gridxy(4, 1).weightx(0.1).build());
        panel.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(5, 0).anchor(GridBagConstraints.LINE_END).build());
        panel.add(applyButton, new GridBagHelper.Builder().gridxy(5, 1).build());
        return panel;
    }

    /**
     * sets undo state and adds offset to the results table
     */
    @Override
    public void runImpl() {
        try {

            int framesPerStagePosition = framesPerStagePositionParam.getValue();
            int stagePositions = stagePositionsParam.getValue();
            double stageStep = stageStepParam.getValue();
            double firstPositionOffset = firstPositionOffsetParam.getValue();

            saveStateForUndo();

            applyToModel(framesPerStagePosition, stagePositions, stageStep, firstPositionOffset);
            addOperationToHistory(new DefaultOperation());
        } catch(Exception ex) {
            GUI.showBalloonTip(applyButton, ex.toString());
        }
    }

    /**
     * changes the z values in results table model
     */
    void applyToModel(int framesPerStagePosition, int stagePositions, double stageStep, double firstPositionOffset) {
        if(!model.columnExists(MoleculeDescriptor.LABEL_FRAME)) {
            throw new RuntimeException("frame column not found in Results table.");
        }
        //
        if(model.columnExists(PSFModel.Params.LABEL_Z)) {
            int zColumn = model.findColumn(PSFModel.Params.LABEL_Z);
            model.setLabel(zColumn, PSFModel.Params.LABEL_Z_REL, MoleculeDescriptor.Units.NANOMETER);
        }
        //
        Vector<Molecule> molecules = IJResultsTable.getResultsTable().getData();
        for(Molecule molecule : molecules) {
            double z = model.columnExists(PSFModel.Params.LABEL_Z_REL) ? molecule.getParam(PSFModel.Params.LABEL_Z_REL) : 0;
            int frame = (int) molecule.getParam(MoleculeDescriptor.LABEL_FRAME);
            double newZ = (((frame - 1) / framesPerStagePosition) % stagePositions) * stageStep + firstPositionOffset + z;
            molecule.setZ(newZ);
        }
        model.fireTableStructureChanged();
        model.fireTableDataChanged();
        table.showPreview();
    }

    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ev) {
            run();
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyButton.doClick();
            }
        }
    }
}
