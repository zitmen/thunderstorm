package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import ij.process.FloatProcessor;

/**
 * The interface every filter has to implement.
 */
public interface IFilter extends IModule{
    
    /**
     * Apply a filter on an input image and return the result.
     *
     * @param image an input image
     * @return a <strong>new instance</strong> of FloatProcessor that contains
     *         the filtered image
     */
    public FloatProcessor filterImage(FloatProcessor image);
    
}
