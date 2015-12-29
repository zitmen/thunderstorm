package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import org.junit.Test;
import static org.junit.Assert.*;
import org.yaml.snakeyaml.Yaml;

public class PolynomialCalibrationTest {

    @Test
    public void testSerialization() {
        double angle = 45;
        double[] sigma1 = {1, -10, 0.0003, 1.986, 1};
        double[] sigma2 = {1, 2.2354, 0.0003, 2.015, 1};
        PolynomialCalibration calibration = new PolynomialCalibration(angle, null, new DefocusFunctionPoly(sigma1, false), new DefocusFunctionPoly(sigma2, false));
        Yaml yaml = new Yaml();
        String output = yaml.dump(calibration);
        System.out.println(output);

        Object loaded = yaml.load(output);
        assertTrue(loaded instanceof PolynomialCalibration);
        PolynomialCalibration loadedCalibration = (PolynomialCalibration) loaded;
        assertEquals(calibration.getAngle(), loadedCalibration.getAngle(), 0.0001);
        assertEquals(calibration.getA1(), loadedCalibration.getA1(), 0.0001);
        assertEquals(sigma1[4], calibration.getD1(), 0.0001);
    }
}
