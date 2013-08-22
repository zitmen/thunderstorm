package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.Drift;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import javax.swing.JSeparator;

public class PerformanceEvaluationPlugIn implements PlugIn {
    
    private int width, height, frames, processing_frame;
    private double pixelsize, density;
    private Drift drift;
    private Range fwhm_range, energy_range, bkg_range;
    private double add_poisson_var, mul_gauss_var, mul_gauss_mean;
    private FloatProcessor mask;
    
    @Override
    public void run(String command) {
        GUI.setLookAndFeel();
        //
        if("showGroundTruthTable".equals(command)) {
            IJGroundTruthTable.getGroundTruthTable().show();
            return;
        }
        //
        try {
            // Create and show the dialog
            GenericDialogPlus gd = new GenericDialogPlus("ThunderSTORM: Data generator (16bit grayscale image sequence)");
            gd.addNumericField("Width [px]: ", 256, 0);
            gd.addNumericField("Height [px]: ", 256, 0);
            gd.addNumericField("Frames: ", 1000, 0);
            gd.addNumericField("Pixel width [um]: ", 0.1, 1);
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            gd.addNumericField("Density [mol/um^2]: ", 1, 1);
            gd.addStringField("Emitter FWHM range [px]: ", "1.8:4");
            gd.addStringField("Emitter energy range [digital units]: ", "1500:2000");
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            gd.addStringField("Background intensity range: ", "100:120");
            gd.addMessage("Read-out:");
            gd.addNumericField("Additive Poisson noise variance: ", 10, 0);
            gd.addMessage("Gain:");
            gd.addNumericField("Multiplicative Gaussian noise mean: ", 1, 0);
            gd.addNumericField("Multiplicative Gaussian noise variance: ", 10, 0);
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            gd.addNumericField("Linear drift distance [px]: ", 0, 0);
            gd.addNumericField("Linear drift angle [deg]: ", 0, 0);
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            gd.addFileField("Grayscale mask (optional): ", "");
            gd.showDialog();
            
            if(!gd.wasCanceled()) {
                readParams(gd);
                //runGenerator();
            }
        } catch (Exception ex) {
            IJ.handleException(ex);
        }
    }
    
    private void readParams(GenericDialogPlus gd) {
        width = (int)gd.getNextNumber();
        height = (int)gd.getNextNumber();
        frames = (int)gd.getNextNumber();
        pixelsize = gd.getNextNumber();
        density = gd.getNextNumber();
        fwhm_range = Range.parseFromTo(gd.getNextString());
        energy_range = Range.parseFromTo(gd.getNextString());
        bkg_range = Range.parseFromTo(gd.getNextString());
        add_poisson_var = gd.getNextNumber();
        mul_gauss_var = gd.getNextNumber();
        mul_gauss_mean = gd.getNextNumber();
        drift = new Drift(gd.getNextNumber(), gd.getNextNumber(), false, frames);
    }

}
