package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.drift.CorrelationDriftEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.drift.CrossCorrelationDriftResults;
import cz.cuni.lf1.lge.ThunderSTORM.drift.DriftResults;
import cz.cuni.lf1.lge.ThunderSTORM.drift.FiducialDriftEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
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
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker.Condition;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.StringValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.ValidatorException;
import ij.IJ;
import ij.plugin.frame.Recorder;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.BalloonTipStyle;
import net.java.balloontip.styles.RoundedBalloonStyle;

public class ResultsDriftCorrection extends PostProcessingModule {

    JTextField numStepsTextField;
    JTextField magnificationTextField;
    JCheckBox showCorrelationsCheckBox;
    JButton applyButton;

    private String[] actions = {"Cross correlation", "Fiducial markers", "Load from file"};
    private ParameterKey.String actionParam;
    //cross correlation params
    private ParameterKey.Integer binsParam;
    private ParameterKey.Double magnificationParam;
    private ParameterKey.Boolean showCorrelationImagesParam;
    //fiducials params
    private ParameterKey.Double distanceThresholdParam;
    private ParameterKey.Double onTimeRatioParam;
    private ParameterKey.Double smoothingBandwidthParam;
    //load save params
    private ParameterKey.Boolean saveParam;
    private ParameterKey.String pathParam;

    private BalloonTip ccOptions;
    private BalloonTip fiducialOptions;

    @Override
    public String getMacroName() {
        return "drift";
    }

    @Override
    public String getTabName() {
        return "Drift correction";
    }

    public ResultsDriftCorrection() {
        ParameterTracker.Condition crossCorrCondition = new Condition() {
            @Override
            public boolean isSatisfied() {
                return actionParam.getValue().equals(actions[0]);
            }

            @Override
            public ParameterKey[] dependsOn() {
                return new ParameterKey[]{actionParam};
            }
        };
        ParameterTracker.Condition fiducialCondition = new Condition() {
            @Override
            public boolean isSatisfied() {
                return actionParam.getValue().equals(actions[1]);
            }

            @Override
            public ParameterKey[] dependsOn() {
                return new ParameterKey[]{actionParam};
            }
        };
        ParameterTracker.Condition notLoadCondition = new Condition() {
            @Override
            public boolean isSatisfied() {
                return !(actionParam.getValue().equals(actions[2]));
            }

            @Override
            public ParameterKey[] dependsOn() {
                return new ParameterKey[]{actionParam};
            }
        };
        ParameterTracker.Condition pathCondition = new Condition() {
            @Override
            public boolean isSatisfied() {
                return actionParam.getValue().equals(actions[2]) || saveParam.getValue(); //load action or enabled save
            }

            @Override
            public ParameterKey[] dependsOn() {
                return new ParameterKey[]{actionParam, saveParam};
            }
        };

        actionParam = params.createStringField("method", StringValidatorFactory.isMember(actions), actions[0]);
        //cross correlation params
        binsParam = params.createIntField("steps", IntegerValidatorFactory.rangeInclusive(3, Integer.MAX_VALUE), 5, crossCorrCondition);
        magnificationParam = params.createDoubleField("magnification", DoubleValidatorFactory.positiveNonZero(), 5, crossCorrCondition);
        showCorrelationImagesParam = params.createBooleanField("showCorrelations", null, false, crossCorrCondition);
        //fiducials params
        distanceThresholdParam = params.createDoubleField("distanceThr", DoubleValidatorFactory.positiveNonZero(), 40, fiducialCondition);
        onTimeRatioParam = params.createDoubleField("onTimeRatio", DoubleValidatorFactory.rangeInclusive(0, 1), 0.1, fiducialCondition);
        smoothingBandwidthParam = params.createDoubleField("smoothingBandwidth", DoubleValidatorFactory.rangeInclusive(0, 1), 0.25, fiducialCondition);
        //load save params
        saveParam = params.createBooleanField("save", null, false, notLoadCondition);
        pathParam = params.createStringField("path", null, "", pathCondition);
    }

    @Override
    protected JPanel createUIPanel() {
        final JPanel uiPanel = new JPanel(new GridBagLayout());
        InputListener listener = new InputListener();

        //cross correlation options panel
        final JPanel ccPanel = new JPanel(new GridBagLayout());

        numStepsTextField = new JTextField(10);
        numStepsTextField.addKeyListener(listener);
        magnificationTextField = new JTextField(10);
        magnificationTextField.addKeyListener(listener);
        showCorrelationsCheckBox = new JCheckBox("Show cross correlations", false);

        binsParam.registerComponent(numStepsTextField);
        magnificationParam.registerComponent(magnificationTextField);
        showCorrelationImagesParam.registerComponent(showCorrelationsCheckBox);

        ccPanel.add(new JLabel("Number of bins:", SwingConstants.TRAILING), GridBagHelper.leftCol());
        ccPanel.add(numStepsTextField, GridBagHelper.rightCol());
        ccPanel.add(new JLabel("Rendering magnification:", SwingConstants.TRAILING), GridBagHelper.leftCol());
        ccPanel.add(magnificationTextField, GridBagHelper.rightCol());
        ccPanel.add(showCorrelationsCheckBox, GridBagHelper.twoCols());

        //fiducials options panel
        JPanel fiducialPanel = new JPanel(new GridBagLayout());

        JTextField distanceThrTextField = new JTextField(10);
        JTextField onTimeRatioTextField = new JTextField(10);
        JTextField smoothingBandwidthTextField = new JTextField(10);

        distanceThresholdParam.registerComponent(distanceThrTextField);
        onTimeRatioParam.registerComponent(onTimeRatioTextField);
        smoothingBandwidthParam.registerComponent(smoothingBandwidthTextField);

        fiducialPanel.add(new JLabel("Max distance:"), GridBagHelper.leftCol());
        fiducialPanel.add(distanceThrTextField, GridBagHelper.rightCol());
        fiducialPanel.add(new JLabel("Min frames ratio:"), GridBagHelper.leftCol());
        fiducialPanel.add(onTimeRatioTextField, GridBagHelper.rightCol());
        fiducialPanel.add(new JLabel("Smoothing bandwidth:"), GridBagHelper.leftCol());
        fiducialPanel.add(smoothingBandwidthTextField, GridBagHelper.rightCol());

        //save panel
        final JPanel savePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        final JCheckBox saveCheckBox = new JCheckBox("Save to file:");
        final JTextField savePathTextField = new JTextField(20);
        JButton browseButton = new JButton("...");
        browseButton.setMargin(new Insets(1, 1, 1, 1));
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser jfc = new JFileChooser(savePathTextField.getText().isEmpty() ? null : savePathTextField.getText());
                if(jfc.showDialog(uiPanel, "OK") == JFileChooser.APPROVE_OPTION) {
                    savePathTextField.setText(jfc.getSelectedFile().getPath());
                }
            }
        });
        saveParam.registerComponent(saveCheckBox);
        pathParam.registerComponent(savePathTextField);
        final JLabel loadLabel = new JLabel("Load file path:", SwingConstants.TRAILING) {
            @Override
            public Dimension getPreferredSize() {
                return saveCheckBox.getPreferredSize();     //same size as saveCheckBox
            }
        };
        savePanel.add(loadLabel);
        savePanel.add(saveCheckBox);
        savePanel.add(savePathTextField);
        savePanel.add(browseButton);

        //load panel
        saveParam.registerComponent(saveCheckBox);
        pathParam.registerComponent(savePathTextField);

        //Table ui panel
        GridBagHelper.Builder glueConstr = new GridBagHelper.Builder().fill(GridBagConstraints.HORIZONTAL).weightx(1);
        GridBagHelper.Builder compConstr = new GridBagHelper.Builder();

        ButtonGroup btnGroup = new ButtonGroup();
        final JRadioButton ccRadioButton = new JRadioButton(actions[0]);
        final JRadioButton fiducialRadioButton = new JRadioButton(actions[1]);
        JRadioButton loadRadioButton = new JRadioButton(actions[2]);
        btnGroup.add(ccRadioButton);
        btnGroup.add(fiducialRadioButton);
        btnGroup.add(loadRadioButton);
        actionParam.registerComponent(btnGroup);
        ActionListener radioListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(ccRadioButton.isSelected() || fiducialRadioButton.isSelected()) {
                    saveCheckBox.setVisible(true);
                    loadLabel.setVisible(false);
                } else {
                    saveCheckBox.setVisible(false);
                    loadLabel.setVisible(true);
                }
            }
        };
        ccRadioButton.addActionListener(radioListener);
        fiducialRadioButton.addActionListener(radioListener);
        loadRadioButton.addActionListener(radioListener);

        final JButton ccOptionsButton = new JButton(">>");
        JButton fiducialOptionsButton = new JButton(">>");
        ccOptionsButton.setMargin(new Insets(1, 1, 1, 1));
        fiducialOptionsButton.setMargin(new Insets(1, 1, 1, 1));
        ccOptionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!ccOptions.isVisible()) {
                    ccOptions.setVisible(true);
                    fiducialOptions.setVisible(false);
                } else {
                    ccOptions.setVisible(false);
                }
            }
        });
        fiducialOptionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(!fiducialOptions.isVisible()) {
                    fiducialOptions.setVisible(true);
                    ccOptions.setVisible(false);
                } else {
                    fiducialOptions.setVisible(false);
                }
            }
        });
        BalloonTipStyle style = new RoundedBalloonStyle(5, 5, fiducialPanel.getBackground(), Color.BLACK);
        ccOptions = new BalloonTip(ccOptionsButton, ccPanel, style, false);
        fiducialOptions = new BalloonTip(fiducialOptionsButton, fiducialPanel, style, false);
        ccOptions.setVisible(false);
        fiducialOptions.setVisible(false);

        applyButton = new JButton("Apply");
        applyButton.addActionListener(listener);

        uiPanel.add(Box.createGlue(), glueConstr.gridxy(0, 0).build());
        uiPanel.add(ccRadioButton, compConstr.gridxy(1, 0).build());
        uiPanel.add(ccOptionsButton, compConstr.gridxy(2, 0).build());
        uiPanel.add(Box.createGlue(), glueConstr.gridxy(3, 0).build());
        uiPanel.add(fiducialRadioButton, compConstr.gridxy(4, 0).build());
        uiPanel.add(fiducialOptionsButton, compConstr.gridxy(5, 0).build());
        uiPanel.add(Box.createGlue(), glueConstr.gridxy(6, 0).build());
        uiPanel.add(loadRadioButton, compConstr.gridxy(7, 0).build());
        uiPanel.add(Box.createGlue(), glueConstr.gridxy(8, 0).build());
        uiPanel.add(Help.createHelpButton(getClass()), new GridBagHelper.Builder().gridxy(9, 0).anchor(GridBagConstraints.LINE_END).build());
        uiPanel.add(savePanel, new GridBagHelper.Builder().gridxy(0, 1).gridwidth(9).build());
        uiPanel.add(applyButton, compConstr.gridxy(9, 1).build());

        params.updateComponents();
        return uiPanel;
    }

    @Override
    public void runImpl() {
        try {
            //hide options balloons
            ccOptions.setVisible(false);
            fiducialOptions.setVisible(false);

            applyButton.setEnabled(false);

            saveStateForUndo(DefaultOperation.class);

            if(!model.columnExists(PSFModel.Params.LABEL_X) || !model.columnExists(PSFModel.Params.LABEL_Y)) {
                throw new RuntimeException("Could not find " + PSFModel.Params.LABEL_X + " and " + PSFModel.Params.LABEL_Y + " columns.");
            }
            if(!model.columnExists(MoleculeDescriptor.LABEL_FRAME)) {
                throw new RuntimeException("Could not find \"" + MoleculeDescriptor.LABEL_FRAME + "\" column.");
            }
        } catch(RuntimeException ex) {
            applyButton.setEnabled(true);
            throw ex;
        }
        new SwingWorker<DriftResults, Void>() {
            @Override
            protected DriftResults doInBackground() {
                String action = actionParam.getValue();
                if(action.equals(actions[0])) {
                    //cross correlation
                    double[] x = model.getColumnAsDoubles(PSFModel.Params.LABEL_X, MoleculeDescriptor.Units.PIXEL);
                    double[] y = model.getColumnAsDoubles(PSFModel.Params.LABEL_Y, MoleculeDescriptor.Units.PIXEL);
                    double[] frame = model.getColumnAsDoubles(MoleculeDescriptor.LABEL_FRAME, null);

                    return CorrelationDriftEstimator.estimateDriftFromCoords(x, y, frame, binsParam.getValue(), magnificationParam.getValue(), -1, -1, showCorrelationImagesParam.getValue());
                } else if(action.equals(actions[1])) {
                    //fiducials
                    List<Molecule> molecules = getClonedMoleculeList();

                    return new FiducialDriftEstimator().estimateDrift(
                            molecules,
                            distanceThresholdParam.getValue(),
                            onTimeRatioParam.getValue(),
                            smoothingBandwidthParam.getValue());
                } else if(action.equals(actions[2])) {
                    throw new RuntimeException("NYI");
                } else {
                    throw new RuntimeException("unknown action");
                }
            }

            @Override
            protected void done() {
                try {
                    DriftResults driftCorrection = get();
                    //show plots
                    showDriftPlot(driftCorrection);
                    if((driftCorrection instanceof CrossCorrelationDriftResults) && showCorrelationImagesParam.getValue()) {
                        showCorrelations((CrossCorrelationDriftResults) driftCorrection);
                    }
                    //update results table
                    applyToResultsTable(driftCorrection);
                    addOperationToHistory(new DefaultOperation());
                    table.setStatus("Drift correction applied.");
                    table.showPreview();
                } catch(ExecutionException ex) {
                    handleException(ex.getCause());
                } catch(Exception ex) {
                    handleException(ex);
                } finally {
                    applyButton.setEnabled(true);
                }
            }
        }.execute();

    }

    private void applyToResultsTable(DriftResults driftCorrection) {
        IJ.showStatus("Applying drift...");
        IJResultsTable rt = IJResultsTable.getResultsTable();
        Units unitsX = rt.getColumnUnits(PSFModel.Params.LABEL_X);
        Units unitsY = rt.getColumnUnits(PSFModel.Params.LABEL_Y);
        Units unitsDrift = driftCorrection.getUnits();
        for(int i = 0; i < rt.getRowCount(); i++) {
            double frameNumber = rt.getValue(i, MoleculeDescriptor.LABEL_FRAME);
            double xVal = rt.getValue(i, PSFModel.Params.LABEL_X);
            double yVal = rt.getValue(i, PSFModel.Params.LABEL_Y);
            Point2D.Double drift = driftCorrection.getInterpolatedDrift(frameNumber);
            rt.setValueAt(xVal - unitsDrift.convertTo(unitsX, drift.x), i, PSFModel.Params.LABEL_X);
            rt.setValueAt(yVal - unitsDrift.convertTo(unitsY, drift.y), i, PSFModel.Params.LABEL_Y);
        }
    }

    static void showDriftPlot(DriftResults driftCorrection) {
        int minFrame = driftCorrection.getMinFrame();
        int maxFrame = driftCorrection.getMaxFrame();
        int gridTicks = 200;
        double tickStep = (maxFrame - minFrame) / (double) gridTicks;
        double[] grid = new double[gridTicks];
        double[] driftX = new double[gridTicks];
        double[] driftY = new double[gridTicks];
        for(int i = 0; i < gridTicks; i++) {
            grid[i] = i * tickStep + minFrame;
            Point2D.Double offset = driftCorrection.getInterpolatedDrift(grid[i]);
            driftX[i] = offset.x;
            driftY[i] = offset.y;
        }
        Plot plot = new Plot("Drift", "frame", "drift [" + driftCorrection.getUnits() + "]", (float[]) null, null);
        if(driftCorrection.getDriftDataX().length > 50) {
            plot.setFrameSize(1280, 720);
        }
        plot.setLimits(minFrame, driftCorrection.getMaxFrame(),
                Math.min(VectorMath.min(driftCorrection.getDriftDataX()), VectorMath.min(driftCorrection.getDriftDataY())),
                Math.max(VectorMath.max(driftCorrection.getDriftDataX()), VectorMath.max(driftCorrection.getDriftDataY())));
        plot.setColor(new Color(255, 128, 128));
        plot.addPoints(driftCorrection.getDriftDataFrame(), driftCorrection.getDriftDataX(), Plot.CROSS);
        plot.draw();
        plot.setColor(new Color(128, 255, 128));
        plot.addPoints(driftCorrection.getDriftDataFrame(), driftCorrection.getDriftDataY(), Plot.CROSS);
        plot.setColor(Color.red);
        plot.addPoints(grid, driftX, Plot.LINE);
        plot.addLabel(0.05, 0.8, "x drift");
        plot.setColor(Color.green);
        plot.addPoints(grid, driftY, Plot.LINE);
        plot.addLabel(0.05, 0.9, "y drift");
        plot.show();
    }

    static void showCorrelations(CrossCorrelationDriftResults driftCorrection) {
        ImagePlus imp = new ImagePlus("Cross correlations", driftCorrection.getCorrelationImages());
        //add center markers
        double[] binDriftsX = driftCorrection.getDriftDataX();
        double[] binDriftsY = driftCorrection.getDriftDataY();
        for(int i = 1; i < binDriftsX.length; i++) {
            RenderingOverlay.showPointsInImage(imp, new double[]{-binDriftsX[i] / driftCorrection.getScaleFactor() + imp.getWidth() / 2 + 0.5}, new double[]{-binDriftsY[i] / driftCorrection.getScaleFactor() + imp.getHeight() / 2 + 0.5}, i, Color.red, RenderingOverlay.MARKER_CROSS);
        }
        imp.show();
    }

    @Override
    protected void handleException(Throwable ex) {
        if(ex instanceof ValidatorException) {
            String action = actionParam.getValue();
            if(action.equals(actions[0])) {
                ccOptions.setVisible(true);
            } else if(action.equals(actions[1])) {
                fiducialOptions.setVisible(true);
            }
        }
        super.handleException(ex);
    }


    private class InputListener extends KeyAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            //run drift correction
            try {
                run();
            } catch(Exception ex) {
                handleException(ex);
            }
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                applyButton.doClick();
            }
        }
    }

    private List<Molecule> getClonedMoleculeList() {
        List<Molecule> molecules = new ArrayList<Molecule>(model.getRowCount());
        MoleculeDescriptor clonedDescriptor = model.cloneDescriptor();
        for(int i = 0; i < model.getRowCount(); i++) {
            molecules.add(model.getRow(i).clone(clonedDescriptor));
        }
        return molecules;
    }
}
