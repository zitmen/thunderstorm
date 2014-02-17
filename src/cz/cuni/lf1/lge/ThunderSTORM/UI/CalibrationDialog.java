package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.ModuleLoader;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.PluginCommands;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.DialogStub;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.Roi;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CalibrationDialog extends DialogStub implements ActionListener {

    ParameterKey.Double stageStep;
    ParameterKey.Double zRangeLimit;
    ParameterKey.String calibrationFilePath;
    ParameterKey.String filterName;
    ParameterKey.String detectorName;
    ParameterKey.String estimatorName;

    private List<IFilterUI> filters;
    private List<IDetectorUI> detectors;
    private List<IEstimatorUI> estimators;
    private ImagePlus imp;
    ExecutorService previewThredRunner = Executors.newSingleThreadExecutor();
    Future<?> previewFuture = null;

    public CalibrationDialog(ImagePlus imp, List<IFilterUI> filters, List<IDetectorUI> detectors, List<IEstimatorUI> estimators) {
        super(new ParameterTracker("thunderstorm.calibration"), IJ.getInstance(), "Calibration options");
        params.getComponentHandlers().addForStringParameters(CardsPanel.class, new CardsPanelMacroUIHandler());

        stageStep = params.createDoubleField("stage", DoubleValidatorFactory.positiveNonZero(), 10);
        zRangeLimit = params.createDoubleField("zRange", DoubleValidatorFactory.positiveNonZero(), 400);
        calibrationFilePath = params.createStringField("saveto", null, "");
        filterName = params.createStringField("filter", null, filters.get(0).getName());
        detectorName = params.createStringField("detector", null, detectors.get(0).getName());
        estimatorName = params.createStringField("estimator", null, estimators.get(0).getName());

        this.filters = filters;
        this.detectors = detectors;
        this.estimators = estimators;
        this.imp = imp;
    }

    @Override
    protected void layoutComponents() {
        Container pane = getContentPane();
        //
        pane.setLayout(new GridBagLayout());
        GridBagConstraints componentConstraints = new GridBagConstraints();
        componentConstraints.gridx = 0;
        componentConstraints.fill = GridBagConstraints.BOTH;
        componentConstraints.weightx = 1;

        JButton cameraSetup = new JButton("Camera setup");
        cameraSetup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                MacroParser.runNestedWithRecording(PluginCommands.CAMERA_SETUP, null);
            }
        });
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.add(cameraSetup);
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera"));
        pane.add(cameraPanel, componentConstraints);
        CardsPanel<IFilterUI> filterCards = new CardsPanel<IFilterUI>(filters, 0);
        filterName.registerComponent(filterCards);
        JPanel p = filterCards.getPanel("Filter:");
        p.setBorder(BorderFactory.createTitledBorder("Image filtering"));
        pane.add(p, componentConstraints);
        CardsPanel<IDetectorUI> detectorCards = new CardsPanel<IDetectorUI>(detectors, 0);
        detectorName.registerComponent(detectorCards);
        p = detectorCards.getPanel("Method:");
        p.setBorder(BorderFactory.createTitledBorder("Approximate localization of molecules"));
        pane.add(p, componentConstraints);
        CardsPanel<IEstimatorUI> estimatorCards = new CardsPanel<IEstimatorUI>(estimators, 0);
        estimatorName.registerComponent(estimatorCards);
        p = estimatorCards.getPanel("Method:");
        p.setBorder(BorderFactory.createTitledBorder("Sub-pixel localization of molecules"));
        pane.add(p, componentConstraints);

        JPanel aditionalOptions = new JPanel(new GridBagLayout());
        aditionalOptions.setBorder(BorderFactory.createTitledBorder("Additional options"));
        aditionalOptions.add(new JLabel("Z stage step [nm]:"), GridBagHelper.leftCol());
        JTextField stageStepTextField = new JTextField("", 20);
        stageStep.registerComponent(stageStepTextField);
        aditionalOptions.add(stageStepTextField, GridBagHelper.rightCol());
        aditionalOptions.add(new JLabel("Z range limit [nm]:"), GridBagHelper.leftCol());
        JTextField zRangeTextField = new JTextField("", 20);
        zRangeLimit.registerComponent(zRangeTextField);
        aditionalOptions.add(zRangeTextField, GridBagHelper.rightCol());
        aditionalOptions.add(new JLabel("Save to file: "), GridBagHelper.leftCol());
        JPanel calibrationPanel = new JPanel(new BorderLayout());
        JTextField calibrationFileTextField = new JTextField(15);
        calibrationFilePath.registerComponent(calibrationFileTextField);
        calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
        calibrationPanel.add(createBrowseButton(calibrationFileTextField, true), BorderLayout.EAST);
        GridBagConstraints gbc = GridBagHelper.rightCol();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        aditionalOptions.add(calibrationPanel, gbc);
        pane.add(aditionalOptions, componentConstraints);

        JButton defaults = new JButton("Defaults");
        JButton preview = new JButton("Preview");
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        defaults.addActionListener(this);
        preview.addActionListener(this);
        ok.addActionListener(this);
        cancel.addActionListener(this);
        //
        JPanel buttons = new JPanel();
        buttons.add(defaults);
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(preview);
        buttons.add(ok);
        buttons.add(cancel);
        pane.add(buttons, componentConstraints);
        getRootPane().setDefaultButton(ok);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setResizable(false);
        params.loadPrefs();
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if("Preview".equals(e.getActionCommand())) {
                Thresholder.setActiveFilter(getActiveFilterUIIndex());
            // parse parameters

                params.readDialogOptions();
                getActiveFilterUI().readParameters();
                getActiveDetectorUI().readParameters();
                getActiveEstimatorUI().readParameters();
                getActiveFilterUI().resetThreadLocal();
                getActiveDetectorUI().resetThreadLocal();
                getActiveEstimatorUI().resetThreadLocal();

                params.savePrefs();

                //if another preview task is still running, cancel it
                if(previewFuture != null) {
                    previewFuture.cancel(true);
                }
                //do the preview task
                previewFuture = previewThredRunner.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            IJ.showStatus("Creating preview image.");
                            FloatProcessor fp = (FloatProcessor) imp.getProcessor().crop().convertToFloat();
                            Roi roi = imp.getRoi();
                            if(roi != null) {
                                fp.setMask(roi.getMask());
                            }
                            Thresholder.setCurrentImage(fp);
                            FloatProcessor filtered = getActiveFilterUI().getThreadLocalImplementation().filterImage(fp);
                            new ImagePlus("ThunderSTORM: filtered frame " + Integer.toString(imp.getSlice()), filtered).show();
                            GUI.checkIJEscapePressed();
                            List<Point> detections = Point.applyRoiMask(imp.getRoi(), getActiveDetectorUI().getThreadLocalImplementation().detectMoleculeCandidates(filtered));
                            GUI.checkIJEscapePressed();
                            //
                            double[] xCoord = new double[detections.size()];
                            double[] yCoord = new double[detections.size()];
                            for(int i = 0; i < detections.size(); i++) {
                                xCoord[i] = detections.get(i).getX().doubleValue();
                                yCoord[i] = detections.get(i).getY().doubleValue();
                            }
                            //
                            ImagePlus impPreview = new ImagePlus("ThunderSTORM preview for frame " + Integer.toString(imp.getSlice()), imp.getProcessor().crop());
                            RenderingOverlay.showPointsInImage(impPreview, xCoord, yCoord, Color.red, RenderingOverlay.MARKER_CROSS);
                            impPreview.show();
                        } catch(StoppedByUserException ex) {
                            IJ.resetEscape();
                            IJ.showStatus("Preview interrupted.");
                        } catch(Exception ex) {
                            IJ.handleException(ex);
                        }
                    }
                });
            } else if("OK".equals(e.getActionCommand())) {
                    params.readDialogOptions();
                    Thresholder.setActiveFilter(getActiveFilterUIIndex());
                    getActiveFilterUI().readParameters();
                    getActiveDetectorUI().readParameters();
                    getActiveEstimatorUI().readParameters();
                    params.savePrefs();
                    if(Recorder.record) {
                        getActiveFilterUI().recordOptions();
                        getActiveDetectorUI().recordOptions();
                        getActiveEstimatorUI().recordOptions();
                        params.recordMacroOptions();
                    }
                    result = JOptionPane.OK_OPTION;
                    dispose();
            } else if("Cancel".equals(e.getActionCommand())) {
                dispose();
            } else if("Defaults".equals(e.getActionCommand())) {
                params.resetToDefaults(true);
                AnalysisOptionsDialog.resetModuleUIs(filters, detectors, estimators);
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    @Override
    public int showAndGetResult() {
        if(MacroParser.isRanFromMacro()) {
            params.readMacroOptions();
            String options = Macro.getOptions();
            getActiveFilterUI().readMacroOptions(options);
            getActiveDetectorUI().readMacroOptions(options);
            getActiveEstimatorUI().readMacroOptions(options);

            return JOptionPane.OK_OPTION;
        } else {
            return super.showAndGetResult();
        }
    }

    public IFilterUI getActiveFilterUI() {
        return ModuleLoader.moduleByName(filters, filterName.getValue());
    }

    public int getActiveFilterUIIndex() {
        return ModuleLoader.moduleIndexByName(filters, filterName.getValue());
    }

    public IDetectorUI getActiveDetectorUI() {
        return ModuleLoader.moduleByName(detectors, detectorName.getValue());
    }

    public IEstimatorUI getActiveEstimatorUI() {
        return ModuleLoader.moduleByName(estimators, estimatorName.getValue());
    }

    public String getSavePath() {
        return calibrationFilePath.getValue();
    }

    public double getStageStep() {
        return stageStep.getValue();
    }

    public double getZRangeLimit() {
        return zRangeLimit.getValue();
    }

    @Override
    public void dispose() {
        super.dispose();
        if(previewThredRunner != null) {
            previewThredRunner.shutdownNow();
        }
    }
}
