package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageMath.subtract;
import ij.process.FloatProcessor;
import java.util.HashMap;

/**
 * Similarly to DoG filter, this filter is a difference of two filters with different kernels,
 * in this case both are box kernels.
 * Usualy the first one has smaller kernel than the second one.
 *
 * This filter uses the separable kernel feature.
 *
 * @see BoxFilter
 */
public final class DifferenceOfBoxFilters implements IFilter {
    
    private FloatProcessor input = null, result = null, result_B1 = null, result_B2 = null;
    private HashMap<String, FloatProcessor> export_variables = null;
    private int size1, size2;
    private BoxFilter box1, box2;

    public DifferenceOfBoxFilters() {
        this(3, 6);
    }

    /**
     * Initialize the filter.
     *
     * @param size1 size of the first box (if size is 3, then the box is 3x3 pixels)
     * @param size2 size of the second, usualy larger, box (if size is 6, then the box is 6x6 pixels)
     */
    public DifferenceOfBoxFilters(int size1, int size2) {
        export_variables = null;
        this.size1 = size1;
        this.size2 = size2;
        this.box1 = new BoxFilter(size1);
        this.box2 = new BoxFilter(size2);
    }

    @Override
    public String getFilterVarName() {
        return "DoB";
    }
    
    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        input = image;
        result_B1 = box1.filterImage(image);
        result_B2 = box2.filterImage(image);
        result = subtract(result_B1, result_B2);
        return result;
    }

    @Override
    public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
        if(export_variables == null) {
            export_variables = new HashMap<String, FloatProcessor>();
        }
        //
        if(reevaluate) {
            filterImage(Thresholder.getCurrentImage());
        }
        //
        export_variables.put("I", input);
        export_variables.put("B1", result_B1);
        export_variables.put("B2", result_B2);
        export_variables.put("F", result);
        return export_variables;
    }

    @Override
    public IFilter clone() {
        return new DifferenceOfBoxFilters(size1, size2);
    }
}
