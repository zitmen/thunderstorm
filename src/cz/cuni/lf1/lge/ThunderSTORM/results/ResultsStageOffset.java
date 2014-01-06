package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

class ResultsStageOffset {

    private ResultsTableWindow table;
    private TripleStateTableModel model;
    private JPanel panel;
    private JButton applyButton;
    JTextField framesPerStagePositionTextField;
    JTextField stagePositionsTextField;
    JTextField stageStepTextField;
    JTextField firstPositionOffsetTextField;

    public ResultsStageOffset(ResultsTableWindow table, TripleStateTableModel model) {
        this.table = table;
        this.model = model;
    }

    public JPanel createUIPanel() {
        panel = new JPanel(new GridBagLayout());
        InputListener listener = new InputListener();
        framesPerStagePositionTextField = new JTextField("1");
        stagePositionsTextField = new JTextField("1");
        stageStepTextField = new JTextField("0");
        firstPositionOffsetTextField = new JTextField("0");

        framesPerStagePositionTextField.addKeyListener(listener);
        stagePositionsTextField.addKeyListener(listener);
        stageStepTextField.addKeyListener(listener);
        firstPositionOffsetTextField.addKeyListener(listener);

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

    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent ev) {
            //parse
            try {
                int framesPerStagePosition = Integer.parseInt(framesPerStagePositionTextField.getText());
                int stagePositions = Integer.parseInt(stagePositionsTextField.getText());
                double stageStep = Double.parseDouble(stageStepTextField.getText());
                double firstPositionOffset = Double.parseDouble(firstPositionOffsetTextField.getText());
                runAddStageOffset(framesPerStagePosition, stagePositions, stageStep, firstPositionOffset);
            } catch(Exception e) {
                GUI.showBalloonTip(applyButton, "Illegal argument vaue. " + e.getMessage());
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyButton.doClick();
            }
        }
    }

    /**
     * sets undo state and adds offset to the results table
     */
    public void runAddStageOffset(int framesPerStagePosition, int stagePositions, double stageStep, double firstPositionOffset) {
        if(!applyButton.isEnabled()) {
            return;
        }
        GUI.closeBalloonTip();
        try {
            final OperationsHistoryPanel opHistory = table.getOperationHistoryPanel();
            if(opHistory.getLastOperation() instanceof ResultsStageOffset.StageOffsetOperation) {
                if(!opHistory.isLastOperationUndone()) {
                    model.swapUndoAndActual();
                }
                opHistory.removeLastOperation();
            } 
            model.copyActualToUndo();
            model.setSelectedState(TripleStateTableModel.StateName.ACTUAL);

            applyToModel(framesPerStagePosition, stagePositions, stageStep, firstPositionOffset);
            opHistory.addOperation(new StageOffsetOperation(framesPerStagePosition, stagePositions, stageStep, firstPositionOffset));
            TableHandlerPlugin.recordStageOffset(framesPerStagePosition, stagePositions, stageStep, firstPositionOffset);
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
            double z = model.columnExists(PSFModel.Params.LABEL_Z_REL)? molecule.getParam(PSFModel.Params.LABEL_Z_REL): 0;
            int frame = (int) molecule.getParam(MoleculeDescriptor.LABEL_FRAME);
            double newZ = (((frame - 1) / framesPerStagePosition) % stagePositions) * stageStep + firstPositionOffset + z;
            molecule.setZ(newZ);
        }
        model.fireTableStructureChanged();
        model.fireTableDataChanged();
        table.showPreview();
    }

    class StageOffsetOperation extends OperationsHistoryPanel.Operation {

        final String name = "Z-stage scanning";
        int framesPerStagePosition;
        int stagePositions;
        double stageStep;
        double firstPositionOffset;

        public StageOffsetOperation(int framesPerStagePosition, int stagePositions, double stageStep, double firstPositionOffset) {
            this.framesPerStagePosition = framesPerStagePosition;
            this.stagePositions = stagePositions;
            this.stageStep = stageStep;
            this.firstPositionOffset = firstPositionOffset;
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
            if(panel.getParent() instanceof JTabbedPane) {
                JTabbedPane tabbedPane = (JTabbedPane) panel.getParent();
                tabbedPane.setSelectedComponent(panel);

                framesPerStagePositionTextField.setText(framesPerStagePosition + "");
                stagePositionsTextField.setText(stagePositions + "");
                stageStepTextField.setText(stagePositions + "");
                firstPositionOffsetTextField.setText(firstPositionOffset + "");
            }
        }

        @Override
        protected void undo() {
            model.swapUndoAndActual();
            table.setStatus("Stage offset: Undo.");
            table.showPreview();
        }

        @Override
        protected void redo() {
            model.swapUndoAndActual();
            table.setStatus("Stage offset: Redo.");
            table.showPreview();
        }
    }
}
