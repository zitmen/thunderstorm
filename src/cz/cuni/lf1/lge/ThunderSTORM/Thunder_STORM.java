package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.AnalysisOptionsDialog;
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
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class Thunder_STORM implements ExtendedPlugInFilter {

    private IFilter filter;
    private IDetector detector;
    private IEstimator estimator;
    
    private int stackSize;
    private int nProcessed;
    
    private final ReentrantLock lock = new ReentrantLock();
    
    private final int pluginFlags = DOES_8G | DOES_16 | DOES_32 | NO_CHANGES | NO_UNDO | DOES_STACKS | PARALLELIZE_STACKS ;
    private Vector<PSF>[] results;
    
    @Override
    public int setup(String string, ImagePlus imp) {
        // Grayscale only, no changes to the image and therefore no undo
        return pluginFlags;
    }
    
    @Override
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        // Use an appropriate Look and Feel
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
            //UIManager.put("swing.boldMetal", Boolean.FALSE);
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
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
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.addComponentsToPane();
        dialog.pack();
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

    @Override
    public void setNPasses(int nPasses) {
        stackSize = nPasses;
        nProcessed = 0;
        results = new Vector[stackSize+1];  // indexing from 1 for simplicity
    }

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
            nProcessed += 1;
            lastFrame = (nProcessed == stackSize);
        } finally {
            lock.unlock();
        }
        //
        if(lastFrame) {
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
            IJ.showProgress(1.0);
            IJ.showStatus("ThunderSTORM finished.");
        } else {
            IJ.showProgress((double)nProcessed / (double)stackSize);
            IJ.showStatus("ThunderSTORM processing frame " + Integer.toString(nProcessed) + " of " + Integer.toString(stackSize) + "...");
        }
    }
}