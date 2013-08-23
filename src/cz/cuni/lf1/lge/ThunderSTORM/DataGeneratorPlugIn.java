package cz.cuni.lf1.lge.ThunderSTORM;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_INTENSITY;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_FRAME;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.DataGenerator;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.Drift;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.IntegratedGaussian;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
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
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JSeparator;

// TODO: macro, defaults, save preferences
public class DataGeneratorPlugIn implements PlugIn {
    
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
        try {
            // Create and show the dialog
            GenericDialogPlus gd = new GenericDialogPlus("ThunderSTORM: Data generator (16bit grayscale image sequence)");
            gd.addNumericField("Width [px]: ", 256, 0);
            gd.addNumericField("Height [px]: ", 256, 0);
            gd.addNumericField("Frames: ", 1000, 0);
            gd.addNumericField("Pixel width [um]: ", 0.8, 1);
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            gd.addNumericField("Density [mol/um^2]: ", 0.01, 1);
            gd.addStringField("Emitter FWHM range [px]: ", "1.5:2.5");
            gd.addStringField("Emitter energy range [digital units]: ", "30000:60000"); // ~ (1000-2000) [photons]
            gd.addComponent(new JSeparator(JSeparator.HORIZONTAL));
            gd.addStringField("Background intensity range [digital units]: ", "100:120");
            gd.addMessage("Read-out:");
            gd.addNumericField("Additive Poisson noise variance [digital units]: ", 10, 0);
            gd.addMessage("Gain:");
            gd.addNumericField("Multiplicative Gaussian noise mean [digital units]: ", 1, 0);
            gd.addNumericField("Multiplicative Gaussian noise variance [digital units]: ", 10, 0);
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
        drift = new Drift(gd.getNextNumber(), gd.getNextNumber(), false, frames);
        mask = readMask(gd.getNextString());
    }
    
    private void runGenerator() throws InterruptedException {
        IJ.showStatus("ThunderSTORM is generating your image sequence...");
        IJ.showProgress(0.0);            
        //
        IJGroundTruthTable gt = IJGroundTruthTable.getGroundTruthTable();
        gt.reset();
        ImageStack stack = new ImageStack(width, height);
        FloatProcessor bkg = new DataGenerator().generateBackground(width, height, drift, bkg_range);
        //
        int cores = Runtime.getRuntime().availableProcessors();
        GeneratorWorker [] generators = new GeneratorWorker[cores];
        Thread [] threads = new Thread[cores];
        processing_frame = 0;
        // prepare the workers and allocate resources for all the threads
        for(int c = 0, f_start = 0, f_end, f_inc = frames / cores; c < cores; c++) {
            if((c+1) < cores) {
                f_end = f_start + f_inc;
            } else {
                f_end = frames - 1;
            }
            generators[c] = new GeneratorWorker(f_start, f_end, bkg);
            threads[c] = new Thread(generators[c]);
            f_start = f_end + 1;
        }
        // start all the workers
        for(int c = 0; c < cores; c++) {
            threads[c].start();
        }
        // wait for all the workers to finish
        int wait = 1000 / cores;    // max 1s
        boolean finished = false;
        while(!finished) {
            finished = true;
            for(int c = 0; c < cores; c++) {
                threads[c].join(wait);
                finished &= !threads[c].isAlive();   // all threads must not be alive to finish!
            }
            if(IJ.escapePressed()) {    // abort?
                // stop the workers
                for(int ci = 0; ci < cores; ci++) {
                    threads[ci].interrupt();
                }
                // wait so the message below is not overwritten by any of the threads
                for(int ci = 0; ci < cores; ci++) {
                    threads[ci].join();
                }
                // show info and exit the plugin
                IJ.showProgress(1.0);
                IJ.showStatus("Operation has been aborted by user!");
                return;
            }
        }
        processing_frame = 0;
        gt.setOriginalState();
        for(int c = 0; c < cores; c++) {
            generators[c].fillResults(stack, gt);   // and generate stack and table of ground-truth data
        }
        gt.copyOriginalToActual();
        gt.setActualState();
        //
        ImagePlus imp = IJ.createImage("ThunderSTORM: artificial dataset", "16-bit", width, height, frames);
        imp.setStack(stack);
        Calibration cal = new Calibration();
        cal.setUnit("um");
        cal.pixelWidth = cal.pixelHeight = pixelsize;
        imp.setCalibration(cal);
        imp.show();
        gt.show();
        //
        IJ.showProgress(1.0);
        IJ.showStatus("ThunderSTORM has finished generating your image sequence.");
    }
    
    private synchronized void processingNewFrame(String message) {
        IJ.showStatus(String.format(message, processing_frame, frames));
        IJ.showProgress((double)(processing_frame) / (double)frames);
        processing_frame++;
    }
    
    private class GeneratorWorker implements Runnable {
        
        private int frame_start, frame_end;
        private FloatProcessor bkg;
        private DataGenerator datagen;
        private Vector<ShortProcessor> local_stack;
        private Vector<Vector<IntegratedGaussian>> local_table;
        
        public GeneratorWorker(int frame_start, int frame_end, FloatProcessor bkg) {
            this.frame_start = frame_start;
            this.frame_end = frame_end;
            this.bkg = bkg;
            
            datagen = new DataGenerator();
            local_stack = new Vector<ShortProcessor>();
            local_table = new Vector<Vector<IntegratedGaussian>>();
        }
        
        @Override
        public void run() {
            for(int f = frame_start; f <= frame_end; f++) {
                if(Thread.interrupted()) {
                    local_stack.clear();
                    local_table.clear();
                    return;
                }
                processingNewFrame("ThunderSTORM is generating frame %d out of %d...");
                FloatProcessor add_noise, mul_noise;
                if(add_poisson_var > 0) {
                    add_noise = datagen.generatePoissonNoise(width, height, add_poisson_var);
                } else {
                    float [] data = new float[width*height];
                    Arrays.fill(data, 0f);
                    add_noise = new FloatProcessor(width, height, data);
                }
                if(mul_gauss_var > 0) {
                    mul_noise = datagen.generateGaussianNoise(width, height, mul_gauss_mean, mul_gauss_var);
                } else {
                    float [] data = new float[width*height];
                    Arrays.fill(data, (float)mul_gauss_mean);
                    mul_noise = new FloatProcessor(width, height, data);
                }
                Vector<IntegratedGaussian> molecules = datagen.generateMolecules(width, height, mask, pixelsize, density, energy_range, fwhm_range);
                ShortProcessor slice = datagen.renderFrame(width, height, f, drift, molecules, bkg, add_noise, mul_noise);
                local_stack.add(slice);
                local_table.add(molecules);
            }
        }

        private void fillResults(ImageStack stack, IJGroundTruthTable gt) {
            gt.setDescriptor(new MoleculeDescriptor(new String[] { LABEL_FRAME, LABEL_X, LABEL_Y, LABEL_INTENSITY, LABEL_SIGMA }));
            for(int f = frame_start, i = 0; f <= frame_end; f++, i++) {
                processingNewFrame("ThunderSTORM is building the image stack - frame %d out of %d...");
                stack.addSlice(local_stack.elementAt(i));
                for(IntegratedGaussian psf : local_table.elementAt(i)) {
                    gt.addRow(new double[] { f+1, psf.x0, psf.y0, psf.I0, psf.sig0 });
                }
            }
            gt.insertIdColumn();
        }
        
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

}
