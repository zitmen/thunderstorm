package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.AnalysisOptionsDialog;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.CentroidOfConnectedComponentsDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.LocalMaximaDetector;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.NonMaxSuppressionDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LeastSquaresEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.filters.BoxFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfGaussiansFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.EmptyFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.GaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.LoweredGaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.MedianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.Graph;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.ImageWindow;
import ij.plugin.filter.ExtendedPlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_16;
import static ij.plugin.filter.PlugInFilter.DOES_32;
import static ij.plugin.filter.PlugInFilter.DOES_8G;
import static ij.plugin.filter.PlugInFilter.NO_CHANGES;
import static ij.plugin.filter.PlugInFilter.NO_UNDO;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.ImageProcessor;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public final class Thunder_STORM implements ExtendedPlugInFilter {

    private ImagePlus imp;
    private PlugInFilterRunner pfr;
    
    private final int pluginFlags = DOES_8G | DOES_16 | DOES_32 | NO_CHANGES | NO_UNDO ;
    
    @Override
    public int setup(String string, ImagePlus imp) {
        this.imp = imp;
        // Grayscale only, no changes to the image and therefore no undo
        return pluginFlags;
    }
    
    @Override
    public synchronized int showDialog(final ImagePlus imp, final String command, final PlugInFilterRunner pfr) {
        this.pfr = pfr;
        this.imp = imp; // should be the same as in the setup, but whatever.. :)
        
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
        AnalysisOptionsDialog dialog = new AnalysisOptionsDialog(imp, pfr, command, filters, 6, detectors, 2, estimators, 0);
        dialog.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dialog.addComponentsToPane();
        dialog.pack();
        dialog.setVisible(true);
        
        try {
            wait(); // TODO: nefunguje...furt ceka
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        
        if(dialog.wasCanceled())
            return DONE;
        else
            return pluginFlags; // ok
    }

    @Override
    public void setNPasses(int nPasses) {
        //
    }

    @Override
    public void run(final ImageProcessor ip) {
        JOptionPane.showMessageDialog(null,"ALERT MESSAGE","TITLE",JOptionPane.WARNING_MESSAGE);
    }
    
    public static void main(String[] args) {
        Thunder_STORM thunder = new Thunder_STORM();
        //ImagePlus image = IJ.openImage("../eye_00010.tif");
        //ImagePlus image = IJ.openImage("../tubulins1_00020.tif");
        ImagePlus img = IJ.openImage("../tubulins1_01400.tif");
        ImageWindow wnd = new ImageWindow(img);
        wnd.setVisible(true);
        thunder.setup(null, img);
        thunder.run(img.getProcessor());
    }
}