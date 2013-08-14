package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import javax.swing.JPanel;

public class EmptyRendererUI implements IRendererUI {

  @Override
  public String getName() {
    return "No Renderer";
  }

  @Override
  public JPanel getOptionsPanel() {
    return null;
  }

  @Override
  public void readParameters() {
  }

  @Override
  public void setSize(int width, int height) {
  }

  @Override
  public void recordOptions() {
  }

  @Override
  public void readMacroOptions(String options) {
  }

  @Override
  public int getRepaintFrequency() {
    return 0;
  }

  @Override
  public IncrementalRenderingMethod getImplementation() {
    return null;
  }

  @Override
  public void resetToDefaults() {
  }
}
