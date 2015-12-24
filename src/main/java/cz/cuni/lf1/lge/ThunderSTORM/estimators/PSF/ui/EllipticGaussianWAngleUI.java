package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui;

import cz.cuni.lf1.lge.ThunderSTORM.calibration.DefocusCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.EllipticGaussianWAnglePSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.ThunderSTORM.util.RangeValidatorFactory;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.StringValidatorFactory;
import ij.IJ;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.yaml.snakeyaml.Yaml;

public class EllipticGaussianWAngleUI extends IPsfUI {

    private final String name = "Eliptical Gaussian (3D astigmatism)";
    private final transient ParameterKey.String CALIBRATION = parameters.createStringField("calibration", StringValidatorFactory.fileExists(), Defaults.CALIBRATION);
    private final transient ParameterKey.String Z_RANGE = parameters.createStringField("z_range", RangeValidatorFactory.fromTo(), Defaults.Z_RANGE);
    
    private DefocusCalibration calibration;
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        final JTextField calibrationTextField = new JTextField("", 20);
        JTextField zRangeTextField = new JTextField("", 20);
        parameters.registerComponent(CALIBRATION, calibrationTextField);
        parameters.registerComponent(Z_RANGE, zRangeTextField);
        
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Calibration file:"), GridBagHelper.leftCol());
        
        JButton findCalibrationButton = new JButton("...");
        findCalibrationButton.setMargin(new Insets(1, 1, 1, 1));
        findCalibrationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser(IJ.getDirectory("image"));
                int userAction = fileChooser.showOpenDialog(null);
                if(userAction == JFileChooser.APPROVE_OPTION) {
                    calibrationTextField.setText(fileChooser.getSelectedFile().getPath());
                }
            }
        });
        JPanel calibrationPanel = new JPanel(new BorderLayout()){
            @Override
            public Dimension getPreferredSize(){
                return ((JTextField)parameters.getRegisteredComponent(Z_RANGE)).getPreferredSize();
            }
        };
        calibrationPanel.add(calibrationTextField, BorderLayout.CENTER);
        calibrationPanel.add(findCalibrationButton, BorderLayout.EAST);
        panel.add(calibrationPanel, GridBagHelper.rightCol());
        
        panel.add(new JLabel("Z-range (from:to) [nm]:"), GridBagHelper.leftCol());
        panel.add(zRangeTextField, GridBagHelper.rightCol());
        
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public PSFModel getImplementation() {
        return new EllipticGaussianWAnglePSF(1.6, 0);
    }

    @Override
    public synchronized double getAngle() {
        if(calibration == null) {
            calibration = loadCalibration(CALIBRATION.getValue());
        }
        return calibration.getAngle();  // [rad]
    }

    @Override
    public synchronized Range getZRange() {
        return Range.parseFromTo(Z_RANGE.getValue());
    }

    @Override
    public synchronized double getSigma1(double z) {
        if(calibration == null) {
            calibration = loadCalibration(CALIBRATION.getValue());
        }
        return calibration.getSigma1(z);
    }

    @Override
    public synchronized double getSigma2(double z) {
        if(calibration == null) {
            calibration = loadCalibration(CALIBRATION.getValue());
        }
        return calibration.getSigma2(z);
    }

    @Override
    public boolean is3D() {
        return true;
    }

    static class Defaults {
        public static final String CALIBRATION = "";
        public static final String Z_RANGE = "-300:+300";
    }
    
    private DefocusCalibration loadCalibration(String calibrationFilePath) {
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
