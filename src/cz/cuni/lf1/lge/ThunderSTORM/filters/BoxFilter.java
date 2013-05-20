package cz.cuni.lf1.lge.ThunderSTORM.filters;

import ij.process.FloatProcessor;
import java.util.HashMap;

/**
 * Box filter is a uniform convolution filter with its kernel filled with ones,
 * i.e., it is a mean filter, because it calculates mean value of intensities of
 * surrounding pixels.
 *
 * This filter uses the separable kernel feature.
 *
 * @see ConvolutionFilter
 */
public final class BoxFilter extends UniformFilter implements IFilter {

  /**
   * Initialize the filter.
   *
   * @param size size of a box (if size is 5, then the box is 5x5 pixels)
   */
  public BoxFilter(int size) {
    super(size, 1.0f / (float) size);
    export_variables = null;
  }

  private void updateKernel() {
    super.updateKernel(size, 1.0f / (float) size);
  }

  @Override
  public String getFilterVarName() {
    return "Box";
  }

  @Override
  public HashMap<String, FloatProcessor> exportVariables() {
    if (export_variables == null) {
      export_variables = new HashMap<String, FloatProcessor>();
    }
    //
    export_variables.put("I", input);
    export_variables.put("F", result);
    return export_variables;
  }
}
