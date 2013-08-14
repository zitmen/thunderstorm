package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Graph;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Detection of local maxima points.
 */
public class LocalMaximaDetector implements IDetector, IDetectorUI {
  
  private int connectivity;
  private String threshold;
  private JTextField thrTextField;
  private JRadioButton conn4RadioButton, conn8RadioButton;
  private final static String DEFAULT_THRESHOLD = "10*std(F)";
  private final static int DEFAULT_CONNECTIVITY = Graph.CONNECTIVITY_8;
  
  private boolean isMax4Thr(FloatProcessor image, float thr, int x, int y, float local, boolean w, boolean e, boolean n, boolean s) {
    if (local < thr) {
      return false;
    }
    
    if (w) {
      if (image.getf(x - 1, y) > local) {
        return false;
      }
    }
    if (e) {
      if (image.getf(x + 1, y) > local) {
        return false;
      }
    }
    if (n) {
      if (image.getf(x, y - 1) > local) {
        return false;
      }
    }
    if (s) {
      if (image.getf(x, y + 1) > local) {
        return false;
      }
    }
    return true;
  }
  
  private boolean isMax8Thr(FloatProcessor image, float thr, int x, int y, float local, boolean w, boolean e, boolean n, boolean s) {
    if (isMax4Thr(image, thr, x, y, local, w, e, n, s) == false) {
      return false;
    }
    
    if (w && n) {
      if (image.getf(x - 1, y - 1) > local) {
        return false;
      }
    }
    if (w && s) {
      if (image.getf(x - 1, y + 1) > local) {
        return false;
      }
    }
    if (e && n) {
      if (image.getf(x + 1, y - 1) > local) {
        return false;
      }
    }
    if (e && s) {
      if (image.getf(x + 1, y + 1) > local) {
        return false;
      }
    }
    return true;
  }

  // the following two methods are duplicates, because of speed...this way I dont need to check every iteration if it is 4 or 8 neighbourhood version
  private Vector<Point> getMax4Candidates(FloatProcessor image, float thr) {
    Vector<Point> detections = new Vector<Point>();
    int cx = image.getWidth(), cy = image.getHeight();

    // inner part of the image
    for (int x = 1, xm = cx - 1; x < xm; x++) {
      for (int y = 1, ym = cy - 1; y < ym; y++) {
        if (isMax4Thr(image, thr, x, y, image.getf(x, y), true, true, true, true)) {
          detections.add(new Point(x, y, image.getf(x, y)));
        }
      }
    }
    // left border of the image
    for (int x = 0, y = 1, ym = cy - 1; y < ym; y++) {
      if (isMax4Thr(image, thr, x, y, image.getf(x, y), false, true, true, true)) {
        detections.add(new Point(x, y, image.getf(x, y)));
      }
    }
    // right border of the image
    for (int x = cx - 1, y = 1, ym = cy - 1; y < ym; y++) {
      if (isMax4Thr(image, thr, x, y, image.getf(x, y), true, false, true, true)) {
        detections.add(new Point(x, y, image.getf(x, y)));
      }
    }
    // top border of the image
    for (int x = 1, xm = cx - 1, y = 0; x < xm; x++) {
      if (isMax4Thr(image, thr, x, y, image.getf(x, y), true, true, false, true)) {
        detections.add(new Point(x, y, image.getf(x, y)));
      }
    }
    // bottom border of the image
    for (int x = 1, xm = cx - 1, y = cy - 1; x < xm; x++) {
      if (isMax4Thr(image, thr, x, y, image.getf(x, y), true, true, true, false)) {
        detections.add(new Point(x, y, image.getf(x, y)));
      }
    }
    // corners
    cx -= 1;
    cy -= 1;
    if (isMax4Thr(image, thr, 0, 0, image.getf(0, 0), false, true, false, true)) {
      detections.add(new Point(0, 0, image.getf(0, 0)));
    }
    if (isMax4Thr(image, thr, cx, 0, image.getf(cx, 0), true, false, false, true)) {
      detections.add(new Point(cx, 0, image.getf(cx, 0)));
    }
    if (isMax4Thr(image, thr, 0, cy, image.getf(0, cy), false, true, true, false)) {
      detections.add(new Point(0, cy, image.getf(0, cy)));
    }
    if (isMax4Thr(image, thr, cx, cy, image.getf(cx, cy), true, false, true, false)) {
      detections.add(new Point(cx, cy, image.getf(cx, cy)));
    }
    
    return detections;
  }
  
  private Vector<Point> getMax8Candidates(FloatProcessor image, float thr) {
    Vector<Point> detections = new Vector<Point>();
    int cx = image.getWidth(), cy = image.getHeight();

    // inner part of the image
    for (int x = 1, xm = cx - 1; x < xm; x++) {
      for (int y = 1, ym = cy - 1; y < ym; y++) {
        if (isMax8Thr(image, thr, x, y, image.getf(x, y), true, true, true, true)) {
          detections.add(new Point(x, y, image.getf(x, y)));
        }
      }
    }
    // left border of the image
    for (int x = 0, y = 1, ym = cy - 1; y < ym; y++) {
      if (isMax8Thr(image, thr, x, y, image.getf(x, y), false, true, true, true)) {
        detections.add(new Point(x, y, image.getf(x, y)));
      }
    }
    // right border of the image
    for (int x = cx - 1, y = 1, ym = cy - 1; y < ym; y++) {
      if (isMax8Thr(image, thr, x, y, image.getf(x, y), true, false, true, true)) {
        detections.add(new Point(x, y, image.getf(x, y)));
      }
    }
    // top border of the image
    for (int x = 1, xm = cx - 1, y = 0; x < xm; x++) {
      if (isMax8Thr(image, thr, x, y, image.getf(x, y), true, true, false, true)) {
        detections.add(new Point(x, y, image.getf(x, y)));
      }
    }
    // bottom border of the image
    for (int x = 1, xm = cx - 1, y = cy - 1; x < xm; x++) {
      if (isMax8Thr(image, thr, x, y, image.getf(x, y), true, true, true, false)) {
        detections.add(new Point(x, y, image.getf(x, y)));
      }
    }
    // corners
    cx -= 1;
    cy -= 1;
    if (isMax8Thr(image, thr, 0, 0, image.getf(0, 0), false, true, false, true)) {
      detections.add(new Point(0, 0, image.getf(0, 0)));
    }
    if (isMax8Thr(image, thr, cx, 0, image.getf(cx, 0), true, false, false, true)) {
      detections.add(new Point(cx, 0, image.getf(cx, 0)));
    }
    if (isMax8Thr(image, thr, 0, cy, image.getf(0, cy), false, true, true, false)) {
      detections.add(new Point(0, cy, image.getf(0, cy)));
    }
    if (isMax8Thr(image, thr, cx, cy, image.getf(cx, cy), true, false, true, false)) {
      detections.add(new Point(cx, cy, image.getf(cx, cy)));
    }
    
    return detections;
  }
  
  public LocalMaximaDetector() throws FormulaParserException {
    this(DEFAULT_CONNECTIVITY, DEFAULT_THRESHOLD);
  }

  /**
   * Constructor.
   *
   * @param connectivity determines in whar neighbourhood will be maxima looked
   * for
   * @param threshold points with their intensities lower than the threshold
   * will not be included in a list of molecule candidates
   */
  public LocalMaximaDetector(int connectivity, String threshold) throws FormulaParserException {
    assert ((connectivity == Graph.CONNECTIVITY_4) || (connectivity == Graph.CONNECTIVITY_8));
    
    this.connectivity = connectivity;
    this.threshold = threshold;
  }

  /**
   * Detection of candidates using the local maxima method.
   *
   * Examples:
   * <pre>
   * {@code
   * 5487
   * 1934
   * 4467}
   * </pre> Points [x=1,y=1]=9 and [x=3,y=2]=7 were detected as local maxima.
   *
   * <pre>
   * {@code
   * 5487
   * 1994
   * 4467}
   * </pre> Points [x=1,y=1]=9 and [x=2,y=1]=9 were detected as local maxima.
   *
   * @param image an input image
   * @return {@code Vector} of detected {@code Points} {x,y,I}
   */
  @Override
  public Vector<Point> detectMoleculeCandidates(FloatProcessor image) throws FormulaParserException {
    float thr = Thresholder.getThreshold(threshold);
    return ((connectivity == Graph.CONNECTIVITY_4) ? getMax4Candidates(image, thr) : getMax8Candidates(image, thr));
  }
  
  @Override
  public String getName() {
    return "Search for local maxima";
  }
  
  @Override
  public JPanel getOptionsPanel() {
    thrTextField = new JTextField(Prefs.get("thunderstorm.detectors.locmax.thr", DEFAULT_THRESHOLD), 20);
    ButtonGroup btnGroup = new ButtonGroup();
    conn4RadioButton = new JRadioButton("4-neighbourhood");
    conn8RadioButton = new JRadioButton("8-neighbourhood");
    btnGroup.add(conn4RadioButton);
    btnGroup.add(conn8RadioButton);
    //
    connectivity = Integer.parseInt(Prefs.get("thunderstorm.detectors.locmax.connectivity", "" + DEFAULT_CONNECTIVITY));
    conn4RadioButton.setSelected(connectivity == Graph.CONNECTIVITY_4);
    conn8RadioButton.setSelected(connectivity == Graph.CONNECTIVITY_8);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Peak intensity threshold: "), GridBagHelper.leftCol());
    panel.add(thrTextField, GridBagHelper.rightCol());
    panel.add(new JLabel("Connectivity: "), GridBagHelper.leftCol());
    panel.add(conn8RadioButton, GridBagHelper.rightCol());
    panel.add(conn4RadioButton, GridBagHelper.rightCol());
    return panel;
  }
  
  @Override
  public void readParameters() {
    threshold = thrTextField.getText();
    Thresholder.parseThreshold(threshold);
    if (conn4RadioButton.isSelected()) {
      connectivity = Graph.CONNECTIVITY_4;
    }
    if (conn8RadioButton.isSelected()) {
      connectivity = Graph.CONNECTIVITY_8;
    }
    
    Prefs.set("thunderstorm.detectors.locmax.thr", threshold);
    Prefs.set("thunderstorm.detectors.locmax.connectivity", "" + connectivity);
  }
  
  @Override
  public IDetector getImplementation() {
    return this;
  }
  
  @Override
  public void recordOptions() {
    if (!DEFAULT_THRESHOLD.equals(threshold)) {
      Recorder.recordOption("threshold", threshold);
    }
    if (connectivity != DEFAULT_CONNECTIVITY) {
      Recorder.recordOption("connectivity", (connectivity == Graph.CONNECTIVITY_4) ? "4" : "8");
    }
  }
  
  @Override
  public void readMacroOptions(String options) {
    threshold = Macro.getValue(options, "threshold", DEFAULT_THRESHOLD);
    String value = Macro.getValue(options, "connectivity", (DEFAULT_CONNECTIVITY == Graph.CONNECTIVITY_4) ? "4" : "8");
    connectivity = value.equals("4") ? Graph.CONNECTIVITY_4 : Graph.CONNECTIVITY_8;
    Thresholder.parseThreshold(threshold);
    
    Prefs.set("thunderstorm.detectors.locmax.thr", threshold);
    Prefs.set("thunderstorm.detectors.locmax.thr", connectivity);
  }
  
  @Override
  public void resetToDefaults() {
    conn4RadioButton.setSelected(DEFAULT_CONNECTIVITY == Graph.CONNECTIVITY_4);
    conn8RadioButton.setSelected(DEFAULT_CONNECTIVITY == Graph.CONNECTIVITY_8);
    thrTextField.setText(DEFAULT_THRESHOLD);
  }
}
