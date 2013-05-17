package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.AnalysisOptionsDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.GaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IRenderer;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
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
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
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

  private IFilter filter;
  private IDetector detector;
  private IEstimator estimator;
  private IRenderer renderer;
  private int stackSize;
  private AtomicInteger nProcessed = new AtomicInteger(0);
  private final int pluginFlags = DOES_8G | DOES_16 | DOES_32 | NO_CHANGES | NO_UNDO | DOES_STACKS | PARALLELIZE_STACKS | FINAL_PROCESSING;
  private Vector<PSF>[] results;
  private FloatProcessor[] images;

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
        for (PSF psf : results[frame]) {
          rt.incrementCounter();
          rt.addValue("frame", frame);
          rt.addValue("x", psf.xpos);
          rt.addValue("y", psf.ypos);
          rt.addValue("\u03C3", ((GaussianPSF) psf).sigma);
          rt.addValue("Intensity", psf.intensity);
          rt.addValue("background", psf.background);
        }
      }
      rt.show("Results");
      //
      // Show detections in the image
      ImageStack stack = new ImageStack(images[1].getWidth(), images[1].getHeight());
      for (int frame = 1; frame <= stackSize; frame++) {
        stack.addSlice(images[frame]);
      }
      //
      ImagePlus impPreview = new ImagePlus("ThunderSTORM results preview", stack);
      for (int frame = 1; frame <= stackSize; frame++) {
        RenderingOverlay.showPointsInImageSlice(impPreview, extractX(results[frame]), extractY(results[frame]), frame, Color.red, RenderingOverlay.MARKER_CROSS);
      }
      impPreview.show("Results preview");
      renderer.repaintAsync();
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
      Vector<IFilter> filters = ModuleLoader.getModules(IFilter.class);
      Vector<IDetector> detectors = ModuleLoader.getModules(IDetector.class);
      Vector<IEstimator> estimators = ModuleLoader.getModules(IEstimator.class);
      Vector<IRenderer> renderers = ModuleLoader.getModules(IRenderer.class);
      for (IRenderer r : renderers) {
        r.setSize(imp.getWidth(), imp.getHeight());
      }

      // Create and show the dialog
      AnalysisOptionsDialog dialog = new AnalysisOptionsDialog(imp, command, filters, 0, detectors, 0, estimators, 0, renderers, 0);
      dialog.setVisible(true);
      if (dialog.wasCanceled()) {  // This is a blocking call!!
        filter = null;
        detector = null;
        estimator = null;
        return DONE;    // cancel
      } else {
        filter = dialog.getFilter();
        detector = dialog.getDetector();
        estimator = dialog.getEstimator();
        renderer = dialog.getRenderer();

        return pluginFlags; // ok
      }
    } catch (Exception ex) {
      IJ.log(ex.getMessage());
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
    images = new FloatProcessor[stackSize + 1];
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
    assert (filter != null) : "Filter was not selected!";
    assert (detector != null) : "Detector was not selected!";
    assert (estimator != null) : "Estimator was not selected!";
    assert (renderer != null) : "Renderer was not selected!";
    //
    FloatProcessor fp = (FloatProcessor) ip.convertToFloat();
    Vector<PSF> fits = estimator.estimateParameters(fp, detector.detectMoleculeCandidates(filter.filterImage(fp)));
    //
    results[ip.getSliceNumber()] = fits;
    images[ip.getSliceNumber()] = fp;
    nProcessed.incrementAndGet();

    renderer.renderAsync(extractX(fits), extractY(fits), 0.2);
    //
    IJ.showProgress((double) nProcessed.intValue() / (double) stackSize);
    IJ.showStatus("ThunderSTORM processing frame " + nProcessed + " of " + stackSize + "...");
  }

  private double[] extractX(Vector<PSF> fits) {
    double[] x = new double[fits.size()];
    for (int i = 0; i < fits.size(); i++) {
      x[i] = fits.get(i).xpos;
    }
    return x;
  }

  private double[] extractY(Vector<PSF> fits) {
    double[] y = new double[fits.size()];
    for (int i = 0; i < fits.size(); i++) {
      y[i] = fits.get(i).ypos;
    }
    return y;
  }
}
