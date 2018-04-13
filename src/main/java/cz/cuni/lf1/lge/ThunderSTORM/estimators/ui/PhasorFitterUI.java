package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PhasorFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ij.Prefs;
import ij.IJ;
import ij.ImagePlus;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.yaml.snakeyaml.Yaml;

public class PhasorFitterUI extends IEstimatorUI {
    public transient static final String Phasor = "pSMLM2D";
    public transient static final String PhasorAstig = "pSMLMastigmatism";
    
    private final String name = "Phasor Fitting";
    private boolean astigmatism;
    private int fittingRadius;
    private int fitregsize;
    private String calibrationFile;
    private String method;
    //private transient ParameterKey.Integer FITRADPhasor;
    //private transient ParameterKey.String CALIBRATION_PATH;
    //private transient ParameterKey.Boolean USE_ASTIGMATISM;
    JCheckBox astigmatismCheckBox = new JCheckBox("enable");
    private DaostormCalibration daoCalibration;   // internal variable for calculation of uncertainty
    
    public DefocusCalibration calibration;
    private String calibrationFilePath;
    
    protected transient ParameterTracker params;
    protected transient ParameterKey.Boolean USE_ASTIGMATISM;
    protected transient ParameterKey.Integer FITRADPhasor;
    protected transient ParameterKey.String CALIBRATION_PATH;
    protected transient ParameterKey.String METHOD;

    public PhasorFitterUI() {
        params = new ParameterTracker("thunderstorm.estimators.phasor.isthreedim");
        ParameterTracker.Condition enabledPhasorAstigmatism = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return USE_ASTIGMATISM.getValue();
            }

            @Override
            public ParameterKey[] dependsOn() {
                return new ParameterKey[]{USE_ASTIGMATISM};
            }
        };

        FITRADPhasor= params.createIntField("fitradius", IntegerValidatorFactory.positiveNonZero(), 2);
        USE_ASTIGMATISM = params.createBooleanField("astigmatism", null, false);
        CALIBRATION_PATH = params.createStringField("calibrationpath", null, "", enabledPhasorAstigmatism);
        //METHOD = params.createStringField("method", StringValidatorFactory.isMember(new String[]{PhasorAstig, Phasor}), Phasor);
    }

    @Override
    public String getName() {
        return name;
    }
    public String getMethod() {
        loadValues();
        if (USE_ASTIGMATISM.getValue()){//(astigmatismCheckBox.isSelected()){
            return "PhasorAstig";
        }else{
            return "Phasor";
        }
        //return method;
    }
    
    public DaostormCalibration getDaoCalibration() {
        if (daoCalibration == null) {
            daoCalibration = calibration.getDaoCalibration();
        }
        return daoCalibration;
    }

    public boolean isEnabled() {
        return USE_ASTIGMATISM.getValue();
    }
    
    @Override
    public JPanel getOptionsPanel() {
        //final JCheckBox astigmatismCheckBox = new JCheckBox("enable",true);
    
        JTextField fitregsizeTextField = new JTextField("", 20);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Fit radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());
        params.registerComponent(FITRADPhasor, fitregsizeTextField);
        
        panel.add(new JLabel("Use 3D astigmatism:"), GridBagHelper.leftCol());
        panel.add(astigmatismCheckBox, GridBagHelper.rightCol());
        params.registerComponent(USE_ASTIGMATISM, astigmatismCheckBox);
                
        panel.add(new JLabel("Astigmatism calibration file:"), GridBagHelper.leftCol());
        final JTextField calibrationFileTextField = new JTextField(Prefs.get("thunderstorm.estimators.calibrationpath", ""));
        JButton findCalibrationButton = DialogStub.createBrowseButton(calibrationFileTextField, true, new FileNameExtensionFilter("Yaml file", "yaml"));
        JPanel calibrationPanel = new JPanel(new BorderLayout()){
            @Override
            public Dimension getPreferredSize() {
                return ((JTextField) params.getRegisteredComponent(FITRADPhasor)).getPreferredSize();
            }
        };
        calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
        calibrationPanel.add(findCalibrationButton, BorderLayout.EAST);
        GridBagConstraints gbc = GridBagHelper.rightCol();
        panel.add(calibrationPanel, gbc);
        //Set enabled status of calibration file finder to status of checkbox
        calibrationFileTextField.setEnabled(astigmatismCheckBox.isSelected());
        findCalibrationButton.setEnabled(astigmatismCheckBox.isSelected());
        params.registerComponent(CALIBRATION_PATH, calibrationFileTextField);
        
        //If checkbox is changed, change status of calibration file finder
        astigmatismCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calibrationFileTextField.setEnabled(astigmatismCheckBox.isSelected());
                findCalibrationButton.setEnabled(astigmatismCheckBox.isSelected());
            }
        });
        params.loadPrefs();
        
        return panel;
    }

    @Override
    public void readParameters() {
        super.readParameters();
        params.readDialogOptions();
        params.savePrefs();
    }

    @Override
    public void recordOptions() {
        super.recordOptions();
        params.recordMacroOptions();
    }

    @Override
    public void readMacroOptions(String options) {
        super.readMacroOptions(options);
        params.readMacroOptions();
    }

    @Override
    public void resetToDefaults() {
        super.resetToDefaults();
        params.resetToDefaults(true);
    }
    
    @Override
    protected ParameterTracker getParameterTracker() {
        return params;
    }

    @Override
    public IEstimator getImplementation() {
        //3D Phasor
        /*if (astigmatismCheckBox.isSelected()){
            loadValues();
            //calibration = loadCalibration(params.getString(CALIBRATION_PATH));
            //return new MultipleLocationsImageFitting(fittingRadius = FITRADPhasor.getValue(), new PhasorFitter(FITRADPhasor.getValue(),calibration));
            calibration = loadCalibration(calibrationFile);
            return new MultipleLocationsImageFitting(fitregsize,new PhasorFitter(fitregsize,calibration));
        }//2D Phasor
        else {
            loadValues();
            //return new MultipleLocationsImageFitting(fittingRadius = FITRADPhasor.getValue(), new PhasorFitter(FITRADPhasor.getValue()));
            return new MultipleLocationsImageFitting(fitregsize,new PhasorFitter(fitregsize));
        }*/
        //method = METHOD.getValue();
        method = getMethod();
        loadValues();
        if (astigmatism){
            return new MultipleLocationsImageFitting(fittingRadius = getROIsize(),new PhasorFitter(getROIsize(),calibration = loadCalibration(getCalibrationPath())));
        }else{
            return new MultipleLocationsImageFitting(fittingRadius = getROIsize(),new PhasorFitter(getROIsize()));
        }
    }
    
    private void loadValues() {
        fitregsize = getROIsize();
        astigmatism = isEnabled();
        if (astigmatism){
            calibrationFile = getCalibrationPath();
        }
        //method = getMethod();
    }
    
    public int getROIsize() {
        return FITRADPhasor.getValue();
    }

    public String getCalibrationPath() {
        return CALIBRATION_PATH.getValue();
    }
    
    private DefocusCalibration loadCalibration(String calibrationFilePath) {
        this.calibrationFilePath = getCalibrationPath();//calibrationFilePath;
        try {
            Yaml yaml = new Yaml();
            Object loaded = yaml.load(new FileReader(calibrationFilePath));
            return (DefocusCalibration) loaded;
        } catch(FileNotFoundException ex) {
            throw new RuntimeException("Could not read calibration file.", ex);
        } catch(ClassCastException ex) {
            throw new RuntimeException("Could not parse calibration file.", ex);
        }
    }
}
