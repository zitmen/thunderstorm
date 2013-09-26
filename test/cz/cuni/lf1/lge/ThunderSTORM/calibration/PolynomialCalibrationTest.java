package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import org.junit.Test;
import static org.junit.Assert.*;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class PolynomialCalibrationTest {
  
  @Test
  public void testSerialization() {
    double angle = 45;
    double [] sigma1 = {-10,0.0003,1.986};
    double [] sigma2 = {2.2354,0.0003,2.015};
    PolynomialCalibration calibration = new PolynomialCalibration(angle,new QuadraticFunction(sigma1),new QuadraticFunction(sigma2));
    Yaml yaml = new Yaml();
    String output = yaml.dump(calibration);
    System.out.println(output);
    
    Object loaded = yaml.load(output);
    assertTrue(loaded instanceof PolynomialCalibration);
    PolynomialCalibration loadedCalibration = (PolynomialCalibration) loaded;
    assertEquals(calibration.getAngle(), loadedCalibration.getAngle(), 0.0001);
    assertEquals(calibration.getA1(), loadedCalibration.getA1(), 0.0001);
  }
  
  @Test
  public void testGetZ() {
    PolynomialCalibration calibration = new PolynomialCalibration();
    calibration.a1 = 0.0006;
    calibration.c1 = 104.583;
    calibration.b1 = 1.7453;
    
    calibration.a2 = 0.0006;
    calibration.c2 = 119.75;
    calibration.b2 = 1.47696;
    
    double z = calibration.getZ(3.5, 2.5);
    System.out.println("z: " + z);
    assertEquals(160, z, 1);
  }
}