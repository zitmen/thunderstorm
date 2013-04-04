package ThunderSTORM;

import ThunderSTORM.UI.AnalysisOptionsDialog;
import ThunderSTORM.detectors.LocalMaximaDetector;
import ThunderSTORM.detectors.NonMaxSuppressionDetector;
import ThunderSTORM.detectors.WatershedDetector;
import ThunderSTORM.estimators.LeastSquaresEstimator;
import ThunderSTORM.filters.BoxFilter;
import ThunderSTORM.filters.CompoundWaveletFilter;
import ThunderSTORM.filters.DifferenceOfGaussiansFilter;
import ThunderSTORM.filters.EmptyFilter;
import ThunderSTORM.filters.GaussianFilter;
import ThunderSTORM.filters.LoweredGaussianFilter;
import ThunderSTORM.filters.MedianFilter;
import ThunderSTORM.utils.Graph;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.plugin.filter.PlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_16;
import static ij.plugin.filter.PlugInFilter.DOES_32;
import static ij.plugin.filter.PlugInFilter.DOES_8G;
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
    public void run(final ImageProcessor ip) {
        /**
         * Create the GUI and show it.  For thread safety,
         * this method should be invoked from the
         * event dispatch thread.
         */
        /* Use an appropriate Look and Feel */
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
         
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create and set up the window.
                JFrame frame = new JFrame("CardLayoutDemo");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
                detectors.add(new WatershedDetector(false, 1.0));
                
                Vector<IModule> estimators = new Vector<IModule>();
                estimators.add(new LeastSquaresEstimator(11));
                
                AnalysisOptionsDialog dialog = new AnalysisOptionsDialog(imp, ip, frame, filters, 6, detectors, 2, estimators, 0);
                dialog.addComponentsToPane();

                // Display the window.
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
    
    public static void main(String[] args) {
        Thunder_STORM thunder = new Thunder_STORM();
        ImagePlus image = IJ.openImage("../eye_00010.tif");
        //ImagePlus image = IJ.openImage("../tubulins1_00020.tif");
        ImageWindow wnd = new ImageWindow(image);
        wnd.setVisible(true);
        thunder.setup(null, image);
        thunder.run(image.getProcessor());
    }
}