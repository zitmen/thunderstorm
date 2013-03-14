package ThunderSTORM.filters;

import ij.process.FloatProcessor;

public class EmptyFilter implements IFilter {

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        return image;
    }
    
}
