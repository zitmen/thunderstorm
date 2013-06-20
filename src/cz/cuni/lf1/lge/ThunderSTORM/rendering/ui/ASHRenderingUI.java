package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.ASHRendering;
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
public class ASHRenderingUI extends AbstractRenderingUI {

  JTextField shiftsTextField;
  int shifts;
  private static final int DEFAULT_SHIFTS = 2;

  public ASHRenderingUI() {
  }

  public ASHRenderingUI(int sizeX, int sizeY) {
    super(sizeX, sizeY);
  }

  @Override
  public String getName() {
    return "ASH Renderer";
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = super.getOptionsPanel();

    shiftsTextField = new JTextField(Integer.toString(DEFAULT_SHIFTS), 20);
    panel.add(new JLabel("Shifts:"), GridBagHelper.leftCol());
    panel.add(shiftsTextField, GridBagHelper.rightCol());

    return panel;
  }

  @Override
  public void readParameters() {
    shifts = Integer.parseInt(shiftsTextField.getText());
    super.readParameters();
  }

  @Override
  public void recordOptions() {
    super.recordOptions();
    if (shifts != DEFAULT_SHIFTS) {
      Recorder.recordOption("shifts", Integer.toString(shifts));
    }
  }

  @Override
  public void readMacroOptions(String options) {
    super.readMacroOptions(options);
    shifts = Integer.parseInt(Macro.getValue(options, "shifts", Integer.toString(DEFAULT_SHIFTS)));
  }

  @Override
  public IncrementalRenderingMethod getMethod() {
    return new ASHRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(resolution).shifts(shifts).build();
  }
}
