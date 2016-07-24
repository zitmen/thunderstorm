package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
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
public final class BoxFilter implements IFilter {

    private final int mSize;
    private final ConvolutionFilter mFilter;
    private FloatProcessor input;
    private FloatProcessor result;

    /**
     * Generate a new square kernel of specified size and filled with a specified value.
     *
     * @param size size of the kernel
     * @param value value you want the kernel fill with
     * @return a new 2D square array of specified size and filled with a specified value
     */
    private static float[] getKernel(int size, float value) {
        float[] kernel = new float[size];
        Arrays.fill(kernel, value);
        return kernel;
    }

    public FloatProcessor getKernel() {
        return mFilter.getKernel();
    }

    public FloatProcessor getKernelX() {
        return mFilter.getKernelX();
    }

    public FloatProcessor getKernelY() {
        return mFilter.getKernelY();
    }

    /**
     * Initialize the filter.
     *
     * @param size size of a box (if size is 5, then the box is 5x5 pixels)
     */
    public BoxFilter(int size) {
        this(size, 1.0f/(float)size, Padding.PADDING_DUPLICATE);
    }

    public BoxFilter(int size, float value, Padding padding) {
        mSize = size;
        mFilter = ConvolutionFilter.Companion.createFromSeparableKernel(new FloatProcessor(1, size, getKernel(size, value), null), padding);
    }

    @Override
    public String getFilterVarName() {
    return "Box";
  }

    @NotNull
    @Override
    public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
        HashMap<String, FloatProcessor> export_variables = new HashMap<>();

        if(reevaluate) {
            filterImage(Thresholder.getCurrentImage());
        }
        //
        export_variables.put("I", input);
        export_variables.put("F", result);
        return export_variables;
    }

    @Override
    public FloatProcessor filterImage(@NotNull FloatProcessor image) {
        GUI.checkIJEscapePressed();
        input = image;
        result = mFilter.filterImage(image);
        return result;
    }

    @NotNull
    @Override
    public IFilter clone() {
        return new BoxFilter(mSize);
    }
}
