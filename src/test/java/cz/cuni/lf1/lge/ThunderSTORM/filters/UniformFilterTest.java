package cz.cuni.lf1.lge.ThunderSTORM.filters;

import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class UniformFilterTest {
    
    /**
     * Test of updateKernel method, of class UniformFilter.
     */
    @Test
    public void testUpdateKernel() {
        System.out.println("UniformFilterTest::updateKernel");
        
        UniformFilter instance = new UniformFilter(3, 3f);
        cmpKernel(instance, 3, 3f);
        instance.updateKernel(5, 5f);
        cmpKernel(instance, 5, 5f);
    }
    
    private void cmpKernel(UniformFilter instance, int size, float val) {
        float [][] kernel;
        
        assertEquals("Uniform kernel is separable, thus there must be X and Y components instead of one large kernel!", instance.getKernel(), null);
        
        kernel = instance.getKernelX().getFloatArray();
        for(int i = 0; i < kernel.length; i++)
            assertArrayEquals("getKernelX", getArray(1, val), kernel[i], 0.0f);
        
        kernel = instance.getKernelY().getFloatArray();
        for(int i = 0; i < kernel.length; i++)
            assertArrayEquals("getKernelY", getArray(size, val), kernel[i], 0.0f);
    }
    
    private float [] getArray(int size, float val) {
        float [] arr = new float[size];
        Arrays.fill(arr, val);
        return arr;
    }
            
}