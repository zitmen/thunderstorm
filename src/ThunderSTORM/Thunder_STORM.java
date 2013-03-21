package ThunderSTORM;

// TODO#1: JUnit tests!!! (conversion to maven? or can Ant do it too?)
// TODO#2: speed & refactoring where needed

import static ThunderSTORM.utils.Math.sqr;
import static ThunderSTORM.utils.ImageProcessor.subtractImage;
import static ThunderSTORM.utils.ImageProcessor.threshold;
import static ThunderSTORM.utils.ImageProcessor.applyMask;
import ThunderSTORM.utils.Convolution;
import LMA.LMA;
import LMA.LMAMultiDimFunction;
import ThunderSTORM.UI.AnalysisOptionsDialog;
import ThunderSTORM.detectors.LocalMaximaDetector;
import ThunderSTORM.detectors.NonMaxSuppressionDetector;
import ThunderSTORM.detectors.WatershedDetector;
import ThunderSTORM.estimators.LeastSquaresEstimator;
import ThunderSTORM.estimators.MaximumLikelihoodEstimator;
import ThunderSTORM.filters.BoxFilter;
import ThunderSTORM.filters.CompoundWaveletFilter;
import ThunderSTORM.filters.DifferenceOfGaussiansFilter;
import ThunderSTORM.filters.EmptyFilter;
import ThunderSTORM.filters.GaussianFilter;
import ThunderSTORM.filters.LoweredGaussianFilter;
import ThunderSTORM.filters.MedianFilter;
import ThunderSTORM.filters.WaveletFilter;
import ThunderSTORM.utils.Graph;
import ThunderSTORM.utils.Padding;
import ThunderSTORM.utils.Point;
import Watershed.WatershedAlgorithm;
import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.Analyzer;
import ij.plugin.filter.PlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_16;
import static ij.plugin.filter.PlugInFilter.DOES_32;
import static ij.plugin.filter.PlugInFilter.DOES_8G;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class Thunder_STORM implements PlugInFilter {

    private ImagePlus imp;
    
    @Override
    public int setup(String string, ImagePlus imp) {
        this.imp = imp;
        // Grayscale only, no changes to the image and therefore no undo
        return DOES_8G | DOES_16 | DOES_32 | NO_CHANGES | NO_UNDO ;
    }

    @Override
    public void run(ImageProcessor ip) {
        FloatProcessor fp = (FloatProcessor) ip.convertToFloat();
        Vector<Point> detections = WaveletDetector(fp, false, true, false);
        //IJ.log("DETECTION:\n");
        //IJ.log(detections.toString());
        //IJ.log("\n\n");
        //IJ.log("LOCALIZATION:\n");
        Vector<Point<Double>> fits = ExponentialGaussianEstimator(fp, detections);
        //IJ.log(fits.toString());
        //
        ResultsTable rt = Analyzer.getResultsTable();
        if (rt == null) {
            rt = new ResultsTable();
            Analyzer.setResultsTable(rt);
        }
        for(Point<Double> p : fits) {
            rt.incrementCounter();
            rt.addValue("x [nm]", p.getX());
            rt.addValue("y [nm]", p.getY());
            rt.show("Results");
        }
    }

    public static class Gaussian extends LMAMultiDimFunction {

        @Override
        public double getY(double x[], double[] a) {
            // a = {x0,y0,Intensity,sigma,background}
            return a[2]/2.0/Math.PI/sqr(a[3]) * Math.exp(-(sqr(x[0]-a[0]) + sqr(x[1]-a[1])) / 2.0 / sqr(a[3])) + a[4];
        }

        @Override
        public double getPartialDerivate(double x[], double[] a, int parameterIndex) {
            double arg = sqr(x[0] - a[0]) + sqr(x[1] - a[1]);
            switch (parameterIndex) {
                case 0: return a[2]/2.0/Math.PI/Math.pow(a[3],4) * (x[0]-a[0]) * Math.exp(-arg/2.0/sqr(a[3])); // x0
                case 1: return a[2]/2.0/Math.PI/Math.pow(a[3],4) * (x[1]-a[1]) * Math.exp(-arg/2.0/sqr(a[3])); // y0
                case 2: return Math.exp(-arg/2.0/sqr(a[3])) / 2.0 / Math.PI / sqr(a[3]); // Intensity
                case 3: return a[2]/2.0/Math.PI/Math.pow(a[3],5) * (arg - 2.0 * sqr(a[3])) * Math.exp(-arg/2.0/sqr(a[3])); // sigma
                case 4: return 1.0; // background
            }
            throw new RuntimeException("No such parameter index: " + parameterIndex);
        }
    }

    public static Vector<Point> WaveletDetector(FloatProcessor image, boolean third_plane, boolean watershed, boolean upsample) {
        assert (!((upsample == true) && (watershed == false))) : "Upsampling can be performed only along with watershed transform!";

        // wavelets definition
        float[] g1 = new float[]{1f/16f, 1f/4f, 3f/8f, 1f/4f, 1f/16f};
        float[] g2 = new float[]{1f/16f, 0f, 1f/4f, 0f, 3f/8f, 0f, 1f/4f, 0f, 1f/16f};
        float[] g3 = new float[]{1f/16f, 0f, 0f, 0f, 1f/4f, 0f, 0f, 0f, 3f/8f, 0f, 0f, 0f, 1f/4f, 0f, 0f, 0f, 1f/16f};

        // prepare the wavelets for convolution
        FloatProcessor k1 = Convolution.getSeparableKernelFromVectors(g1, g1);
        FloatProcessor k2 = Convolution.getSeparableKernelFromVectors(g2, g2);
        FloatProcessor k3 = Convolution.getSeparableKernelFromVectors(g3, g3);

        // convolve with the wavelets
        FloatProcessor V1 = Convolution.Convolve(image, k1, Padding.PADDING_DUPLICATE);
        FloatProcessor V2 = Convolution.Convolve(V1, k2, Padding.PADDING_DUPLICATE);
        FloatProcessor V3 = null;
        if (third_plane) {
            V3 = Convolution.Convolve(V2, k3, Padding.PADDING_DUPLICATE);
        }

        // create wavelet planes
        FloatProcessor first_plane = subtractImage(image, V1); // 1st
        FloatProcessor final_plane = subtractImage(V1, V2);    // 2nd
        if (third_plane) {
            final_plane = subtractImage(V2, V3);  // 3rd
        }
        // detection - thresholding
        threshold(final_plane, 1.25f * (float) first_plane.getStatistics().stdDev, 1.0f, 0.0f); // these are in reverse (1=low,0=high) on purpose!
                                                                                                //the result is negated image, which is exactly what i need
        // detection - watershed transform with[out] upscaling
        if (watershed) {
            if (upsample) {
                final_plane.setInterpolationMethod(FloatProcessor.NEAREST_NEIGHBOR);
                final_plane = (FloatProcessor) final_plane.resize(final_plane.getWidth() * 2);
            }
            // run the watershed algorithm - it works only with ByteProcessor! that's all I need though
            FloatProcessor w = (FloatProcessor) WatershedAlgorithm.run((ByteProcessor) final_plane.convertToByte(false)).convertToFloat();
            final_plane = applyMask(w, final_plane);
            if (upsample) {
                final_plane = (FloatProcessor) final_plane.resize(final_plane.getWidth() / 2);
            }
        }

        // Detection - finding a center of gravity (with subpixel precision),
        Vector<Point> detections = new Vector<Point>();
        for (Graph.ConnectedComponent c : Graph.getConnectedComponents((ImageProcessor) final_plane, Graph.CONNECTIVITY_8)) {
            detections.add(c.centroid());
            detections.lastElement().val = null;
        }

        return detections;
    }

    // TODO: boundary pixels!! coordinates would go negative!! especially with the zero-based indexing
    // TODO: refactor...!
    public static Vector<Point<Double>> ExponentialGaussianEstimator(FloatProcessor fp, Vector<Point> detections) {
        Vector<Point<Double>> fits = new Vector<Point<Double>>();
        
        for(int d = 0, dm = detections.size(); d < dm; d++)
        {
            Point p = detections.elementAt(d);
            
            // params = {x0,y0,Intensity,sigma,background}
            double[] init_guess = new double[]{ p.getX().doubleValue(), p.getY().doubleValue(), fp.getPixelValue(p.roundToInteger().getX().intValue(), p.roundToInteger().getY().intValue()), 1.3, 100.0 };
            double[][] x = new double[11 * 11][2];
            double[] y = new double[11 * 11];
            for (int r = 0; r < 11; r++) {
                for (int c = 0; c < 11; c++) {
                    int idx = r * 11 + c;
                    x[idx][0] = (int) init_guess[0] + c - 5;  // x
                    x[idx][1] = (int) init_guess[1] + r - 5;  // y
                    y[idx] = new Float(fp.getPixelValue((int) x[idx][0], (int) x[idx][1])).doubleValue();    // G(x,y)
                }
            }
            
            LMA lma = new LMA(new Gaussian(), init_guess, y, x);
            lma.fit();
            
            fits.add(new Point((lma.parameters[0]+0.5)*150.0, (lma.parameters[1]+0.5)*150.0));  // force pixelsize = 150nm + 0.5px shift to the center of each pixel
        }
        
        return fits;
    }

    public static void main(String[] args) {
        /*
        LoweredGaussianFilter lg = new LoweredGaussianFilter(11, 1.6);
        //
        ImagePlus image = IJ.openImage("../eye_00010.tif");
        //ImagePlus image = IJ.openImage("../tubulins1_00020.tif");
        Vector<Point> detections = WaveletDetector((FloatProcessor) image.getProcessor().convertToFloat(), false, true, false);
        System.out.println("DETECTION:");
        System.out.println(detections.toString());
        System.out.println("");
        System.out.println("LOCALIZATION:");
        Vector<Point<Double>> fits = ExponentialGaussianEstimator((FloatProcessor) image.getProcessor().convertToFloat(), detections);
        System.out.println(fits.toString());
        //FloatProcessor fp = WaveletDetector((FloatProcessor) image.getProcessor().convertToFloat(), false, true, false);
        //image.setProcessor(fp.convertToShort(false));
        //IJ.save(image, "../output.tif");
        */
        /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
        /* Use an appropriate Look and Feel */
        try {
            //UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch (UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        } catch (InstantiationException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        /* Turn off metal's use of bold fonts */
        UIManager.put("swing.boldMetal", Boolean.FALSE);
         
        //Schedule a job for the event dispatch thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                //Create and set up the window.
                JFrame frame = new JFrame("CardLayoutDemo");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                //Create and set up the content pane.
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
                detectors.add(new WatershedDetector(false, 1.0));
                
                Vector<IModule> estimators = new Vector<IModule>();
                estimators.add(new LeastSquaresEstimator(11));
                estimators.add(new MaximumLikelihoodEstimator(11));
                
                AnalysisOptionsDialog dialog = new AnalysisOptionsDialog(filters, detectors, estimators);
                dialog.addComponentsToPane(frame.getContentPane());

                //Display the window.
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}