package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import cz.cuni.lf1.lge.ThunderSTORM.util.GrayScaleImageImpl;
import cz.cuni.lf1.thunderstorm.algorithms.filters.WaveletFilter;
import cz.cuni.lf1.thunderstorm.algorithms.padding.DuplicatePadding;
import cz.cuni.lf1.thunderstorm.algorithms.padding.ZeroPadding;
import cz.cuni.lf1.thunderstorm.datastructures.GrayScaleImage;
import ij.process.FloatProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageMath.subtract;

/**
 * This wavelet filter is implemented as an undecimated wavelet transform using
 * scaled B-spline of k-th order.
 *
 * This filter uses the separable kernel feature. Note that in the convolution
 * with the wavelet kernels we use padding twice to simulate {@code conv2}
 * function from Matlab to keep the results identical to the results we got in
 * Matlab version of ThunderSTORM.
 */
public final class CompoundWaveletFilter implements IFilter {

    private FloatProcessor input = null, result = null, result_F1 = null, result_F2 = null;
    private HashMap<String, FloatProcessor> export_variables = null;
    private cz.cuni.lf1.thunderstorm.algorithms.filters.CompoundWaveletFilter filter;
    private WaveletFilter w1, w2;

    public CompoundWaveletFilter() {
        this(3, 2.0, 5);    // these settings yield the identical kernel to the one proposed by Izeddin
    }

    /**
     * Initialize the filter with all the wavelet kernels needed to create the
     * wavelet transform.
     */
    public CompoundWaveletFilter(int spline_order, double spline_scale, int spline_n_samples) {
        w1 = new WaveletFilter(1, spline_order, spline_scale, spline_n_samples, DuplicatePadding::new);
        w2 = new WaveletFilter(2, spline_order, spline_scale, spline_n_samples, DuplicatePadding::new);
        filter = new cz.cuni.lf1.thunderstorm.algorithms.filters.CompoundWaveletFilter(
                spline_order, spline_scale, spline_n_samples, ZeroPadding::new);
    }

    @NotNull
    @Override
    public FloatProcessor filterImage(@NotNull FloatProcessor image) {
        GUI.checkIJEscapePressed();

        input = image;
        GrayScaleImage V1 = w1.filter(new GrayScaleImageImpl(image));
        GrayScaleImage V2 = w2.filter(V1);

        result_F1 = subtract(input, GrayScaleImageImpl.convertToFloatProcessor(V1));
        result_F2 = subtract(input, GrayScaleImageImpl.convertToFloatProcessor(V2));
        result = GrayScaleImageImpl.convertToFloatProcessor(filter.filter(new GrayScaleImageImpl(image)));
        
        return result;
    }

    @Override
    public String getFilterVarName() {
        return "Wave";
    }

    @NotNull
    @Override
    public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
        if(export_variables == null) {
            export_variables = new HashMap<>();
        }
        //
        if(reevaluate) {
            filterImage(Thresholder.getCurrentImage());
        }
        //
        export_variables.put("I", input);
        export_variables.put("F", result);
        export_variables.put("F1", result_F1);
        export_variables.put("F2", result_F2);
        return export_variables;
    }
}
