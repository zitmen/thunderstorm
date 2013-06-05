package cz.cuni.lf1.lge.ThunderSTORM.estimators;

/**
 *
 */
public class CylindricalLensCalibration {
  double angle;
  double[][] values;

  public CylindricalLensCalibration(double angle, double[][] values) {
    this.angle = angle;
    this.values = values;
  }

  public CylindricalLensCalibration() {
  }

  public double getAngle() {
    return angle;
  }

  public void setAngle(double angle) {
    this.angle = angle;
  }

  public double[][] getValues() {
    return values;
  }

  public void setValues(double[][] values) {
    this.values = values;
  }
  
  
  public double getZ(double ratio){
    
    if(ratio < values[0][0] || ratio > values[values.length-1][0]){
      return Double.NaN;
    }
    
    int lowerIndex  = 0;
    int higherIndex = values.length-1;
    
    while(higherIndex-lowerIndex > 1){
      int newIndex = ((higherIndex-lowerIndex)/2) + lowerIndex;
      if(values[newIndex][0] < ratio){
        lowerIndex = newIndex;
      }else{
        higherIndex = newIndex;
      }
    }
    //linear interploation
    double slope = (values[higherIndex][1]-values[lowerIndex][1])/(values[higherIndex][0]-values[lowerIndex][0]);
    double interpolatedValue = (ratio-values[lowerIndex][0])*slope + values[lowerIndex][1];
    
    return interpolatedValue;
  }
}
