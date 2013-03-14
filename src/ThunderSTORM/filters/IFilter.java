package ThunderSTORM.filters;

import ij.process.FloatProcessor;

public interface IFilter {
    
    public FloatProcessor filterImage(FloatProcessor image);
    
}
