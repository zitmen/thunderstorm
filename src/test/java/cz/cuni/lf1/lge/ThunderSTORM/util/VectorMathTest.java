package cz.cuni.lf1.lge.ThunderSTORM.util;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class VectorMathTest {

    @Test
    public void testMovingAverage() {
        System.out.println("VectorMath::movingAverage");
        double[] values = new double[10];
        double[] expected;
        double[] result;

        Arrays.fill(values, 1);
        expected = values;
        result = VectorMath.movingAverage(values, 5);
        assertArrayEquals(expected, result, 1e-12);

        Arrays.fill(values, 5);
        expected = values;
        result = VectorMath.movingAverage(values, 1);
        assertArrayEquals(expected, result, 1e-12);

        values = new double[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };
        expected = new double[11]; Arrays.fill(expected, 5.0);
        result = VectorMath.movingAverage(values, 23);
        assertArrayEquals(expected, result, 1e-12);

        values = new double[] { 3, 5, 1, 0, 2 };
        expected = new double[] { 4, 3, 2, 1, 1 };
        result = VectorMath.movingAverage(values, 3);
        assertArrayEquals(expected, result, 1e-12);

        long thrown = 0;
        try {
            VectorMath.movingAverage(values, 2);
        } catch(Exception ex) {
            thrown++;
        }
        try {
            VectorMath.movingAverage(values, 0);
        } catch(Exception ex) {
            thrown++;
        }
        try {
            VectorMath.movingAverage(values, -3);
        } catch(Exception ex) {
            thrown++;
        }
        assertEquals(3, thrown);
    }
}
