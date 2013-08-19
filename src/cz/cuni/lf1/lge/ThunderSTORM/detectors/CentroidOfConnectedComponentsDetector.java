package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.applyMask;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.threshold;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Graph;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Look for pixels with their intensities equal or greater then a threshold and
 * if there are more of these pixels connected together account them as a single
 * molecule, unless a shape of these connected pixels indicates that there is in
 * fact more of them.
 */
public final class CentroidOfConnectedComponentsDetector implements IDetector, IDetectorUI {

    private boolean upsample;
    private String threshold;
    private float thresholdValue;
    private JTextField thrTextField;
    private JCheckBox upCheckBox;
    private final static String DEFAULT_THRESHOLD = "std(I-Wave.V1)";
    private final static boolean DEFAULT_UPSAMPLE = false;

    public CentroidOfConnectedComponentsDetector() throws FormulaParserException {
        this(DEFAULT_UPSAMPLE, DEFAULT_THRESHOLD);
    }

    /**
     * Filter initialization.
     *
     * @param upsample if {@code true}, the input image will be upsamplex by
     * factor of 2 to achieve more accurate recognition of two or more molecules
     * that are connected together
     * @param threshold a threshold value of intensity
     */
    public CentroidOfConnectedComponentsDetector(boolean upsample, String threshold) throws FormulaParserException {
        this.upsample = upsample;
        this.threshold = threshold;
    }

    /**
     * Detection algorithm works simply by applying a binary threshold on an
     * input image, then by upsampling, if it is set to be done, and finally
     * simple recognition if there is only one molecule or more molecules in a
     * single group of pixels region.
     *
     * In more detail this is how it is done:
     * <ol>
     * <li>apply the binary threshold</li>
     * <li>if upsampling was enabled, upsample by factor of 2</li>
     * <li>
     * perform a watershed transform (this is the trick for recognition of more
     * connected molecules; also this is why the upsampling can help, because
     * the distance transform in the watershed algorithm will assign the same
     * values to all the pixels if there are two connected molecules of radius
     * 1; after the resampling, this boundary case is no more an issue); the
     * result of the watershed transform is the same looking image but isteda of
     * intensities pixels contain molecule id
     * </li>
     * <li>
     * then we think of the image resulting of the watershed transform as an
     * undirected graph with 8-connectivity and find all connected components
     * with the same id
     * </li>
     * <li>
     * finally, positions of molecules are calculated as centroids of components
     * with the same id and ,of course, if the resampling was applied at the
     * beginning of the algorithm, then the positions are downsampled to
     * correspond with the original size of the input image
     * </li>
     * </ol>
     *
     * @param image an input image
     * @return a {@code Vector} of {@code Points} containing positions of
     * detected molecules
     */
    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) throws FormulaParserException {
        // thresholding first to make the image binary
        thresholdValue = Thresholder.getThreshold(threshold);
        threshold(image, thresholdValue, 0.0f, 255.0f);
        // watershed transform with[out] upscaling
        if(upsample) {
            image.setInterpolationMethod(FloatProcessor.NEAREST_NEIGHBOR);
            image = (FloatProcessor) image.resize(image.getWidth() * 2);
        }
        // run the watershed algorithm - it works only with ByteProcessor! that's all I need though
        ByteProcessor w = (ByteProcessor) image.convertToByte(false);
        ImageJ_EDM edm = new ImageJ_EDM();  // use this version instead of built-in ImageJ version,
        edm.setup("watershed", null);       // because the build-in version uses showStatus and
        edm.run(w);                         // showProgress methods and it disturbs my progress/status info
        image = applyMask((FloatProcessor) w.convertToFloat(), image);
        // finding a center of gravity (with subpixel precision)
        Vector<Point> detections = new Vector<Point>();
        for(Graph.ConnectedComponent c : Graph.getConnectedComponents((ImageProcessor) image, Graph.CONNECTIVITY_8)) {
            detections.add(c.centroid());
            if(upsample) {
                detections.lastElement().scaleXY(0.5);
            }
            detections.lastElement().val = null;
        }
        return detections;
    }

    @Override
    public String getName() {
        return "Centroid of connected components";
    }

    @Override
    public JPanel getOptionsPanel() {
        thrTextField = new JTextField(Prefs.get("thunderstorm.detectors.centroid.thr", DEFAULT_THRESHOLD), 20);
        upCheckBox = new JCheckBox("upsample");
        upCheckBox.setSelected(Prefs.get("thunderstorm.detectors.centroid.upsample", DEFAULT_UPSAMPLE));
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Peak intensity threshold: "), GridBagHelper.leftCol());
        panel.add(thrTextField, GridBagHelper.rightCol());
        panel.add(upCheckBox, GridBagHelper.pos_size(0, 1, 2, 1));
        return panel;
    }

    @Override
    public void readParameters() {
        threshold = thrTextField.getText();
        upsample = upCheckBox.isSelected();
        
        Prefs.set("thunderstorm.detectors.centroid.thr", threshold);
        Prefs.set("thunderstorm.detectors.centroid.upsample", upsample);
    }

    @Override
    public IDetector getImplementation() {
        return this;
    }

    @Override
    public void recordOptions() {
        if(!DEFAULT_THRESHOLD.equals(threshold)) {
            Recorder.recordOption("threshold", threshold);
        }
        if(DEFAULT_UPSAMPLE != upsample) {
            Recorder.recordOption("upsample", Boolean.toString(upsample));
        }
    }

    @Override
    public void readMacroOptions(String options) {
        threshold = Macro.getValue(options, "threshold", DEFAULT_THRESHOLD);
        upsample = Boolean.parseBoolean(Macro.getValue(options, "upsample", Boolean.toString(upsample)));
    }

    @Override
    public void resetToDefaults() {
        upCheckBox.setSelected(DEFAULT_UPSAMPLE);
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
