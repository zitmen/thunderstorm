package ThunderSTORM.filters;

import static ThunderSTORM.utils.Math.mean;
import ThunderSTORM.utils.ImageProcessor;
import ij.process.FloatProcessor;

// This filter uses the same trick to be effective as the DoG filter
public class LoweredGaussianFilter implements IFilter {
    
    private GaussianFilter g;
    private UniformFilter u;
    
    public LoweredGaussianFilter(int size, double sigma) {
        g = new GaussianFilter(size, sigma);
        u = new UniformFilter(size, mean((float []) g.getKernelX().getPixels()));
    }

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        return ImageProcessor.subtractImage(g.filterImage(image), u.filterImage(image));
    }
    
}
