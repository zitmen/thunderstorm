package ThunderSTORM.utils;

import ij.process.FloatProcessor;
import org.junit.Test;
import static org.junit.Assert.*;

public class PaddingTest {
    
    /**
     * Test of addBorder method, of class Padding.
     */
    @Test
    public void testAddBorder() {
        System.out.println("addBorder");
        FloatProcessor image = null;
        int size = 0;
        int type = 0;
        FloatProcessor expResult = null;
        FloatProcessor result = Padding.addBorder(image, size, type);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
}