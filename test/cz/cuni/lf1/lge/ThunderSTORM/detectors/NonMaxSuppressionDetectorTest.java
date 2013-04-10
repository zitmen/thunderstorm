package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Collections;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class NonMaxSuppressionDetectorTest {
    
    /**
     * Test of detectMoleculeCandidates method, of class NonMaxSuppressionDetector.
     */
    @Test
    public void testDetectMoleculeCandidates() {
        System.out.println("NonMaxSuppressionDetector::detectMoleculeCandidates");
        
        Vector<Point> result, expResult;
        NonMaxSuppressionDetector instance;
        FloatProcessor image = new FloatProcessor(new float [][] {  // transposed
            { 9f, 9f, 7f, 7f, 6f },
            { 4f, 6f, 7f, 5f, 6f },
            { 1f, 1f, 1f, 1f, 1f },
            { 2f, 3f, 4f, 3f, 2f },
            { 2f, 3f, 3f, 3f, 2f }
        });
        instance = new NonMaxSuppressionDetector(3, 3.0);
        expResult = new Vector<Point>();
        expResult.add(new Point(3,2,4f));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult, result);
    }

}