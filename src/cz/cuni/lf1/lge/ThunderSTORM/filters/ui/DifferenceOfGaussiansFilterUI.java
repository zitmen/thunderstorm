package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.DifferenceOfGaussiansFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DifferenceOfGaussiansFilterUI extends IFilterUI {

    private final String name = "Difference of Gaussians";
    private int size;
    private double sigma_g1, sigma_g2;
    private transient static final int DEFAULT_SIZE = 11;
    private transient static final double DEFAULT_SIGMA_G1 = 1.6;
    private transient static final double DEFAULT_SIGMA_G2 = 1;
    private transient JTextField sigma1TextField, sigma2TextField, sizeTextField;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        sizeTextField = new JTextField(Prefs.get("thunderstorm.filters.dog.size", "" + DEFAULT_SIZE), 20);
        sigma1TextField = new JTextField(Prefs.get("thunderstorm.filters.dog.sigma1", "" + DEFAULT_SIGMA_G1), 20);
        sigma2TextField = new JTextField(Prefs.get("thunderstorm.filters.dog.sigma2", "" + DEFAULT_SIGMA_G2), 20);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
        panel.add(sizeTextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Sigma1 [px]: "), GridBagHelper.leftCol());
        panel.add(sigma1TextField, GridBagHelper.rightCol());
        panel.add(new JLabel("Sigma2 [px]: "), GridBagHelper.leftCol());
        panel.add(sigma2TextField, GridBagHelper.rightCol());
        return panel;
    }

    @Override
    public void readParameters() {
        size = Integer.parseInt(sizeTextField.getText());
        sigma_g1 = Double.parseDouble(sigma1TextField.getText());
        sigma_g2 = Double.parseDouble(sigma2TextField.getText());

        Prefs.set("thunderstorm.filters.dog.size", "" + size);
        Prefs.set("thunderstorm.filters.dog.sigma1", "" + sigma_g1);
        Prefs.set("thunderstorm.filters.dog.sigma2", "" + sigma_g2);
    }

    @Override
    public IFilter getImplementation() {
        return new DifferenceOfGaussiansFilter(size, sigma_g1, sigma_g2);
    }

    @Override
    public void recordOptions() {
        if(size != DEFAULT_SIZE) {
            Recorder.recordOption("size", Integer.toString(size));
        }
        if(sigma_g1 != DEFAULT_SIGMA_G1) {
            Recorder.recordOption("sigma_g1", Double.toString(sigma_g1));
        }
        if(sigma_g2 != DEFAULT_SIGMA_G2) {
            Recorder.recordOption("sigma_g2", Double.toString(sigma_g2));
        }
    }

    @Override
    public void readMacroOptions(String options) {
        size = Integer.parseInt(Macro.getValue(options, "size", Integer.toString(DEFAULT_SIZE)));
        sigma_g1 = Double.parseDouble(Macro.getValue(options, "sigma_g1", Double.toString(DEFAULT_SIGMA_G1)));
        sigma_g2 = Double.parseDouble(Macro.getValue(options, "sigma_g2", Double.toString(DEFAULT_SIGMA_G2)));
    }

    @Override
    public void resetToDefaults() {
        sigma1TextField.setText(Double.toString(DEFAULT_SIGMA_G1));
        sigma2TextField.setText(Double.toString(DEFAULT_SIGMA_G2));
        sizeTextField.setText(Integer.toString(DEFAULT_SIZE));
    }
}
