package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.subtract;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.process.FloatProcessor;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

/**
 * Dialog with settings of filters, detectors, estimators, and other parameters
 * used for analysis.
 */
public class AnalysisOptionsDialog extends JDialog implements ActionListener {

    private CardsPanel<IFilterUI> filtersPanel;
    private CardsPanel<IDetectorUI> detectorsPanel;
    private CardsPanel<IEstimatorUI> estimatorsPanel;
    private CardsPanel<IRendererUI> renderersPanel;
    private List<IFilterUI> allFilters;
    private List<IDetectorUI> allDetectors;
    private List<IEstimatorUI> allEstimators;
    private List<IRendererUI> allRenderers;
    private JButton cameraSetup, defaults, preview, ok, cancel;
    private ImagePlus imp;
    private boolean canceled;
    private Semaphore semaphore;    // ensures waiting for a dialog without the dialog being modal!
    private int activeFilterIndex;
    private int activeDetectorIndex;
    private int activeEstimatorIndex;
    private int activeRendererIndex;
    ExecutorService previewThreadRunner = Executors.newSingleThreadExecutor();
    Future<?> previewFuture = null;

    /**
     * Initialize and show the analysis options dialog.
     *
     * @param imp {@code ImagePlus} that was active when the plugin was executed
     * @param title title of the frame
     * @param filters vector of filter modules (they all must implement
     * {@code IFilter} interface)
     * @param default_filter {@code filters[default_filter]} will be initially
     * selected in combo box
     * @param detectors vector of detector modules (they all must implement
     * {@code IDetector} interface)
     * @param default_detector {@code detector[default_detector]} will be
     * initially selected in combo box
     * @param estimators vector of estimator modules (they all must implement
     * {@code IEstimator} interface)
     * @param default_estimator {@code estimator[default_estimator]} will be
     * initially selected in combo box
     */
    public AnalysisOptionsDialog(final ImagePlus imp, String title,
            List<IFilterUI> filters,
            List<IDetectorUI> detectors,
            List<IEstimatorUI> estimators,
            List<IRendererUI> renderers) {
        //
        super(IJ.getInstance(), title);
        //
        this.canceled = true;
        //
        this.imp = imp;
        //
        this.cameraSetup = new JButton("Camera setup...");
        this.cameraSetup.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new CameraSetupPlugIn().run(null);
            }
        });
        //
        this.allFilters = filters;
        this.allDetectors = detectors;
        this.allEstimators = estimators;
        this.allRenderers = renderers;
        //
        this.activeFilterIndex = Integer.parseInt(Prefs.get("thunderstorm.filters.index", "0"));
        this.activeDetectorIndex = Integer.parseInt(Prefs.get("thunderstorm.detectors.index", "0"));
        this.activeEstimatorIndex = Integer.parseInt(Prefs.get("thunderstorm.estimators.index", "0"));
        this.activeRendererIndex = Integer.parseInt(Prefs.get("thunderstorm.rendering.index", "0"));
        //
        this.filtersPanel = new CardsPanel<IFilterUI>(filters, activeFilterIndex);
        this.detectorsPanel = new CardsPanel<IDetectorUI>(detectors, activeDetectorIndex);
        this.estimatorsPanel = new CardsPanel<IEstimatorUI>(estimators, activeEstimatorIndex);
        this.renderersPanel = new CardsPanel<IRendererUI>(renderers, activeRendererIndex);
        //
        this.defaults = new JButton("Defaults");
        this.preview = new JButton("Preview");
        this.ok = new JButton("Ok");
        this.cancel = new JButton("Cancel");
        //
        this.semaphore = new Semaphore(0);
        //
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

        pane.add(cameraSetup, componentConstraints);
        pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);
        pane.add(filtersPanel.getPanel("Filters: "), componentConstraints);
        pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);
        pane.add(detectorsPanel.getPanel("Detectors: "), componentConstraints);
        pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);
        pane.add(estimatorsPanel.getPanel("Estimators: "), componentConstraints);
        pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);
        pane.add(renderersPanel.getPanel("Renderers: "), componentConstraints);
        pane.add(new JSeparator(JSeparator.HORIZONTAL), lineConstraints);
        //
        defaults.addActionListener(this);
        preview.addActionListener(this);
        ok.addActionListener(this);
        cancel.addActionListener(this);
        //
        JPanel buttons = new JPanel();
        buttons.add(defaults);
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(preview);
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(ok);
        buttons.add(cancel);
        pane.add(buttons, componentConstraints);
        getRootPane().setDefaultButton(ok);
        pack();
    }

    /**
     * Action handler.
     *
     * There are three possible actions. Canceling the analysis, confirming the
     * settings of analysis, and preview the results of analysis on a single
     * frame selected in active {@code ImagePlus} window.
     *
     * @param e event object holding the action details. It gets processed as
     * follows: <ul> <li>actionCommand == "Cancel": cancel the analysis</li>
     * <li>actionCommand == "Ok": confirm the settings and run the analysis</li>
     * <li>actionCommand == "Preview": preview the results of analysis with the
     * current selected settings on a single frame</li> </ul>
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("Cancel")) {
            closeDialog(true);
        } else if(e.getActionCommand().equals("Ok")) {
            Calibration cal = new Calibration();
            cal.setUnit("um");
            cal.pixelWidth = cal.pixelHeight = CameraSetupPlugIn.pixelSize / 1000.0;
            imp.setCalibration(cal);
            //
            activeFilterIndex = filtersPanel.getActiveComboBoxItemIndex();
            activeDetectorIndex = detectorsPanel.getActiveComboBoxItemIndex();
            activeEstimatorIndex = estimatorsPanel.getActiveComboBoxItemIndex();
            activeRendererIndex = renderersPanel.getActiveComboBoxItemIndex();
            //
            try {
                allFilters.get(activeFilterIndex).readParameters();
                allDetectors.get(activeDetectorIndex).readParameters();
                allEstimators.get(activeEstimatorIndex).readParameters();
                allRenderers.get(activeRendererIndex).readParameters();

                saveSelectedModuleIndexesToPrefs(activeFilterIndex, activeDetectorIndex, activeEstimatorIndex, activeRendererIndex);
            } catch(Exception ex) {
                IJ.error("Error parsing parameters: " + ex.toString());
                return;
            }
            //
            closeDialog(false);
        } else if(e.getActionCommand().equals("Preview")) {
            activeFilterIndex = filtersPanel.getActiveComboBoxItemIndex();
            activeDetectorIndex = detectorsPanel.getActiveComboBoxItemIndex();
            activeEstimatorIndex = estimatorsPanel.getActiveComboBoxItemIndex();
            activeRendererIndex = renderersPanel.getActiveComboBoxItemIndex();
            //
            try {
                allFilters.get(activeFilterIndex).readParameters();
                allDetectors.get(activeDetectorIndex).readParameters();
                allEstimators.get(activeEstimatorIndex).readParameters();
                allRenderers.get(activeRendererIndex).readParameters();

                saveSelectedModuleIndexesToPrefs(activeFilterIndex, activeDetectorIndex, activeEstimatorIndex, activeRendererIndex);
            } catch(Exception ex) {
                IJ.error("Error parsing parameters: " + ex.toString());
                return;
            }
            //
            try {
                Thresholder.loadFilters(allFilters);
                Thresholder.setActiveFilter(activeFilterIndex);   // !! must be called before any threshold is evaluated !!
                Thresholder.parseThreshold(allDetectors.get(activeDetectorIndex).getImplementation().getThresholdFormula());
            } catch(Exception ex) {
                IJ.error("Error parsing threshold formula! " + ex.toString());
            }
            //
            if(previewFuture != null) {
                previewFuture.cancel(true);
            }
            previewFuture = previewThreadRunner.submit(new Runnable() {
                void checkForInterruption() throws InterruptedException {
                    if(Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    if(IJ.escapePressed()) {
                        IJ.resetEscape();
                        throw new InterruptedException();
                    }
                }

                @Override
                public void run() {
                    try {
                        IJ.showStatus("Creating preview image.");
                        FloatProcessor fp = subtract((FloatProcessor) imp.getProcessor().crop().convertToFloat(), (float) CameraSetupPlugIn.offset);
                        Roi roi = imp.getRoi();
                        if(roi != null) {
                            fp.setMask(roi.getMask());
                        }
                        Thresholder.setCurrentImage(fp);
                        FloatProcessor filtered = allFilters.get(activeFilterIndex).getImplementation().filterImage(fp);
                        checkForInterruption();
                        Vector<Point> detections = Point.applyRoiMask(imp.getRoi(), allDetectors.get(activeDetectorIndex).getImplementation().detectMoleculeCandidates(filtered));
                        checkForInterruption();
                        Vector<Molecule> results = allEstimators.get(activeEstimatorIndex).getImplementation().estimateParameters(fp, detections);
                        checkForInterruption();
                        //
                        ImagePlus impPreview = new ImagePlus("ThunderSTORM preview for frame " + Integer.toString(imp.getSlice()), imp.getProcessor().crop());
                        RenderingOverlay.showPointsInImage(impPreview,
                                Molecule.extractParamToArray(results, PSFModel.Params.LABEL_X),
                                Molecule.extractParamToArray(results, PSFModel.Params.LABEL_Y),
                                Color.red, RenderingOverlay.MARKER_CROSS);
                        impPreview.show();
                    } catch(InterruptedException ex) {
                        IJ.showStatus("Preview interrupted.");
                    } catch(Exception ex) {
                        IJ.handleException(ex);
                    }
                }
            });

        } else if(e.getActionCommand().equals("Defaults")) {
            resetModuleUIs(allDetectors, allEstimators, allFilters, allRenderers);
            resetCardsPanels(detectorsPanel, estimatorsPanel, filtersPanel, renderersPanel);
        } else {
            throw new UnsupportedOperationException("Command '" + e.getActionCommand() + "' is not supported!");
        }
    }

    /**
     * Override the default {@code JDialog.dispose} method to release the
     * {@code semaphore} (see {
     *
     * @wasCanceled}).
     */
    @Override
    public void dispose() {
        super.dispose();
        semaphore.release();
        if(previewThreadRunner != null) {
            previewThreadRunner.shutdownNow();
        }
    }

    /**
     * Close (dispose) the dialog.
     *
     * @param cancel is the dialog closing because the operation has been
     * canceled?
     */
    public void closeDialog(boolean cancel) {
        canceled = cancel;
        dispose();
    }

    /**
     * Query if the dialog was closed by canceling (cancel button or red cross
     * window button) or by clicking on OK.
     *
     * <strong>This is a blocking call!</strong> Meaning that when creating a
     * non-modal dialog it is created and runs in its own thread and does not
     * block the creator thread. Call this method, however, calls
     * {@code semaphore.acquire}, which is a blocking call and waits until the
     * semaphore is released (if it wasn't already) which is done after closing
     * the dialog. Clearly, if this wasn't a blocking call, there wouldn't be a
     * way to know how was the dialog closed, because it wouldn't need to be
     * closed at the time of calling this method.
     *
     * @return {@code true} if the dialog was canceled, {@code false} otherwise
     */
    public boolean wasCanceled() {
        try {
            semaphore.acquire();
        } catch(InterruptedException ex) {
            IJ.handleException(ex);
        }
        return canceled;
    }

    /**
     * Return a filter selected from the combo box.
     *
     * @return selected filter
     */
    public IFilterUI getFilter() {
        return allFilters.get(activeFilterIndex);
    }

    /**
     * Return a detector selected from the combo box.
     *
     * @return selected detector
     */
    public IDetectorUI getDetector() {
        return allDetectors.get(activeDetectorIndex);
    }

    /**
     * Return an estimator selected from the combo box.
     *
     * @return selected estimator
     */
    public IEstimatorUI getEstimator() {
        return allEstimators.get(activeEstimatorIndex);
    }

    public IRendererUI getRenderer() {
        return allRenderers.get(activeRendererIndex);
    }

    public int getFilterIndex() {
        return activeFilterIndex;
    }

    public int getDetectorIndex() {
        return activeDetectorIndex;
    }

    public int getEstimatorIndex() {
        return activeEstimatorIndex;
    }

    public int getRendererIndex() {
        return activeRendererIndex;
    }

    static void resetModuleUIs(List<? extends IModuleUI>... lists) {
        for(int i = 0; i < lists.length; i++) {
            List<? extends IModuleUI> list = lists[i];
            for(IModuleUI module : list) {
                module.resetToDefaults();
            }
        }
    }

    static void resetCardsPanels(CardsPanel<?>... panels) {
        for(int i = 0; i < panels.length; i++) {
            panels[i].setSelectedItemIndex(0);
        }
    }

    static void saveSelectedModuleIndexesToPrefs(int filterIndex, int detectorIndex, int estimatorIndex, int rendererIndex) {
        if(filterIndex >= 0) {
            Prefs.set("thunderstorm.filters.index", filterIndex);
        }
        if(detectorIndex >= 0) {
            Prefs.set("thunderstorm.detectors.index", detectorIndex);
        }
        if(estimatorIndex >= 0) {
            Prefs.set("thunderstorm.estimators.index", estimatorIndex);
        }
        if(rendererIndex >= 0) {
            Prefs.set("thunderstorm.rendering.index", rendererIndex);
        }
    }
}
