package cz.cuni.lf1.lge.ThunderSTORM;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_INTENSITY;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.LABEL_FRAME;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.DataGenerator;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.Drift;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.IntegratedGaussian;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import fiji.util.gui.GenericDialogPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.Prefs;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;

public class DataGeneratorPlugIn implements PlugIn {
    
    private int width, height, frames, processing_frame;
    private double density;
    private Drift drift;
    private Range fwhm_range, intensity_range;
    private double add_poisson_var;
    private String maskPath;
    private FloatProcessor mask;
    
    @Override
    public void run(String command) {
        GUI.setLookAndFeel();
        CameraSetupPlugIn.loadPreferences();
        loadPreferences();
        //
        try {
            if(MacroParser.isRanFromMacro()) {
                // Load preferences from the macro and run the generator
                readMacroOptions(Macro.getOptions());
                savePreferences();
                runGenerator();
            } else {
                // Create and show the dialog
                boolean record = Recorder.record;
                Recorder.record = false;
                
                final GenericDialogPlus gd = new GenericDialogPlus("ThunderSTORM: Data generator (16bit grayscale image sequence)");
                gd.addButton("Camera setup...", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new CameraSetupPlugIn().run("");
                    }
                });
                gd.addMessage("Image stack:");
                gd.addNumericField("Width [px]: ", width, 0);
                gd.addNumericField("Height [px]: ", height, 0);
                gd.addNumericField("Frames: ", frames, 0);
                gd.addMessage("Emitters:");
                gd.addNumericField("Density [um^-2]: ", density, 1);
                gd.addStringField("FWHM range [px]: ", fwhm_range.toStrFromTo());
                gd.addStringField("Intensity range [photons]: ", intensity_range.toStrFromTo());
                gd.addMessage("Noise:");
                gd.addNumericField("Poisson noise variance [photons]: ", add_poisson_var, 0);
                gd.addMessage("Linear drift:");
                gd.addNumericField("Drift distance [px]: ", drift.dist, 0);
                gd.addNumericField("Drift angle [deg]: ", drift.angle, 0);
                gd.addMessage("Additional settings:");
                gd.addFileField("Grayscale mask (optional): ", maskPath);
                gd.addMessage("");
                gd.addButton("Reset to defaults", new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        resetToDefaults(gd);
                    }
                });
                gd.showDialog();
                
                Recorder.record = record;
            
                if(!gd.wasCanceled()) {
                    readParams(gd);
                    if(Recorder.record) {
                        recordOptions();
                    }
                    savePreferences();
                    runGenerator();
                }
            }
        } catch (Exception ex) {
            IJ.handleException(ex);
        }
    }
    
    private void readParams(GenericDialogPlus gd) {
        width = (int)gd.getNextNumber();
        height = (int)gd.getNextNumber();
        frames = (int)gd.getNextNumber();
        density = gd.getNextNumber();
        fwhm_range = Range.parseFromTo(gd.getNextString());
        intensity_range = Range.parseFromTo(gd.getNextString());
        add_poisson_var = gd.getNextNumber();
        drift = new Drift(gd.getNextNumber(), gd.getNextNumber(), false, frames);
        maskPath = gd.getNextString();
        mask = readMask(maskPath);
    }
    
    private void runGenerator() throws InterruptedException {
        IJ.showStatus("ThunderSTORM is generating your image sequence...");
        IJ.showProgress(0.0);       
        // convert units
        intensity_range.convert(Units.PHOTON, Units.DIGITAL);
        add_poisson_var = Units.PHOTON.convertTo(Units.DIGITAL, add_poisson_var);
        //
        IJGroundTruthTable gt = IJGroundTruthTable.getGroundTruthTable();
        gt.reset();
        ImageStack stack = new ImageStack(width, height);
        //FloatProcessor bkg = new DataGenerator().generateBackground(width, height, drift);
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
            generators[c] = new GeneratorWorker(f_start, f_end/*, bkg*/);
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
        cal.pixelWidth = cal.pixelHeight = Units.NANOMETER.convertTo(Units.MICROMETER, CameraSetupPlugIn.pixelSize);
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
        //private FloatProcessor bkg;
        private DataGenerator datagen;
        private Vector<ShortProcessor> local_stack;
        private Vector<Vector<IntegratedGaussian>> local_table;
        
        public GeneratorWorker(int frame_start, int frame_end/*, FloatProcessor bkg*/) {
            this.frame_start = frame_start;
            this.frame_end = frame_end;
            //this.bkg = bkg;
            
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
                FloatProcessor add_noise;
                if(add_poisson_var > 0) {
                    add_noise = datagen.generatePoissonNoise(width, height, add_poisson_var);
                } else {
                    float [] data = new float[width*height];
                    Arrays.fill(data, 0f);
                    add_noise = new FloatProcessor(width, height, data);
                }
                Vector<IntegratedGaussian> molecules = datagen.generateMolecules(width, height, mask, density, intensity_range, fwhm_range);
                ShortProcessor slice = datagen.renderFrame(width, height, f, drift, molecules, /*bkg, */add_noise);
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
    
    // ====================================================================
    
    public void loadPreferences() {
        width = Integer.parseInt(Prefs.get("thunderstorm.datagen.width", Integer.toString(Defaults.WIDTH)));
        height = Integer.parseInt(Prefs.get("thunderstorm.datagen.height", Integer.toString(Defaults.HEIGHT)));
        frames = Integer.parseInt(Prefs.get("thunderstorm.datagen.frames", Integer.toString(Defaults.FRAMES)));
        density = Double.parseDouble(Prefs.get("thunderstorm.datagen.density", Double.toString(Defaults.DENSITY)));
        add_poisson_var = Double.parseDouble(Prefs.get("thunderstorm.datagen.addPoissonVar", Double.toString(Defaults.ADD_POISSON_VAR)));
        fwhm_range = Range.parseFromTo(Prefs.get("thunderstorm.datagen.fwhmRange", Defaults.FWHM_RANGE));
        intensity_range = Range.parseFromTo(Prefs.get("thunderstorm.datagen.intensityRange", Defaults.INTENSITY_RANGE));
        double driftDist = Double.parseDouble(Prefs.get("thunderstorm.datagen.driftDist", Double.toString(Defaults.DRIFT_DISTANCE)));
        double driftAngle = Double.parseDouble(Prefs.get("thunderstorm.datagen.driftAngle", Double.toString(Defaults.DRIFT_ANGLE)));
        drift = new Drift(driftDist, driftAngle, false, frames);
        maskPath = Prefs.get("thunderstorm.datagen.maskPath", Defaults.MASK_PATH);
        mask = readMask(maskPath);
    }
    
    public void savePreferences() {
        Prefs.set("thunderstorm.datagen.width", width);
        Prefs.set("thunderstorm.datagen.height", height);
        Prefs.set("thunderstorm.datagen.frames", frames);
        Prefs.set("thunderstorm.datagen.density", density);
        Prefs.set("thunderstorm.datagen.addPoissonVar", add_poisson_var);
        Prefs.set("thunderstorm.datagen.fwhmRange", fwhm_range.toStrFromTo());
        Prefs.set("thunderstorm.datagen.intensityRange", intensity_range.toStrFromTo());
        Prefs.set("thunderstorm.datagen.driftDist", drift.dist);
        Prefs.set("thunderstorm.datagen.driftAngle", drift.angle);
        Prefs.set("thunderstorm.datagen.maskPath", maskPath);
    }

    public void recordOptions() {
        Recorder.recordOption("width", Integer.toString(width));
        Recorder.recordOption("height", Integer.toString(height));
        Recorder.recordOption("frames", Integer.toString(frames));
        Recorder.recordOption("density", Double.toString(density));
        Recorder.recordOption("addPoissonVar", Double.toString(add_poisson_var));
        Recorder.recordOption("fwhmRange", fwhm_range.toStrFromTo());
        Recorder.recordOption("intensityRange", intensity_range.toStrFromTo());
        Recorder.recordOption("driftDist", Double.toString(drift.dist));
        Recorder.recordOption("driftAngle", Double.toString(drift.angle));
        Recorder.recordOption("maskPath", maskPath);
    }

    public void readMacroOptions(String options) {
        width = Integer.parseInt(Macro.getValue(options, "width", Integer.toString(Defaults.WIDTH)));
        height = Integer.parseInt(Macro.getValue(options, "height", Integer.toString(Defaults.HEIGHT)));
        frames = Integer.parseInt(Macro.getValue(options, "frames", Integer.toString(Defaults.FRAMES)));
        density = Double.parseDouble(Macro.getValue(options, "density", Double.toString(Defaults.DENSITY)));
        add_poisson_var = Double.parseDouble(Macro.getValue(options, "addPoissonVar", Double.toString(Defaults.ADD_POISSON_VAR)));
        fwhm_range = Range.parseFromTo(Macro.getValue(options, "fwhmRange", Defaults.FWHM_RANGE));
        intensity_range = Range.parseFromTo(Macro.getValue(options, "intensityRange", Defaults.INTENSITY_RANGE));
        double driftDist = Double.parseDouble(Macro.getValue(options, "driftDist", Double.toString(Defaults.DRIFT_DISTANCE)));
        double driftAngle = Double.parseDouble(Macro.getValue(options, "driftAngle", Double.toString(Defaults.DRIFT_ANGLE)));
        drift = new Drift(driftDist, driftAngle, false, frames);
        maskPath = Macro.getValue(options, "maskPath", Defaults.MASK_PATH);
        mask = readMask(maskPath);
    }

    public void resetToDefaults(GenericDialogPlus gd) {
        width = Defaults.WIDTH;
        height = Defaults.HEIGHT;
        frames = Defaults.FRAMES;
        density = Defaults.DENSITY;
        add_poisson_var = Defaults.ADD_POISSON_VAR;
        fwhm_range = Range.parseFromTo(Defaults.FWHM_RANGE);
        intensity_range = Range.parseFromTo(Defaults.INTENSITY_RANGE);
        drift = new Drift(Defaults.DRIFT_DISTANCE, Defaults.DRIFT_ANGLE, false, frames);
        maskPath = Defaults.MASK_PATH;
        mask = readMask(maskPath);
        //
        Vector<TextField> numFields = (Vector<TextField>)gd.getNumericFields();
        numFields.elementAt(0).setText(Integer.toString(Defaults.WIDTH));
        numFields.elementAt(1).setText(Integer.toString(Defaults.HEIGHT));
        numFields.elementAt(2).setText(Integer.toString(Defaults.FRAMES));
        numFields.elementAt(3).setText(Double.toString(Defaults.DENSITY));
        numFields.elementAt(4).setText(Double.toString(Defaults.ADD_POISSON_VAR));
        numFields.elementAt(5).setText(Double.toString(Defaults.DRIFT_DISTANCE));
        numFields.elementAt(6).setText(Double.toString(Defaults.DRIFT_ANGLE));
        
        Vector<TextField> strFields = (Vector<TextField>)gd.getStringFields();
        strFields.elementAt(0).setText(Defaults.FWHM_RANGE);
        strFields.elementAt(1).setText(Defaults.INTENSITY_RANGE);
        strFields.elementAt(2).setText(Defaults.MASK_PATH);
    }
    
    static class Defaults {
        public static final int WIDTH = 256;
        public static final int HEIGHT = 256;
        public static final int FRAMES = 100;
        public static final double DENSITY = 0.1;
        public static final double ADD_POISSON_VAR = 10;
        public static final double DRIFT_DISTANCE = 0;
        public static final double DRIFT_ANGLE = 0;
        public static final String FWHM_RANGE = "2.5:3.5";
        public static final String INTENSITY_RANGE = "700:900";
        public static final String MASK_PATH = "";
    }

}
