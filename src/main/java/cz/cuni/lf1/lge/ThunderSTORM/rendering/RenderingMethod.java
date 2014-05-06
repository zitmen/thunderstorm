package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import ij.ImagePlus;

/**
 * SMLM rendering in one step - when all the molecule locations are already
 * known.
 */
public interface RenderingMethod {

  /**
   * Returns superresolution image rendered from the molecule locations.Length
   * of x,y and dx arrays must be the same.
   *
   * @param x x coordinates of localized molecules
   * @param y y coordinates of localized molecules
   * @param z z coordinates of localized molecules. When null, 0 is used for all molecules. 
   * @param dx localization XY uncertainty (not used in all implementations). When null, default value is used.
   * @param dz localization Z uncertainty (not used in all implementations). When null, default value is used.
   */
  ImagePlus getRenderedImage(double[] x, double[] y, double[] z, double[] dx, double[] dz);
}
