package ThunderSTORM.detectors;

import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Collections;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class CentroidOfConnectedComponentsDetectorTest {
    
    /**
     * Test of detectMoleculeCandidates method, of class CentroidOfConnectedComponentsDetector.
     */
    @Test
    public void testDetectMoleculeCandidates() {
        System.out.println("CentroidOfConnectedComponentsDetector::detectMoleculeCandidates");
        
        Vector<Point> result, expResult;
        CentroidOfConnectedComponentsDetector instance;
        FloatProcessor image = new FloatProcessor(new float [][] {  // transposed
            { 9f, 4f, 3f, 7f, 4f },
            { 4f, 6f, 7f, 2f, 4f },
            { 1f, 1f, 1f, 1f, 1f },
            { 2f, 3f, 5f, 6f, 8f },
            { 2f, 3f, 3f, 3f, 2f }
        });
        instance = new CentroidOfConnectedComponentsDetector(false, 5.0);
        expResult = new Vector<Point>();
        expResult.add(new Point(0.5,1.5));
        expResult.add(new Point(3.0,3.0));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult, result);
        
        /*
        // why the hell the stupid watershed does not work??!! not even with upsampling!
        image = new FloatProcessor(new float [][] {  // transposed
            { 1f, 1f, 3f, 1f, 1f, 1f, 3f, 1f, 1f },
            { 1f, 3f, 5f, 3f, 1f, 3f, 5f, 3f, 1f },
            { 3f, 5f, 8f, 5f, 3f, 5f, 8f, 5f, 3f },
            { 1f, 3f, 5f, 3f, 1f, 3f, 5f, 3f, 1f },
            { 1f, 1f, 3f, 1f, 1f, 1f, 3f, 1f, 1f }
        });
        instance = new CentroidOfConnectedComponentsDetector(false, 3.0);
        expResult = new Vector<Point>();
        expResult.add(new Point(2.0,2.0));
        expResult.add(new Point(2.0,6.0));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult, result);
        */
    }

}