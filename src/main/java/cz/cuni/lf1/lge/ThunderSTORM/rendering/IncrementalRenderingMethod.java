package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import ij.ImagePlus;

import java.util.List;

/**
 * Incremental SMLM rendering. Image can be slowly built as new molecule
 * locations are supplied.
 */
public interface IncrementalRenderingMethod extends IModule {

  /**
   * Draws the specified molecules into the image.
   *
   * @param x x coordinates of localized molecules
   * @param y y coordinates of localized molecules
   * @param z z coordinates of localized molecules. When null, 0 is used instead.
   * @param dx localization XY uncertainty - for each molecule (not used in all implementations). When null default value is used instead.
   * @param dz localization Z uncertainty - for each molecule (not used in all implementations). When null default value is used instead.
   */
  public void addToImage(double[] x, double[] y, double[] z, double[] dx, double[] dz);
  
  /**
   * Draws the specified molecules into the image.
   *
   * @param fits information about localized molecules
   */
  public void addToImage(List<Molecule> fits);

  /**
   * Returns the image to which points are rendered. Subsequent calls to
   * addToImage or reset will alter the returned ImageProcessor.
   *
   * @return
   */
  public ImagePlus getRenderedImage();
  
  /**
   * Returns the name of the renderer.
   * 
   * @return
   */
  public abstract String getRendererName();

  /**
   * Sets the whole image to zeros. Makes the image returned by getRenderedImage
   * displayable by ImagePlus#show() even if it was previously closed.
   */
  public void reset();
}
