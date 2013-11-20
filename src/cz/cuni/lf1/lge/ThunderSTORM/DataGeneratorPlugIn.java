package cz.cuni.lf1.lge.ThunderSTORM;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqrt;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.DataGenerator;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.Drift;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.EmitterModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.ThunderSTORM.util.RangeValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.ValidatorException;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.Prefs;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DataGeneratorPlugIn implements PlugIn {

    private int width, height, frames, processing_frame;
    private double density;
    private Drift drift;
    private Range fwhm_range, intensity_range;
    private double add_poisson_var;
    private FloatProcessor mask;

    @Override
    public void run(String command) {
        try {
            GUI.setLookAndFeel();
            CameraSetupPlugIn.loadPreferences();

            if(getInput()) {
                runGenerator();
            }
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }

    private void runGenerator() throws InterruptedException {
        IJ.showStatus("ThunderSTORM is generating your image sequence...");
        IJ.showProgress(0.0);
        //
        IJGroundTruthTable gt = IJGroundTruthTable.getGroundTruthTable();
        gt.reset();
        ImageStack stack = new ImageStack(width, height);
        //FloatProcessor bkg = new DataGenerator().generateBackground(width, height, drift);
        //
        int numThreads = Math.min(Prefs.getThreads(), frames);
        GeneratorWorker[] generators = new GeneratorWorker[numThreads];
        Thread[] threads = new Thread[numThreads];
        processing_frame = 0;
        // prepare the workers and allocate resources for all the threads
        for(int c = 0, f_start = 0, f_end, f_inc = frames / numThreads; c < numThreads; c++) {
            if((c + 1) < numThreads) {
                f_end = f_start + f_inc - 1;
            } else {
                f_end = frames - 1;
            }
            generators[c] = new GeneratorWorker(f_start, f_end/*, bkg*/);
            threads[c] = new Thread(generators[c]);
            f_start = f_end + 1;
        }
        // start all the workers
        for(int c = 0; c < numThreads; c++) {
            threads[c].start();
        }
        // wait for all the workers to finish
        int wait = 1000 / numThreads;    // max 1s
        boolean finished = false;
        while(!finished) {
            finished = true;
            for(int c = 0; c < numThreads; c++) {
                threads[c].join(wait);
                finished &= !threads[c].isAlive();   // all threads must not be alive to finish!
            }
            if(IJ.escapePressed()) {    // abort?
                // stop the workers
                for(int ci = 0; ci < numThreads; ci++) {
                    threads[ci].interrupt();
                }
                // wait so the message below is not overwritten by any of the threads
                for(int ci = 0; ci < numThreads; ci++) {
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
        for(int c = 0; c < numThreads; c++) {
            generators[c].fillResults(stack, gt);   // and generate stack and table of ground-truth data
        }
        gt.insertIdColumn();
        gt.copyOriginalToActual();
        gt.setActualState();
        //
        ImagePlus imp = IJ.createImage("ThunderSTORM: artificial dataset", "16-bit", width, height, frames);
        imp.setStack(stack);
        Calibration cal = new Calibration();
        cal.setUnit("um");
        cal.pixelWidth = cal.pixelHeight = Units.NANOMETER.convertTo(Units.MICROMETER, CameraSetupPlugIn.getPixelSize());
        imp.setCalibration(cal);
        imp.show();
        gt.show();
        //
        IJ.showProgress(1.0);
        IJ.showStatus("ThunderSTORM has finished generating your image sequence.");
    }

    private synchronized void processingNewFrame(String message) {
        IJ.showStatus(String.format(message, processing_frame, frames));
        IJ.showProgress((double) (processing_frame) / (double) frames);
        processing_frame++;
    }

    private class GeneratorWorker implements Runnable {

        private int frame_start, frame_end;
        //private FloatProcessor bkg;
        private DataGenerator datagen;
        private Vector<ShortProcessor> local_stack;
        private Vector<Vector<EmitterModel>> local_table;

        public GeneratorWorker(int frame_start, int frame_end/*, FloatProcessor bkg*/) {
            this.frame_start = frame_start;
            this.frame_end = frame_end;
            //this.bkg = bkg;

            datagen = new DataGenerator();
            local_stack = new Vector<ShortProcessor>();
            local_table = new Vector<Vector<EmitterModel>>();
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
                FloatProcessor backgroundMeanIntensity;
                float[] data = new float[width * height];
                Arrays.fill(data, add_poisson_var > 0 ? (float) add_poisson_var : 0f);
                backgroundMeanIntensity = new FloatProcessor(width, height, data, null);

                Vector<EmitterModel> molecules = datagen.generateMolecules(width, height, mask, density, intensity_range, fwhm_range);
                ShortProcessor slice = datagen.renderFrame(width, height, f, drift, molecules, /*bkg, */ backgroundMeanIntensity);
                local_stack.add(slice);
                local_table.add(molecules);
            }
        }

        private void fillResults(ImageStack stack, IJGroundTruthTable gt) {
            double bkgstd = Units.PHOTON.convertTo(Units.getDefaultUnit(PSFModel.Params.LABEL_BACKGROUND), sqrt(add_poisson_var));
            for(int f = frame_start, i = 0; f <= frame_end; f++, i++) {
                processingNewFrame("ThunderSTORM is building the image stack - frame %d out of %d...");
                stack.addSlice("", local_stack.elementAt(i));
                for(EmitterModel psf : local_table.elementAt(i)) {
                    psf.molecule.insertParamAt(0, MoleculeDescriptor.LABEL_FRAME, MoleculeDescriptor.Units.UNITLESS, (double) (f + 1));
                    psf.molecule.setParam(PSFModel.Params.LABEL_OFFSET, CameraSetupPlugIn.getOffset());
                    psf.molecule.setParam(PSFModel.Params.LABEL_BACKGROUND, bkgstd);
                    gt.addRow(psf.molecule);
                }
            }
        }

    }

    private FloatProcessor readMask(String imagePath) {
        if((imagePath != null) && (imagePath.trim().length() > 0)) {
            ImagePlus imp = IJ.openImage(imagePath);
            if(imp != null) {
                // ensure that the maximum value cannot be more than 1.0 !
                FloatProcessor fmask = (FloatProcessor) imp.getProcessor().convertToFloat();
                float min = 0;
                float max = (float) fmask.getMax();
                if(max > 0) {
                    for(int x = 0; x < fmask.getWidth(); x++) {
                        for(int y = 0; y < fmask.getHeight(); y++) {
                            fmask.setf(x, y, (fmask.getf(x, y) - min) / (max - min));
                        }
                    }
                }
                return fmask;
            }
        }
        return ImageProcessor.ones(width, height);
    }

    private boolean getInput() {
        final ParameterTracker params = new ParameterTracker("thunderstorm.datagen");
        ParameterName.Integer widthParam = params.createIntField("width", IntegerValidatorFactory.positiveNonZero(), Defaults.WIDTH);
        ParameterName.Integer heightParam = params.createIntField("height", IntegerValidatorFactory.positiveNonZero(), Defaults.WIDTH);
        ParameterName.Integer framesParam = params.createIntField("frames", IntegerValidatorFactory.positiveNonZero(), Defaults.FRAMES);
        ParameterName.Double densityParam = params.createDoubleField("density", DoubleValidatorFactory.positiveNonZero(), Defaults.DENSITY);
        ParameterName.Double addPoissonVarParam = params.createDoubleField("addPoisssonVar", DoubleValidatorFactory.positive(), Defaults.ADD_POISSON_VAR);
        ParameterName.String fwhmRangeParam = params.createStringField("fwhmRange", RangeValidatorFactory.fromTo(), Defaults.FWHM_RANGE);
        ParameterName.String intensityRangeParam = params.createStringField("intensityRange", RangeValidatorFactory.fromTo(), Defaults.INTENSITY_RANGE);
        ParameterName.Double driftDistParam = params.createDoubleField("driftDist", null, Defaults.DRIFT_DISTANCE);
        ParameterName.Double driftAngleParam = params.createDoubleField("driftAngle", null, Defaults.DRIFT_ANGLE);
        ParameterName.String maskPathParam = params.createStringField("maskPath", null, Defaults.MASK_PATH);
        //
        String macroOptions = Macro.getOptions();
        if(macroOptions != null) {
            params.readMacroOptions();
        } else {
            final AtomicBoolean clickedOK = new AtomicBoolean(false);
            GUI.setLookAndFeel();
            final JDialog dialog = new JDialog(IJ.getInstance(), "ThunderSTORM: Data generator (16bit grayscale image sequence)", true);
            dialog.setLayout(new GridBagLayout());

            JPanel cameraPanel = new JPanel(new BorderLayout());
            cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera"));
            JButton cameraButton = new JButton("Camera setup");
            cameraButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    new CameraSetupPlugIn().run(null);
                }
            });
            cameraPanel.add(cameraButton);

            //image stack
            JPanel stackPanel = new JPanel(new GridBagLayout());
            stackPanel.setBorder(BorderFactory.createTitledBorder("Image stack"));

            stackPanel.add(new JLabel("Width [px]:"), GridBagHelper.leftCol());
            JTextField widthTextField = new JTextField(20);
            stackPanel.add(widthTextField, GridBagHelper.rightCol());
            params.registerComponent(widthParam, widthTextField);

            stackPanel.add(new JLabel("Height [px]:"), GridBagHelper.leftCol());
            JTextField heightTextField = new JTextField(20);
            stackPanel.add(heightTextField, GridBagHelper.rightCol());
            params.registerComponent(heightParam, heightTextField);

            stackPanel.add(new JLabel("Frames:"), GridBagHelper.leftCol());
            JTextField framesTextField = new JTextField(20);
            stackPanel.add(framesTextField, GridBagHelper.rightCol());
            params.registerComponent(framesParam, framesTextField);

            //emitters
            JPanel emittersPanel = new JPanel(new GridBagLayout());
            emittersPanel.setBorder(BorderFactory.createTitledBorder("Emitters"));

            emittersPanel.add(new JLabel("Density [um^-2]:"), GridBagHelper.leftCol());
            JTextField densityTextField = new JTextField(20);
            emittersPanel.add(densityTextField, GridBagHelper.rightCol());
            params.registerComponent(densityParam, densityTextField);

            emittersPanel.add(new JLabel("FWHM range [nm]:"), GridBagHelper.leftCol());
            JTextField fwhmTextField = new JTextField(20);
            emittersPanel.add(fwhmTextField, GridBagHelper.rightCol());
            params.registerComponent(fwhmRangeParam, fwhmTextField);

            emittersPanel.add(new JLabel("Intensity range [photons]:"), GridBagHelper.leftCol());
            JTextField intensityTextField = new JTextField(20);
            emittersPanel.add(intensityTextField, GridBagHelper.rightCol());
            params.registerComponent(intensityRangeParam, intensityTextField);
            //Noise
            JPanel noisePanel = new JPanel(new GridBagLayout());
            noisePanel.setBorder(BorderFactory.createTitledBorder("Noise"));

            noisePanel.add(new JLabel("Poisson noise variance [photons]:"), GridBagHelper.leftCol());
            final JTextField noiseVarTextField = new JTextField(20);
            noisePanel.add(noiseVarTextField, GridBagHelper.rightCol());
            params.registerComponent(addPoissonVarParam, noiseVarTextField);

            //drift
            JPanel driftPanel = new JPanel(new GridBagLayout());
            driftPanel.setBorder(BorderFactory.createTitledBorder("Linear drift"));

            driftPanel.add(new JLabel("Drift distance [nm]:"), GridBagHelper.leftCol());
            JTextField driftDistTextField = new JTextField(20);
            driftPanel.add(driftDistTextField, GridBagHelper.rightCol());
            params.registerComponent(driftDistParam, driftDistTextField);

            driftPanel.add(new JLabel("Drift angle [deg]:"), GridBagHelper.leftCol());
            JTextField driftAngleTextField = new JTextField(20);
            driftPanel.add(driftAngleTextField, GridBagHelper.rightCol());
            params.registerComponent(driftAngleParam, driftAngleTextField);

            //additional
            JPanel additionalPanel = new JPanel(new GridBagLayout());
            additionalPanel.setBorder(BorderFactory.createTitledBorder("Additional settings"));

            additionalPanel.add(new JLabel("Grayscale mask (optional):"), GridBagHelper.leftCol());
            JPanel pathPanel = new JPanel(new BorderLayout()) {
                @Override
                public Dimension getPreferredSize() {
                    return noiseVarTextField.getPreferredSize();//same size as fields above
                }
            };
            final JTextField pathTextField = new JTextField(15);
            params.registerComponent(maskPathParam, pathTextField);
            JButton browseButton = new JButton("...");
            browseButton.setMargin(new Insets(1, 1, 1, 1));
            browseButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fileChooser = new JFileChooser(IJ.getDirectory("image"));
                    int userAction = fileChooser.showOpenDialog(dialog);
                    if(userAction == JFileChooser.APPROVE_OPTION) {
                        pathTextField.setText(fileChooser.getSelectedFile().getPath());
                    }
                }
            });
            pathPanel.add(pathTextField);
            pathPanel.add(browseButton, BorderLayout.EAST);
            additionalPanel.add(pathPanel, GridBagHelper.rightCol());

            //buttons
            JPanel buttons = new JPanel(new GridBagLayout());
            JButton defaultsButton = new JButton("Defaults");
            defaultsButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    params.resetToDefaults(true);
                }
            });
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dialog.dispose();
                }
            });
            JButton okButton = new JButton("OK");
            okButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        clickedOK.set(true);
                        params.readDialogOptions();
                        params.recordMacroOptions();
                        params.savePrefs();
                        dialog.dispose();
                    } catch(ValidatorException ex) {
                        IJ.showMessage("Input validation error: " + ex.getMessage());
                    }
                }
            });
            buttons.add(defaultsButton);
            buttons.add(Box.createHorizontalGlue(), new GridBagHelper.Builder()
                    .fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
            buttons.add(Help.createHelpButton(DataGeneratorPlugIn.class));
            buttons.add(okButton);
            buttons.add(cancelButton);

            dialog.add(cameraPanel, GridBagHelper.twoCols());
            dialog.add(stackPanel, GridBagHelper.twoCols());
            dialog.add(emittersPanel, GridBagHelper.twoCols());
            dialog.add(noisePanel, GridBagHelper.twoCols());
            dialog.add(driftPanel, GridBagHelper.twoCols());
            dialog.add(additionalPanel, GridBagHelper.twoCols());
            dialog.add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
            dialog.add(buttons, GridBagHelper.twoCols());

            params.loadPrefs();
            dialog.getRootPane().setDefaultButton(okButton);
            dialog.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setResizable(false);
            dialog.setVisible(true);
            if(!clickedOK.get()) {
                return false;
            }
        }
        width = widthParam.getValue();
        height = heightParam.getValue();
        frames = framesParam.getValue();
        density = densityParam.getValue();
        add_poisson_var = addPoissonVarParam.getValue();
        fwhm_range = Range.parseFromTo(fwhmRangeParam.getValue());
        fwhm_range.convert(Units.NANOMETER, Units.PIXEL);
        intensity_range = Range.parseFromTo(intensityRangeParam.getValue());
        drift = new Drift(Units.NANOMETER.convertTo(Units.PIXEL, driftDistParam.getValue()), driftAngleParam.getValue(), false, frames);
        mask = readMask(maskPathParam.getValue());

        return true;
    }

    static class Defaults {

        public static final int WIDTH = 256;
        public static final int HEIGHT = 256;
        public static final int FRAMES = 1000;
        public static final double DENSITY = 0.1;
        public static final double ADD_POISSON_VAR = 30;
        public static final double DRIFT_DISTANCE = 0;
        public static final double DRIFT_ANGLE = 0;
        public static final String FWHM_RANGE = "200:350";
        public static final String INTENSITY_RANGE = "700:900";
        public static final String MASK_PATH = "";
    }

}
