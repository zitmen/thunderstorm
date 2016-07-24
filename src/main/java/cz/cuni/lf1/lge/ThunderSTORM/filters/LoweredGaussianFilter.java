package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageMath;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.process.FloatProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Lowered Gaussian filter is a convolution filter with a kernel calculated as
 * values of a Gaussian function normalized to zero.
 *
 * Such a Gaussian can be calculated as
 * {@code G(sigma,size) - mean(G(sigma,size))}.
 *
 * This filter uses the the same trick with the separable kernels as DoG filter.
 * The only difference here is that one of the filters is the Gaussian filter
 * and the other one is an uniform filter.
 *
 * @see DifferenceOfGaussiansFilter
 * @see ConvolutionFilter
 *
 */
public final class LoweredGaussianFilter implements IFilter {
    
    private FloatProcessor input = null, result = null;
    private HashMap<String, FloatProcessor> export_variables;
    
    private GaussianFilter g;
    private BoxFilter b;
    
    private int size;
    private Padding padding;
    private double sigma;
   
    
    private void updateKernel() {
        g = new GaussianFilter(size, sigma, padding);
        b = new BoxFilter(size, VectorMath.mean((float []) g.getKernelX().getPixels()), padding);
    }
    
    public LoweredGaussianFilter() {
        this(11, 1.6);
    }
    
    /**
     * Initialize the filter using the Gaussian kernel with specified size and
     * {@mathjax \sigma} normalized to 0 as described above.
     *
     * @param size size of the kernel
     * @param sigma {@mathjax \sigma} of the Gaussian function
     */
    public LoweredGaussianFilter(int size, double sigma) {
        this.size = size;
        this.sigma = sigma;
        this.padding = Padding.PADDING_DUPLICATE;
        updateKernel();
    }
    
    /**
     * Initialize the filter using the Gaussian kernel with specified size and
     * {@mathjax \sigma} normalized to 0 as described above. And also select one of
     * the padding methods.
     *
     * @param size size of the kernel
     * @param sigma {@mathjax \sigma} of the Gaussian function
     * @param paddingMethod a padding method
     * 
     * @see Padding
     */
    public LoweredGaussianFilter(int size, double sigma, Padding paddingMethod) {
        this.size = size;
        this.sigma = sigma;
        this.padding = paddingMethod;
        updateKernel();
    }

    @NotNull
    @Override
    public FloatProcessor filterImage(@NotNull FloatProcessor image) {
        input = image;
        result = ImageMath.subtract(g.filterImage(image), b.filterImage(image));
        return result;
    }

  
    @Override
    public String getFilterVarName() {
        return "LowGauss";
    }
    
    @NotNull
    @Override
    public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
        if(export_variables == null) export_variables = new HashMap<>();
        //
        if(reevaluate) {
          filterImage(Thresholder.getCurrentImage());
        }
        //
        export_variables.put("I", input);
        export_variables.put("F", result);
        return export_variables;
    }
    
    @NotNull
    @Override
    public IFilter clone() {
      return new LoweredGaussianFilter(size, sigma, padding);
    }

}
