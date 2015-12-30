package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.util.IMatchable;
import ij.IJ;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;

public final class Molecule implements Comparable<Molecule>, IMatchable<Molecule> {

    public MoleculeDescriptor descriptor;
    private List<Molecule> detections;    // for merging of re-appearing molecules (post-processing)
    private List<Molecule> neighbors;    // for molecule matching (performance evaluation)
    public double[] values;

    public Molecule(MoleculeDescriptor descriptor, double [] values) {
        this.descriptor = descriptor;
        this.values = new double[values.length];
        System.arraycopy(values, 0, this.values, 0, values.length);
    }
    
    public Molecule(Params params) {
        assert(params.hasParam(Params.X) && params.hasParam(Params.Y));
        
        this.descriptor = new MoleculeDescriptor(params);
        this.values = new double[params.values.length];
        System.arraycopy(params.values, 0, values, 0, params.values.length);
    }
    
    public Molecule(Molecule mol) {
        this.descriptor = mol.descriptor;
        this.values = mol.values;
        this.detections = mol.detections;
    }
    
    public void addNeighbors(List<Molecule> nbrs, boolean threeD, double dist2, Units distUnits) {
        if(neighbors == null) {
            neighbors = new ArrayList<Molecule>();
        }
        if (threeD) {
            for (Molecule nbr : nbrs) {
                if (getDist2(nbr, distUnits) <= dist2) {
                    neighbors.add(nbr);
                }
            }
        } else {
            for (Molecule nbr : nbrs) {
                if (getDist2Lateral(nbr, distUnits) <= dist2) {
                    neighbors.add(nbr);
                }
            }
        }
    }
    
    public double getDist2(Molecule mol, Units distUnits) {
        return sqr(getX(distUnits) - mol.getX(distUnits)) + sqr(getY(distUnits) - mol.getY(distUnits)) + sqr(getZ(distUnits) - mol.getZ(distUnits));
    }

    public double getDist2Lateral(Molecule mol, Units distUnits) {
        return sqr(getX(distUnits) - mol.getX(distUnits)) + sqr(getY(distUnits) - mol.getY(distUnits));
    }
    
    public double getDist(Molecule mol, Units distUnits) {
        return sqrt(getDist2(mol, distUnits));
    }
    public double getDistLateral(Molecule mol, Units distUnits) {
        return sqrt(getDist2Lateral(mol, distUnits));
    }
    public double getDistAxial(Molecule mol, Units distUnits) { return abs(getZ(distUnits) - mol.getZ(distUnits)); }

    @Override
    public double getDist2(IMatchable mol) {
        return sqr(getX() - mol.getX()) + sqr(getY() - mol.getY()) + sqr(getZ() - mol.getZ());
    }

    @Override
    public List<Molecule> getNeighbors() {
        return neighbors;
    }

    public void clearNeighbors() {
        if (neighbors != null) neighbors.clear();
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
            int pos = values.length;
            try {
                descriptor.insertParamAt(column, name, pos, units);
            } catch(Exception ex) {
                assert false : "This was supposed to never happen due to the `hasParam` check!";
            }
            values = realloc(values, pos+1);    // realloc (ineffective, but it's not supposed to be used often anyway)
            values[pos] = value; // values can be added at the last position since we implement extra indexing for this
        }
    }

    private static double[] realloc(double[] source, int newLength) {
        double[] retval = new double[newLength];
        System.arraycopy(source, 0, retval, 0, source.length);
        return retval;
    }
    
    public boolean hasParam(String name) {
        return descriptor.hasParam(name);
    }

    public double getParamAt(int i) {
        return values[i];
    }
    
    public double getParamAtColumn(int c) {
        return values[descriptor.indices.get(c)];
    }
    
    public void setParamAt(int i, double value) {
        if(i >= values.length) {
            values = realloc(values, i + 1);
        }
        values[i] = value;
    }
    
    public double getParam(String param) {
        try {
            return values[descriptor.getParamIndex(param)];
        } catch (Exception ex)  {
            return 0.0;
        }
    }
    
    public double getParam(String param, Units unit) {
        return getParamUnits(param).convertTo(unit, getParam(param));
    }
    
    public void setParam(String param, double value) {
        setParamAt(descriptor.getParamIndex(param), value);
    }
    
    public void setParam(String param, Units unit, double value) {
        int i = descriptor.getParamIndex(param);
        if(i >= values.length) {
            values = realloc(values, i + 1);
            values[i] = value;
            try {
                descriptor.addParam(param, i, unit);
            } catch(Exception ex) {
                //
            }
        } else {
            values[i] = value;
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
                sb.append(values[descriptor.indices.get(i)]);
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
                    sb.append(detection.values[detection.descriptor.indices.get(i)]);
                }
                sb.append("]");
            }
            sb.append("]");
            return sb.toString();
        }
    }

    /**
     * Nothing is copied, the clone references the identical data as the source!
     * For a clone with duplicate data but different references, call `clone(MoleculeDescriptor)`!
     * Note that `neighbors` are ignored by the cloning operation!
     */
    @Override
    public Molecule clone() {
        return new Molecule(this);
    }
    
    /**
     * Clone the molecule.
     * 
     * Caller has to duplicate the descriptor if it is required!
     * Note that `neighbors` are ignored by the cloning operation!
     */
    public Molecule clone(MoleculeDescriptor descriptor) {
        double[] vals = new double[values.length];
        System.arraycopy(values, 0, vals, 0, values.length);
        Molecule mol = new Molecule(descriptor, vals);
        mol.detections = detections != null ? new Vector<Molecule>(detections) : null;
        return mol;
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
            IJ.log("\\Update:Cannot update the parameters: " + ex.getMessage());
        }
    }

    public double dist2xy(Molecule mol, Units units) {
        return (sqr(mol.getX(units) - getX(units)) + sqr(mol.getY(units) - getY(units)));
    }

    public double dist2z(Molecule mol, Units units) {
        return sqr(mol.getZ(units) - getZ(units));
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

    public List<Molecule> getDetections() {
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

    public enum DetectionStatus {
        UNSPECIFIED, TRUE_POSITIVE, FALSE_POSITIVE, FALSE_NEGATIVE
    }

}
