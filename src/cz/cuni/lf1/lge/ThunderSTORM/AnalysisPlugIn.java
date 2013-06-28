package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.AnalysisOptionsDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.ui.IDetectorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import cz.cuni.lf1.lge.ThunderSTORM.util.UI;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Roi;
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
  private final int pluginFlags = DOES_8G | DOES_16 | DOES_32 | NO_CHANGES | NO_UNDO | DOES_STACKS | PARALLELIZE_STACKS | FINAL_PROCESSING | SUPPORTS_MASKING;
  private List<PSFInstance>[] results;
  private IFilterUI selectedFilter;
  private IEstimatorUI selectedEstimator;
  private IDetectorUI selectedDetector;
  Roi roi;
  private RenderingQueue renderingQueue;
  private ImagePlus renderedImage;
  private Runnable repaintTask = new Runnable() {
    @Override
    public void run() {
      renderedImage.show();
      if (renderedImage.isVisible()) {
        IJ.run(renderedImage, "Enhance Contrast", "saturated=0.05");
      }
    }
  };

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
    UI.setLookAndFeel();
    //
    if (command.equals("final")) {
      IJ.showStatus("ThunderSTORM is generating the results...");
      //
      // Show table with results
      IJResultsTable rt = IJResultsTable.getResultsTable();
      if (rt == null) {
        rt = new IJResultsTable();
        IJResultsTable.setResultsTable(rt);
      }
      rt.reset();
      for (int frame = 1; frame <= stackSize; frame++) {
        for (PSFInstance psf : results[frame]) {
          rt.addRow();
          rt.addValue("frame", frame);
          for (Map.Entry<String, Double> parameter : psf) {
            rt.addValue(parameter.getKey(), parameter.getValue());
          }
        }
      }
      rt.show();
      //
      // Show detections in the image
      imp.setOverlay(null);
      for (int frame = 1; frame <= stackSize; frame++) {
        RenderingOverlay.showPointsInImageSlice(imp,
                offset(roi.getBounds().x, PSFInstance.extractParamToArray(results[frame], PSFInstance.X)),
                offset(roi.getBounds().y, PSFInstance.extractParamToArray(results[frame], PSFInstance.Y)),
                frame, Color.red, RenderingOverlay.MARKER_CROSS);
      }
      renderingQueue.repaintLater();
      //
      // Finished
      IJ.showProgress(1.0);
      IJ.showStatus("ThunderSTORM finished.");
      return DONE;
    } else if("showResultsTable".equals(command)) {
      IJResultsTable.getResultsTable().show();
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
    try {
      // load modules
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

        roi = imp.getRoi() != null ? imp.getRoi() : new Roi(0,0,imp.getWidth(), imp.getHeight());
        IRendererUI rendererPanel = parser.getRendererUI();
        rendererPanel.setSize(roi.getBounds().width, roi.getBounds().height);
        IncrementalRenderingMethod method = rendererPanel.getImplementation();
        renderedImage = method.getRenderedImage();
        renderingQueue = new RenderingQueue(method, repaintTask, rendererPanel.getRepaintFrequency());
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

        roi = imp.getRoi() != null ? imp.getRoi() : new Roi(0,0,imp.getWidth(), imp.getHeight());
        IRendererUI selectedRenderer = dialog.getRenderer();
        selectedRenderer.setSize(roi.getBounds().width, roi.getBounds().height);
        IncrementalRenderingMethod method = selectedRenderer.getImplementation();
        renderedImage = method.getRenderedImage();
        renderingQueue = new RenderingQueue(method, repaintTask, selectedRenderer.getRepaintFrequency());

        //if recording window is open, record parameters of all modules
        if (Recorder.record) {
          MacroParser.recordFilterUI(dialog.getFilter());
          MacroParser.recordDetectorUI(dialog.getDetector());
          MacroParser.recordEstimatorUI(dialog.getEstimator());
          MacroParser.recordRendererUI(selectedRenderer);
        }
        return pluginFlags; // ok
      }
    } catch (Exception ex) {
      IJ.handleException(ex);
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
    ip.setRoi(roi);
    FloatProcessor fp = (FloatProcessor) ip.crop().convertToFloat();
    Vector<PSFInstance> fits;
    try {
      fits = selectedEstimator.getImplementation().estimateParameters(
              fp,
              Point.applyRoiMask(roi,
              selectedDetector.getImplementation().detectMoleculeCandidates(
              selectedFilter.getImplementation().filterImage(fp))));

      //
      results[ip.getSliceNumber()] = fits;
      nProcessed.incrementAndGet();

      if (fits.size() > 0) {
        renderingQueue.renderLater(
                PSFInstance.extractParamToArray(fits, PSFInstance.X),
                PSFInstance.extractParamToArray(fits, PSFInstance.Y),
                fits.get(0).hasParam(PSFInstance.Z) ? PSFInstance.extractParamToArray(fits, PSFInstance.Z) : null,
                null);
      }
      //
      IJ.showProgress((double) nProcessed.intValue() / (double) stackSize);
      IJ.showStatus("ThunderSTORM processing frame " + nProcessed + " of " + stackSize + "...");
    } catch (Exception ex) {
      IJ.handleException(ex);
    }
  }

  private double[] offset(double offset, double[] arr) {
    if (offset != 0) {
      for (int i = 0; i < arr.length; i++) {
        arr[i] = arr[i] + offset;
      }
    }
    return arr;
  }
}
