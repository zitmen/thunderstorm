package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IRenderer;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public interface IRendererUI extends IModuleUI<IRenderer> {
  
  public void setSize(int sizeX, int sizeY) ;
  
}
