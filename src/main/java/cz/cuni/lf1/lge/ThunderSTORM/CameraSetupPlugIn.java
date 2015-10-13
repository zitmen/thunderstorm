package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.DialogStub;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterKey;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.validators.DoubleValidatorFactory;
import ij.IJ;
import ij.Macro;
import ij.plugin.PlugIn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;

public class CameraSetupPlugIn implements PlugIn {

    public static final String PIXEL_SIZE = "pixelSize";
    public static final String PHOTONS_TO_ADU = "photons2ADU";
    public static final String QUANTUM_EFFICIENCY = "quantumEfficiency";
    public static final String EM_GAIN = "gain";
    public static final String READOUT_NOISE = "readoutNoise";
    public static final String BASELINE_OFFSET = "offset";
    public static final String EM_GAIN_ENABLED = "isEmGain";

    public static final ParameterTracker params = new ParameterTracker("thunderstorm.camera");
    private static ParameterKey.Double pixelSize = params.createDoubleField("pixelSize", null, 80.0);
    private static ParameterKey.Double photons2ADU = params.createDoubleField("photons2ADU", null, 3.6);
    private static ParameterKey.Double quantumEfficiency = params.createDoubleField("quantumEfficiency", DoubleValidatorFactory.rangeInclusive(0.0, 1.0), 1.0);
    private static ParameterKey.Double gain = params.createDoubleField("gainEM", null, 100, new ParameterTracker.Condition() {
        @Override
        public boolean isSatisfied() {
            return isEmGain.getValue();
        }

        @Override
        public ParameterKey[] dependsOn() {
            return new ParameterKey[]{isEmGain};
        }
    });
    private static ParameterKey.Double readoutNoise = params.createDoubleField("readoutNoise", null, 0, new ParameterTracker.Condition() {
        @Override
        public boolean isSatisfied() {
            return !isEmGain.getValue();
        }

        @Override
        public ParameterKey[] dependsOn() {
            return new ParameterKey[]{isEmGain};
        }
    });
    private static ParameterKey.Double offset = params.createDoubleField("offset", null, 414);
    private static ParameterKey.Boolean isEmGain = params.createBooleanField("isEmGain", null, false);
    
    static{
        loadPreferences();
    }

    public static double getPixelSize() {
        return params.getDouble(pixelSize);
    }

    public static double getPhotons2ADU() {
        return params.getDouble(photons2ADU);
    }

    public static double getQuantumEfficiency() {
        return params.getDouble(quantumEfficiency);
    }

    public static double getGain() {
        return params.getDouble(gain);
    }

    public static double getReadoutNoise() {
        return params.getDouble(readoutNoise);
    }

    public static double getOffset() {
        return params.getDouble(offset);
    }

    public static boolean getIsEmGain() {
        return params.getBoolean(isEmGain);
    }

    public static void setPixelSize(double px) {
        params.setDouble(pixelSize, px);
    }

    public static void setPhotons2ADU(double adc) {
        params.setDouble(photons2ADU, adc);
    }

    public static void setQuantumEfficiency(double qe) {
        params.setDouble(quantumEfficiency, qe);
    }

    public static void setGain(double g) {
        params.setDouble(gain, g);
    }

    public static void setReadoutNoise(double readout) {
        params.setDouble(readoutNoise, readout);
    }

    public static void setOffset(double off) {
        params.setDouble(offset, off);
    }

    public static void setIsEmGain(boolean em) {
        params.setBoolean(isEmGain, em);
    }

    public static HashMap<String, Object> exportSettings() {
        HashMap<String, Object> settings = new HashMap<String, Object>();
        settings.put(PIXEL_SIZE, getPixelSize());
        settings.put(PHOTONS_TO_ADU, getPhotons2ADU());
        settings.put(QUANTUM_EFFICIENCY, getQuantumEfficiency());
        settings.put(EM_GAIN_ENABLED, getIsEmGain());
        settings.put(EM_GAIN, getGain());
        settings.put(READOUT_NOISE, getReadoutNoise());
        settings.put(BASELINE_OFFSET, getOffset());
        return settings;
    }

    @Override
    public void run(String arg) {
        String macroOptions = Macro.getOptions();
        if(macroOptions != null) {
            params.readMacroOptions();
        } else {
            GUI.setLookAndFeel();
            
            DialogStub dialog2 = new DialogStub(params, IJ.getInstance(), "Camera setup") {

                @Override
                protected void layoutComponents() {
                    setLayout(new GridBagLayout());

                    add(new JLabel("Pixel size [nm]:"), GridBagHelper.leftCol());
                    JTextField pixelSizeTextField = new JTextField(20);
                    add(pixelSizeTextField, GridBagHelper.rightCol());
                    params.registerComponent(pixelSize, pixelSizeTextField);

                    add(new JLabel("Photoelectrons per A/D count:"), GridBagHelper.leftCol());
                    JTextField photons2ADUTextField = new JTextField(20);
                    add(photons2ADUTextField, GridBagHelper.rightCol());
                    params.registerComponent(photons2ADU, photons2ADUTextField);

                    add(new JLabel("Quantum efficiency:"), GridBagHelper.leftCol());
                    JTextField quantumEfficiencyTextField = new JTextField(20);
                    add(quantumEfficiencyTextField, GridBagHelper.rightCol());
                    params.registerComponent(quantumEfficiency, quantumEfficiencyTextField);

                    add(new JLabel("Base level [A/D counts]:"), GridBagHelper.leftCol());
                    JTextField offsetTextField = new JTextField(20);
                    add(offsetTextField, GridBagHelper.rightCol());
                    params.registerComponent(offset, offsetTextField);

                    final JCheckBox emGainCheckBox = new JCheckBox("EM gain:", !getIsEmGain()); // force the call of the ItemListener to init the dialog properly (enable/disable gain/readout text fields)
                    emGainCheckBox.setBorder(BorderFactory.createEmptyBorder());
                    final JTextField emGainTextField = new JTextField(20);
                    final JTextField readoutNoiseTextField = new JTextField(20);
                    emGainCheckBox.addItemListener(new ItemListener() {
                        @Override
                        public void itemStateChanged(ItemEvent e) {
                            emGainTextField.setEnabled(emGainCheckBox.isSelected());
                            readoutNoiseTextField.setEnabled(!emGainCheckBox.isSelected());
                        }
                    });
                    add(emGainCheckBox, GridBagHelper.leftCol());
                    add(emGainTextField, GridBagHelper.rightCol());
                    add(new JLabel("Readout noise [e-/pixel]:"), GridBagHelper.leftCol());
                    add(readoutNoiseTextField, GridBagHelper.rightCol());
                    params.registerComponent(isEmGain, emGainCheckBox);
                    params.registerComponent(gain, emGainTextField);
                    params.registerComponent(readoutNoise, readoutNoiseTextField);

                    JPanel buttons = new JPanel(new GridBagLayout());
                    buttons.add(createDefaultsButton());
                    buttons.add(Box.createHorizontalGlue(), new GridBagHelper.Builder()
                            .fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
                    buttons.add(Help.createHelpButton(CameraSetupPlugIn.class));
                    buttons.add(createOKButton());
                    buttons.add(createCancelButton());
                    add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
                    add(buttons, GridBagHelper.twoCols());

                    params.updateComponents();

                    getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    pack();
                    setLocationRelativeTo(null);
                    setModal(true);
                }
            };
            dialog2.showAndGetResult();
        }
    }

    public static double pixelsToNanometers(double pixels) {
        return pixels * getPixelSize();
    }

    public static double nanometersToPixels(double nanometers) {
        return nanometers / getPixelSize();
    }

    public static double pixels2ToNanometers2(double pixels2) {
        return pixels2 * getPixelSize() * getPixelSize();
    }

    public static double nanometers2ToPixels2(double nanometers2) {
        return nanometers2 / getPixelSize() / getPixelSize();
    }

    public static double adCountsToPhotons(double counts) {
        return (counts - getOffset()) * getPhotons2ADU() / getGain();
    }

    public static double photonsToAdCounts(double photons) {
        return photons / getPhotons2ADU() * getGain() + getOffset();
    }

    public static double digitalCountsToPhotons(double intensity) {
        if(getIsEmGain()) {
            return intensity * getPhotons2ADU() / getQuantumEfficiency() / getGain();
        } else {
            return intensity * getPhotons2ADU() / getQuantumEfficiency();
        }
    }

    public static double photonsToDigitalCounts(double photons) {
        if(getIsEmGain()) {
            return photons * getGain() / getPhotons2ADU() * getQuantumEfficiency();
        } else {
            return photons / getPhotons2ADU() * getQuantumEfficiency();
        }
    }

    public static void loadPreferences() {
        params.loadPrefs();
    }
}
