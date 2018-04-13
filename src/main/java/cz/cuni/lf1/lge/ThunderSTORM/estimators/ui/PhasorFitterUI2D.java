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

public class PhasorFitterUI2D extends IEstimatorUI {

    private final String name = "Phasor-based localisation 2D";
    private int fittingRadius;
    private transient ParameterKey.Integer FITRADPhasor;

    public PhasorFitterUI2D() {
        FITRADPhasor= parameters.createIntField("fitradius", IntegerValidatorFactory.positiveNonZero(), 2);
    }

    @Override
    public String getName() {
        return name;
    }
    public String getMethod() {
        
            return "Phasor";
    }
    
    @Override
    public JPanel getOptionsPanel() {
    
        JTextField fitregsizeTextField = new JTextField("", 20);
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Fit radius [px]:"), GridBagHelper.leftCol());
        panel.add(fitregsizeTextField, GridBagHelper.rightCol());
        parameters.registerComponent(FITRADPhasor, fitregsizeTextField);
        parameters.loadPrefs();
        
        return panel;
    }

    @Override
    public IEstimator getImplementation() {
            return new MultipleLocationsImageFitting(fittingRadius = FITRADPhasor.getValue(), new PhasorFitter(FITRADPhasor.getValue()));
        
    }
    
}
