package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GrayScaleImageImpl;
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding;
import ij.process.FloatProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * Gaussian filter is a convolution filter with its kernel filled with values of normalized
 * 2D Gaussian function written as {@mathjax \frac{1}{\sqrt{2\pi\sigma^2}} e^{-\frac{x^2}{2 \sigma^2}}}.
 *
 * This kernel is symmetric and it is also separable, thus the filter uses the separable kernel feature.
 */
public final class GaussianFilter implements IFilter {
    
    private cz.cuni.lf1.thunderstorm.algorithms.filters.GaussianFilter filter;

    private HashMap<String,FloatProcessor> export_variables = null;
    private FloatProcessor input = null;
    private FloatProcessor result = null;
    
    /**
     * Initialize filter to use a kernel with a specified size filled with values
     * of the 2D Gaussian function with a specified {@mathjax \sigma} ({@code sigma}).
     *
     * @param size size of the kernel
     * @param sigma {@mathjax \sigma} of the 2D Gaussian function
     */
    public GaussianFilter(int size, double sigma) {
        this.filter = new cz.cuni.lf1.thunderstorm.algorithms.filters.GaussianFilter(size, sigma, DuplicatePadding::new);
    }
    
    @Override
    public String getFilterVarName() {
        return "Gauss";
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
    public FloatProcessor filterImage(@NotNull FloatProcessor image) throws StoppedByUserException {
        GUI.checkIJEscapePressed();

        input = image;
        result = GrayScaleImageImpl.convertToFloatProcessor(filter.filter(new GrayScaleImageImpl(image)));
        return result;
    }
}
