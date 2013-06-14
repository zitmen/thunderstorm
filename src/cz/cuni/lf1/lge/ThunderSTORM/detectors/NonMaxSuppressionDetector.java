package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Morphology;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.Macro;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Detect pixels with its intensity equal or greater then a threshold and also with its
 * value not changed after a morphological dilation is performed.
 */
public final class NonMaxSuppressionDetector implements IDetector, IDetectorUI {

    private int radius;
    private String threshold;
    private JTextField thrTextField;
    private JTextField radiusTextField;
    private final static String DEFAULT_THRESHOLD = "6*std(F)";
    private final static int DEFAULT_RADIUS = 3;
    
    public NonMaxSuppressionDetector() throws ThresholdFormulaException {
      this(DEFAULT_RADIUS, DEFAULT_THRESHOLD);
    }
    
    /**
     * Initialize the filter.
     * 
     * @param radius a radius of morphological dilation
     * @param threshold a threshold value
     */
    public NonMaxSuppressionDetector(int radius, String threshold) throws ThresholdFormulaException {
        this.radius = radius;
        this.threshold = threshold;
    }

    /**
     * Detection is performed by applying a grayscale dilation with square uniform kernel
     * of specified radius and then selecting points with their intensity same before and after
     * the dilation and at the same time at least as high as a specified threshold.
     *
     * @param image an input image
     * @return  a {@code Vector} of {@code Points} containing positions of detected molecules
     */
    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) throws ThresholdFormulaException {
        Vector<Point> detections = new Vector<Point>();
        FloatProcessor mx = Morphology.dilateBox(image, radius);
        
        float imval, mxval, thr = Thresholder.getThreshold(threshold);
        for(int x = radius/2, xm = image.getWidth()-radius/2; x < xm; x++) {
            for(int y = radius/2, ym = image.getHeight()-radius/2; y < ym; y++) {
                imval = image.getf(x, y);
                mxval = mx.getf(x, y);
                if((mxval == imval) && (imval >= thr))
                    detections.add(new Point(x, y, imval));
            }
        }
        
        return detections;
    }
   

  @Override
  public String getName() {
    return "Non-maxima suppression";
  }

  @Override
  public JPanel getOptionsPanel() {
    thrTextField = new JTextField(DEFAULT_THRESHOLD, 20);
    radiusTextField = new JTextField(Integer.toString(DEFAULT_RADIUS), 20);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Threshold: "), GridBagHelper.leftCol());
    panel.add(thrTextField, GridBagHelper.rightCol());
    panel.add(new JLabel("Radius: "), GridBagHelper.leftCol());
    panel.add(radiusTextField, GridBagHelper.rightCol());
    return panel;
  }

  @Override
  public void readParameters() {
    threshold = thrTextField.getText();
    Thresholder.parseThreshold(threshold);
    radius = Integer.parseInt(radiusTextField.getText());
  }

  @Override
  public IDetector getImplementation() {
    return new NonMaxSuppressionDetector(radius, threshold);
  }

  @Override
  public void recordOptions() {
    if (!DEFAULT_THRESHOLD.equals(threshold)){
      Recorder.recordOption("threshold", threshold);
    }
    if (radius != DEFAULT_RADIUS){
      Recorder.recordOption("radius", Integer.toString(radius));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    threshold = Macro.getValue(options, "threshold", DEFAULT_THRESHOLD);
    radius = Integer.parseInt(Macro.getValue(options, "radius", Integer.toString(DEFAULT_RADIUS)));
    Thresholder.parseThreshold(threshold);
  }
}
