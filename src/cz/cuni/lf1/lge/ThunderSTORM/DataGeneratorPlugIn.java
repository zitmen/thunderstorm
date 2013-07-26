package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.DataGenerator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.GaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import java.util.Map;
import java.util.Vector;
import javax.swing.JSeparator;

public class DataGeneratorPlugIn implements PlugIn {
    
    private int width, height, frames;
    private double pixelsize, density;
    private DataGenerator.Drift drift;
    private Range fwhm_range, energy_range, bkg_range;
    private double add_poisson_var, mul_gauss_var, mul_gauss_mean;
    private FloatProcessor mask;
    
    @Override
    public void run(String command) {
        GUI.setLookAndFeel();
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
                runGenerator();
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
        drift = new DataGenerator.Drift(gd.getNextNumber(), gd.getNextNumber(), false, frames);
        mask = readMask(gd.getNextString());
    }
    
    private void runGenerator() {   // TODO: run in parallel! + allow stop! <ESC>
        IJ.showStatus("ThunderSTORM is generating your image sequence...");
        IJ.showProgress(0.0);            
        //
        IJResultsTable rt = IJResultsTable.getResultsTable();
        rt.reset();
        ImageStack stack = new ImageStack(width, height);
        FloatProcessor bkg = DataGenerator.generateBackground(width, height, drift, bkg_range);
        for(int f = 0; f < frames; f++) {
            IJ.showStatus("ThunderSTORM is generating frame " + (f+1) + " out of " + frames + "...");
            IJ.showProgress((double)(f+1) / (double)frames);
            //
            FloatProcessor add_noise = DataGenerator.generatePoissonNoise(width, height, add_poisson_var);
            FloatProcessor mul_noise = DataGenerator.generateGaussianNoise(width, height, mul_gauss_mean, mul_gauss_var);
            Vector<DataGenerator.IntegratedGaussian> molecules = DataGenerator.generateMolecules(width, height, mask, pixelsize, density, energy_range, fwhm_range);
            ShortProcessor slice = DataGenerator.renderFrame(width, height, f, drift, molecules, bkg, add_noise, mul_noise);
            stack.addSlice(slice);
            addMoleculesToTable(rt, f+1, molecules);
        }
        //
        ImagePlus imp = IJ.createImage("ThunderSTORM: artificial dataset", "16-bit", width, height, frames);
        imp.setStack(stack);
        Calibration cal = new Calibration();
        cal.setUnit("micron");
        cal.pixelWidth = cal.pixelHeight = pixelsize;
        imp.setCalibration(cal);
        imp.show();
        rt.show("Ground-truth parameters");
        //
        IJ.showProgress(1.0);
        IJ.showStatus("ThunderSTORM has finished generating your image sequence.");
    }

    private FloatProcessor readMask(String imagePath) {
        if((imagePath != null) && (imagePath.trim().length() > 0)) {
            ImagePlus imp = IJ.openImage(imagePath);
            if(imp != null) {
                if((imp.getWidth() != width) || (imp.getHeight() != height)) {
                    throw new RuntimeException("Mask must have the same size as the generated images!");
                }
                // ensure that the maximum value cannot be more than 1.0 !
                FloatProcessor fmask = (FloatProcessor)imp.getProcessor().convertToFloat();
                float max = (float)fmask.getMax();
                for(int x = 0; x < width; x++) {
                    for(int y = 0; y < height; y++) {
                        fmask.setf(x, y, fmask.getf(x, y) / max);
                    }
                }
                return fmask;
            }
        }
        return ImageProcessor.ones(width, height);
    }

    private void addMoleculesToTable(IJResultsTable rt, int frame, Vector<DataGenerator.IntegratedGaussian> molecules) {
        for(DataGenerator.IntegratedGaussian mol : molecules) {
            rt.addRow();
            rt.addValue("frame", frame);
            rt.addValue(PSFInstance.X, mol.x0);
            rt.addValue(PSFInstance.Y, mol.y0);
            rt.addValue(PSFInstance.INTENSITY, mol.I0);
            rt.addValue(PSFInstance.SIGMA, mol.sig0);
        }
    }

}
