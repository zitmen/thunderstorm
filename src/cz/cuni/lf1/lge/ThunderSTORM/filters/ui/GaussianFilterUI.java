package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.GaussianFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class GaussianFilterUI implements IFilterUI {

  private int size;
  private double sigma;
  private JTextField sizeTextField, sigmaTextField;
  private static final int DEFAULT_SIZE = 11;
  private static final double DEFAULT_SIGMA = 1.6;

  @Override
  public String getName() {
    return "Gaussian blur";
  }

  @Override
  public JPanel getOptionsPanel() {
    sizeTextField = new JTextField(Prefs.get("thunderstorm.filters.gauss.size", "" + DEFAULT_SIZE), 20);
    sigmaTextField = new JTextField(Prefs.get("thunderstorm.filters.gauss.sigma", "" + DEFAULT_SIGMA), 20);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
    panel.add(sizeTextField, GridBagHelper.rightCol());
    panel.add(new JLabel("Sigma [px]: "), GridBagHelper.leftCol());
    panel.add(sigmaTextField, GridBagHelper.rightCol());
    return panel;
  }

  @Override
  public void readParameters() {
    size = Integer.parseInt(sizeTextField.getText());
    sigma = Double.parseDouble(sigmaTextField.getText());
    
    Prefs.set("thunderstorm.filters.gauss.size", size + "");
    Prefs.set("thunderstorm.filters.gauss.sigma", sigma + "");
  }

  @Override
  public IFilter getImplementation() {
    return new GaussianFilter(size, sigma);
  }

  @Override
  public void recordOptions() {
    if (size != DEFAULT_SIZE) {
      Recorder.recordOption("size", Integer.toString(size));
    }
    if (sigma != DEFAULT_SIGMA) {
      Recorder.recordOption("sigma", Double.toString(sigma));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    size = Integer.parseInt(Macro.getValue(options, "size", Integer.toString(DEFAULT_SIZE)));
    sigma = Double.parseDouble(Macro.getValue(options, "sigma", Double.toString(DEFAULT_SIGMA)));
  }

  @Override
  public void resetToDefaults() {
    sizeTextField.setText("" + DEFAULT_SIZE);
    sigmaTextField.setText("" + DEFAULT_SIGMA);
  }
}
