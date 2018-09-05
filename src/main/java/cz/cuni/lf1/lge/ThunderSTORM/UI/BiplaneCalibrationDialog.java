
package cz.cuni.lf1.lge.ThunderSTORM.UI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import cz.cuni.lf1.lge.ThunderSTORM.ModuleLoader;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.CalibrationConfig;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusFunction;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.PluginCommands;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.plugin.frame.Recorder;

public class BiplaneCalibrationDialog extends DialogStub implements ActionListener {

	ParameterKey.Double stageStep;
	ParameterKey.Double zRangeLimit;
	ParameterKey.String calibrationFilePath;
	ParameterKey.String filterName;
	ParameterKey.String detectorName;
	ParameterKey.String estimatorName;
	ParameterKey.String defocusName;
	ParameterKey.String rawImage1Stack;
	ParameterKey.String rawImage2Stack;

	private List<IFilterUI> filters;
	private List<IDetectorUI> detectors;
	private List<IEstimatorUI> estimators;
	private List<DefocusFunction> defocusing;

	ParameterKey.Double dist2thrZStackMatching;
	ParameterKey.Integer minimumFitsCount;
	ParameterKey.Integer polyFitMaxIters;
	ParameterKey.Integer finalPolyFitMaxIters;
	ParameterKey.Integer minFitsInZRange;
	ParameterKey.Integer movingAverageLag;
	ParameterKey.Boolean checkIfDefocusIsInRange;
	ParameterKey.Integer inlierFittingMaxIters;
	ParameterKey.Double inlierFittingInlierFraction;
	ParameterKey.Boolean showResultsTable;

	ParameterKey.Integer rtfIterNum;
	ParameterKey.Double rtfThDist;
	ParameterKey.Double rtfThInlr;

	ParameterKey.Integer hIterNum;
	ParameterKey.Double hThDist;
	ParameterKey.Double hThInlr;
	ParameterKey.Double hThPairDist;
	ParameterKey.Double hThAllowedTransformChange;

	public BiplaneCalibrationDialog(List<IFilterUI> filters, List<IDetectorUI> detectors,
		List<IEstimatorUI> estimators, List<DefocusFunction> defocusing)
	{
		super(new ParameterTracker("thunderstorm.calibration"), IJ.getInstance(),
			"Calibration options");
		params.getComponentHandlers().addForStringParameters(CardsPanel.class,
			new CardsPanelMacroUIHandler());

		stageStep = params.createDoubleField("stage", DoubleValidatorFactory.positiveNonZero(), 10);
		zRangeLimit = params.createDoubleField("zRange", DoubleValidatorFactory.positiveNonZero(), 400);
		calibrationFilePath = params.createStringField("saveto", null, "");
		filterName = params.createStringField("filter", null, filters.get(0).getName());
		detectorName = params.createStringField("detector", null, detectors.get(0).getName());
		estimatorName = params.createStringField("estimator", null, estimators.get(0).getName());
		defocusName = params.createStringField("defocusing", null, defocusing.get(0).getName());
		rawImage1Stack = params.createStringField("raw_image1_stack", null, "");
		rawImage2Stack = params.createStringField("raw_image2_stack", null, "");

		CalibrationConfig defaultConfig = new CalibrationConfig();
		dist2thrZStackMatching = params.createDoubleField("dist2thrZStackMatching",
			DoubleValidatorFactory.positive(), defaultConfig.dist2thrZStackMatching);
		minimumFitsCount = params.createIntField("minimumFitsCount", IntegerValidatorFactory.positive(),
			defaultConfig.minimumFitsCount);
		polyFitMaxIters = params.createIntField("polyFitMaxIters", IntegerValidatorFactory.positive(),
			defaultConfig.polyFitMaxIters);
		finalPolyFitMaxIters = params.createIntField("finalPolyFitMaxIters", IntegerValidatorFactory
			.positive(), defaultConfig.finalPolyFitMaxIters);
		minFitsInZRange = params.createIntField("minFitsInZRange", IntegerValidatorFactory.positive(),
			defaultConfig.minFitsInZRange);
		movingAverageLag = params.createIntField("movingAverageLag", IntegerValidatorFactory.positive(),
			defaultConfig.movingAverageLag);
		checkIfDefocusIsInRange = params.createBooleanField("checkIfDefocusIsInRange", null,
			defaultConfig.checkIfDefocusIsInRange);
		inlierFittingMaxIters = params.createIntField("inlierFittingMaxIters", IntegerValidatorFactory
			.positive(), defaultConfig.inlierFittingMaxIters);
		inlierFittingInlierFraction = params.createDoubleField("inlierFittingInlierFraction",
			DoubleValidatorFactory.rangeInclusive(0.0, 1.0), defaultConfig.inlierFittingInlierFraction);
		showResultsTable = params.createBooleanField("showResultsTable", null,
			defaultConfig.showResultsTable);

		rtfIterNum = params.createIntField("rtfIterNum", IntegerValidatorFactory.positive(),
			defaultConfig.ransacTranslationAndFlip.iterNum);
		rtfThDist = params.createDoubleField("rtfThDist", DoubleValidatorFactory.positive(),
			defaultConfig.ransacTranslationAndFlip.thDist);
		rtfThInlr = params.createDoubleField("rtfThInlr", DoubleValidatorFactory.rangeInclusive(0.0,
			1.0), defaultConfig.ransacTranslationAndFlip.thInlr);

		hIterNum = params.createIntField("hIterNum", IntegerValidatorFactory.positive(),
			defaultConfig.ransacHomography.iterNum);
		hThDist = params.createDoubleField("hThDist", DoubleValidatorFactory.positive(),
			defaultConfig.ransacHomography.thDist);
		hThInlr = params.createDoubleField("hThInlr", DoubleValidatorFactory.rangeInclusive(0.0, 1.0),
			defaultConfig.ransacHomography.thInlr);
		hThPairDist = params.createDoubleField("hThPairDist", DoubleValidatorFactory.positive(),
			defaultConfig.ransacHomography.thPairDist);
		hThAllowedTransformChange = params.createDoubleField("hThAllowedTransformChange",
			DoubleValidatorFactory.positive(), defaultConfig.ransacHomography.thAllowedTransformChange);

		this.filters = filters;
		this.detectors = detectors;
		this.estimators = estimators;
		this.defocusing = defocusing;
	}

	@Override
	protected void layoutComponents() {
		JPanel pane = new JPanel();
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

		JPanel dataPanel = new JPanel(new GridBagLayout());
		JComboBox<String> rawImage1ComboBox = createOpenImagesComboBox(false);
		JComboBox<String> rawImage2ComboBox = createOpenImagesComboBox(false);
		rawImage1ComboBox.setPreferredSize(new Dimension(150, (int) rawImage1ComboBox.getPreferredSize()
			.getHeight()));
		rawImage2ComboBox.setPreferredSize(new Dimension(150, (int) rawImage2ComboBox.getPreferredSize()
			.getHeight()));
		rawImage1ComboBox.setSelectedIndex(0);
		rawImage2ComboBox.setSelectedIndex(1);
		if (rawImage1ComboBox.getItemCount() < 2) throw new NullPointerException();
		rawImage1Stack.registerComponent(rawImage1ComboBox);
		rawImage2Stack.registerComponent(rawImage2ComboBox);
		dataPanel.add(new JLabel("First plane:"), GridBagHelper.leftCol());
		dataPanel.add(rawImage1ComboBox, GridBagHelper.rightCol());
		dataPanel.add(new JLabel("Second plane:"), GridBagHelper.leftCol());
		dataPanel.add(rawImage2ComboBox, GridBagHelper.rightCol());
		dataPanel.setBorder(BorderFactory.createTitledBorder("Source data"));
		pane.add(dataPanel, componentConstraints);

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

		CardsPanel<DefocusFunction> defocusCards = new CardsPanel<DefocusFunction>(defocusing, 0);
		defocusName.registerComponent(defocusCards);
		p = defocusCards.getPanel("Defocus model:");
		p.setBorder(BorderFactory.createTitledBorder("3D defocusing curve"));
		pane.add(p, componentConstraints);

		JPanel additionalOptions = new JPanel(new GridBagLayout());
		additionalOptions.setBorder(BorderFactory.createTitledBorder("Additional options"));
		additionalOptions.add(new JLabel("Z stage step [nm]:"), GridBagHelper.leftCol());
		JTextField stageStepTextField = new JTextField("", 20);
		stageStep.registerComponent(stageStepTextField);
		additionalOptions.add(stageStepTextField, GridBagHelper.rightCol());
		additionalOptions.add(new JLabel("Z range limit [nm]:"), GridBagHelper.leftCol());
		JTextField zRangeTextField = new JTextField("", 20);
		zRangeLimit.registerComponent(zRangeTextField);
		additionalOptions.add(zRangeTextField, GridBagHelper.rightCol());
		additionalOptions.add(new JLabel("Save to file: "), GridBagHelper.leftCol());
		JPanel calibrationPanel = new JPanel(new BorderLayout());
		JTextField calibrationFileTextField = new JTextField(15);
		calibrationFilePath.registerComponent(calibrationFileTextField);
		calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
		calibrationPanel.add(createBrowseButton(calibrationFileTextField, true,
			new FileNameExtensionFilter("Yaml file", "yaml")), BorderLayout.EAST);
		GridBagConstraints gbc = GridBagHelper.rightCol();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		additionalOptions.add(calibrationPanel, gbc);
		pane.add(additionalOptions, componentConstraints);

		JPanel advancedOptions = new JPanel(new GridBagLayout());
		advancedOptions.setBorder(BorderFactory.createTitledBorder("Advanced calibration settings"));
		advancedOptions.add(new JLabel("Squared dist thr for z-stack matching:"), GridBagHelper
			.leftCol());
		JTextField dist2thrZStackMatchingTextField = new JTextField("", 20);
		dist2thrZStackMatching.registerComponent(dist2thrZStackMatchingTextField);
		advancedOptions.add(dist2thrZStackMatchingTextField, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Minimum fits count:"), GridBagHelper.leftCol());
		JTextField minimumFitsCountTextField = new JTextField("", 20);
		minimumFitsCount.registerComponent(minimumFitsCountTextField);
		advancedOptions.add(minimumFitsCountTextField, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Poly fit max iters:"), GridBagHelper.leftCol());
		JTextField polyFitMaxItersTextField = new JTextField("", 20);
		polyFitMaxIters.registerComponent(polyFitMaxItersTextField);
		advancedOptions.add(polyFitMaxItersTextField, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Final poly fit max iters:"), GridBagHelper.leftCol());
		JTextField finalPolyFitMaxItersTextField = new JTextField("", 20);
		finalPolyFitMaxIters.registerComponent(finalPolyFitMaxItersTextField);
		advancedOptions.add(finalPolyFitMaxItersTextField, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Min fits in z-range:"), GridBagHelper.leftCol());
		JTextField minFitsInZRangeTextField = new JTextField("", 20);
		minFitsInZRange.registerComponent(minFitsInZRangeTextField);
		advancedOptions.add(minFitsInZRangeTextField, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Moving average lag:"), GridBagHelper.leftCol());
		JTextField movingAverageLagTextField = new JTextField("", 20);
		movingAverageLag.registerComponent(movingAverageLagTextField);
		advancedOptions.add(movingAverageLagTextField, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Check if defocus is in range:"), GridBagHelper.leftCol());
		JCheckBox checkIfDefocusIsInRangeCheckBox = new JCheckBox();
		checkIfDefocusIsInRange.registerComponent(checkIfDefocusIsInRangeCheckBox);
		advancedOptions.add(checkIfDefocusIsInRangeCheckBox, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Inlier fitting max iters:"), GridBagHelper.leftCol());
		JTextField inlierFittingMaxItersTextField = new JTextField("", 20);
		inlierFittingMaxIters.registerComponent(inlierFittingMaxItersTextField);
		advancedOptions.add(inlierFittingMaxItersTextField, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Inliers fraction:"), GridBagHelper.leftCol());
		JTextField inlierFittingInlierFractionTextField = new JTextField("", 20);
		inlierFittingInlierFraction.registerComponent(inlierFittingInlierFractionTextField);
		advancedOptions.add(inlierFittingInlierFractionTextField, GridBagHelper.rightCol());
		advancedOptions.add(new JLabel("Show results table:"), GridBagHelper.leftCol());
		JCheckBox showResultsTableCheckBox = new JCheckBox();
		showResultsTable.registerComponent(showResultsTableCheckBox);
		advancedOptions.add(showResultsTableCheckBox, GridBagHelper.rightCol());

		JPanel rtfRansacOptions = new JPanel(new GridBagLayout());
		rtfRansacOptions.setBorder(BorderFactory.createTitledBorder(
			"RANSAC: rough translation and flip estimate"));
		rtfRansacOptions.add(new JLabel("Iterations:"), GridBagHelper.leftCol());
		JTextField rtfIterNumTextField = new JTextField("", 20);
		rtfIterNum.registerComponent(rtfIterNumTextField);
		rtfRansacOptions.add(rtfIterNumTextField, GridBagHelper.rightCol());
		rtfRansacOptions.add(new JLabel("Inlier distance threshold:"), GridBagHelper.leftCol());
		JTextField rtfThDistTextField = new JTextField("", 20);
		rtfThDist.registerComponent(rtfThDistTextField);
		rtfRansacOptions.add(rtfThDistTextField, GridBagHelper.rightCol());
		rtfRansacOptions.add(new JLabel("Inliers portion threshold:"), GridBagHelper.leftCol());
		JTextField rtfThInlrTextField = new JTextField("", 20);
		rtfThInlr.registerComponent(rtfThInlrTextField);
		rtfRansacOptions.add(rtfThInlrTextField, GridBagHelper.rightCol());
		advancedOptions.add(rtfRansacOptions, GridBagHelper.twoCols());

		JPanel hRansacOptions = new JPanel(new GridBagLayout());
		hRansacOptions.setBorder(BorderFactory.createTitledBorder("RANSAC: fine homography estimate"));
		hRansacOptions.add(new JLabel("Iterations:"), GridBagHelper.leftCol());
		JTextField hIterNumTextField = new JTextField("", 20);
		hIterNum.registerComponent(hIterNumTextField);
		hRansacOptions.add(hIterNumTextField, GridBagHelper.rightCol());
		hRansacOptions.add(new JLabel("Inlier distance threshold:"), GridBagHelper.leftCol());
		JTextField hThDistTextField = new JTextField("", 20);
		hThDist.registerComponent(hThDistTextField);
		hRansacOptions.add(hThDistTextField, GridBagHelper.rightCol());
		hRansacOptions.add(new JLabel("Inliers portion threshold:"), GridBagHelper.leftCol());
		JTextField hThInlrTextField = new JTextField("", 20);
		hThInlr.registerComponent(hThInlrTextField);
		hRansacOptions.add(hThInlrTextField, GridBagHelper.rightCol());
		hRansacOptions.add(new JLabel("Pair distance threshold:"), GridBagHelper.leftCol());
		JTextField hThPairDistTextField = new JTextField("", 20);
		hThPairDist.registerComponent(hThPairDistTextField);
		hRansacOptions.add(hThPairDistTextField, GridBagHelper.rightCol());
		hRansacOptions.add(new JLabel("Allowed transform change:"), GridBagHelper.leftCol());
		JTextField hThAllowedTransformChangeTextField = new JTextField("", 20);
		hThAllowedTransformChange.registerComponent(hThAllowedTransformChangeTextField);
		hRansacOptions.add(hThAllowedTransformChangeTextField, GridBagHelper.rightCol());
		advancedOptions.add(hRansacOptions, GridBagHelper.twoCols());

		pane.add(advancedOptions, componentConstraints);

		JButton defaults = new JButton("Defaults");
		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");

		defaults.addActionListener(this);
		ok.addActionListener(this);
		cancel.addActionListener(this);

		JPanel buttons = new JPanel();
		buttons.add(defaults);
		buttons.add(Box.createHorizontalStrut(30));
		buttons.add(ok);
		buttons.add(cancel);
		pane.add(buttons, componentConstraints);
		pane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

		setResizable(true);
		setLayout(new BorderLayout());
		JScrollPane scrollPane = new JScrollPane(pane, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		add(scrollPane);

		getRootPane().setDefaultButton(ok);
		params.loadPrefs();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			if ("OK".equals(e.getActionCommand())) {
				params.readDialogOptions();
				Thresholder.setActiveFilter(getActiveFilterUIIndex());
				getActiveFilterUI().readParameters();
				getActiveDetectorUI().readParameters();
				getActiveEstimatorUI().readParameters();
				getActiveDefocusFunction().readParameters();
				params.savePrefs();
				if (Recorder.record) {
					getActiveFilterUI().recordOptions();
					getActiveDetectorUI().recordOptions();
					getActiveEstimatorUI().recordOptions();
					getActiveDefocusFunction().recordOptions();
					params.recordMacroOptions();
				}
				result = JOptionPane.OK_OPTION;
				dispose();
			}
			else if ("Cancel".equals(e.getActionCommand())) {
				dispose();
			}
			else if ("Defaults".equals(e.getActionCommand())) {
				params.resetToDefaults(true);
				AnalysisOptionsDialog.resetModuleUIs(filters, detectors, estimators);
			}
		}
		catch (Exception ex) {
			IJ.handleException(ex);
		}
	}

	@Override
	public int showAndGetResult() {
		if (MacroParser.isRanFromMacro()) {
			params.readMacroOptions();
			String options = Macro.getOptions();
			getActiveFilterUI().readMacroOptions(options);
			getActiveDetectorUI().readMacroOptions(options);
			getActiveEstimatorUI().readMacroOptions(options);
			getActiveDefocusFunction().readMacroOptions(options);

			return JOptionPane.OK_OPTION;
		}
		else {
			int result = super.showAndGetResult();
			int maxScreenHeight = GraphicsEnvironment.getLocalGraphicsEnvironment()
				.getMaximumWindowBounds().height;
			if (getHeight() > maxScreenHeight) {
				setSize(getWidth(), maxScreenHeight);
			}
			return result;
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

	public DefocusFunction getActiveDefocusFunction() {
		return ModuleLoader.moduleByName(defocusing, defocusName.getValue());
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

	public ImagePlus getFirstPlaneStack() {
		return WindowManager.getImage(rawImage1Stack.getValue());
	}

	public ImagePlus getSecondPlaneStack() {
		return WindowManager.getImage(rawImage2Stack.getValue());
	}

	public CalibrationConfig getCalibrationConfig() {
		return new CalibrationConfig(dist2thrZStackMatching.getValue(), minimumFitsCount.getValue(),
			polyFitMaxIters.getValue(), finalPolyFitMaxIters.getValue(), minFitsInZRange.getValue(),
			movingAverageLag.getValue(), checkIfDefocusIsInRange.getValue(), inlierFittingMaxIters
				.getValue(), inlierFittingInlierFraction.getValue(), showResultsTable.getValue(),
			CalibrationConfig.RansacConfig.createTranslationAndFlipConfig(rtfIterNum.getValue(), rtfThDist
				.getValue(), rtfThInlr.getValue()), CalibrationConfig.RansacConfig.createHomographyConfig(
					hIterNum.getValue(), hThDist.getValue(), hThInlr.getValue(), hThPairDist.getValue(),
					hThAllowedTransformChange.getValue()));
	}
}
