package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.MedianFilter;
import static cz.cuni.lf1.lge.ThunderSTORM.filters.MedianFilter.BOX;
import static cz.cuni.lf1.lge.ThunderSTORM.filters.MedianFilter.CROSS;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.Macro;
import ij.Prefs;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import javax.swing.ButtonGroup;
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
  private static final int DEFAULT_SIZE = 3;
  private static final int DEFAULT_PATTERN = MedianFilter.BOX;

  @Override
  public String getName() {
    return "Median filter";
  }

  @Override
  public JPanel getOptionsPanel() {
    ButtonGroup btnGroup = new ButtonGroup();
    patternBoxRadioButton = new JRadioButton("box");
    patternCrossRadioButton = new JRadioButton("cross");
    btnGroup.add(patternBoxRadioButton);
    btnGroup.add(patternCrossRadioButton);
    sizeTextField = new JTextField(Prefs.get("thunderstorm.filters.median.size", ""+DEFAULT_SIZE), 20);
    //
    String patternString = Prefs.get("thunderstorm.filters.median.pattern", DEFAULT_PATTERN == BOX ? "box" : "cross");
    patternBoxRadioButton.setSelected(patternString.equals("box"));
    patternCrossRadioButton.setSelected(patternString.equals("cross"));
    //
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Kernel size [px]: "), GridBagHelper.leftCol());
    panel.add(sizeTextField, GridBagHelper.rightCol());
    panel.add(new JLabel("Pattern: "), GridBagHelper.leftCol());
    panel.add(patternBoxRadioButton, GridBagHelper.rightCol());
    panel.add(patternCrossRadioButton, GridBagHelper.rightCol());
    
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
    
    Prefs.set("thunderstorm.filters.median.size", "" + size);
    Prefs.set("thunderstorm.filters.median.pattern", pattern == BOX ? "box" : "cross");
  }

  @Override
  public IFilter getImplementation() {
    return new MedianFilter(pattern, size);
  }

  @Override
  public void recordOptions() {
    if (size != DEFAULT_SIZE) {
      Recorder.recordOption("size", Integer.toString(size));
    }
    if (pattern != DEFAULT_PATTERN) {
      Recorder.recordOption("pattern", pattern == BOX ? "box" : "cross");
    }
  }

  @Override
  public void readMacroOptions(String options) {
    size = Integer.parseInt(Macro.getValue(options, "size", Integer.toString(DEFAULT_SIZE)));
    String value = Macro.getValue(options, "pattern", DEFAULT_PATTERN == BOX ? "box" : "cross");
    pattern = value.equals("box") ? BOX : CROSS;
  }

  @Override
  public void resetToDefaults() {
    sizeTextField.setText(Integer.toString(DEFAULT_SIZE));
    patternBoxRadioButton.setSelected(DEFAULT_PATTERN == BOX);
    patternCrossRadioButton.setSelected(DEFAULT_PATTERN == CROSS);
  }
  
}
