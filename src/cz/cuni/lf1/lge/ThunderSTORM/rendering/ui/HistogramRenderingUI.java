package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.HistogramRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.plugin.frame.Recorder;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class HistogramRenderingUI extends AbstractRenderingUI {

  JTextField avgTextField;
  int avg;
  private static final int DEFAULT_AVG = 0;

  public HistogramRenderingUI() {
  }

  public HistogramRenderingUI(int sizeX, int sizeY) {
    super(sizeX, sizeY);
  }

  @Override
  public String getName() {
    return "Histogram Renderer";
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = super.getOptionsPanel();

    avgTextField = new JTextField(Integer.toString(DEFAULT_AVG), 20);
    panel.add(new JLabel("Averages:"), GridBagHelper.leftCol());
    panel.add(avgTextField, GridBagHelper.rightCol());

    return panel;
  }

  @Override
  public void readParameters() {
    avg = Integer.parseInt(avgTextField.getText());
    super.readParameters();
  }

  @Override
  public void recordOptions() {
    super.recordOptions();
    if (avg != DEFAULT_AVG) {
      Recorder.recordOption("avg", Integer.toString(avg));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    super.readMacroOptions(options);
    avg = Integer.parseInt(Macro.getValue(options, "avg", Integer.toString(DEFAULT_AVG)));
  }

  @Override
  public IncrementalRenderingMethod getMethod() {
    return new HistogramRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(resolution).average(avg).build();
  }
}
