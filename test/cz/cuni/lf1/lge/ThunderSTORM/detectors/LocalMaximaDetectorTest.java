package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.utils.Graph;
import cz.cuni.lf1.lge.ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Collections;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Martin Ovesny <martin.ovesny[at]lf1.cuni.cz>
 */
public class LocalMaximaDetectorTest {
    
    /**
     * Test of detectMoleculeCandidates method, of class LocalMaximaDetector.
     */
    @Test
    public void testDetectMoleculeCandidates() {
        System.out.println("LocalMaximadetector::detectMoleculeCandidates");
        
        Vector<Point> result, expResult;
        LocalMaximaDetector instance;
        FloatProcessor image = new FloatProcessor(new float [][] {  // transposed
            { 9f, 9f, 7f, 7f, 6f },
            { 4f, 6f, 7f, 5f, 6f },
            { 1f, 1f, 1f, 1f, 1f },
            { 2f, 3f, 4f, 3f, 2f },
            { 2f, 3f, 3f, 3f, 2f }
        });
        instance = new LocalMaximaDetector(Graph.CONNECTIVITY_4, 5f);
        expResult = new Vector<Point>();
        expResult.add(new Point(0,0,9f));
        expResult.add(new Point(0,1,9f));
        expResult.add(new Point(0,3,7f));
        expResult.add(new Point(1,2,7f));
        expResult.add(new Point(1,4,6f));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals("Searching for a maximum in 4-neighborhood failed!", expResult, result);
        
        instance = new LocalMaximaDetector(Graph.CONNECTIVITY_8, 0f);
        expResult = new Vector<Point>();
        expResult.add(new Point(0,0,9f));
        expResult.add(new Point(0,1,9f));
        expResult.add(new Point(0,3,7f));
        expResult.add(new Point(3,2,4f));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals("Searching for a maximum in 8-neighborhood failed!", expResult, result);
    }

}