package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;

public class SubImage {

    public double[] xgrid;
    public double[] ygrid;
    public double[] values;
    public double detectorX;
    public double detectorY;
    public MoleculeDescriptor.Units units;
    public int size_y;
    public int size_x;

    public SubImage() {
    }

    public SubImage(int sizeX, int sizeY, double[] xgrid, double[] ygrid, double[] values, double detectorX, double detectorY) {
        this(sizeX, sizeY, xgrid, ygrid, values, detectorX, detectorY, MoleculeDescriptor.Units.DIGITAL);
    }

    public SubImage(int sizeX, int sizeY, double[] xgrid, double[] ygrid, double[] values, double detectorX, double detectorY, MoleculeDescriptor.Units units) {
        this.size_x = sizeX;
        this.size_y = sizeY;
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
    public double[] subtract(double[] values) {
        assert (this.values.length == values.length);
        for (int i = 0; i < values.length; i++) {
            values[i] = this.values[i] - values[i];
        }
        return values;
    }

    public void convertTo(MoleculeDescriptor.Units targetUnits) {
        if (units.equals(targetUnits)) {
            return;
        }
        for (int i = 0; i < values.length; i++) {
            values[i] = units.convertTo(targetUnits, values[i]);
        }
        units = targetUnits;
    }
}
