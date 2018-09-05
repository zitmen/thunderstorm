
package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import java.awt.GridBagLayout;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GrayScaleImageImpl;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.thunderstorm.algorithms.detectors.MaxFilterDetector;
import ij.process.FloatProcessor;

/**
 * Detect pixels with its intensity equal or greater then a threshold and also
 * with its value not changed after a morphological dilation is performed.
 */
public final class NonMaxSuppressionDetector extends IDetectorUI implements IDetector {

	private final String name = "Maximum filter";
	private transient final static String DEFAULT_THRESHOLD = "std(Wave.F1)";
	private transient final static int DEFAULT_RADIUS = 1;
	private transient ParameterKey.Integer RADIUS;
	private transient ParameterKey.String THRESHOLD;

	public NonMaxSuppressionDetector() throws FormulaParserException {
		this(DEFAULT_RADIUS, DEFAULT_THRESHOLD);
	}

	/**
	 * Initialize the filter.
	 *
	 * @param radius a radius of morphological dilation
	 * @param threshold a threshold value
	 */
	public NonMaxSuppressionDetector(int radius, String threshold) throws FormulaParserException {
		THRESHOLD = parameters.createStringField("threshold", null, DEFAULT_THRESHOLD);
		RADIUS = parameters.createIntField("radius", IntegerValidatorFactory.positiveNonZero(),
			DEFAULT_RADIUS);
	}

	@Override
	protected String getPreferencesPrefix() {
		return super.getPreferencesPrefix() + ".nonmaxsup";
	}

	/**
	 * Detection is performed by applying a grayscale dilation with square uniform
	 * kernel of specified radius and then selecting points with their intensity
	 * same before and after the dilation and at the same time at least as high as
	 * a specified threshold.
	 *
	 * @param image an input image
	 * @return a {@code Vector} of {@code Points} containing positions of detected
	 *         molecules
	 */
	@Override
	public List<Point> detectMoleculeCandidates(FloatProcessor image) throws FormulaParserException {
		MaxFilterDetector detector = new MaxFilterDetector(RADIUS.getValue(), Thresholder.getThreshold(
			THRESHOLD.getValue()));
		return detector.detect(new GrayScaleImageImpl(image)).stream().map(point2D -> new Point<Double>(
			point2D.getX(), point2D.getY(), (double) image.getPixelValue((int) point2D.getX(),
				(int) point2D.getY()))).collect(Collectors.toList());
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public JPanel getOptionsPanel() {
		JTextField thrTextField = new JTextField("", 20);
		JTextField radiusTextField = new JTextField("", 20);
		parameters.registerComponent(THRESHOLD, thrTextField);
		parameters.registerComponent(RADIUS, radiusTextField);
		//
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(new JLabel("Peak intensity threshold: "), GridBagHelper.leftCol());
		panel.add(thrTextField, GridBagHelper.rightCol());
		panel.add(new JLabel("Dilation radius [px]: "), GridBagHelper.leftCol());
		panel.add(radiusTextField, GridBagHelper.rightCol());

		parameters.loadPrefs();
		return panel;
	}

	@Override
	public IDetector getImplementation() {
		return new NonMaxSuppressionDetector(RADIUS.getValue(), THRESHOLD.getValue());
	}

	@Override
	public String getThresholdFormula() {
		return THRESHOLD.getValue();
	}

	@Override
	public float getThresholdValue() {
		return Thresholder.getThreshold(THRESHOLD.getValue());
	}
}
