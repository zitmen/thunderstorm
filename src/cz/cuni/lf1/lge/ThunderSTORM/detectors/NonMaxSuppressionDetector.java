package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Morphology;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Detect pixels with its intensity equal or greater then a threshold and also
 * with its value not changed after a morphological dilation is performed.
 */
public final class NonMaxSuppressionDetector extends  IDetectorUI implements IDetector {

    private final String name = "Non-maximum suppression";
    private int radius;
    private String threshold;
    private transient float thresholdValue;
    private transient JTextField thrTextField;
    private transient JTextField radiusTextField;
    private transient final static String DEFAULT_THRESHOLD = "6*std(F)";
    private transient final static int DEFAULT_RADIUS = 3;

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
        this.radius = radius;
        this.threshold = threshold;
    }

    /**
     * Detection is performed by applying a grayscale dilation with square
     * uniform kernel of specified radius and then selecting points with their
     * intensity same before and after the dilation and at the same time at
     * least as high as a specified threshold.
     *
     * @param image an input image
     * @return a {@code Vector} of {@code Points} containing positions of
     * detected molecules
     */
    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) throws FormulaParserException {
        Vector<Point> detections = new Vector<Point>();
        FloatProcessor mx = Morphology.dilateBox(image, radius);

        float imval, mxval;
        thresholdValue = Thresholder.getThreshold(threshold);
        for(int x = radius / 2, xm = image.getWidth() - radius / 2; x < xm; x++) {
            for(int y = radius / 2, ym = image.getHeight() - radius / 2; y < ym; y++) {
                imval = image.getf(x, y);
                mxval = mx.getf(x, y);
                if((mxval == imval) && (imval >= thresholdValue)) {
                    detections.add(new Point(x, y, imval));
                }
            }
        }

        return detections;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        thrTextField = new JTextField(Prefs.getString("thunderstorm.detectors.nonmaxsup.thr", threshold), 20);
        radiusTextField = new JTextField(Integer.toString(Prefs.getInt("thunderstorm.detectors.nonmaxsup.radius", radius)), 20);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Peak intensity threshold: "), GridBagHelper.leftCol());
        panel.add(thrTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Dilation radius [px]: "), GridBagHelper.leftCol());
        panel.add(radiusTextField, GridBagHelper.rightCol());
        return panel;
    }

    @Override
    public void readParameters() {
        threshold = thrTextField.getText();
        radius = Integer.parseInt(radiusTextField.getText());

        Prefs.set("thunderstorm.detectors.nonmaxsup.thr", threshold);
        Prefs.set("thunderstorm.detectors.nonmaxsup.radius", radius);
    }

    @Override
    public IDetector getImplementation() {
        return new NonMaxSuppressionDetector(radius, threshold);
    }

    @Override
    public void recordOptions() {
        if(!DEFAULT_THRESHOLD.equals(threshold)) {
            Recorder.recordOption("threshold", threshold);
        }
        if(radius != DEFAULT_RADIUS) {
            Recorder.recordOption("radius", Integer.toString(radius));
        }
    }

    @Override
    public void readMacroOptions(String options) {
        threshold = Macro.getValue(options, "threshold", DEFAULT_THRESHOLD);
        radius = Integer.parseInt(Macro.getValue(options, "radius", Integer.toString(DEFAULT_RADIUS)));
    }

    @Override
    public void resetToDefaults() {
        radiusTextField.setText(Integer.toString(DEFAULT_RADIUS));
        thrTextField.setText(DEFAULT_THRESHOLD);
    }

    @Override
    public String getThresholdFormula() {
        return threshold;
    }
    
    @Override
    public float getThresholdValue() {
        return thresholdValue;
    }
}
