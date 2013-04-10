package cz.cuni.lf1.lge.ThunderSTORM.filters;

import ij.process.FloatProcessor;

public interface IFilter {
    
    public FloatProcessor filterImage(FloatProcessor image);
    
}
