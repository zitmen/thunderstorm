package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;

public interface IRendererUI extends IModuleUI<IncrementalRenderingMethod> {
  
  public void setSize(int sizeX, int sizeY) ;
  
  public int getRepaintFrequency();
  
}
