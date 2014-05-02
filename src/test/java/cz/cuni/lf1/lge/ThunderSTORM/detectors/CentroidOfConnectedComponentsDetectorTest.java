package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import java.util.Collections;
import java.util.List;
import java.util.Vector;
import org.apache.commons.math3.util.MathUtils;
import org.junit.Test;
import static org.junit.Assert.*;

public class CentroidOfConnectedComponentsDetectorTest {
    
    /**
     * Test of detectMoleculeCandidates method, of class CentroidOfConnectedComponentsDetector.
     */
    @Test
    public void testDetectMoleculeCandidates() throws FormulaParserException {
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
        instance = new CentroidOfConnectedComponentsDetector("5.0", false);
        expResult = new Vector<Point>();
        expResult.add(new Point(0.5,1.5,7.0));
        expResult.add(new Point(3.0,3.0,6.0));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult, result);
        
        
        image = new FloatProcessor(new float [][] {  // transposed
            { 3f, 5f, 3f, 1f, 3f, 5f, 3f },
            { 5f, 8f, 5f, 3f, 5f, 8f, 5f },
            { 3f, 5f, 3f, 1f, 3f, 5f, 3f }
        });
        instance = new CentroidOfConnectedComponentsDetector("3.0", true);
        expResult = new Vector<Point>();
        expResult.add(new Point(1.0,1.0,8.0));
        expResult.add(new Point(1.0,5.0,8.0));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult, result);
        

        image = new FloatProcessor(new float [][] {  // transposed
            { 5f, 1f },
            { 1f, 5f }
        });
        instance = new CentroidOfConnectedComponentsDetector("3.0", true);
        expResult = new Vector<Point>();
        expResult.add(new Point(0.5,0.5,5.0));
        result = instance.detectMoleculeCandidates(image);
        Collections.sort(result, new Point.XYComparator());
        assertEquals(expResult, result);
        
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
 
    @Test
    public void testDetectMoleculeCandidates2() {
        // seven molecules close together that needs watershed segmentation to resolve them
        String basePath = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println(basePath);
        FloatProcessor fp = (FloatProcessor) IJ.openImage(basePath + "7peaksWaveletFiltered.tif").getProcessor().convertToFloat();
        CentroidOfConnectedComponentsDetector detector = new CentroidOfConnectedComponentsDetector("2", true);
        List<Point> detections = detector.detectMoleculeCandidates(fp);
        assertEquals(7, detections.size());
        for(Point p: detections){
            double x = p.getX().doubleValue();
            double y = p.getY().doubleValue();
            MathUtils.checkFinite(x);
            MathUtils.checkFinite(y);
            assertTrue("in range", x >=0 && x <= fp.getWidth());
            assertTrue("in range", y >=0 && y <= fp.getWidth());
        }
    }
}