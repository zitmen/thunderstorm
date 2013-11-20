package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import ij.IJ;
import java.util.List;
import java.util.Vector;
import org.apache.commons.collections.primitives.ArrayDoubleList;
import org.apache.commons.collections.primitives.DoubleList;

public final class Molecule implements Comparable<Molecule> {

    public MoleculeDescriptor descriptor;
    private Vector<Molecule> detections;
    public DoubleList values;

    public Molecule(MoleculeDescriptor descriptor, DoubleList values) {
        this.descriptor = descriptor;
        this.values = values;
        //
    }
    
    public Molecule(MoleculeDescriptor descriptor, double [] values) {
        this.descriptor = descriptor;
        this.values = new ArrayDoubleList(values.length);
        for(int i = 0; i < values.length; i++) {
            this.values.add(values[i]);
        }
        //
    }
    
    public Molecule(Params params) {
        assert(params.hasParam(Params.X) && params.hasParam(Params.Y));
        
        this.descriptor = new MoleculeDescriptor(params);
        this.values = new ArrayDoubleList(params.values.length);
        for(int i = 0; i < params.values.length; i++) {
            values.add(params.values[i]);
        }
        //
    }
    
    public Molecule(Molecule mol) {
        this.descriptor = mol.descriptor;
        this.values = mol.values;
        this.detections = mol.detections;
    }
    
    /**
     * Add a parameter to the last column. Note: indexing to the array of `values`
     * is managed internally and the new value will be also to the last position
     * of the array, independently of the column.
     */
    public void addParam(String name, Units units, double value) {
        insertParamAt(descriptor.getParamsCount(), name, units, value);
    }
    
    /**
     * Add a parameter to a specified `column` and shift all the columns with higher index by one.
     * Note: indexing to the array of `values` is managed internally and the new value
     * will be also to the last position of the array, independently of the column.
     */
    public void insertParamAt(int column, String name, Units units, double value) {
        if(hasParam(name)) {   // is the param already present in the table?
            setParam(name, value);  // then just set the value
        } else {                // if it's not, then add new column and set the value
            try {
                descriptor.insertParamAt(column, name, values.size(), units);
            } catch(Exception ex) {
                assert(false) : "This was supposed to never happen due to the `hasParam` check!";
            }
            values.add(value);  // values can be added at the last position since we implement extra indexing for this
        }
    }
    
    public boolean hasParam(String name) {
        return descriptor.hasParam(name);
    }

    public double getParamAt(int i) {
        return values.get(i);
    }
    
    public double getParamAtColumn(int c) {
        return values.get(descriptor.indices.get(c));
    }
    
    public void setParamAt(int i, double value) {
        if(i >= values.size()) {
            values.add(i, value);
        } else {
            values.set(i, value);
        }
    }
    
    public double getParam(String param) {
        return values.get(descriptor.getParamIndex(param));
    }
    
    public double getParam(String param, Units unit) {
        return getParamUnits(param).convertTo(unit, getParam(param));
    }
    
    public void setParam(String param, double value) {
        setParamAt(descriptor.getParamIndex(param), value);
    }
    
    public void setParam(String param, Units unit, double value) {
        int i = descriptor.getParamIndex(param);
        if(i >= values.size()) {
            values.add(i, value);
            try {
                descriptor.addParam(param, i, unit);
            } catch(Exception ex) {
                //
            }
        } else {
            values.set(i, value);
            descriptor.setColumnUnits(unit, descriptor.getParamColumn(param));
        }
    }

    public String getParamNameAtColumn(int c) {
        return descriptor.getParamNameAt(c);
    }
    
    public double getX() {
        return getParam(Params.LABEL_X);
    }
    
    public double getX(Units unit) {
        return descriptor.units.get(descriptor.getParamColumn(Params.LABEL_X)).convertTo(unit, getX());
    }
    
    public void setX(double value) {
        setParam(Params.LABEL_X, value);
    }
    
    public double getY() {
        return getParam(Params.LABEL_Y);
    }
    
    public double getY(Units unit) {
        return descriptor.units.get(descriptor.getParamColumn(Params.LABEL_Y)).convertTo(unit, getY());
    }
    
    public void setY(double value) {
        setParam(Params.LABEL_Y, value);
    }
    
    public double getZ() {
        if(hasParam(Params.LABEL_Z)) {
            return getParam(Params.LABEL_Z);
        } else {
            return 0.0;
        }
    }
    
    public double getZ(Units unit) {
        if(hasParam(Params.LABEL_Z)) {
            return descriptor.units.get(descriptor.getParamColumn(Params.LABEL_Z)).convertTo(unit, getParam(Params.LABEL_Z));
        } else {
            return 0.0;
        }
    }
    
    public void setZ(double value) {
        if(hasParam(Params.LABEL_Z)) {
            setParam(Params.LABEL_Z, value);
        } else {
            addParam(Params.LABEL_Z, Units.getDefaultUnit(Params.LABEL_Z), value);
        }
    }
    
    public Units getParamUnits(String name) {
        return descriptor.units.get(descriptor.getParamColumn(name));
    }
    
    public Units getParamUnits(int column) {
        return descriptor.units.get(column);
    }

    @Override
    public String toString() {
        if(isSingleMolecule()) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for(int i = 0, im = descriptor.labels.size(); i < im; i++) {
                if(i != 0) {
                    sb.append(", ");
                }
                sb.append(descriptor.labels.get(i));
                sb.append("=");
                sb.append(values.get(descriptor.indices.get(i)));
            }
            sb.append("]");
            return sb.toString();
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            for(Molecule detection : getDetections()) {
                if(detection != this) {
                    sb.append(", ");
                }
                sb.append("[");
                for(int i = 0, im = detection.descriptor.labels.size(); i < im; i++) {
                    if(i != 0) {
                        sb.append(", ");
                    }
                    sb.append(detection.descriptor.labels.get(i));
                    sb.append("=");
                    sb.append(detection.values.get(detection.descriptor.indices.get(i)));
                }
                sb.append("]");
            }
            sb.append("]");
            return sb.toString();
        }
    }
    
    @Override
    public Molecule clone() {
        assert(true) : "Use clone(MoleculeDescriptor) instead!!!";
        throw new UnsupportedOperationException("Use `Molecule.clone(MoleculeDescriptor)` instead!!!");
    }
    
    /**
     * Clone the molecule.
     * 
     * Caller has to duplicate the descriptor if it is required!
     */
    public Molecule clone(MoleculeDescriptor descriptor) {
        Molecule mol = new Molecule(descriptor, new ArrayDoubleList(values));
        mol.detections = detections != null ? new Vector<Molecule>(detections) : null;
        return mol;
    }

    public static double[] extractParamToArray(List<Molecule> fits, int param) {
        double[] array = new double[fits.size()];
        for (int i = 0; i < fits.size(); i++) {
            array[i] = fits.get(i).getParamAt(param);
        }
        return array;
    }
    
    public static double[] extractParamToArray(List<Molecule> fits, String param) {
        double[] array = new double[fits.size()];
        for (int i = 0; i < fits.size(); i++) {
            array[i] = fits.get(i).getParam(param);
        }
        return array;
    }
    
    // ======================== [ MERGING ] ===================================
    
    public void updateParameters() {
        try {
            for(int i = 0, im = descriptor.getParamsCount(); i < im; i++) {
                MoleculeDescriptor.MergingOperations.merge(this, detections, descriptor.getParamNameAt(i));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
            IJ.showMessage("Error!", ex.toString());
        }
    }

    public double dist2xy(Molecule mol) {
        return (sqr(mol.getX() - getX()) + sqr(mol.getY() - getY()));
    }
    
    public double dist2xy(Molecule mol, Units units) {
        return (sqr(mol.getX(units) - getX(units)) + sqr(mol.getY(units) - getY(units)));
    }
    
    public double dist2z(Molecule mol) {
        return sqr(mol.getZ() - getZ());
    }
    
    public double dist2z(Molecule mol, Units units) {
        return sqr(mol.getZ(units) - getZ(units));
    }
    
    public double dist2xyz(Molecule mol) {
        return (sqr(mol.getX() - getX()) + sqr(mol.getY() - getY()));
    }
    
    public double dist2xyz(Molecule mol, Units units) {
        return (sqr(mol.getX(units) - getX(units)) + sqr(mol.getY(units) - getY(units)) + sqr(mol.getZ(units) - getZ(units)));
    }

    public void addDetection(Molecule mol) {
        if(detections == null){
            detections = new Vector<Molecule>();
        }
        if(mol.isSingleMolecule()) {
            detections.add(mol);
        } else {    // if it is not empty, it already contains, at least, itself
            for(Molecule m : mol.detections) {
                detections.add(m);
            }
        }
    }

    public Vector<Molecule> getDetections() {
        return detections;
    }

    public void setDetections(Vector<Molecule> detections) {
        this.detections = detections;
    }
    
    public int getDetectionsCount(){
        return detections == null  ? 1: detections.size();
    }

    public boolean isSingleMolecule() {
        return (detections == null || detections.size() <= 1);
    }

    @Override
    public int compareTo(Molecule mol) {
        // first by frame, then by id, but it should never happen,
        // since two molecules cannot be merged if they are in the same frame
        double frame = getParam(MoleculeDescriptor.LABEL_FRAME), molFrame = mol.getParam(MoleculeDescriptor.LABEL_FRAME);
        if(frame == molFrame) {
            return (int)(getParam(MoleculeDescriptor.LABEL_ID) - mol.getParam(MoleculeDescriptor.LABEL_ID));
        } else {
            return (int)(frame - molFrame);
        }
    }
    
    // ================================================================
    //       Ground-truth testing
    // ================================================================
    
    private DetectionStatus status = DetectionStatus.UNSPECIFIED;
    
    public void setStatus(DetectionStatus status) {
        this.status = status;
    }
    
    public DetectionStatus getStatus() {
        return status;
    }
    
    public static enum DetectionStatus {
        UNSPECIFIED, TRUE_POSITIVE, FALSE_POSITIVE, FALSE_NEGATIVE;
    }

}
