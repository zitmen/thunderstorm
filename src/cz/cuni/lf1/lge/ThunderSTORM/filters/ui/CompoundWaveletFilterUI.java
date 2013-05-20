package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.CompoundWaveletFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class CompoundWaveletFilterUI implements IFilterUI {

  private JCheckBox thirdCheckBox;
  private boolean third_plane = false;

  @Override
  public String getName() {
    return "Wavelet filter";
  }

  @Override
  public JPanel getOptionsPanel() {
    thirdCheckBox = new JCheckBox("third plane");
    thirdCheckBox.setSelected(third_plane);
    //
    JPanel panel = new JPanel();
    panel.add(thirdCheckBox);
    return panel;
  }

  @Override
  public void readParameters() {
    third_plane = thirdCheckBox.isSelected();
  }

  @Override
  public IFilter getImplementation() {
    return new CompoundWaveletFilter(third_plane);
  }
}
