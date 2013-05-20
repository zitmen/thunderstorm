package cz.cuni.lf1.lge.ThunderSTORM.filters;

import ij.process.FloatProcessor;
import java.util.HashMap;

/**
 * The interface every filter has to implement.
 */
public interface IFilter {
    
    /**
     * Apply a filter on an input image and return the result.
     *
     * @param image an input image
     * @return a <strong>new instance</strong> of FloatProcessor that contains
     *         the filtered image
     */
    public FloatProcessor filterImage(FloatProcessor image);
    
    /**
     * Return a name of the filter that will be used when building a thresholding formula.
     * 
     * The function can also return {@code null} to signalize that there are no exported variables.
     * 
     * @return a name of the filter that will be used when building a thresholding formula
     */
    public String getFilterVarName();
    
    /**
     * Filters can provide variables, typically processed (filtered) images,
     * for purpose of building a thresholding formula.
     * 
     * All filters must return at least variable {@code I}, the original input image, and
     * and also variable {@code F}, the final filtered image!
     * 
     * <p><strong>Warning:</strong> values of the {@code HashMap} are not guaranted to be valid - they can be
     * {@code null} if the filtering hadn't been done before the thresholding, which is the case
     * if the threshold formula uses a variable of different filter than in the filtering step.
     * Caller must ensure this case to be covered!</p>
     * 
     * @return a hash map with a key that represents name of a variable and with
     *         a value that represents a processed image
     */
    public HashMap<String,FloatProcessor> exportVariables();
    
}
