package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.ThresholdFormulaException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

/**
 *
 */
public class CalibrationDialog extends JDialog implements ActionListener {

  private CardsPanel<IFilterUI> filters;
  private CardsPanel<IDetectorUI> detectors;
  private CardsPanel<IEstimatorUI> estimators;
  private JButton preview, ok, cancel, findCalibrationButton;
  private JTextField calibrationFileTextField;
  JTextField fitRegionTextArea;
  private Semaphore semaphore;    // ensures waiting for a dialog without the dialog being modal!
  private int dialogResult = JOptionPane.CLOSED_OPTION;
  ExecutorService previewThredRunner = Executors.newSingleThreadExecutor();
  Future<?> previewFuture = null;

  public CalibrationDialog(List<IFilterUI> filters, List<IDetectorUI> detectors, List<IEstimatorUI> estimators) {
    super(IJ.getInstance(), "Calibration options");
    this.filters = new CardsPanel<IFilterUI>(filters, 0);
    this.detectors = new CardsPanel<IDetectorUI>(detectors, 0);
    this.estimators = new CardsPanel<IEstimatorUI>(estimators, 0);
    semaphore = new Semaphore(0);
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
    pane.add(estimators.getPanel("Estimator: "), componentConstraints);
    pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);

    JPanel aditionalOptions = new JPanel(new GridBagLayout());
    aditionalOptions.add(new JLabel("Save to: "), GridBagHelper.leftCol());
    JPanel calibrationPanel = new JPanel(new BorderLayout());
    calibrationFileTextField = new JTextField(20);
    findCalibrationButton = new JButton("Find");
    calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
    calibrationPanel.add(findCalibrationButton, BorderLayout.EAST);
    GridBagConstraints gbc = GridBagHelper.rightCol();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    aditionalOptions.add(calibrationPanel, gbc);
    pane.add(aditionalOptions, componentConstraints);

    pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);

    preview = new JButton("Preview");
    ok = new JButton("OK");
    cancel = new JButton("Cancel");
    preview.addActionListener(this);
    ok.addActionListener(this);
    cancel.addActionListener(this);
    findCalibrationButton.addActionListener(this);
    //
    JPanel buttons = new JPanel();
    buttons.add(preview);
    buttons.add(Box.createHorizontalStrut(30));
    buttons.add(ok);
    buttons.add(cancel);
    pane.add(buttons, componentConstraints);
    getRootPane().setDefaultButton(ok);
    pack();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    if ("Preview".equals(e.getActionCommand())) {
      final ImagePlus imp = IJ.getImage();
      Thresholder.setActiveFilter(filters.getActiveComboBoxItemIndex());
      //
      try {
        filters.getActiveComboBoxItem().readParameters();
        detectors.getActiveComboBoxItem().readParameters();
        estimators.getActiveComboBoxItem().readParameters();

      } catch (ThresholdFormulaException ex) {
        IJ.log("Thresholding: " + ex.getMessage());
      } catch (Exception ex) {
        IJ.log(ex.toString() + "\n" + Arrays.toString(ex.getStackTrace()));
      }
      if (previewFuture != null) {
        previewFuture.cancel(true);
      }
      previewFuture = previewThredRunner.submit(new Runnable() {
        void checkForInterruption() throws InterruptedException {
          if (Thread.interrupted()) {
            throw new InterruptedException();
          }
          if (IJ.escapePressed()) {
            IJ.resetEscape();
            throw new InterruptedException();
          }
        }

        @Override
        public void run() {
          try {
            IJ.showStatus("Creating preview image.");
            FloatProcessor fp = (FloatProcessor) imp.getProcessor().convertToFloat();
            FloatProcessor filtered = getActiveFilterUI().getImplementation().filterImage(fp);
            checkForInterruption();
            List<Point> detections = getActiveDetectorUI().getImplementation().detectMoleculeCandidates(filtered);
            checkForInterruption();
            //
            double[] xCoord = new double[detections.size()];
            double[] yCoord = new double[detections.size()];
            for (int i = 0; i < detections.size(); i++) {
              xCoord[i] = detections.get(i).getX().doubleValue();
              yCoord[i] = detections.get(i).getY().doubleValue();
            }
            //
            ImagePlus impPreview = new ImagePlus("ThunderSTORM preview for frame " + Integer.toString(imp.getSlice()), imp.getProcessor().duplicate());
            RenderingOverlay.showPointsInImage(impPreview, xCoord, yCoord, Color.red, RenderingOverlay.MARKER_CROSS);
            impPreview.show();
          } catch (InterruptedException ex) {
            IJ.showStatus("Preview interrupted.");
          } catch (Exception ex) {
            IJ.log(ex.toString() + "\n" + Arrays.toString(ex.getStackTrace()));
          }
        }
      });

    } else if ("Find".equals(e.getActionCommand())) {
      JFileChooser fileChooser = new JFileChooser(IJ.getDirectory("image"));
      int userAction = fileChooser.showSaveDialog(null);
      if (userAction == JFileChooser.APPROVE_OPTION) {
        calibrationFileTextField.setText(fileChooser.getSelectedFile().getPath());
      }
    } else if ("OK".equals(e.getActionCommand())) {
      try {
        filters.getActiveComboBoxItem().readParameters();
        detectors.getActiveComboBoxItem().readParameters();
        estimators.getActiveComboBoxItem().readParameters();
        //
        dialogResult = JOptionPane.OK_OPTION;
        dispose();
      } catch (Exception ex) {
        IJ.error(ex.toString());
      }
    } else if ("Cancel".equals(e.getActionCommand())) {
      dialogResult = JOptionPane.CANCEL_OPTION;
      dispose();
    }
  }

  public IFilterUI getActiveFilterUI() {
    return filters.getActiveComboBoxItem();
  }

  public IDetectorUI getActiveDetectorUI() {
    return detectors.getActiveComboBoxItem();
  }

  public IEstimatorUI getActiveEstimatorUI() {
    return estimators.getActiveComboBoxItem();
  }

  public String getSavePath() {
    return calibrationFileTextField.getText();
  }

  @Override
  public void dispose() {
    semaphore.release();
    super.dispose();
  }

  /**
   * Blocks until the user closes the dialog in any way and returns the user
   * action.
   *
   * @return JOptionPane.OK_OPTION or JOptionPane.CANCEL_OPTION or
   * JOptionPane.CLOSED_OPTION
   */
  public int waitForResult() {
    try {
      semaphore.acquire();
    } catch (InterruptedException ex) {
      IJ.error(ex.getMessage());
    }
    return dialogResult;
  }
}
