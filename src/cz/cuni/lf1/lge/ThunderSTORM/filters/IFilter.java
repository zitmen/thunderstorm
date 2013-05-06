package cz.cuni.lf1.lge.ThunderSTORM.filters;

import ij.process.FloatProcessor;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public interface IFilter {
    
    /**
     *
     * @param image
     * @return
     */
    public FloatProcessor filterImage(FloatProcessor image);
    
}
