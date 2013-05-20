package cz.cuni.lf1.lge.ThunderSTORM.filters.ui;

import cz.cuni.lf1.lge.ThunderSTORM.filters.EmptyFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import javax.swing.JPanel;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class EmptyFilterUI implements IFilterUI {

  @Override
  public String getName() {
    return "No filter";
  }

  @Override
  public JPanel getOptionsPanel() {
    return null;
  }

  @Override
  public void readParameters() {
    // nothing to do here
  }

  @Override
  public IFilter getImplementation() {
    return new EmptyFilter();
  }
}
