package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.AnalysisOptionsDialog;
import cz.cuni.lf1.lge.ThunderSTORM.UI.RenderingOverlay;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.CentroidOfConnectedComponentsDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.LocalMaximaDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.NonMaxSuppressionDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LeastSquaresEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.GaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.filters.BoxFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfGaussiansFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.EmptyFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.GaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.LoweredGaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.MedianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.Graph;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.ExtendedPlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_16;
import static ij.plugin.filter.PlugInFilter.DOES_32;
import static ij.plugin.filter.PlugInFilter.DOES_8G;
import static ij.plugin.filter.PlugInFilter.NO_CHANGES;
import static ij.plugin.filter.PlugInFilter.NO_UNDO;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * ThunderSTORM plugin.
 * 
 * <br /><strong>Commands:</strong>
 * <ul>
 *   <li>analysis: open the options dialog, process an image stack to revieve
 *                 a list of localized molecules which will get displayed in
 *                 the ResultsTable and previed in a new ImageStack with detections
 *                 marked as crosses in Overlay of each slice of the stack</li>
 *   <li>rendering: TODO</li>
 * </ul>
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public final class Thunder_STORM implements ExtendedPlugInFilter {

    private IFilter filter;
    private IDetector detector;
    private IEstimator estimator;
    
    private int stackSize;
    private int nProcessed;
    
    private final ReentrantLock lock = new ReentrantLock();
    
    private final int pluginFlags = DOES_8G | DOES_16 | DOES_32 | NO_CHANGES | NO_UNDO | DOES_STACKS | PARALLELIZE_STACKS ;
    private Vector<PSF>[] results;
    private FloatProcessor[] images;
    
    /**
     * Returns flags specifying capabilities of the plugin.
     * 
     * @param command command, e.g., "analysis", "rendering", etc. (not required in this version)
     * @param imp ImagePlus instance holding the active image (not required in this version)
     * @return flags specifying capabilities of the plugin
     */
    @Override
    public int setup(String command, ImagePlus imp) {
        // Grayscale only, no changes to the image and therefore no undo
        return pluginFlags;
    }
    
    /**
     * Show the options dialog for a particular command and block the current
     * processing thread until user confirms his settings or cancels the operation.
     * 
     * @param command command, e.g., "analysis", "rendering", etc. (not required in this version)
     * @param imp ImagePlus instance holding the active image (not required in this version)
     * @param pfr (not required in this version)
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
        Vector<IModule> filters = new Vector<IModule>();
        filters.add(new EmptyFilter());
        filters.add(new BoxFilter(3));
        filters.add(new MedianFilter(MedianFilter.BOX, 3));
        filters.add(new GaussianFilter(11, 1.6));
        filters.add(new DifferenceOfGaussiansFilter(11, 1.6, 1.0));
        filters.add(new LoweredGaussianFilter(11, 1.6));
        filters.add(new CompoundWaveletFilter(false));

        Vector<IModule> detectors = new Vector<IModule>();
        detectors.add(new LocalMaximaDetector(Graph.CONNECTIVITY_8, 10.0));
        detectors.add(new NonMaxSuppressionDetector(3, 6.0));
        detectors.add(new CentroidOfConnectedComponentsDetector(false, 1.0));

        Vector<IModule> estimators = new Vector<IModule>();
        estimators.add(new LeastSquaresEstimator(11));

        // Create and show the dialog
        AnalysisOptionsDialog dialog = new AnalysisOptionsDialog(imp, command, filters, 6, detectors, 2, estimators, 0);
        dialog.setVisible(true);
        if(dialog.wasCanceled())    // This is a blocking call!!
        {
            filter = null;
            detector = null;
            estimator = null;
            return DONE;    // cancel
        }
        else
        {
            filter = dialog.getFilter();
            detector = dialog.getDetector();
            estimator = dialog.getEstimator();
            return pluginFlags; // ok
        }
    }

    /**
     * Gives the plugin information about the number of passes through the image stack we want to process.
     * 
     * Allocation of resources to store the results is done here.
     * 
     * @param nPasses number of passes through the image stack we want to process
     */
    @Override
    public void setNPasses(int nPasses) {
        stackSize = nPasses;
        nProcessed = 0;
        results = new Vector[stackSize+1];  // indexing from 1 for simplicity
        images = new FloatProcessor[stackSize+1];
    }

    /**
     * Run the plugin.
     * 
     * This method is ran in parallel, thus saving the results must be synchronized,
     * which is achieved by using the ReentrantLock. When processing the last frame,
     * the ResultsTable is filled and the image stack with visualization of detections
     * is built.
     * 
     * @param ip input image
     */
    @Override
    public void run(ImageProcessor ip) {
        assert(filter != null) : "Filter was not selected!";
        assert(detector != null) : "Detector was not selected!";
        assert(estimator != null) : "Estimator was not selected!";
        //
        FloatProcessor fp = (FloatProcessor)ip.convertToFloat();
        Vector<PSF> fits = estimator.estimateParameters(fp, detector.detectMoleculeCandidates(filter.filterImage(fp)));
        boolean lastFrame = false;
        //
        lock.lock();
        try {
            results[ip.getSliceNumber()] = fits;
            images[ip.getSliceNumber()] = fp;
            nProcessed += 1;
            lastFrame = (nProcessed == stackSize);
        } finally {
            lock.unlock();
        }
        //
        if(lastFrame) {
            IJ.showStatus("ThunderSTORM is generating the results...");
            //
            // Show table with results
            ResultsTable rt = Analyzer.getResultsTable();
            if (rt == null) {
                rt = new ResultsTable();
                Analyzer.setResultsTable(rt);
            }
            for(int frame = 1; frame <= stackSize; frame++) {
                for(PSF psf : results[frame]) {
                    rt.incrementCounter();
                    rt.addValue("frame", frame);
                    rt.addValue("x [px]", psf.xpos);
                    rt.addValue("y [px]", psf.ypos);
                    rt.addValue("\u03C3 [px]", ((GaussianPSF)psf).sigma);
                    rt.addValue("Intensity", psf.intensity);
                    rt.addValue("background", psf.background);
                }
            }
            rt.show("Results");
            //
            // Show detections in the image
            ImageStack stack = new ImageStack(images[1].getWidth(),images[1].getHeight());
            for(int frame = 1; frame <= stackSize; frame++)
                stack.addSlice(images[frame]);
            //
            ImagePlus impPreview = new ImagePlus("ThunderSTORM results preview", stack);
            for(int frame = 1; frame <= stackSize; frame++) {
                double [] xCoord = new double[results[frame].size()];
                double [] yCoord = new double[results[frame].size()];
                for(int i = 0; i < results[frame].size(); i++) {
                    xCoord[i] = results[frame].elementAt(i).xpos;
                    yCoord[i] = results[frame].elementAt(i).ypos;
                }
                RenderingOverlay.showPointsInImageSlice(impPreview, xCoord, yCoord, frame, Color.red, RenderingOverlay.MARKER_CROSS);
            }
            impPreview.show("Results preview");
            //
            // Finished
            IJ.showProgress(1.0);
            IJ.showStatus("ThunderSTORM finished.");
        } else {
            IJ.showProgress((double)nProcessed / (double)stackSize);
            IJ.showStatus("ThunderSTORM processing frame " + Integer.toString(nProcessed) + " of " + Integer.toString(stackSize) + "...");
        }
    }
}