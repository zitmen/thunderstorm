package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.subtract;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.crop;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import java.util.HashMap;

/**
 * This wavelet filter is implemented as an undecimated wavelet transform using B-spline of third order.
 * 
 * This filter uses the separable kernel feature.
 * Note that in the convolution with the wavelet kernels we use padding twice to
 * simulate {@code conv2} function from Matlab to keep the results identical to
 * the results we got in Matlab version of ThunderSTORM.
 * 
 * @see WaveletFilter
 * @see ConvolutionFilter
 */
public final class CompoundWaveletFilter implements IFilter {
    
    private FloatProcessor input = null, result = null, result_V1 = null, result_V2 = null, result_V3 = null;
    private HashMap<String, FloatProcessor> export_variables = null;

    private int margin;
    private boolean third_plane;
    
    private WaveletFilter w1, w2, w3;
    
    

  public CompoundWaveletFilter() {
    this(false);
  }
    
    /**
     * Initialize the filter with all the wavelet kernels needed to create the wavelet transform.
     *
     * @param third_plane if {@code true} use 3rd plane for detection; otherwise use 2nd plane instead
     */
    public CompoundWaveletFilter(boolean third_plane) {
        this.third_plane = third_plane;
        //
        w1 = new WaveletFilter(1, Padding.PADDING_ZERO);
        w2 = new WaveletFilter(2, Padding.PADDING_ZERO);
        w3 = new WaveletFilter(3, Padding.PADDING_ZERO);
        //
        this.margin = w3.getKernelX().getWidth() / 2;
    }
    
    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        input = image;
        FloatProcessor padded = Padding.addBorder(image, Padding.PADDING_DUPLICATE, margin);
        FloatProcessor V1 = w1.filterImage(padded);
        FloatProcessor V2 = w2.filterImage(V1);
        FloatProcessor V3 = w3.filterImage(V2);
        result_V1 = crop(V1, margin, margin, image.getWidth(), image.getHeight());
        result_V2 = crop(V2, margin, margin, image.getWidth(), image.getHeight());
        result_V3 = crop(V3, margin, margin, image.getWidth(), image.getHeight());
        
        if (third_plane)
            result = crop(subtract(V2, V3), margin, margin, image.getWidth(), image.getHeight());
        else
            result = crop(subtract(V1, V2), margin, margin, image.getWidth(), image.getHeight());
        
        return result;
    }
    
    
    @Override
    public String getFilterVarName() {
        return "Wave";
    }
    
    @Override
    public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
        if(export_variables == null) export_variables = new HashMap<String, FloatProcessor>();
        //
        if(reevaluate) {
          filterImage(Thresholder.getCurrentImage());
        }
        //
        export_variables.put("I", input);
        export_variables.put("F", result);
        export_variables.put("V1", result_V1);
        export_variables.put("V2", result_V2);
        export_variables.put("V3", result_V3);
        return export_variables;
    }
    
}
