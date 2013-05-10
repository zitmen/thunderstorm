package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public class AnalysisOptionsDialog extends JDialog implements ActionListener {

  private CardsPanel filters, detectors, estimators;
  private JButton preview, ok, cancel;
  private ImagePlus imp;
  private boolean canceled;
  private Semaphore semaphore;    // ensures waiting for a dialog without the dialog being modal!
  private IFilter activeFilter;
  private IDetector activeDetector;
  private IEstimator activeEstimator;

  /**
   *
   * @param imp
   * @param command
   * @param filters
   * @param default_filter
   * @param detectors
   * @param default_detector
   * @param estimators
   * @param default_estimator
   */
  public AnalysisOptionsDialog(ImagePlus imp, String command, Vector<IModule> filters, int default_filter, Vector<IModule> detectors, int default_detector, Vector<IModule> estimators, int default_estimator) {
    super((JFrame) null, command);
    //
    this.canceled = false;
    //
    this.imp = imp;
    //
    this.filters = new CardsPanel(filters);
    this.detectors = new CardsPanel(detectors);
    this.estimators = new CardsPanel(estimators);
    //
    this.filters.setDefaultComboBoxItem(default_filter);
    this.detectors.setDefaultComboBoxItem(default_detector);
    this.estimators.setDefaultComboBoxItem(default_estimator);
    //
    this.preview = new JButton("Preview");
    this.ok = new JButton("Ok");
    this.cancel = new JButton("Cancel");
    //
    this.semaphore = new Semaphore(0);
    //
    // Outputs from this dialog
    this.activeFilter = null;
    this.activeDetector = null;
    this.activeEstimator = null;
  }

  /**
   *
   */
  public void addComponentsToPane() {
    Container pane = getContentPane();
    //
    pane.setLayout(new GridBagLayout());
    GridBagConstraints componentConstraints = new GridBagConstraints();
    componentConstraints.gridx = 0;
    componentConstraints.insets = new Insets(10, 5, 10, 5);
    componentConstraints.fill = GridBagConstraints.BOTH;
    componentConstraints.weightx = 1;
    GridBagConstraints lineConstraints = (GridBagConstraints) componentConstraints.clone();
    lineConstraints.insets = new Insets(0, 0, 0, 0);

    pane.add(filters.getPanel("Filters: "), componentConstraints);
    pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);
    pane.add(detectors.getPanel("Detectors: "), componentConstraints);
    pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);
    pane.add(estimators.getPanel("Estimators: "), componentConstraints);
    pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);
    //
    preview.addActionListener(this);
    ok.addActionListener(this);
    cancel.addActionListener(this);
    //
    JPanel buttons = new JPanel();
    buttons.add(preview);
    buttons.add(Box.createHorizontalStrut(30));
    buttons.add(ok);
    buttons.add(cancel);
    pane.add(buttons, componentConstraints);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Cancel")) {
      dispose(true);
    } else if (e.getActionCommand().equals("Ok")) {
      activeFilter = (IFilter) filters.getActiveComboBoxItem();
      activeDetector = (IDetector) detectors.getActiveComboBoxItem();
      activeEstimator = (IEstimator) estimators.getActiveComboBoxItem();
      //
      ((IModule) activeFilter).readParameters();
      ((IModule) activeDetector).readParameters();
      ((IModule) activeEstimator).readParameters();
      //
      dispose(false);
    } else if (e.getActionCommand().equals("Preview")) {
      activeFilter = (IFilter) filters.getActiveComboBoxItem();
      activeDetector = (IDetector) detectors.getActiveComboBoxItem();
      activeEstimator = (IEstimator) estimators.getActiveComboBoxItem();
      //
      ((IModule) activeFilter).readParameters();
      ((IModule) activeDetector).readParameters();
      ((IModule) activeEstimator).readParameters();
      //
      FloatProcessor fp = (FloatProcessor) imp.getProcessor().convertToFloat();
      Vector<PSF> results = activeEstimator.estimateParameters(fp, activeDetector.detectMoleculeCandidates(activeFilter.filterImage(fp)));
      //
      double[] xCoord = new double[results.size()];
      double[] yCoord = new double[results.size()];
      for (int i = 0; i < results.size(); i++) {
        xCoord[i] = results.elementAt(i).xpos;
        yCoord[i] = results.elementAt(i).ypos;
      }
      //
      ImagePlus impPreview = new ImagePlus("ThunderSTORM preview for frame " + Integer.toString(imp.getSlice()), fp);
      RenderingOverlay.showPointsInImage(impPreview, xCoord, yCoord, Color.red, RenderingOverlay.MARKER_CROSS);
      impPreview.show();
    } else {
      throw new UnsupportedOperationException("Command '" + e.getActionCommand() + "' is not supported!");
    }
  }

  /**
   *
   * @param cancel
   */
  public void dispose(boolean cancel) {
    canceled = cancel;
    semaphore.release();
    dispose();
  }

  /**
   *
   * @return
   */
  public boolean wasCanceled() {
    try {
      semaphore.acquire();
    } catch (InterruptedException ex) {
      IJ.error(ex.getMessage());
    }
    return canceled;
  }

  /**
   *
   * @return
   */
  public IFilter getFilter() {
    return activeFilter;
  }

  /**
   *
   * @return
   */
  public IDetector getDetector() {
    return activeDetector;
  }

  /**
   *
   * @return
   */
  public IEstimator getEstimator() {
    return activeEstimator;
  }
}