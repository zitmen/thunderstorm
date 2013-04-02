/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ThunderSTORM.filters;

import ij.IJ;
import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class EmptyFilterTest {
    
    /**
     * Test of filterImage method, of class EmptyFilter.
     */
    @Test
    public void testFilterImage() {
        System.out.println("filterImage");
        FloatProcessor image = (FloatProcessor) IJ.openImage("test/resources/rice.png").getProcessor().convertToFloat();
        EmptyFilter instance = new EmptyFilter();
        FloatProcessor result = instance.filterImage(image);
        assertEquals(image, result);
    }
}