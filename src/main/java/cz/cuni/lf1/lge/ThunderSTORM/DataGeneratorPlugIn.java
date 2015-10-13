package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.CardsPanel;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.DataGenerator;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.Drift;
import cz.cuni.lf1.lge.ThunderSTORM.datagen.EmitterModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui.IPsfUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJGroundTruthTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.*;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.Validator;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.ValidatorException;
import ij.*;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt;

public class DataGeneratorPlugIn implements PlugIn {

    private int width, height, frames, processing_frame;
    private double density;
    private Drift drift;
    private Range intensity_range;
    private IPsfUI psf;
    private double add_poisson_var;
    private FloatProcessor densityMask;
    private FloatProcessor backgroundMask;
    private boolean singleFixedMolecule;

    @Override
    public void run(String command) {
        try {
            GUI.setLookAndFeel();

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
        gt.convertAllColumnsToAnalogUnits();
        //
        ImagePlus imp = IJ.createImage("Artificial dataset", "16-bit", width, height, frames);
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

        private final int frame_start, frame_end;
        //private FloatProcessor bkg;
        private final DataGenerator datagen;
        private final Vector<ShortProcessor> local_stack;
        private final Vector<Vector<EmitterModel>> local_table;

        public GeneratorWorker(int frame_start, int frame_end) {
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
                backgroundMeanIntensity = createBackgroundIntensityImage();
                Vector<EmitterModel> molecules = singleFixedMolecule
                        ? datagen.generateSingleFixedMolecule(width, height, 0, 0, intensity_range, psf)
                        : datagen.generateMolecules(width, height, densityMask, density, intensity_range, psf);
                ShortProcessor slice = datagen.renderFrame(width, height, f, drift, molecules, backgroundMeanIntensity);
                local_stack.add(slice);
                local_table.add(molecules);
            }
        }

        private void fillResults(ImageStack stack, IJGroundTruthTable gt) {
            double bkgstd = sqrt(add_poisson_var);
            for(int f = frame_start, i = 0; f <= frame_end; f++, i++) {
                processingNewFrame("ThunderSTORM is building the image stack - frame %d out of %d...");
                stack.addSlice("", local_stack.elementAt(i));
                for(EmitterModel psf : local_table.elementAt(i)) {
                    psf.molecule.insertParamAt(0, MoleculeDescriptor.LABEL_FRAME, MoleculeDescriptor.Units.UNITLESS, (double) (f + 1));
                    psf.molecule.setParam(PSFModel.Params.LABEL_OFFSET, MoleculeDescriptor.Units.DIGITAL, CameraSetupPlugIn.getOffset());
                    psf.molecule.setParam(PSFModel.Params.LABEL_BACKGROUND, bkgstd);
                    gt.addRow(psf.molecule);
                }
            }
        }

    }

    private FloatProcessor createBackgroundIntensityImage() {
        FloatProcessor backgroundMeanIntensity;
        if(backgroundMask == null) {
            float[] data = new float[width * height];
            Arrays.fill(data, add_poisson_var > 0 ? (float) add_poisson_var : 0f);
            backgroundMeanIntensity = new FloatProcessor(width, height, data, null);
            return backgroundMeanIntensity;
        } else {
            backgroundMeanIntensity = (FloatProcessor) backgroundMask.resize(width, height);
            return ImageMath.multiply((float) (add_poisson_var), backgroundMeanIntensity);
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
        return ImageMath.ones(width, height);
    }

    private boolean getInput() {
        final ParameterTracker params = new ParameterTracker("thunderstorm.datagen");
        params.setNoGuiParametersAllowed(true);
        final ParameterKey.Integer widthParam = params.createIntField("width", IntegerValidatorFactory.positiveNonZero(), Defaults.WIDTH);
        final ParameterKey.Integer heightParam = params.createIntField("height", IntegerValidatorFactory.positiveNonZero(), Defaults.HEIGHT);
        final ParameterKey.Integer framesParam = params.createIntField("frames", IntegerValidatorFactory.positiveNonZero(), Defaults.FRAMES);
        final ParameterKey.Double densityParam = params.createDoubleField("density", DoubleValidatorFactory.positive(), Defaults.DENSITY);
        final ParameterKey.String psfParam = params.createStringField("psf", new PsfValidator(), Defaults.PSF);
        final ParameterKey.Double addPoissonVarParam = params.createDoubleField("addPoisssonVar", DoubleValidatorFactory.positive(), Defaults.ADD_POISSON_VAR);
        final ParameterKey.String intensityRangeParam = params.createStringField("intensityRange", RangeValidatorFactory.fromTo(), Defaults.INTENSITY_RANGE);
        final ParameterKey.Double driftDistParam = params.createDoubleField("driftDist", null, Defaults.DRIFT_DISTANCE);
        final ParameterKey.Double driftAngleParam = params.createDoubleField("driftAngle", null, Defaults.DRIFT_ANGLE);
        final ParameterKey.String maskPathParam = params.createStringField("maskPath", null, Defaults.MASK_PATH);
        final ParameterKey.String backgroundMaskPathParam = params.createStringField("maskPathBg", null, Defaults.BG_MASK_PATH);
        ParameterKey.Boolean singleFixedMoleculeParam = params.createBooleanField("singleFixed", null, false);
        final CardsPanel psfPanel = new CardsPanel(ModuleLoader.getUIModules(IPsfUI.class), 0);
        //
        String macroOptions = Macro.getOptions();
        if(macroOptions != null) {
            params.readMacroOptions();
            List<IPsfUI> allPSFs = ModuleLoader.getUIModules(IPsfUI.class);
            for(IPsfUI psfUi : allPSFs) {
                if(psfUi.getName().equalsIgnoreCase(psfParam.getValue())) {
                    psfUi.readMacroOptions(macroOptions);
                    psf = psfUi;
                    break;
                }
            }
        } else {
            GUI.setLookAndFeel();
            DialogStub dialog = new DialogStub(params, IJ.getInstance(), "ThunderSTORM: Data generator (16bit grayscale image sequence)") {

                @Override
                protected void layoutComponents() {
                    setLayout(new GridBagLayout());

                    JPanel cameraPanel = new JPanel(new BorderLayout());
                    cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera"));
                    JButton cameraButton = new JButton("Camera setup");
                    cameraButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            MacroParser.runNestedWithRecording(PluginCommands.CAMERA_SETUP, null);
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

                    JPanel p = psfPanel.getPanel("PSF:");
                    emittersPanel.add(p, GridBagHelper.twoCols());
                    params.registerComponent(psfParam, psfPanel.getComboBox());

                    emittersPanel.add(new JLabel("Intensity range (from:to) [photons]:"), GridBagHelper.leftCol());
                    JTextField intensityTextField = new JTextField(20);
                    emittersPanel.add(intensityTextField, GridBagHelper.rightCol());
                    params.registerComponent(intensityRangeParam, intensityTextField);
                    
                    emittersPanel.add(new JLabel("Density [um^-2]:"), GridBagHelper.leftCol());
                    final JTextField densityTextField = new JTextField(20);
                    emittersPanel.add(densityTextField, GridBagHelper.rightCol());
                    params.registerComponent(densityParam, densityTextField);

                    emittersPanel.add(new JLabel("Density mask (optional):"), GridBagHelper.leftCol());
                    JPanel pathPanel = new JPanel(new BorderLayout()) {
                        @Override
                        public Dimension getPreferredSize() {
                            return densityTextField.getPreferredSize();//same size as fields above
                        }
                    };
                    final JTextField pathTextField = new JTextField(15);
                    params.registerComponent(maskPathParam, pathTextField);
                    pathPanel.add(pathTextField);
                    pathPanel.add(createBrowseButton(pathTextField, true), BorderLayout.EAST);
                    emittersPanel.add(pathPanel, GridBagHelper.rightCol());
                    
                    //Noise
                    JPanel noisePanel = new JPanel(new GridBagLayout());
                    noisePanel.setBorder(BorderFactory.createTitledBorder("Background noise"));

                    noisePanel.add(new JLabel("Mean photon background [photons]:"), GridBagHelper.leftCol());
                    final JTextField noiseVarTextField = new JTextField(20);
                    noisePanel.add(noiseVarTextField, GridBagHelper.rightCol());
                    params.registerComponent(addPoissonVarParam, noiseVarTextField);

                    noisePanel.add(new JLabel("Background intensity mask (optional):"), GridBagHelper.leftCol());
                    JPanel bgPathPanel = new JPanel(new BorderLayout()) {
                        @Override
                        public Dimension getPreferredSize() {
                            return noiseVarTextField.getPreferredSize();//same size as fields above
                        }
                    };
                    final JTextField bgPathTextField = new JTextField(15);
                    params.registerComponent(backgroundMaskPathParam, bgPathTextField);
                    bgPathPanel.add(bgPathTextField);
                    bgPathPanel.add(createBrowseButton(bgPathTextField, true), BorderLayout.EAST);
                    noisePanel.add(bgPathPanel, GridBagHelper.rightCol());

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

                    //buttons
                    JPanel buttons = new JPanel(new GridBagLayout());
                    JButton defaultButton = createDefaultsButton();
                    buttons.add(defaultButton);
                    buttons.add(Box.createHorizontalGlue(), new GridBagHelper.Builder()
                            .fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
                    buttons.add(Help.createHelpButton(DataGeneratorPlugIn.class));
                    JButton okButton = createOKButton();
                    buttons.add(okButton);
                    buttons.add(createCancelButton());

                    okButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            IPsfUI psfui = (IPsfUI) psfPanel.getActiveComboBoxItem();
                            psfui.parameters.readDialogOptions();
                            psfui.parameters.recordMacroOptions();
                            if(psfui.parameters.isPrefsSavingEnabled()) {
                                psfui.parameters.savePrefs();
                            }
                        }
                    });
                    defaultButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent ae) {
                            psfPanel.getActiveComboBoxItem().resetToDefaults();
                        }
                    });

                    add(cameraPanel, GridBagHelper.twoCols());
                    add(stackPanel, GridBagHelper.twoCols());
                    add(emittersPanel, GridBagHelper.twoCols());
                    add(noisePanel, GridBagHelper.twoCols());
                    add(driftPanel, GridBagHelper.twoCols());
                    add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
                    add(buttons, GridBagHelper.twoCols());

                    params.loadPrefs();
                    getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    pack();
                    setLocationRelativeTo(null);
                }
            };

            if(dialog.showAndGetResult() != JOptionPane.OK_OPTION) {
                return false;
            }

            psf = (IPsfUI) psfPanel.getActiveComboBoxItem();
        }
        width = widthParam.getValue();
        height = heightParam.getValue();
        frames = framesParam.getValue();
        density = densityParam.getValue();
        add_poisson_var = addPoissonVarParam.getValue();
        intensity_range = Range.parseFromTo(intensityRangeParam.getValue());
        drift = new Drift(Units.NANOMETER.convertTo(Units.PIXEL, driftDistParam.getValue()), driftAngleParam.getValue(), false, frames);
        densityMask = readMask(maskPathParam.getValue());
        backgroundMask = readMask(backgroundMaskPathParam.getValue());
        singleFixedMolecule = singleFixedMoleculeParam.getValue();

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
        public static final String INTENSITY_RANGE = "700:900";
        public static final String MASK_PATH = "";
        public static final String BG_MASK_PATH = "";
        public static final String PSF = "0";
    }

    static class PsfValidator implements Validator<String> {

        @Override
        public void validate(String input) throws ValidatorException {
            List<IPsfUI> allPSFs = ModuleLoader.getUIModules(IPsfUI.class);
            for(IPsfUI psf : allPSFs) {
                if(psf.getName().equals(input)) {
                    return; // ok
                }
            }
            throw new ValidatorException("Unknown PSF model `" + input + "`!");
        }
    }
}
