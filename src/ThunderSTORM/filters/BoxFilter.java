package ThunderSTORM.filters;

public class BoxFilter extends UniformFilter {
    
    public BoxFilter(int size) {
        super(size, 1.0f / (float) size);
    }
    
}
