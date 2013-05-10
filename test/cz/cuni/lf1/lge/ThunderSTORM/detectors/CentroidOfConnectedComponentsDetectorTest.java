package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Collections;
import java.util.Vector;
import org.junit.Test;
import static org.junit.Assert.*;

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
        
        
        image = new FloatProcessor(new float [][] {  // transposed
            { 3f, 5f, 3f, 1f, 3f, 5f, 3f },
            { 5f, 8f, 5f, 3f, 5f, 8f, 5f },
            { 3f, 5f, 3f, 1f, 3f, 5f, 3f }
        });
        instance = new CentroidOfConnectedComponentsDetector(false, 3.0);
        expResult = new Vector<Point>();
        expResult.add(new Point(1.0,1.0));
        expResult.add(new Point(1.0,5.0));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult, result);
        
        
        image = new FloatProcessor(new float [][] {  // transposed
            { 5f, 1f },
            { 1f, 5f }
        });
        instance = new CentroidOfConnectedComponentsDetector(false, 3.0);
        expResult = new Vector<Point>();
        expResult.add(new Point(0.5,0.5));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult, result);
        
        instance.updateUpsample(true);
        expResult.clear();
        // these coordinates are little off, because watershed has to remove part of
        // one pixel to divide the regions; the higher upsample factor, the more
        // precise the results, BUT more computational time!
        // --> factor of 2 is sufficient, because the next step is fitting
        expResult.add(new Point(0.16,0.16));
        expResult.add(new Point(1.25,1.25));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult.size(), result.size());
        for(int i = 0, im = result.size(); i < im; i++) {
            assertEquals(expResult.get(i).x.doubleValue(), result.get(i).x.doubleValue(), 0.01);
            assertEquals(expResult.get(i).y.doubleValue(), result.get(i).y.doubleValue(), 0.01);
        }
        
        
        /* Let's skip this test, because I dont know where are the real results from Matlab...in the CSV is ground-truth.
         * -- five points were not found...and it is ok, because if you look at them in the image, it is impossible to see them :-)
         * 
         * [69.25779947916666,181.7811328125]
         * [68.47399739583334,98.31500000000001]
         * [69.75059895833334,102.15853515625001]
         * [151.1509375,46.70306640625]
         * [230.85726562500003,62.75619791666667]
         * [244.14781250000001,106.15860026041668]
         * 
         * 
        try {
            image = (FloatProcessor) IJ.openImage("test/resources/tubulins1_00020.tif").getProcessor().convertToFloat();
            FloatProcessor filtered = (new CompoundWaveletFilter(false)).filterImage(image);
            result = (new CentroidOfConnectedComponentsDetector(false, 14.2835)).detectMoleculeCandidates(filtered);
            expResult = CSV.csv2point("test/resources/tubulins1_00020.csv", 1, 2);
            for(Point pt : expResult) { // lets work with the pixels for a moment
                pt.scaleXY(1.0/150.0);  // pixelsize = 150nm
            }
            Collections.sort(result, new Point.XYComparator());
            Collections.sort(expResult, new Point.XYComparator());
            //
            System.out.println("\n============= RESULT ===============");
            for(Point pt : result) System.out.println(pt);
            System.out.println("\n\n======= EXPECTED RESULT ==========");
            for(Point pt : expResult) System.out.println(pt);
            //
            assertEquals(expResult, result); // TODO: approximate!!!
        } catch(IOException ex) {
            fail("Error in CentroidOfConnectedComponentsDetector test with real data: " + ex.getMessage());
        }
        */
    }

}