package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.AnalysisOptionsDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IRenderer;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ExtendedPlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_16;
import static ij.plugin.filter.PlugInFilter.DOES_32;
import static ij.plugin.filter.PlugInFilter.DOES_8G;
import static ij.plugin.filter.PlugInFilter.DONE;
import static ij.plugin.filter.PlugInFilter.NO_CHANGES;
import static ij.plugin.filter.PlugInFilter.NO_UNDO;
import ij.plugin.filter.PlugInFilterRunner;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * ThunderSTORM Analysis plugin.
 *
 * Open the options dialog, process an image stack to recieve a list of
 * localized molecules which will get displayed in the {@code ResultsTable} and
 * previed in a new {@code ImageStack} with detections marked as crosses in
 * {@code Overlay} of each slice of the stack.
 */
public final class AnalysisPlugIn implements ExtendedPlugInFilter {

  private int stackSize;
  private AtomicInteger nProcessed = new AtomicInteger(0);
  private final int pluginFlags = DOES_8G | DOES_16 | DOES_32 | NO_CHANGES | NO_UNDO | DOES_STACKS | PARALLELIZE_STACKS | FINAL_PROCESSING;
  private List<PSFInstance>[] results;
  private IFilterUI selectedFilter;
  private IEstimatorUI selectedEstimator;
  private IDetectorUI selectedDetector;
  private IRenderer renderingQueue;

  /**
   * Returns flags specifying capabilities of the plugin.
   *
   * This method is called before an actual analysis and returns flags supported
   * by the plugin. The method is also called after the processing is finished
   * to fill the {@code ResultsTable} and to visualize the detections directly
   * in image stack (a new copy of image stack is created).
   *
   * <strong>The {@code ResultsTable} is always guaranteed to contain columns
   * <i>frame, x, y</i>!</strong> The other parameters are optional and can
   * change for different PSFs.
   *
   * @param command command
   * @param imp ImagePlus instance holding the active image (not required)
   * @return flags specifying capabilities of the plugin
   */
  @Override
  public int setup(String command, ImagePlus imp) {
    if (command.equals("final")) {
      IJ.showStatus("ThunderSTORM is generating the results...");
      //
      // Show table with results
      ResultsTable rt = Analyzer.getResultsTable();
      if (rt == null) {
        rt = new ResultsTable();
        Analyzer.setResultsTable(rt);
      }
      rt.reset();
      for (int frame = 1; frame <= stackSize; frame++) {
        for (PSFInstance psf : results[frame]) {
          rt.incrementCounter();
          rt.addValue("frame", frame);
          for (Map.Entry<String, Double> parameter : psf) {
            rt.addValue(parameter.getKey(), parameter.getValue());
          }
        }
      }
      rt.show("Results");
      //
      // Show detections in the image
      imp.setOverlay(null);
      for (int frame = 1; frame <= stackSize; frame++) {
        RenderingOverlay.showPointsInImageSlice(imp, extractX(results[frame]), extractY(results[frame]), frame, Color.red, RenderingOverlay.MARKER_CROSS);
      }
      renderingQueue.repaintLater();
      //
      // Finished
      IJ.showProgress(1.0);
      IJ.showStatus("ThunderSTORM finished.");
      return DONE;
    } else {
      return pluginFlags; // Grayscale only, no changes to the image and therefore no undo
    }
  }

  /**
   * Show the options dialog for a particular command and block the current
   * processing thread until user confirms his settings or cancels the
   * operation.
   *
   * @param command command (not required)
   * @param imp ImagePlus instance holding the active image (not required)
   * @param pfr instance that initiated this plugin (not required)
   * @return
   */
  @Override
  public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
    // Use an appropriate Look and Feel
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
      //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
      //UIManager.put("swing.boldMetal", Boolean.FALSE);
    } catch (UnsupportedLookAndFeelException ex) {
      IJ.error(ex.getMessage());
    } catch (IllegalAccessException ex) {
      IJ.error(ex.getMessage());
    } catch (InstantiationException ex) {
      IJ.error(ex.getMessage());
    } catch (ClassNotFoundException ex) {
      IJ.error(ex.getMessage());
    }

    // Create and set up the content pane.
    try {
      List<IFilterUI> filters = ThreadLocalWrapper.wrapFilters(ModuleLoader.getUIModules(IFilterUI.class));
      List<IDetectorUI> detectors = ThreadLocalWrapper.wrapDetectors(ModuleLoader.getUIModules(IDetectorUI.class));
      List<IEstimatorUI> estimators = ThreadLocalWrapper.wrapEstimators(ModuleLoader.getUIModules(IEstimatorUI.class));
      List<IRendererUI> renderers = ModuleLoader.getUIModules(IRendererUI.class);

      int default_filter = 0;
      int default_detector = 0;
      int default_estimator = 0;

      Thresholder.loadFilters(filters);
      Thresholder.setActiveFilter(default_filter);

      if (MacroParser.isRanFromMacro()) {
        //parse the macro options
        MacroParser parser = new MacroParser(filters, estimators, detectors, renderers);
        selectedFilter = parser.getFilterUI();
        selectedDetector = parser.getDetectorUI();
        selectedEstimator = parser.getEstimatorUI();

        IRendererUI rendererPanel = parser.getRendererUI();
        rendererPanel.setSize(imp.getWidth(), imp.getHeight());
        renderingQueue = rendererPanel.getImplementation();
        return pluginFlags;
      } else {
        // Create and show the dialog
        AnalysisOptionsDialog dialog = new AnalysisOptionsDialog(imp, command, filters, default_filter, detectors, default_detector, estimators, default_estimator, renderers, 0);
        dialog.setVisible(true);
        if (dialog.wasCanceled()) {  // This is a blocking call!!
          return DONE;    // cancel
        }
        selectedFilter = dialog.getFilter();
        selectedDetector = dialog.getDetector();
        selectedEstimator = dialog.getEstimator();

        IRendererUI rendererPanel = dialog.getRenderer();
        rendererPanel.setSize(imp.getWidth(), imp.getHeight());
        renderingQueue = rendererPanel.getImplementation();

        //if recording window is open, record parameters of all modules
        if (Recorder.record) {
          MacroParser.recordFilterUI(dialog.getFilter());
          MacroParser.recordDetectorUI(dialog.getDetector());
          MacroParser.recordEstimatorUI(dialog.getEstimator());
          MacroParser.recordRendererUI(rendererPanel);
        }
        return pluginFlags; // ok
      }
    } catch (Exception ex) {
      IJ.error(ex.getMessage());
      return DONE;
    }
  }

  /**
   * Gives the plugin information about the number of passes through the image
   * stack we want to process.
   *
   * Allocation of resources to store the results is done here.
   *
   * @param nPasses number of passes through the image stack we want to process
   */
  @Override
  public void setNPasses(int nPasses) {
    stackSize = nPasses;
    nProcessed.set(0);
    results = new Vector[stackSize + 1];  // indexing from 1 for simplicity
  }

  /**
   * Run the plugin.
   *
   * This method is ran in parallel, thus counting the results must be done
   * atomicaly.
   *
   * @param ip input image
   */
  @Override
  public void run(ImageProcessor ip) {
    assert (selectedFilter != null) : "Filter was not selected!";
    assert (selectedDetector != null) : "Detector was not selected!";
    assert (selectedEstimator != null) : "Estimator was not selected!";
    assert (renderingQueue != null) : "Renderer was not selected!";
    //
    FloatProcessor fp = (FloatProcessor) ip.convertToFloat();
    Vector<PSFInstance> fits = null;
    try {
      fits = selectedEstimator.getImplementation().estimateParameters(
              fp,
              selectedDetector.getImplementation().detectMoleculeCandidates(
              selectedFilter.getImplementation().filterImage(fp)));
    } catch (Exception ex) {
      IJ.error("Thresholding: " + ex.getMessage());
    }
    //
    results[ip.getSliceNumber()] = fits;
    nProcessed.incrementAndGet();

    renderingQueue.renderLater(extractX(fits), extractY(fits), 0.2);
    //
    IJ.showProgress((double) nProcessed.intValue() / (double) stackSize);
    IJ.showStatus("ThunderSTORM processing frame " + nProcessed + " of " + stackSize + "...");

  }

  private double[] extractX(List<PSFInstance> fits) {
    double[] x = new double[fits.size()];
    for (int i = 0; i < fits.size(); i++) {
      x[i] = fits.get(i).getX();
    }
    return x;
  }

  private double[] extractY(List<PSFInstance> fits) {
    double[] y = new double[fits.size()];
    for (int i = 0; i < fits.size(); i++) {
      y[i] = fits.get(i).getY();
    }
    return y;
  }
}
