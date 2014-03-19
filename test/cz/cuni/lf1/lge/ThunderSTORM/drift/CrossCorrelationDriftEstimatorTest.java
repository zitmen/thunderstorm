package cz.cuni.lf1.lge.ThunderSTORM.drift;

import org.junit.Test;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.ImageStack;
import static org.junit.Assert.*;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class CrossCorrelationDriftEstimatorTest {

    @Test
    public void testArtificialData() throws InterruptedException {
        double[] x = {20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30};
        double[] y = {20, 21, 22, 23, 24, 25, 24, 23, 22, 21, 20};
        double[] frame = {11, 21, 31, 41, 51, 61, 71, 81, 91, 101, 111};

        int bins = 11;

        CrossCorrelationDriftResults driftCorrection = CorrelationDriftEstimator.estimateDriftFromCoords(x, y, frame, bins, 5, 50, 50, true);

//    ResultsDriftCorrection.showDriftPlot(driftCorrection);
//    new ImagePlus("corr", driftCorrection.getCorrelationImages()).show();
//    Thread.sleep(200000);
        assertEquals(driftCorrection.getMinFrame(), VectorMath.min(frame), 0.00001);
        assertEquals(driftCorrection.getMaxFrame(), VectorMath.max(frame), 0.00001);

        ImageStack correlationStack = driftCorrection.getCorrelationImages();
        assertNotNull(correlationStack);
        assertEquals(bins - 1, correlationStack.getSize());

        assertArrayEquals(new double[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10}, driftCorrection.getDriftDataX(), 0.001);
        assertArrayEquals(new double[]{0, 1, 2, 3, 4, 5, 4, 3, 2, 1, 0}, driftCorrection.getDriftDataY(), 0.001);

        assertEquals("slope", 0.1, (driftCorrection.getInterpolatedDrift(90).x - driftCorrection.getInterpolatedDrift(20).x) / (90 - 20), 0.01);

        assertEquals(0.5, driftCorrection.getInterpolatedDrift(16).x, 0.01);
        assertEquals(0.5, driftCorrection.getInterpolatedDrift(16).y, 0.01);
        assertEquals(8.5, driftCorrection.getInterpolatedDrift(96).x, 0.01);
        assertEquals(1.5, driftCorrection.getInterpolatedDrift(96).y, 0.01);
    }
}
