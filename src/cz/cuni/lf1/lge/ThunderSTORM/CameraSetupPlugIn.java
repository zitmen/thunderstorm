package cz.cuni.lf1.lge.ThunderSTORM;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.Help;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.DialogStub;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterKey;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import ij.IJ;
import ij.Macro;
import ij.plugin.PlugIn;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GridBagLayout;
import java.util.HashMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class CameraSetupPlugIn implements PlugIn {

    private static final ParameterTracker params = new ParameterTracker("thunderstorm.camera");
    private static ParameterKey.Double pixelSize = params.createDoubleField("pixelSize", null, 80.0);
    private static ParameterKey.Double photons2ADU = params.createDoubleField("photons2ADU", null, 3.6);
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
    private static ParameterKey.Double offset = params.createDoubleField("offset", null, 414);
    private static ParameterKey.Boolean isEmGain = params.createBooleanField("isEmGain", null, false);

    public static double getPixelSize() {
        return params.getDouble(pixelSize);
    }

    public static double getPhotons2ADU() {
        return params.getDouble(photons2ADU);
    }

    public static double getGain() {
        return params.getDouble(gain);
    }

    public static double getOffset() {
        return params.getDouble(offset);
    }

    public static boolean isIsEmGain() {
        return params.getBoolean(isEmGain);
    }

    public static HashMap<String, Object> exportSettings() {
        HashMap<String, Object> settings = new HashMap<String, Object>();
        settings.put("pixelSize", getPixelSize());
        settings.put("photons2ADU", getPhotons2ADU());
        settings.put("gain", getGain());
        settings.put("offset", getOffset());
        settings.put("isEmGain", isIsEmGain());
        return settings;
    }

    @Override
    public void run(String arg) {
        loadPreferences();
        //
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

                    add(new JLabel("Base level [A/D counts]:"), GridBagHelper.leftCol());
                    JTextField offsetTextField = new JTextField(20);
                    add(offsetTextField, GridBagHelper.rightCol());
                    params.registerComponent(offset, offsetTextField);

                    final JCheckBox emGainCheckBox = new JCheckBox("EM gain:", true);
                    emGainCheckBox.setBorder(BorderFactory.createEmptyBorder());
                    final JTextField emGain = new JTextField(20);
                    emGainCheckBox.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            emGain.setEnabled(emGainCheckBox.isSelected());
                        }
                    });
                    add(emGainCheckBox, GridBagHelper.leftCol());
                    add(emGain, GridBagHelper.rightCol());
                    params.registerComponent(isEmGain, emGainCheckBox);
                    params.registerComponent(gain, emGain);

                    JPanel buttons = new JPanel(new GridBagLayout());
                    buttons.add(createDefaultsButton());
                    buttons.add(Box.createHorizontalGlue(), new GridBagHelper.Builder()
                            .fill(GridBagConstraints.HORIZONTAL).weightx(1).build());
                    buttons.add(Help.createHelpButton(CameraSetupPlugIn.class));
                    buttons.add(createOKButton());
                    buttons.add(createCancelButton());
                    add(Box.createVerticalStrut(10), GridBagHelper.twoCols());
                    add(buttons, GridBagHelper.twoCols());

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
        if(isIsEmGain()) {
            return intensity * getPhotons2ADU() / getGain();
        } else {
            return intensity * getPhotons2ADU();
        }
    }

    public static double photonsToDigitalCounts(double photons) {
        if(isIsEmGain()) {
            return photons * getGain() / getPhotons2ADU();
        } else {
            return photons / getPhotons2ADU();
        }
    }

    public static void loadPreferences() {
        params.loadPrefs();
    }
}
