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
 * Dialog with settings of filters, detectors, estimators, and other parameters used for analysis.
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
   * Initialize and show the analysis options dialog.
   *
   * @param imp {@code ImagePlus} that was active when the plugin was executed
   * @param title title of the frame
   * @param filters vector of filter modules (they all must implement {@code IFilter} interface)
   * @param default_filter {@code filters[default_filter]} will be initially selected in combo box
   * @param detectors vector of detector modules (they all must implement {@code IDetector} interface)
   * @param default_detector {@code detector[default_detector]} will be initially selected in combo box
   * @param estimators vector of estimator modules (they all must implement {@code IEstimator} interface)
   * @param default_estimator {@code estimator[default_estimator]} will be initially selected in combo box
   */
  public AnalysisOptionsDialog(ImagePlus imp, String title, Vector<IModule> filters, int default_filter, Vector<IModule> detectors, int default_detector, Vector<IModule> estimators, int default_estimator) {
    super((JFrame) null, title);
    //
    this.canceled = true;
    //
    this.imp = imp;
    //
    this.filters = new CardsPanel(filters, default_filter);
    this.detectors = new CardsPanel(detectors, default_detector);
    this.estimators = new CardsPanel(estimators, default_estimator);
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

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    addComponentsToPane();
  }

  private void addComponentsToPane() {
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

    pack();
  }

  /**
   * Action handler.
   * 
   * There are three possible actions. Canceling the analysis, confirming the settings of analysis,
   * and preview the results of analysis on a single frame selected in active {@code ImagePlus} window.
   * 
   * @param e event object holding the action details. It gets processed as follows:
   *          <ul>
   *            <li>actionCommand == "Cancel": cancel the analysis</li>
   *            <li>actionCommand == "Ok": confirm the settings and run the analysis</li>
   *            <li>actionCommand == "Preview": preview the results of analysis with the current selected settings on a single frame</li>
   *          </ul>
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (e.getActionCommand().equals("Cancel")) {
      closeDialog(true);
    } else if (e.getActionCommand().equals("Ok")) {
      activeFilter = (IFilter) filters.getActiveComboBoxItem();
      activeDetector = (IDetector) detectors.getActiveComboBoxItem();
      activeEstimator = (IEstimator) estimators.getActiveComboBoxItem();
      //
      ((IModule) activeFilter).readParameters();
      ((IModule) activeDetector).readParameters();
      ((IModule) activeEstimator).readParameters();
      //
      closeDialog(false);
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
   * Override the default {@code JDialog.dispose} method to release the {@code semaphore} (see {@wasCanceled}).
   */
  @Override
  public void dispose() {
    super.dispose();
    semaphore.release();
  }

  /**
   * Close (dispose) the dialog.
   *
   * @param cancel is the dialog closing because the operation has been canceled?
   */
  public void closeDialog(boolean cancel) {
    canceled = cancel;
    dispose();
  }

  /**
   * Query if the dialog was closed by canceling (cancel button or red cross window button)
   * or by clicking on OK.
   * 
   * <strong>This is a blocking call!</strong> Meaning that when creating a non-modal dialog
   * it is created and runs in its own thread and does not block the creator thread.
   * Call this method, however, calls {@code semaphore.acquire}, which is a blocking call
   * and waits until the semaphore is released (if it wasn't already) which is done after closing
   * the dialog. Clearly, if this wasn't a blocking call, there wouldn't be a way to know how was
   * the dialog closed, because it wouldn't need to be closed at the time of calling this method.
   * 
   * @return {@code true} if the dialog was canceled, {@code false} otherwise
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
   * Return a filter selected from the combo box.
   *
   * @return selected filter
   */
  public IFilter getFilter() {
    return activeFilter;
  }

  /**
   * Return a detector selected from the combo box.
   *
   * @return selected detector
   */
  public IDetector getDetector() {
    return activeDetector;
  }

  /**
   * Return an estimator selected from the combo box.
   *
   * @return selected estimator
   */
  public IEstimator getEstimator() {
    return activeEstimator;
  }
}
