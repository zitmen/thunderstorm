package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.drift.CrossCorrelationDriftCorrection;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.ImagePlus;
import ij.gui.Plot;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;

public class ResultsDriftCorrection {

    double[] x, y, frame;
    JPanel uiPanel;
    JTextField numStepsTextField;
    JTextField magnificationTextField;
    JCheckBox showDriftPlotCheckBox;
    JCheckBox showCorrelationsCheckBox;
    JButton applyButton;

    public JPanel createUIPanel() {
        uiPanel = new JPanel(new GridBagLayout());
        InputListener listener = new InputListener();
        numStepsTextField = new JTextField("10");
        numStepsTextField.addKeyListener(listener);
        magnificationTextField = new JTextField("5");
        magnificationTextField.addKeyListener(listener);
        showDriftPlotCheckBox = new JCheckBox("Show drift plot", true);
        showCorrelationsCheckBox = new JCheckBox("Show cross correlations", false);
        applyButton = new JButton("Apply");
        applyButton.addActionListener(listener);
        uiPanel.add(new JLabel("Number of bins:", SwingConstants.TRAILING), new GridBagConstraints(0, 0, 1, 1, 0.25, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        uiPanel.add(numStepsTextField, new GridBagConstraints(1, 0, 1, 1, 0.25, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 20), 0, 0));
        uiPanel.add(new JLabel("Rendering magnification:", SwingConstants.TRAILING), new GridBagConstraints(0, 1, 1, 1, 0.25, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        uiPanel.add(magnificationTextField, new GridBagConstraints(1, 1, 1, 1, 0.25, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 20), 0, 0));
        uiPanel.add(showDriftPlotCheckBox, new GridBagConstraints(2, 0, 1, 1, 0.25, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        uiPanel.add(showCorrelationsCheckBox, new GridBagConstraints(2, 1, 1, 1, 0.25, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        uiPanel.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(3, 0).anchor(GridBagConstraints.LINE_END).build());
        uiPanel.add(applyButton, new GridBagConstraints(3, 1, 1, 1, 0, 0, GridBagConstraints.BASELINE, 0, new Insets(0, 0, 0, 0), 0, 0));
        return uiPanel;
    }

    public void runDriftCorrection(final int bins, final double magnification, final boolean showCorrelationImages, final boolean showPlot) throws IllegalArgumentException {
        if(bins < 3) {
            throw new IllegalArgumentException("Number of images must be greater than 2. Input: " + bins);
        }
        if(magnification <= 0) {
            throw new IllegalArgumentException("Rendering magnification must be greater than 0. Input: " + magnification);
        }
        try {
            applyButton.setEnabled(false);
            final IJResultsTable rt = IJResultsTable.getResultsTable();
            final OperationsHistoryPanel history = rt.getOperationHistoryPanel();
            if(history.getLastOperation() instanceof DriftCorrectionOperation) {
                if(!history.isLastOperationUndone()){
                    rt.swapUndoAndActual(); //undo last operation
                }
                history.removeLastOperation();
            }
            rt.copyActualToUndo();  //save state for later undo
            getResultsFromTable();

            new SwingWorker<CrossCorrelationDriftCorrection, Void>() {
                @Override
                protected CrossCorrelationDriftCorrection doInBackground() {
                    CrossCorrelationDriftCorrection driftCorrection = new CrossCorrelationDriftCorrection(x, y, frame, bins, magnification, -1, -1, showCorrelationImages);
                    return driftCorrection;
                }

                @Override
                protected void done() {
                    try {
                        CrossCorrelationDriftCorrection driftCorrection = get();
                        //show plots
                        if(showPlot) {
                            showDriftPlot(driftCorrection);
                        }
                        if(showCorrelationImages) {
                            showCorrelations(driftCorrection);
                        }
                        //update results table
                        applyToResultsTable(driftCorrection);
                        history.addOperation(new DriftCorrectionOperation(magnification, bins, showPlot, showCorrelationImages));
                        rt.setStatus("Drift correction applied.");
                        rt.showPreview();
                    } catch(InterruptedException ex) {
                        GUI.showBalloonTip(applyButton, ex.getMessage());
                    } catch(ExecutionException ex) {
                        GUI.showBalloonTip(applyButton, ex.getCause().getMessage());
                    } finally {
                        applyButton.setEnabled(true);
                    }
                }
            }.execute();
            
            TableHandlerPlugin.recordDrift(bins, magnification, showCorrelationImages, showPlot);
        } catch(Throwable ex) {
            applyButton.setEnabled(true);
        }
    }

    void getResultsFromTable() {
        IJResultsTable rt = IJResultsTable.getResultsTable();
        if(!rt.columnExists(PSFModel.Params.LABEL_X) || !rt.columnExists(PSFModel.Params.LABEL_Y)) {
            throw new RuntimeException("Could not find " + PSFModel.Params.LABEL_X + " and " + PSFModel.Params.LABEL_Y + " columns.");
        }
        if(!rt.columnExists(MoleculeDescriptor.LABEL_FRAME)) {
            throw new RuntimeException("Could not find \"" + MoleculeDescriptor.LABEL_FRAME + "\" column.");
        }
        x = rt.getColumnAsDoubles(PSFModel.Params.LABEL_X, MoleculeDescriptor.Units.PIXEL);
        y = rt.getColumnAsDoubles(PSFModel.Params.LABEL_Y, MoleculeDescriptor.Units.PIXEL);
        frame = rt.getColumnAsDoubles(MoleculeDescriptor.LABEL_FRAME);
    }

    static void showDriftPlot(CrossCorrelationDriftCorrection driftCorrection) {
        int minFrame = driftCorrection.getMinFrame();
        int maxFrame = driftCorrection.getMaxFrame();
        int gridTicks = 200;
        double tickStep = (maxFrame-minFrame)/(double)gridTicks;
        double[] grid = new double[gridTicks];
        double[] driftX = new double[gridTicks];
        double[] driftY = new double[gridTicks];
        for(int i = 0; i < gridTicks; i++) {
            grid[i] = i*tickStep + minFrame;
            Point2D.Double offset = driftCorrection.getInterpolatedDrift(grid[i]);
            driftX[i] = offset.x;
            driftY[i] = offset.y;
        }
        Plot plot = new Plot("drift", "frame", "drift [px]", grid, driftX);
        plot.setLimits(minFrame, driftCorrection.getMaxFrame(), Math.min(VectorMath.min(driftCorrection.getBinDriftX()),VectorMath.min(driftCorrection.getBinDriftY())), Math.max(VectorMath.max(driftCorrection.getBinDriftX()),VectorMath.max(driftCorrection.getBinDriftY())));
        plot.setColor(Color.blue);
        plot.addPoints(driftCorrection.getBinCenters(), driftCorrection.getBinDriftX(), Plot.CROSS);
        plot.addLabel(0.05, 0.8, "x drift");
        plot.draw();
        plot.setColor(Color.red);
        plot.addPoints(grid, driftY, Plot.LINE);
        plot.addPoints(driftCorrection.getBinCenters(), driftCorrection.getBinDriftY(), Plot.CROSS);
        plot.addLabel(0.05, 0.9, "y drift");
        plot.show();
    }

    private void showCorrelations(CrossCorrelationDriftCorrection driftCorrection) {
        ImagePlus imp = new ImagePlus("Cross correlations", driftCorrection.getCorrelationImages());
        //add center markers
        double[] binDriftsX = driftCorrection.getBinDriftX();
        double[] binDriftsY = driftCorrection.getBinDriftY();
        for(int i = 1; i < binDriftsX.length; i++) {
            RenderingOverlay.showPointsInImage(imp, new double[]{-binDriftsX[i] * driftCorrection.getMagnification() + imp.getWidth() / 2 + 0.5}, new double[]{-binDriftsY[i]*driftCorrection.getMagnification() + imp.getHeight() / 2 + 0.5}, i, Color.red, RenderingOverlay.MARKER_CROSS);
        }
        imp.show();
    }

    private void applyToResultsTable(CrossCorrelationDriftCorrection driftCorrection) {
        IJResultsTable rt = IJResultsTable.getResultsTable();
        Units unitsX = rt.getColumnUnits(PSFModel.Params.LABEL_X);
        Units unitsY = rt.getColumnUnits(PSFModel.Params.LABEL_Y);
        for(int i = 0; i < rt.getRowCount(); i++) {
            double frameNumber = rt.getValue(i, MoleculeDescriptor.LABEL_FRAME);
            double xVal = rt.getValue(i, PSFModel.Params.LABEL_X);
            double yVal = rt.getValue(i, PSFModel.Params.LABEL_Y);
            Point2D.Double drift = driftCorrection.getInterpolatedDrift(frameNumber);
            rt.setValueAt(xVal - Units.PIXEL.convertTo(unitsX, drift.x), i, PSFModel.Params.LABEL_X);
            rt.setValueAt(yVal - Units.PIXEL.convertTo(unitsY, drift.y), i, PSFModel.Params.LABEL_Y);
        }
    }

    class DriftCorrectionOperation extends OperationsHistoryPanel.Operation {

        final String name = "Drift";
        double magnification;
        int numSteps;
        transient boolean showDrift;
        transient boolean showCorrelations;

        public DriftCorrectionOperation(double magnification, int numSteps, boolean showDrift, boolean showCorrelations) {
            this.magnification = magnification;
            this.numSteps = numSteps;
            this.showDrift = showDrift;
            this.showCorrelations = showCorrelations;
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
            magnificationTextField.setText(Double.toString(magnification));
            numStepsTextField.setText(Integer.toString(numSteps));
            showDriftPlotCheckBox.setSelected(showDrift);
            showCorrelationsCheckBox.setSelected(showCorrelations);
        }

        @Override
        protected void undo() {
            IJResultsTable rt = IJResultsTable.getResultsTable();
            rt.swapUndoAndActual();
            rt.setStatus("Drift correction: Undo.");
            rt.showPreview();
        }

        @Override
        protected void redo() {
            IJResultsTable rt = IJResultsTable.getResultsTable();
            rt.swapUndoAndActual();
            rt.setStatus("Drift correction: Redo.");
            rt.showPreview();
        }
    }

    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //parse input
            int bins;
            double magnification;
            try {
                bins = Integer.parseInt(numStepsTextField.getText());
            } catch(NumberFormatException ex) {
                GUI.showBalloonTip(numStepsTextField, "Illegal argument vaue. " + ex.getMessage());
                return;
            }
            try {
                magnification = Double.parseDouble(magnificationTextField.getText());
            } catch(NumberFormatException ex) {
                GUI.showBalloonTip(magnificationTextField, "Illegal argument vaue. " + ex.getMessage());
                return;
            }
            //run drift correction
            try {
                runDriftCorrection(bins, magnification, showCorrelationsCheckBox.isSelected(), showDriftPlotCheckBox.isSelected());
            } catch(Exception ex) {
                GUI.showBalloonTip(applyButton, ex.getMessage());
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyButton.doClick();
            }
        }
    }
}
