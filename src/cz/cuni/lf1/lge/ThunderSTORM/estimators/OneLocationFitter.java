package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;

public interface OneLocationFitter {

    public static class SubImage {

        public int[] xgrid;
        public int[] ygrid;
        public double[] values;
        public double detectorX;
        public double detectorY;
        public int size;
        public MoleculeDescriptor.Units units;

        public SubImage() {
        }

        public SubImage(int size, int[] xgrid, int[] ygrid, double[] values, double detectorX, double detectorY) {
            this(size, xgrid, ygrid, values, detectorX, detectorY, Units.DIGITAL);
        }
        
        public SubImage(int size, int[] xgrid, int[] ygrid, double[] values, double detectorX, double detectorY, MoleculeDescriptor.Units units) {
            this.size = size;
            this.xgrid = xgrid;
            this.ygrid = ygrid;
            this.values = values;
            this.detectorX = detectorX;
            this.detectorY = detectorY;
            this.units = units;
        }

        public double getMax() {
            return VectorMath.max(values);
        }

        public double getMin() {
            return VectorMath.min(values);
        }
        
        public double getSum() {
            return VectorMath.sum(values);
        }
        
        // note: the function changes the input array!
        public double [] subtract(double [] values) {
            assert(this.values.length == values.length);
            for(int i = 0; i < values.length; i++) {
                values[i] = this.values[i] - values[i];
            }
            return values;
        }
        
        public void convertTo(Units targetUnits){
            if(units.equals(targetUnits)){
                return;
            }
            for(int i = 0; i < values.length; i++) {
                values[i] = units.convertTo(targetUnits, values[i]);
            }
            units = targetUnits;
        }
    }

    public Molecule fit(SubImage img);
}
