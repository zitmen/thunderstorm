package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.max;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.min;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sum;

public interface OneLocationFitter {

    public static class SubImage {

        public int[] xgrid;
        public int[] ygrid;
        public double[] values;
        public double detectorX;
        public double detectorY;
        public int size;

        public SubImage() {
        }

        public SubImage(int size, int[] xgrid, int[] ygrid, double[] values, double detectorX, double detectorY) {
            this.size = size;
            this.xgrid = xgrid;
            this.ygrid = ygrid;
            this.values = values;
            this.detectorX = detectorX;
            this.detectorY = detectorY;
        }

        public double getMax() {
            return max(values);
        }

        public double getMin() {
            return min(values);
        }
        
        public double getSum() {
            return sum(values);
        }
    }

    public PSFInstance fit(SubImage img);
}
