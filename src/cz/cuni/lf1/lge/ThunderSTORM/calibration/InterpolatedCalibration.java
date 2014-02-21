package cz.cuni.lf1.lge.ThunderSTORM.calibration;

public class InterpolatedCalibration implements CylindricalLensCalibration {

    final String name = "Interpolated calibration";
    double angle;
    double[][] values;
    double sigma0 = 1.6;

    public InterpolatedCalibration(double angle, double[][] values) {
        this.angle = angle;
        this.values = values;
    }

    public InterpolatedCalibration() {
    }
    
    public void setSigmaAtZ0(double sigma0) {
        this.sigma0 = sigma0;
    }
    
    public double getSigmaAtZ0() {
        return sigma0;
    }

    @Override
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

    @Override
    public double getZ(double sigma1, double sigma2) {
        double ratio = sigma1 / sigma2;
        if(!sigmaRatioInRange(ratio)) {
            return Double.NaN;
        }

        int lowerIndex = 0;
        int higherIndex = values.length - 1;

        while(higherIndex - lowerIndex > 1) {
            int newIndex = ((higherIndex - lowerIndex) / 2) + lowerIndex;
            if(values[newIndex][0] < ratio) {
                lowerIndex = newIndex;
            } else {
                higherIndex = newIndex;
            }
        }
        //linear interploation
        double slope = (values[higherIndex][1] - values[lowerIndex][1]) / (values[higherIndex][0] - values[lowerIndex][0]);
        double interpolatedValue = (ratio - values[lowerIndex][0]) * slope + values[lowerIndex][1];

        return interpolatedValue;
    }

    @Override
    public double getSigma1(double z) {
        if(!zInRange(z)) {
            return Double.NaN;
        }

        int lowerIndex = 0;
        int higherIndex = values.length - 1;

        while(higherIndex - lowerIndex > 1) {
            int newIndex = ((higherIndex - lowerIndex) / 2) + lowerIndex;
            if(values[newIndex][1] < z) {
                lowerIndex = newIndex;
            } else {
                higherIndex = newIndex;
            }
        }
        //linear interploation
        double slope = (values[higherIndex][0] - values[lowerIndex][0]) / (values[higherIndex][1] - values[lowerIndex][1]);
        double ratio = (z - values[lowerIndex][1]) * slope + values[lowerIndex][0];

        // with the knowledge of sigma in z=0, it is possible to calculate sigma1 and sigma2 at any z
        double diff = sigma0*(ratio-1)/(ratio+1);
        return (sigma0 + diff);
    }

    @Override
    public double getSigma2(double z) {
        if(!zInRange(z)) {
            return Double.NaN;
        }

        int lowerIndex = 0;
        int higherIndex = values.length - 1;

        while(higherIndex - lowerIndex > 1) {
            int newIndex = ((higherIndex - lowerIndex) / 2) + lowerIndex;
            if(values[newIndex][1] < z) {
                lowerIndex = newIndex;
            } else {
                higherIndex = newIndex;
            }
        }
        //linear interploation
        double slope = (values[higherIndex][0] - values[lowerIndex][0]) / (values[higherIndex][1] - values[lowerIndex][1]);
        double ratio = (z - values[lowerIndex][1]) * slope + values[lowerIndex][0];
        
        // with the knowledge of sigma in z=0, it is possible to calculate sigma1 and sigma2 at any z
        double diff = sigma0*(ratio-1)/(ratio+1);
        return (sigma0 - diff);
    }

    public boolean sigmaRatioInRange(double ratio) {
        return ((ratio >= values[0][0]) && (ratio <= values[values.length - 1][0]));
    }
    
    public boolean zInRange(double z) {
        return ((z >= values[0][1]) && (z <= values[values.length - 1][1]));
    }
}
