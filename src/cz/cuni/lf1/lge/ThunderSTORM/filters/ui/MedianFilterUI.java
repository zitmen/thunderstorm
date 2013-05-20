package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import static cz.cuni.lf1.lge.ThunderSTORM.filters.MedianFilter.BOX;
import static cz.cuni.lf1.lge.ThunderSTORM.filters.MedianFilter.CROSS;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class MedianFilterUI implements IFilterUI {

  private int pattern;
  private int size;
  private JTextField sizeTextField;
  private JRadioButton patternCrossRadioButton, patternBoxRadioButton;

  @Override
  public String getName() {
    return "Median filter";
  }

  @Override
  public JPanel getOptionsPanel() {
    patternBoxRadioButton = new JRadioButton("box");
    patternCrossRadioButton = new JRadioButton("cross");
    sizeTextField = new JTextField(Integer.toString(size), 20);
    //
    patternBoxRadioButton.setSelected(pattern == BOX);
    patternCrossRadioButton.setSelected(pattern == CROSS);
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Pattern: "), GridBagHelper.pos(0, 0));
    panel.add(patternBoxRadioButton, GridBagHelper.pos(1, 0));
    panel.add(patternCrossRadioButton, GridBagHelper.pos(1, 1));
    panel.add(new JLabel("Size: "), GridBagHelper.pos(0, 2));
    panel.add(sizeTextField, GridBagHelper.pos(1, 2));
    return panel;
  }

  @Override
  public void readParameters() {
    size = Integer.parseInt(sizeTextField.getText());
    if (patternBoxRadioButton.isSelected()) {
      pattern = BOX;
    }
    if (patternCrossRadioButton.isSelected()) {
      pattern = CROSS;
    }
  }

  @Override
  public IFilter getImplementation() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
