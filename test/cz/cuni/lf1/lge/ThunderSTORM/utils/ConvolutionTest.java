package cz.cuni.lf1.lge.ThunderSTORM.utils;

import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import cz.cuni.lf1.lge.ThunderSTORM.util.Convolution;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConvolutionTest {
    
    /**
     * Test of convolve2D method, of class Convolution.
     */
    @Test
    public void testConvolve() {
        System.out.println("Convolution::Convolve");
        
        float [] expResult;
        FloatProcessor kernel, result;
        FloatProcessor image = new FloatProcessor(5, 5, new float [] {
            17f, 24f,  1f,  8f, 15f,
            23f,  5f,  7f, 14f, 16f,
             4f,  6f, 13f, 20f, 22f,
            10f, 12f, 19f, 21f,  3f,
            11f, 18f, 25f,  2f,  9f
        });
        
        // row vector kernel
        kernel = new FloatProcessor(3, 1, new float [] {0.1576f, 0.4854f, 0.4218f});
        result = Convolution.convolve2D(image, kernel, Padding.PADDING_ZERO);
        expResult = new float [] {
            12.0342f, 18.9778f, 11.8694f,  6.6690f, 10.6554f,
            11.9522f, 13.2316f,  7.7132f, 12.2698f, 13.6716f,
             2.8872f,  6.6484f, 11.9930f, 18.6586f, 19.1148f,
             6.7452f, 13.0372f, 17.5938f, 18.6804f, 10.3140f,
             8.1762f, 17.3170f, 20.0426f, 12.9342f,  5.2122f
        };
        assertArrayEquals("Convolution with row vector kernel", expResult, (float[])result.getPixels(), 0.0001f);
        
        // column vector kernel
        kernel = new FloatProcessor(1, 3, new float [] {0.9572f, 0.4854f, 0.8003f});
        result = Convolution.convolve2D(image, kernel, Padding.PADDING_ZERO);
        expResult = new float [] {
            30.2674f, 16.4356f,  7.1858f, 17.2840f, 22.5962f,
            28.5981f, 27.3774f, 16.6417f, 32.3420f, 40.8293f,
            29.9205f, 18.4003f, 30.0991f, 41.0134f, 26.3552f,
            18.5844f, 27.8562f, 43.5565f, 28.1138f, 27.6776f,
            13.3424f, 18.3408f, 27.3407f, 17.7771f,  6.7695f
        };
        assertArrayEquals("Convolution with column vector kernel", expResult, (float[])result.getPixels(), 0.0001f);
        
        // matrix kernel
        kernel = new FloatProcessor(3, 3, new float [] {
            0.9649f, 0.9572f, 0.1419f,
            0.1576f, 0.4854f, 0.4218f,
            0.9706f, 0.8003f, 0.9157f
        });
        result = Convolution.convolve2D(image, kernel, Padding.PADDING_ZERO);
        expResult = new float [] {
            38.8743f, 33.7818f, 32.7879f, 36.5015f, 27.9572f,
            58.4699f, 67.8308f, 70.8481f, 76.3634f, 56.8981f,
            47.2979f, 69.7437f, 75.9145f, 77.4943f, 50.5909f,
            43.6674f, 77.0326f, 81.3179f, 82.0897f, 55.1332f,
            27.8264f, 54.5190f, 66.6193f, 50.0506f, 26.8428f
        };
        assertArrayEquals("Convolution with matrix kernel", expResult, (float[])result.getPixels(), 0.0001f);
    }

}