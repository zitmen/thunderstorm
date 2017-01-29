package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import ij.process.FloatProcessor;
import java.util.HashMap;

/**
 * The interface every filter has to implement.
 */
interface IFilter : IModule {
    
    /**
     * Apply a filter on an input image and return the result.
     *
     * @param image an input image
     * @return a <strong>new instance</strong> of FloatProcessor that contains
     *         the filtered image
     */
    @Throws(StoppedByUserException::class)
    fun filterImage(image: FloatProcessor): FloatProcessor
    
    /**
     * Return a name of the filter that will be used when building a thresholding formula.
     * 
     * The function can also return {@code null} to signalize that there are no exported variables.
     * 
     * @return a name of the filter that will be used when building a thresholding formula
     */
    fun getFilterVarName(): String
    
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
     * @param reevaluate if true, then the filterImage is evaluated before exporting the variables
     * 
     * @return a hash map with a key that represents name of a variable and with
     *         a value that represents a processed image
     */
    fun exportVariables(reevaluate: Boolean): HashMap<String,FloatProcessor>
}
