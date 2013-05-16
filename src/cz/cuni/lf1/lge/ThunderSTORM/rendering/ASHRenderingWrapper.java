package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.rendering.ASHRendering;
import cz.cuni.lf1.rendering.DensityRendering;
import cz.cuni.lf1.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.rendering.QueuedRenderer;
import ij.IJ;
import ij.ImagePlus;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class ASHRenderingWrapper extends AbstractRenderingWrapper {

  JTextField shiftsTextField;
  int shifts;

  public ASHRenderingWrapper(int sizeX, int sizeY) {
    super(sizeX, sizeY);
  }

  @Override
  public String getName() {
    return "ASH Renderer";
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = super.getOptionsPanel();

    shiftsTextField = new JTextField("2", 20);
    panel.add(new JLabel("Shifts:"), GridBagHelper.pos(0, GridBagConstraints.RELATIVE));
    panel.add(shiftsTextField, GridBagHelper.pos(1, GridBagConstraints.RELATIVE));

    return panel;
  }

  @Override
  public void readParameters() {
    shifts = Integer.parseInt(shiftsTextField.getText());
    super.readParameters();
  }

  @Override
  protected IncrementalRenderingMethod getMethod() {
    return new ASHRendering.Builder().roi(0, sizeX, 0, sizeY).resolution(resolution).shifts(shifts).build();
  }
}
