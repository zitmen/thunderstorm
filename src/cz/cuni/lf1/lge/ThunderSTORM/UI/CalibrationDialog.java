package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.process.FloatProcessor;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
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

public class CalibrationDialog extends JDialog implements ActionListener {

    private CardsPanel<IFilterUI> filters;
    private CardsPanel<IDetectorUI> detectors;
    private CardsPanel<IEstimatorUI> estimators;
    private JButton defaults, preview, ok, cancel, findCalibrationButton;
    private JTextField calibrationFileTextField, stageStepTextField;
    private double stageStep;
    JTextField fitRegionTextArea;
    private Semaphore semaphore;    // ensures waiting for a dialog without the dialog being modal!
    private int dialogResult = JOptionPane.CLOSED_OPTION;
    ExecutorService previewThredRunner = Executors.newSingleThreadExecutor();
    Future<?> previewFuture = null;

    public CalibrationDialog(List<IFilterUI> filters, List<IDetectorUI> detectors, List<IEstimatorUI> estimators) {
        super(IJ.getInstance(), "Calibration options");
        this.filters = new CardsPanel<IFilterUI>(filters, Integer.parseInt(Prefs.get("thunderstorm.filters.index", "0")));
        this.detectors = new CardsPanel<IDetectorUI>(detectors, Integer.parseInt(Prefs.get("thunderstorm.detectors.index", "0")));
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

        JPanel stageOffsetPanel = new JPanel(new GridBagLayout());
        stageOffsetPanel.add(new JLabel("Z stage step [nm]:"), GridBagHelper.leftCol());
        stageStepTextField = new JTextField(Prefs.get("thunderstorm.calibration.step", "10"), 20);
        stageOffsetPanel.add(stageStepTextField, GridBagHelper.rightCol());
        pane.add(stageOffsetPanel, componentConstraints);

        JPanel aditionalOptions = new JPanel(new GridBagLayout());
        aditionalOptions.add(new JLabel("Save to: "), GridBagHelper.leftCol());
        JPanel calibrationPanel = new JPanel(new BorderLayout());
        calibrationFileTextField = new JTextField(20);
        findCalibrationButton = new JButton("Browse...");
        calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
        calibrationPanel.add(findCalibrationButton, BorderLayout.EAST);
        GridBagConstraints gbc = GridBagHelper.rightCol();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        aditionalOptions.add(calibrationPanel, gbc);
        pane.add(aditionalOptions, componentConstraints);

        pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);

        defaults = new JButton("Defaults");
        preview = new JButton("Preview");
        ok = new JButton("OK");
        cancel = new JButton("Cancel");

        defaults.addActionListener(this);
        preview.addActionListener(this);
        ok.addActionListener(this);
        cancel.addActionListener(this);
        findCalibrationButton.addActionListener(this);
        //
        JPanel buttons = new JPanel();
        buttons.add(defaults);
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(preview);
        buttons.add(ok);
        buttons.add(cancel);
        pane.add(buttons, componentConstraints);
        getRootPane().setDefaultButton(ok);
        pack();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if("Preview".equals(e.getActionCommand())) {
            final ImagePlus imp = IJ.getImage();
            Thresholder.setActiveFilter(filters.getActiveComboBoxItemIndex());
            // parse parameters
            try {
                filters.getActiveComboBoxItem().readParameters();
                detectors.getActiveComboBoxItem().readParameters();
                estimators.getActiveComboBoxItem().readParameters();
                filters.getActiveComboBoxItem().resetThreadLocal();
                detectors.getActiveComboBoxItem().resetThreadLocal();
                estimators.getActiveComboBoxItem().resetThreadLocal();
                stageStep = Double.parseDouble(stageStepTextField.getText());

                Prefs.set("thunderstorm.calibration.step", stageStepTextField.getText());
                AnalysisOptionsDialog.saveSelectedModuleIndexesToPrefs(filters.getActiveComboBoxItemIndex(), detectors.getActiveComboBoxItemIndex(), -1, -1);
            } catch(Exception ex) {
                IJ.error("Error parsing parameters: " + ex.toString());
                return;
            }
            //if another preview task is still running, cancel it
            if(previewFuture != null) {
                previewFuture.cancel(true);
            }
            //do the preview task
            previewFuture = previewThredRunner.submit(new Runnable() {
                

                @Override
                public void run() {
                    try {
                        IJ.showStatus("Creating preview image.");
                        FloatProcessor fp = (FloatProcessor) imp.getProcessor().crop().convertToFloat();
                        Roi roi = imp.getRoi();
                        if(roi != null) {
                            fp.setMask(roi.getMask());
                        }
                        Thresholder.setCurrentImage(fp);
                        FloatProcessor filtered = getActiveFilterUI().getThreadLocalImplementation().filterImage(fp);
                        new ImagePlus("ThunderSTORM: filtered frame " + Integer.toString(imp.getSlice()), filtered).show();
                        GUI.checkIJEscapePressed();
                        List<Point> detections = Point.applyRoiMask(imp.getRoi(), getActiveDetectorUI().getThreadLocalImplementation().detectMoleculeCandidates(filtered));
                        GUI.checkIJEscapePressed();
                        //
                        double[] xCoord = new double[detections.size()];
                        double[] yCoord = new double[detections.size()];
                        for(int i = 0; i < detections.size(); i++) {
                            xCoord[i] = detections.get(i).getX().doubleValue();
                            yCoord[i] = detections.get(i).getY().doubleValue();
                        }
                        //
                        ImagePlus impPreview = new ImagePlus("ThunderSTORM preview for frame " + Integer.toString(imp.getSlice()), imp.getProcessor().crop());
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

        } else if("Browse...".equals(e.getActionCommand())) {
            JFileChooser fileChooser = new JFileChooser(IJ.getDirectory("image"));
            int userAction = fileChooser.showSaveDialog(null);
            if(userAction == JFileChooser.APPROVE_OPTION) {
                calibrationFileTextField.setText(fileChooser.getSelectedFile().getPath());
            }
        } else if("OK".equals(e.getActionCommand())) {
            try {
                Thresholder.setActiveFilter(filters.getActiveComboBoxItemIndex());
                filters.getActiveComboBoxItem().readParameters();
                detectors.getActiveComboBoxItem().readParameters();
                estimators.getActiveComboBoxItem().readParameters();
                stageStep = Double.parseDouble(stageStepTextField.getText());

                Prefs.set("thunderstorm.calibration.step", stageStepTextField.getText());
                AnalysisOptionsDialog.saveSelectedModuleIndexesToPrefs(filters.getActiveComboBoxItemIndex(), detectors.getActiveComboBoxItemIndex(), -1, -1);
                //
                dialogResult = JOptionPane.OK_OPTION;
                dispose();
            } catch(Exception ex) {
                IJ.error("Error parsing parameters: " + ex.toString());
            }
        } else if("Cancel".equals(e.getActionCommand())) {
            dialogResult = JOptionPane.CANCEL_OPTION;
            dispose();
        } else if("Defaults".equals(e.getActionCommand())) {
            stageStepTextField.setText("10");
            AnalysisOptionsDialog.resetModuleUIs(filters.getItems(), detectors.getItems(), estimators.getItems());
            AnalysisOptionsDialog.resetCardsPanels(filters, detectors, estimators);
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
    
    public double getStageStep(){
        return stageStep;
    }

    @Override
    public void dispose() {
        semaphore.release();
        super.dispose();
        if(previewThredRunner != null) {
            previewThredRunner.shutdownNow();
        }
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
        } catch(InterruptedException ex) {
            IJ.handleException(ex);
        }
        return dialogResult;
    }
}
