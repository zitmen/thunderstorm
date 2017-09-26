package cz.cuni.lf1.lge.ThunderSTORM.estimators.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.LSQFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.MultipleLocationsImageFitting;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PhasorFitter;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.IEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.IntegerValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import ij.Prefs;
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

    private final String name = "Phasor Fitting";
    private int fittingRadius;
    private transient ParameterKey.Integer FITRADPhasor;
    private transient ParameterKey.String CALIBRATION_PATH;
    private transient ParameterKey.Boolean USE_ASTIGMATISM;
    JCheckBox astigmatismCheckBox = new JCheckBox("enable");
    
    public DefocusCalibration calibration;
    private String calibrationFilePath;

    public PhasorFitterUI() {
        FITRADPhasor= parameters.createIntField("fitradius", IntegerValidatorFactory.positiveNonZero(), 2);
        USE_ASTIGMATISM = parameters.createBooleanField("astigmatism", null, false);
        CALIBRATION_PATH = parameters.createStringField("calibrationpath", null, "");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        //final JCheckBox astigmatismCheckBox = new JCheckBox("enable");
    
        JTextField fitregsizeTextField = new JTextField("", 20);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Fit radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());
        parameters.registerComponent(FITRADPhasor, fitregsizeTextField);
        
        panel.add(new JLabel("Use 3D astigmatism:"), GridBagHelper.leftCol());
        panel.add(astigmatismCheckBox, GridBagHelper.rightCol());
        parameters.registerComponent(USE_ASTIGMATISM, astigmatismCheckBox);
        
        
        panel.add(new JLabel("Astigmatism calibration file:"), GridBagHelper.leftCol());
        final JTextField calibrationFileTextField = new JTextField(Prefs.get("thunderstorm.estimators.calibrationpath", ""));
        JButton findCalibrationButton = DialogStub.createBrowseButton(calibrationFileTextField, true, new FileNameExtensionFilter("Yaml file", "yaml"));
        JPanel calibrationPanel = new JPanel(new BorderLayout()){
            @Override
            public Dimension getPreferredSize() {
                return ((JTextField) parameters.getRegisteredComponent(FITRADPhasor)).getPreferredSize();
            }
        };
        calibrationPanel.add(calibrationFileTextField, BorderLayout.CENTER);
        calibrationPanel.add(findCalibrationButton, BorderLayout.EAST);
        GridBagConstraints gbc = GridBagHelper.rightCol();
        panel.add(calibrationPanel, gbc);
        //Set enabled status of calibration file finder to status of checkbox
        calibrationFileTextField.setEnabled(astigmatismCheckBox.isSelected());
        findCalibrationButton.setEnabled(astigmatismCheckBox.isSelected());
        parameters.registerComponent(CALIBRATION_PATH, calibrationFileTextField);
        
        //If checkbox is changed, change status of calibration file finder
        astigmatismCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calibrationFileTextField.setEnabled(astigmatismCheckBox.isSelected());
                findCalibrationButton.setEnabled(astigmatismCheckBox.isSelected());
            }
        });
        parameters.loadPrefs();
        
        return panel;
    }

    @Override
    public IEstimator getImplementation() {
        //3D Phasor
        if (astigmatismCheckBox.isSelected()){
            calibration = loadCalibration(parameters.getString(CALIBRATION_PATH));
            return new MultipleLocationsImageFitting(fittingRadius = FITRADPhasor.getValue(), new PhasorFitter(FITRADPhasor.getValue(),calibration));
        }//2D Phasor
        else {
            return new MultipleLocationsImageFitting(fittingRadius = FITRADPhasor.getValue(), new PhasorFitter(FITRADPhasor.getValue()));
        }
    }
    
    private DefocusCalibration loadCalibration(String calibrationFilePath) {
        this.calibrationFilePath = calibrationFilePath;
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
