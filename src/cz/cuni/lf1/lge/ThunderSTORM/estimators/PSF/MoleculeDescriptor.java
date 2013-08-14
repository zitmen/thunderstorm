package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

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
import java.util.HashMap;
import java.util.Vector;

/**
 * This class provides all the semantic information about instances of Molecule class,
 * independently of PSFModel, Estimator or any other additional source of information.
 */
public class MoleculeDescriptor implements Cloneable {
    private HashMap<String, IndexAndColumn> paramNames; // duplicate of arrays `names` and `indices`, but it is much faster in some cases
    
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
    
    // the `column` refers to everything in `MoleculeDescriptor`.
    public int getParamColumn(String param) {
        return paramNames.get(param).column;
    }

    public double getPixelSize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public double getDigital2Photons() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        public static final String LABEL_CCD_THOMPSON = "d_xy (CCD)";
        public static final String LABEL_EMCCD_THOMPSON = "d_xy (EMCCD)";
        
        public static double ccdThompson(double psfSigma2, double psfEnergy, double backgroundVariance, double pixelSize) {
            return ((psfSigma2 + pixelSize*pixelSize/12) / psfEnergy) +
                    ((8*PI*psfSigma2*psfSigma2*backgroundVariance) / (pixelSize*pixelSize*psfEnergy));
        }
        
        public static double emccdThompson(double psfSigma2, double psfEnergy, double backgroundStdDev, double pixelSize) {
            return ((2*psfSigma2 + pixelSize*pixelSize/12) / psfEnergy) +
                    ((8*PI*psfSigma2*psfSigma2*backgroundStdDev) / (pixelSize*pixelSize*psfEnergy*psfEnergy));
        }
    }
    
    public static final String LABEL_ID = "id";
    public static final String LABEL_FRAME = "frame";
    public static final String LABEL_DETECTIONS = "detections";
    
    public static enum Units {
        MICROMETER("um"),
        NANOMETER("nm"),
        PIXEL("px"),
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
        
        private static HashMap<String, Units> allUnitsNames = null;
        private static HashMap<String, Units> allUnits = null;
        
        public static Units fromString(String unit) {
            if (allUnitsNames == null) {
                allUnitsNames = new HashMap<String, Units>();
                allUnitsNames.put(Units.PIXEL.toString(), Units.PIXEL);
                allUnitsNames.put(Units.NANOMETER.toString(), Units.NANOMETER);
                allUnitsNames.put(Units.MICROMETER.toString(), Units.MICROMETER);
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
                allUnits.put(PSFModel.Params.LABEL_BACKGROUND_VARIANCE, Units.DIGITAL);
                allUnits.put(PSFModel.Params.LABEL_ANGLE, Units.DEGREE);
                //
                allUnits.put(LABEL_ID, Units.UNITLESS);
                allUnits.put(LABEL_FRAME, Units.UNITLESS);
                allUnits.put(LABEL_DETECTIONS, Units.UNITLESS);
                //
                allUnits.put(Fitting.LABEL_CCD_THOMPSON, Units.PIXEL);
                allUnits.put(Fitting.LABEL_EMCCD_THOMPSON, Units.PIXEL);
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
            allParams.put(PSFModel.Params.LABEL_BACKGROUND_VARIANCE, MergingOperations.SUM);
            allParams.put(PSFModel.Params.LABEL_ANGLE, MergingOperations.MEAN);
            //
            allParams.put(LABEL_ID, MergingOperations.MIN);
            allParams.put(LABEL_FRAME, MergingOperations.MIN);
            allParams.put(LABEL_DETECTIONS, MergingOperations.COUNT);
            //
            allParams.put(Fitting.LABEL_CCD_THOMPSON, MergingOperations.RECALC);
            allParams.put(Fitting.LABEL_EMCCD_THOMPSON, MergingOperations.RECALC);
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
                    } else if(Fitting.LABEL_CCD_THOMPSON.equals(paramName)) {
                        double psfSigma2, psfEnergy, bkgVar;
                        if(molecule.hasParam(PSFModel.Params.LABEL_SIGMA)) {    // symmetric
                            psfSigma2 = molecule.getParam(PSFModel.Params.LABEL_SIGMA) *
                                        molecule.getParam(PSFModel.Params.LABEL_SIGMA);
                        } else if(molecule.hasParam(PSFModel.Params.LABEL_SIGMA1) && molecule.hasParam(PSFModel.Params.LABEL_SIGMA2)) { // eliptic
                            psfSigma2 = molecule.getParam(PSFModel.Params.LABEL_SIGMA1) *
                                        molecule.getParam(PSFModel.Params.LABEL_SIGMA2);
                        } else {
                            throw new Exception("Cannot calculate Thompson equation!");
                        }
                        psfEnergy = molecule.getParam(PSFModel.Params.LABEL_INTENSITY);
                        bkgVar = molecule.getParam(PSFModel.Params.LABEL_BACKGROUND_VARIANCE);
                        molecule.setParam(paramName, Fitting.ccdThompson(psfSigma2, psfEnergy, bkgVar, molecule.descriptor.getPixelSize()));
                    } else if(Fitting.LABEL_EMCCD_THOMPSON.equals(paramName)) {
                        double psfSigma2, psfEnergy, bkgStd;
                        if(molecule.hasParam(PSFModel.Params.LABEL_SIGMA)) {    // symmetric
                            psfSigma2 = molecule.getParam(PSFModel.Params.LABEL_SIGMA) *
                                        molecule.getParam(PSFModel.Params.LABEL_SIGMA);
                        } else if(molecule.hasParam(PSFModel.Params.LABEL_SIGMA1) && molecule.hasParam(PSFModel.Params.LABEL_SIGMA2)) { // eliptic
                            psfSigma2 = molecule.getParam(PSFModel.Params.LABEL_SIGMA1) *
                                        molecule.getParam(PSFModel.Params.LABEL_SIGMA2);
                        } else {
                            throw new Exception("Cannot calculate Thompson equation!");
                        }
                        psfEnergy = molecule.getParam(PSFModel.Params.LABEL_INTENSITY);
                        bkgStd = sqrt(molecule.getParam(PSFModel.Params.LABEL_BACKGROUND_VARIANCE));
                        molecule.setParam(paramName, Fitting.emccdThompson(psfSigma2, psfEnergy, bkgStd, molecule.descriptor.getPixelSize()));
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