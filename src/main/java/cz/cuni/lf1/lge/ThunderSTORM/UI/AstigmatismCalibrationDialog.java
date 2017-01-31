package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusFunction;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusFunctionFactory;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.DetectorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.DetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.EstimatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.EstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.FilterFactory;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.FilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.GrayScaleImageImpl;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.PluginCommands;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage;
import cz.cuni.lf1.thunderstorm.datastructures.Point2D;
import cz.cuni.lf1.thunderstorm.parser.thresholding.Thresholder;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.gui.Roi;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AstigmatismCalibrationDialog extends DialogStub implements ActionListener {

    ParameterKey.Double stageStep;
    ParameterKey.Double zRangeLimit;
    ParameterKey.String calibrationFilePath;
    ParameterKey.String filterName;
    ParameterKey.String detectorName;
    ParameterKey.String estimatorName;
    ParameterKey.String defocusName;

    private FilterUI[] filters;
    private DetectorUI[] detectors;
    private EstimatorUI[] estimators;
    private DefocusFunction[] defocusing;
    private ImagePlus imp;
    ExecutorService previewThreadRunner = Executors.newSingleThreadExecutor();
    Future<?> previewFuture = null;

    public AstigmatismCalibrationDialog(ImagePlus imp, FilterUI[] filters, DetectorUI[] detectors, EstimatorUI[] estimators, DefocusFunction[] defocusing) {
        super(new ParameterTracker("thunderstorm.calibration"), IJ.getInstance(), "Calibration options");
        params.getComponentHandlers().addForStringParameters(CardsPanel.class, new CardsPanelMacroUIHandler());

        stageStep = params.createDoubleField("stage", DoubleValidatorFactory.positiveNonZero(), 10);
        zRangeLimit = params.createDoubleField("zRange", DoubleValidatorFactory.positiveNonZero(), 400);
        calibrationFilePath = params.createStringField("saveto", null, "");
        filterName = params.createStringField("filter", null, filters[0].getName());
        detectorName = params.createStringField("detector", null, detectors[0].getName());
        estimatorName = params.createStringField("estimator", null, estimators[0].getName());
        defocusName = params.createStringField("defocusing", null, defocusing[0].getName());

        this.filters = filters;
        this.detectors = detectors;
        this.estimators = estimators;
        this.defocusing = defocusing;
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
                MacroParser.runNestedWithRecording(PluginCommands.CAMERA_SETUP.getValue(), null);
            }
        });
        JPanel cameraPanel = new JPanel(new BorderLayout());
        cameraPanel.add(cameraSetup);
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera"));
        pane.add(cameraPanel, componentConstraints);
        CardsPanel<FilterUI> filterCards = new CardsPanel<FilterUI>(filters, 0);
        filterName.registerComponent(filterCards);
        JPanel p = filterCards.getPanel("Filter:");
        p.setBorder(BorderFactory.createTitledBorder("Image filtering"));
        pane.add(p, componentConstraints);
        CardsPanel<DetectorUI> detectorCards = new CardsPanel<DetectorUI>(detectors, 0);
        detectorName.registerComponent(detectorCards);
        p = detectorCards.getPanel("Method:");
        p.setBorder(BorderFactory.createTitledBorder("Approximate localization of molecules"));
        pane.add(p, componentConstraints);
        CardsPanel<EstimatorUI> estimatorCards = new CardsPanel<EstimatorUI>(estimators, 0);
        estimatorName.registerComponent(estimatorCards);
        p = estimatorCards.getPanel("Method:");
        p.setBorder(BorderFactory.createTitledBorder("Sub-pixel localization of molecules"));
        pane.add(p, componentConstraints);
        CardsPanel<DefocusFunction> defocusCards = new CardsPanel<DefocusFunction>(defocusing, 0);
        defocusName.registerComponent(defocusCards);
        p = defocusCards.getPanel("Defocus model:");
        p.setBorder(BorderFactory.createTitledBorder("3D defocusing curve"));
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
        calibrationPanel.add(createBrowseButton(calibrationFileTextField, true, new FileNameExtensionFilter("Yaml file", "yaml")), BorderLayout.EAST);
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
                // parse parameters

                params.readDialogOptions();
                getActiveFilterUI().readParameters();
                getActiveDetectorUI().readParameters();
                getActiveEstimatorUI().readParameters();
                getActiveDefocusFunction().readParameters();

                params.savePrefs();

                //if another preview task is still running, cancel it
                if(previewFuture != null) {
                    previewFuture.cancel(true);
                }
                //do the preview task
                previewFuture = previewThreadRunner.submit(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            IJ.showStatus("Creating preview image.");
                            Roi roi = imp.getRoi();
                            ImageProcessor processor = imp.getProcessor();
                            if(roi != null) {
                                processor.setRoi(roi.getBounds());
                                processor = processor.crop();
                            } else {
                                processor = processor.duplicate();
                            }
                            FloatProcessor fp = (FloatProcessor) processor.convertToFloat();

                            if(roi != null) {
                                fp.setMask(roi.getMask());
                            }
                            GrayScaleImage input = new GrayScaleImageImpl(fp);
                            GrayScaleImage filtered = getActiveFilterUI().getImplementation().filter(input);
                            new ImagePlus("Filtered frame " + Integer.toString(imp.getSlice()), GrayScaleImageImpl.convertToFloatProcessor(filtered)).show();
                            GUI.checkIJEscapePressed();
                            Thresholder thresholder = new Thresholder(
                                    getActiveDetectorUI().getThresholdFormula(),
                                    FilterFactory.createThresholderSymbolTable(filters, getActiveFilterUIIndex()));
                            List<Point2D> detections = Point.applyRoiMask(imp.getRoi(), getActiveDetectorUI().getImplementation().detect(filtered, thresholder.evaluate(input)));
                            GUI.checkIJEscapePressed();
                            //
                            double[] xCoord = new double[detections.size()];
                            double[] yCoord = new double[detections.size()];
                            for(int i = 0; i < detections.size(); i++) {
                                xCoord[i] = detections.get(i).getX();
                                yCoord[i] = detections.get(i).getY();
                            }
                            //
                            ImagePlus impPreview = new ImagePlus("Preview for frame " + Integer.toString(imp.getSlice()), processor);
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
                getActiveFilterUI().readParameters();
                getActiveDetectorUI().readParameters();
                getActiveEstimatorUI().readParameters();
                getActiveDefocusFunction().readParameters();
                params.savePrefs();
                if(Recorder.record) {
                    getActiveFilterUI().recordOptions();
                    getActiveDetectorUI().recordOptions();
                    getActiveEstimatorUI().recordOptions();
                    getActiveDefocusFunction().recordOptions();
                    params.recordMacroOptions();
                }
                result = JOptionPane.OK_OPTION;
                dispose();
            } else if("Cancel".equals(e.getActionCommand())) {
                dispose();
            } else if("Defaults".equals(e.getActionCommand())) {
                params.resetToDefaults(true);
                AnalysisOptionsDialog.resetModuleUI(filters);
                AnalysisOptionsDialog.resetModuleUI(detectors);
                AnalysisOptionsDialog.resetModuleUI(estimators);
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
            getActiveDefocusFunction().readMacroOptions(options);

            return JOptionPane.OK_OPTION;
        } else {
            return super.showAndGetResult();
        }
    }

    public FilterUI getActiveFilterUI() {
        return FilterFactory.getFilterByName(filterName.getValue());
    }

    public int getActiveFilterUIIndex() {
        return FilterFactory.getFilterIndexByName(filterName.getValue());
    }

    public DetectorUI getActiveDetectorUI() {
        return DetectorFactory.getDetectorByName(detectorName.getValue());
    }

    public EstimatorUI getActiveEstimatorUI() { return EstimatorFactory.getEstimatorByName(estimatorName.getValue()); }

    public DefocusFunction getActiveDefocusFunction() { return DefocusFunctionFactory.getDefocusFunctionByName(defocusName.getValue()); }

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
        if(previewThreadRunner != null) {
            previewThreadRunner.shutdownNow();
        }
    }
}
