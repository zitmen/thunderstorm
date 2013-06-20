package cz.cuni.lf1.lge.ThunderSTORM.rendering;

import ij.ImagePlus;

/**
 * SMLM rendering in one step - when all the molecule locations are already
 * known.
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public interface RenderingMethod {

  /**
   * Returns superresolution image rendered from the molecule locations.Length
   * of x,y and dx arrays must be the same.
   *
   * @param x x coordinates of localized molecules
   * @param y y coordinates of localized molecules
   * @param z z coordinates of localized molecules
   * @param dx localization uncertainty - the same for all molecules (not used
   * in all implementations)
   */
  ImagePlus getRenderedImage(double[] x, double[] y, double[] z, double[] dx);
}
