package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.genIntSequence;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.min;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.max;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sum;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.mean;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqrt;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.PI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Vector;

/**
 * This class provides all the semantic information about instances of Molecule class,
 * independently of PSFModel, Estimator or any other additional source of information.
 */
public class MoleculeDescriptor implements Cloneable {
    private HashMap<String, IndexAndColumn> paramNames; // duplicate of arrays `names` and `indices`, but it is much faster in some cases

    public void setColumnName(int column, String newName) {
        String oldName = names.get(column);
        names.setElementAt(newName, column);
        paramNames.put(newName, paramNames.get(oldName));
        paramNames.remove(oldName);
        buildLabels();
    }

    class IndexAndColumn {
        public int index;
        public int column;
        
        public IndexAndColumn(int index, int column) {
            this.index = index;
            this.column = column;
        }
    }
    
    public Vector<String> names; // names of the parameters in Molecules.values - the order correspond to the `indices`
    public Vector<Integer> indices;  // indices into Molecule.values array
    public Vector<Units> units;  // units of the parameters stored in `names`
    public Vector<String> labels;// labels are basically of form "name [unit]"
    
    public MoleculeDescriptor(MoleculeDescriptor desc) {
        this.names = desc.names;
        this.indices = desc.indices;
        this.units = desc.units;
        this.labels = desc.labels;
    }
    
    public MoleculeDescriptor(Params params) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(params.names));
        setIndices(params.indices);
        fillUnits();
        buildLabels();
        fillNamesSet();
    }
    
    // note: in all the constructors, if the order of indices is simply {0,1,2...,names.length-1},
    //       then you can call for example MoleculeDescriptor(names, null) and the constructor fills
    //       the `indices` automaticaly
    public MoleculeDescriptor(String [] names) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(null);
        fillUnits();
        buildLabels();
        fillNamesSet();
    }
    
    public MoleculeDescriptor(String [] names, int [] indices) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(indices);
        fillUnits();
        buildLabels();
        fillNamesSet();
    }
    
    public MoleculeDescriptor(String [] names, Units [] units) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(null);
        this.units = new Vector<Units>();
        this.units.addAll(Arrays.asList(units));
        buildLabels();
        fillNamesSet();
    }
    
    public MoleculeDescriptor(String [] names, int [] indices, Units [] units) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(indices);
        this.units = new Vector<Units>();
        this.units.addAll(Arrays.asList(units));
        buildLabels();
        fillNamesSet();
    }
    
    public MoleculeDescriptor(String [] names, int [] indices, Units [] units, String [] labels) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(indices);
        this.units = new Vector<Units>();
        this.units.addAll(Arrays.asList(units));
        this.labels = new Vector<String>();
        this.labels.addAll(Arrays.asList(labels));
        fillNamesSet();
    }
    
    private void setIndices(int [] indices) {
        int [] ind = indices;
        if(indices == null) {
            ind = genIntSequence(0, names.size());
        }
        this.indices = new Vector<Integer>();
        for(int i = 0; i < ind.length; i++) {
            this.indices.add(ind[i]);
        }
    }
    
    private void fillUnits() {
        assert(names != null);
        
        this.units = new Vector<Units>();
        for(int i = 0, im = names.size(); i < im; i++) {
            this.units.add(Units.getDefaultUnit(names.elementAt(i)));
        }
    }
    
    private void buildLabels() {
        assert(names != null);
        assert(units != null);
        
        labels = new Vector<String>();
        for(int i = 0, im = names.size(); i < im; i++) {
            labels.add(getLabel(i, true));
        }
    }
    
    private void fillNamesSet() {
        assert(names != null);
        
        paramNames = new HashMap<String, IndexAndColumn>();
        for(int i = 0, im = names.size(); i < im; i++) {
            paramNames.put(names.elementAt(i), new IndexAndColumn(indices.elementAt(i), i));
        }
    }
    
    public String getLabel(int i, boolean rebuild) {
        assert(names != null);
        assert(units != null);
        assert(labels != null);
        assert(i < units.size());
        
        if(rebuild || (labels.size() <= i) || (labels.elementAt(i) == null)) {
            if(units.elementAt(i) == Units.UNITLESS) {
                return names.elementAt(i);
            } else {
                return names.elementAt(i) + " [" + units.elementAt(i) + "]";
            }
        } else {
            return labels.elementAt(i);
        }
    }
    
    public void removeParam(String name) {
        if(hasParam(name)) {
            int col = getParamColumn(name);
            indices.removeElementAt(col);
            names.removeElementAt(col);
            units.removeElementAt(col);
            labels.removeElementAt(col);
        }
    }
    
    public void addParam(String name, int index, Units unit) throws Exception {
        insertParamAt(getParamsCount(), name, index, unit);
    }
    
    public void insertParamAt(int column, String name, int index, Units unit) throws Exception {
        if(paramNames.containsKey(name)) {
            throw new Exception("Parameter `" + name + "` already exists!");
        }
        indices.insertElementAt(index, column);
        names.insertElementAt(name, column);
        fillNamesSet();
        units.insertElementAt(((units == null) ? Units.getDefaultUnit(name) : unit), column);
        labels.insertElementAt(getLabel(column, true), column);
    }
    
    public boolean hasParam(String param) {
        assert((param != null) && (paramNames != null));
        
        return paramNames.containsKey(param);
    }

    public String getParamNameAt(int i) {
        return names.elementAt(i);
    }
    
    public int getParamsCount() {
        return names.size();
    }

    // While `index` refers to `Molecule.values`, ...
    public int getParamIndex(String param) {
        return paramNames.get(param).index;
    }
    
    // the `column` refers to everything else in `MoleculeDescriptor`.
    public int getParamColumn(String param) {
        return paramNames.get(param).column;
    }
    
    public void setColumnUnits(Units new_units, int columnIndex) {
        units.setElementAt(new_units, columnIndex);
        setColumnLabel(columnIndex);
    }
    
    private void setColumnLabel(int columnIndex) {
        labels.setElementAt(getLabel(columnIndex, true), columnIndex);
    }

    public void validateMolecule(Molecule mol) {
        int max_i = 0;
        // What is the maximum index of any of the parameters?
        for(int i = 0, im = indices.size(); i < im; i++) {
            if(indices.elementAt(i) > max_i) {
                max_i = indices.elementAt(i);
            }
        }
        // Molecule can't have less values then it has parameters!
        for(int i = max_i+1 - mol.values.size(); i > 0; i--) {
            mol.values.add(0.0);    // fill with zeros
        }
    }
    
    @Override
    public MoleculeDescriptor clone() {
        String [] clonedNames = new String[names.size()];
        int [] clonedIndices = new int[indices.size()];
        Units [] clonedUnits = new Units[units.size()];
        for(int i = 0, im = names.size(); i < im; i++) {
            if(names.elementAt(i) != null) {
                clonedNames[i] = new String(names.elementAt(i).toCharArray());  // don't know how else to create a new instance of the string
            } else {
                clonedNames[i] = null;
            }
            if(indices.elementAt(i) != null) {
                clonedIndices[i] = indices.elementAt(i).intValue();
            } else {
                clonedIndices[i] = 0;
            }
            if(units.elementAt(i) != null) {
                clonedUnits[i] = units.elementAt(i);
            } else {
                clonedUnits[i] = Units.UNITLESS;
            }
        }
        return new MoleculeDescriptor(clonedNames, clonedIndices, clonedUnits);
    }
    
    public static Pair<String,Units> parseColumnLabel(String label) {
        assert(label != null);
        
        int start = label.lastIndexOf('['), end = label.lastIndexOf(']');
        if((start < 0) || (end < 0) || (start > end)) {
            return new Pair<String,Units>(label, Units.UNITLESS);
        } else {
            return new Pair<String,Units>(label.substring(0, start).trim(), Units.fromString(label.substring(start+1, end).trim()));
        }
    }
    
    // ===============================================================
    
    public static class Fitting {
        public static final String LABEL_THOMPSON = "uncertainty";
        
        // return uncertainty in nanometers
        public static double ccdThompson(Molecule molecule) throws Exception {
            double psfSigma2, psfEnergy, bkgStd, pixelSize;
            pixelSize = CameraSetupPlugIn.pixelSize;
            if(molecule.hasParam(PSFModel.Params.LABEL_SIGMA)) {    // symmetric
                psfSigma2 = molecule.getParamUnits(PSFModel.Params.LABEL_SIGMA).convertTo(Units.NANOMETER, molecule.getParam(PSFModel.Params.LABEL_SIGMA)) *
                            molecule.getParamUnits(PSFModel.Params.LABEL_SIGMA).convertTo(Units.NANOMETER, molecule.getParam(PSFModel.Params.LABEL_SIGMA));
            } else if(molecule.hasParam(PSFModel.Params.LABEL_SIGMA1) && molecule.hasParam(PSFModel.Params.LABEL_SIGMA2)) { // eliptic
                psfSigma2 = molecule.getParamUnits(PSFModel.Params.LABEL_SIGMA1).convertTo(Units.NANOMETER, molecule.getParam(PSFModel.Params.LABEL_SIGMA1)) *
                            molecule.getParamUnits(PSFModel.Params.LABEL_SIGMA2).convertTo(Units.NANOMETER, molecule.getParam(PSFModel.Params.LABEL_SIGMA2));
            } else {
                throw new Exception("Cannot calculate Thompson equation!");
            }
            psfEnergy = molecule.getParamUnits(PSFModel.Params.LABEL_INTENSITY).convertTo(Units.PHOTON, molecule.getParam(PSFModel.Params.LABEL_INTENSITY));
            bkgStd = molecule.getParamUnits(PSFModel.Params.LABEL_BACKGROUND).convertTo(Units.PHOTON, molecule.getParam(PSFModel.Params.LABEL_BACKGROUND));
            //
            double xyVar = ((psfSigma2 + pixelSize*pixelSize/12) / psfEnergy) +
                    ((8*PI*psfSigma2*psfSigma2*bkgStd) / (pixelSize*pixelSize*psfEnergy*psfEnergy));
            return sqrt(xyVar);
        }
        
        // return uncertainty in nanometers
        public static double emccdThompson(Molecule molecule) throws Exception {
            double psfSigma2, psfEnergy, bkgStd, pixelSize;
            pixelSize = CameraSetupPlugIn.pixelSize;
            if(molecule.hasParam(PSFModel.Params.LABEL_SIGMA)) {    // symmetric
                psfSigma2 = molecule.getParamUnits(PSFModel.Params.LABEL_SIGMA).convertTo(Units.NANOMETER, molecule.getParam(PSFModel.Params.LABEL_SIGMA)) *
                            molecule.getParamUnits(PSFModel.Params.LABEL_SIGMA).convertTo(Units.NANOMETER, molecule.getParam(PSFModel.Params.LABEL_SIGMA));
            } else if(molecule.hasParam(PSFModel.Params.LABEL_SIGMA1) && molecule.hasParam(PSFModel.Params.LABEL_SIGMA2)) { // eliptic
                psfSigma2 = molecule.getParamUnits(PSFModel.Params.LABEL_SIGMA).convertTo(Units.NANOMETER, molecule.getParam(PSFModel.Params.LABEL_SIGMA1)) *
                            molecule.getParamUnits(PSFModel.Params.LABEL_SIGMA).convertTo(Units.NANOMETER, molecule.getParam(PSFModel.Params.LABEL_SIGMA2));
            } else {
                throw new Exception("Cannot calculate Thompson equation!");
            }
            psfEnergy = molecule.getParamUnits(PSFModel.Params.LABEL_INTENSITY).convertTo(Units.PHOTON, molecule.getParam(PSFModel.Params.LABEL_INTENSITY));
            bkgStd = molecule.getParamUnits(PSFModel.Params.LABEL_BACKGROUND).convertTo(Units.PHOTON, molecule.getParam(PSFModel.Params.LABEL_BACKGROUND));
            //
            double xyVar = ((2*psfSigma2 + pixelSize*pixelSize/12) / psfEnergy) +
                    ((8*PI*psfSigma2*psfSigma2*bkgStd) / (pixelSize*pixelSize*psfEnergy*psfEnergy));
            //
            return sqrt(xyVar);
        }
    }
    
    public static final String LABEL_ID = "id";
    public static final String LABEL_FRAME = "frame";
    public static final String LABEL_DETECTIONS = "detections";
    public static final String LABEL_GROUND_TRUTH_ID = "gt-id";
    public static final String LABEL_DISTANCE_TO_GROUND_TRUTH = "gt-distance";
    
    public static enum Units {
        MICROMETER("um"),
        NANOMETER("nm"),
        PIXEL("px"),
        MICROMETER_SQUARED("um^2"),
        NANOMETER_SQUARED("nm^2"),
        PIXEL_SQUARED("px^2"),
        DIGITAL("ADU"),
        PHOTON("photon"),
        DEGREE("deg"),
        RADIAN("rad"),
        UNITLESS("");

        private String label;
        
        private Units(String label) {
            this.label = label;
        }
        
        public String getLabel() {
            return label;
        }
        
        public double convertTo(Units target, double value) {
            switch(this) {
                case MICROMETER:
                    switch(target) {
                        case PIXEL: return CameraSetupPlugIn.nanometersToPixels(1000.0 * value);
                        case NANOMETER: return 1000.0 * value;
                        case MICROMETER: return value;
                        case UNITLESS: return value;
                    }
                    break;
                
                case NANOMETER:
                    switch(target) {
                        case PIXEL: return CameraSetupPlugIn.nanometersToPixels(value);
                        case NANOMETER: return value;
                        case MICROMETER: return value / 1000.0;
                        case UNITLESS: return value;
                    }
                    break;
                    
                case PIXEL:
                    switch(target) {
                        case PIXEL: return value;
                        case MICROMETER: return CameraSetupPlugIn.pixelsToNanometers(value) / 1000.0;
                        case NANOMETER: return CameraSetupPlugIn.pixelsToNanometers(value);
                        case UNITLESS: return value;
                    }
                    break;
                    
                case MICROMETER_SQUARED:
                    switch(target) {
                        case PIXEL_SQUARED: return CameraSetupPlugIn.nanometers2ToPixels2(value * 1000.0 * 1000.0);
                        case MICROMETER_SQUARED: return value;
                        case NANOMETER_SQUARED: return value * 1000.0 * 1000.0;
                        case UNITLESS: return value;
                    }
                    break;
                    
                case NANOMETER_SQUARED:
                    switch(target) {
                        case PIXEL_SQUARED: return CameraSetupPlugIn.nanometers2ToPixels2(value);
                        case MICROMETER_SQUARED: return value / 1000.0 / 1000.0;
                        case NANOMETER_SQUARED: return value;
                        case UNITLESS: return value;
                    }
                    break;
                    
                case PIXEL_SQUARED:
                    switch(target) {
                        case PIXEL_SQUARED: return value;
                        case MICROMETER_SQUARED: return CameraSetupPlugIn.pixels2ToNanometers2(value) / 1000.0 / 1000.0;
                        case NANOMETER_SQUARED: return CameraSetupPlugIn.pixels2ToNanometers2(value);
                        case UNITLESS: return value;
                    }
                    break;
                
                case DIGITAL:
                    switch(target) {
                        case PHOTON: return CameraSetupPlugIn.digitalCountsToPhotons(value);
                        case DIGITAL: return value;
                        case UNITLESS: return value;
                    }
                    break;
                
                case PHOTON:
                    switch(target) {
                        case PHOTON: return value;
                        case DIGITAL: return CameraSetupPlugIn.photonsToDigitalCounts(value);
                        case UNITLESS: return value;
                    }
                    break;
                
                case DEGREE:
                    switch(target) {
                        case DEGREE: return value;
                        case RADIAN: return value / 180.0 * PI;
                        case UNITLESS: return value;
                    }
                    break;
                    
                case RADIAN:
                    switch(target) {
                        case DEGREE: return value / PI * 180.0;
                        case RADIAN: return value;
                        case UNITLESS: return value;
                    }
                    break;
                    
                case UNITLESS:
                    return value;
            }
            throw new UnsupportedOperationException("Conversion from " + toString() + " to " + target.toString() + " is not allowed!");
        }
        
        private static HashMap<String, Units> allUnitsNames = null;
        private static HashMap<String, Units> allUnits = null;
        private static Vector<Units> [] groups = null;
        private static EnumMap<Units, Integer> groupMap = null;
        
        public static Units fromString(String unit) {
            if (allUnitsNames == null) {
                allUnitsNames = new HashMap<String, Units>();
                allUnitsNames.put(Units.PIXEL.toString(), Units.PIXEL);
                allUnitsNames.put(Units.NANOMETER.toString(), Units.NANOMETER);
                allUnitsNames.put(Units.MICROMETER.toString(), Units.MICROMETER);
                allUnitsNames.put(Units.PIXEL_SQUARED.toString(), Units.PIXEL_SQUARED);
                allUnitsNames.put(Units.NANOMETER_SQUARED.toString(), Units.NANOMETER_SQUARED);
                allUnitsNames.put(Units.MICROMETER_SQUARED.toString(), Units.MICROMETER_SQUARED);
                allUnitsNames.put(Units.DIGITAL.toString(), Units.DIGITAL);
                allUnitsNames.put(Units.PHOTON.toString(), Units.PHOTON);
                allUnitsNames.put(Units.DEGREE.toString(), Units.DEGREE);
                allUnitsNames.put(Units.RADIAN.toString(), Units.RADIAN);
                allUnitsNames.put(Units.UNITLESS.toString(), Units.UNITLESS);
            }
            if(allUnitsNames.containsKey(unit)) {
                return allUnitsNames.get(unit);
            } else {
                return Units.UNITLESS;
            }
        }
        
        public static Vector<Units> getCompatibleUnits(Units selected) {
            if((groups == null) || (groupMap == null)) {
                groups = new Vector[5];
                groups[0] = new Vector<Units>(Arrays.asList(new Units[] { Units.PIXEL, Units.NANOMETER, Units.MICROMETER }));
                groups[1] = new Vector<Units>(Arrays.asList(new Units[] { Units.PIXEL_SQUARED, Units.NANOMETER_SQUARED, Units.MICROMETER_SQUARED }));
                groups[2] = new Vector<Units>(Arrays.asList(new Units[] { Units.DIGITAL, Units.PHOTON }));
                groups[3] = new Vector<Units>(Arrays.asList(new Units[] { Units.DEGREE, Units.RADIAN }));
                groups[4] = new Vector<Units>(Arrays.asList(new Units[] { Units.UNITLESS }));
                //
                groupMap = new EnumMap<Units, Integer>(Units.class);
                groupMap.put(Units.PIXEL, 0);
                groupMap.put(Units.NANOMETER, 0);
                groupMap.put(Units.MICROMETER, 0);
                groupMap.put(Units.PIXEL_SQUARED, 1);
                groupMap.put(Units.NANOMETER_SQUARED, 1);
                groupMap.put(Units.MICROMETER_SQUARED, 1);
                groupMap.put(Units.DIGITAL, 2);
                groupMap.put(Units.PHOTON, 2);
                groupMap.put(Units.DEGREE, 3);
                groupMap.put(Units.RADIAN, 3);
                groupMap.put(Units.UNITLESS, 4);
            }
            return groups[groupMap.get(selected).intValue()];
        }

        public static Units getDefaultUnit(String paramName) {
            if (allUnits == null) {
                allUnits = new HashMap<String, Units>();
                allUnits.put(PSFModel.Params.LABEL_X, Units.PIXEL);
                allUnits.put(PSFModel.Params.LABEL_Y, Units.PIXEL);
                allUnits.put(PSFModel.Params.LABEL_Z, Units.NANOMETER);
                allUnits.put(PSFModel.Params.LABEL_SIGMA, Units.PIXEL);
                allUnits.put(PSFModel.Params.LABEL_SIGMA1, Units.PIXEL);
                allUnits.put(PSFModel.Params.LABEL_SIGMA2, Units.PIXEL);
                allUnits.put(PSFModel.Params.LABEL_INTENSITY, Units.DIGITAL);
                allUnits.put(PSFModel.Params.LABEL_OFFSET, Units.DIGITAL);
                allUnits.put(PSFModel.Params.LABEL_BACKGROUND, Units.DIGITAL);
                allUnits.put(PSFModel.Params.LABEL_ANGLE, Units.DEGREE);
                //
                allUnits.put(LABEL_ID, Units.UNITLESS);
                allUnits.put(LABEL_FRAME, Units.UNITLESS);
                allUnits.put(LABEL_DETECTIONS, Units.UNITLESS);
                allUnits.put(LABEL_GROUND_TRUTH_ID, Units.UNITLESS);
                allUnits.put(LABEL_DISTANCE_TO_GROUND_TRUTH, Units.NANOMETER);
                //
                allUnits.put(Fitting.LABEL_THOMPSON, Units.NANOMETER);
            }
            if(allUnits.containsKey(paramName)) {
                return allUnits.get(paramName);
            } else {
                return Units.UNITLESS;
            }
        }
        
        @Override
        public String toString() {
            return label;
        }
    }
    
    public static enum MergingOperations {
        NONE, MIN,  MAX, SUM, MEAN, COUNT, RECALC;
        
        public static HashMap<String, MergingOperations> allParams = null;

        public static void init() {
            allParams = new HashMap<String, MergingOperations>();
            allParams.put(PSFModel.Params.LABEL_X, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_Y, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_Z, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_SIGMA, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_SIGMA1, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_SIGMA2, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_INTENSITY, MergingOperations.SUM);
            allParams.put(PSFModel.Params.LABEL_OFFSET, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_BACKGROUND, MergingOperations.SUM);
            allParams.put(PSFModel.Params.LABEL_ANGLE, MergingOperations.MEAN);
            //
            allParams.put(LABEL_ID, MergingOperations.MIN);
            allParams.put(LABEL_FRAME, MergingOperations.MIN);
            allParams.put(LABEL_DETECTIONS, MergingOperations.COUNT);
            //
            allParams.put(Fitting.LABEL_THOMPSON, MergingOperations.RECALC);
        }
        
        // molecule <-- target
        // detections <-- source
        // paramName <-- parameter, which is this operation applied to
        public static void merge(Molecule molecule, Vector<Molecule> detections, String paramName) throws Exception {
            if (allParams == null) {
                init();
            }
            switch(allParams.get(paramName)) {
                case MIN: molecule.setParam(paramName, min(Molecule.extractParamToArray(detections, paramName))); break;
                case MAX: molecule.setParam(paramName, max(Molecule.extractParamToArray(detections, paramName))); break;
                case SUM: molecule.setParam(paramName, sum(Molecule.extractParamToArray(detections, paramName))); break;
                case MEAN: molecule.setParam(paramName, mean(Molecule.extractParamToArray(detections, paramName))); break;
                case COUNT: molecule.setParam(paramName, detections.size()); break;
                case RECALC:
                    if(LABEL_ID.equals(paramName)) {
                        molecule.setParam(paramName, IJResultsTable.getResultsTable().getNewId());
                    } else if(Fitting.LABEL_THOMPSON.equals(paramName)) {
                        if(CameraSetupPlugIn.isEmGain) {
                            molecule.setParam(paramName, Units.NANOMETER, Fitting.emccdThompson(molecule));
                        } else {
                            molecule.setParam(paramName, Units.NANOMETER, Fitting.ccdThompson(molecule));
                        }
                    } else {
                        throw new IllegalArgumentException("Parameter `" + paramName + "` can't be recalculated.");
                    }
                    break;
                default: // NONE
                    break;
            }
        }
    }
}