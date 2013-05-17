package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public interface IRenderer extends IModule {
  
  public void setSize(int width, int height);

  public void renderAsync(double[] x, double[] y, double[] dx);

  public void renderAsync(double[] x, double[] y, double dx);

  public void repaintAsync();
}
