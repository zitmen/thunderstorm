package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.DaostormCalibration;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.*;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.*;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.EllipticGaussianEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.ui.SymmetricGaussianEstimatorUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.results.MeasurementProtocol;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * This class provides all the semantic information about instances of Molecule
 * class, independently of PSFModel, Estimator or any other additional source of
 * information.
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
    public MoleculeDescriptor(String[] names) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(null);
        fillUnits();
        buildLabels();
        fillNamesSet();
    }

    public MoleculeDescriptor(String[] names, int[] indices) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(indices);
        fillUnits();
        buildLabels();
        fillNamesSet();
    }

    public MoleculeDescriptor(String[] names, Units[] units) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(null);
        this.units = new Vector<Units>();
        this.units.addAll(Arrays.asList(units));
        buildLabels();
        fillNamesSet();
    }

    public MoleculeDescriptor(String[] names, int[] indices, Units[] units) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(indices);
        this.units = new Vector<Units>();
        this.units.addAll(Arrays.asList(units));
        buildLabels();
        fillNamesSet();
    }

    public MoleculeDescriptor(String[] names, int[] indices, Units[] units, String[] labels) {
        this.names = new Vector<String>();
        this.names.addAll(Arrays.asList(names));
        setIndices(indices);
        this.units = new Vector<Units>();
        this.units.addAll(Arrays.asList(units));
        this.labels = new Vector<String>();
        this.labels.addAll(Arrays.asList(labels));
        fillNamesSet();
    }

    private void setIndices(int[] indices) {
        int[] ind = indices;
        if(indices == null) {
            ind = genIntSequence(0, names.size());
        }
        this.indices = new Vector<Integer>();
        for(int i = 0; i < ind.length; i++) {
            this.indices.add(ind[i]);
        }
    }

    private void fillUnits() {
        assert (names != null);

        this.units = new Vector<Units>();
        for(int i = 0, im = names.size(); i < im; i++) {
            this.units.add(Units.getDefaultUnit(names.elementAt(i)));
        }
    }

    private void buildLabels() {
        assert (names != null);
        assert (units != null);

        labels = new Vector<String>();
        for(int i = 0, im = names.size(); i < im; i++) {
            labels.add(getLabel(i, true));
        }
    }

    private void fillNamesSet() {
        assert (names != null);

        paramNames = new HashMap<String, IndexAndColumn>();
        for(int i = 0, im = names.size(); i < im; i++) {
            paramNames.put(names.elementAt(i), new IndexAndColumn(indices.elementAt(i), i));
        }
    }

    public String getLabel(int i, boolean rebuild) {
        assert (names != null);
        assert (units != null);
        assert (labels != null);
        assert (i < units.size());

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
        assert ((param != null) && (paramNames != null));

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
        IndexAndColumn ic = paramNames.get(param);
        if (ic == null) return getParamsCount();
        return ic.index;
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
        if (max_i+1 > mol.values.length) {
            double[] tmp = mol.values;
            mol.values = new double[max_i+1];
            System.arraycopy(tmp, 0, mol.values, 0, tmp.length);
            Arrays.fill(mol.values, tmp.length, mol.values.length, 0.0);
        }
    }

    @Override
    public MoleculeDescriptor clone() {
        String[] clonedNames = new String[names.size()];
        int[] clonedIndices = new int[indices.size()];
        Units[] clonedUnits = new Units[units.size()];
        for(int i = 0, im = names.size(); i < im; i++) {
            if(names.elementAt(i) != null) {
                clonedNames[i] = new String(names.elementAt(i).toCharArray());  // don't know how else to create a new instance of the string
            } else {
                clonedNames[i] = null;
            }
            if(indices.elementAt(i) != null) {
                clonedIndices[i] = indices.elementAt(i);
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

    public static Pair<String, Units> parseColumnLabel(String label) {
        assert (label != null);

        int start = label.lastIndexOf('['), end = label.lastIndexOf(']');
        if((start < 0) || (end < 0) || (start > end)) {
            return new Pair<String, Units>(label.trim(), Units.UNITLESS);
        } else {
            return new Pair<String, Units>(label.substring(0, start).trim(), Units.fromString(label.substring(start + 1, end).trim()));
        }
    }

    // ===============================================================
    public static class Fitting {

        public static final String LABEL_CHI2 = "chi2";
        public static final String LABEL_UNCERTAINTY_XY = "uncertainty_xy";
        public static final String LABEL_UNCERTAINTY_Z = "uncertainty_z";

        /**
         * Returns lateral uncertainty in nanometers.
         *
         * Thompson, et al. 2002; corrected by Mortensen, et al. 2010 (16/9 instead of 1 to not underestimate)
         * compensation for EM gain worked out by Quan, et al. 2010
         */
        public static double uncertaintyXY(Molecule molecule) throws UncertaintyNotApplicableException {
            double psfSigma2;
            if(molecule.hasParam(LABEL_SIGMA)) {    // 2D (symmetric Gaussian)
                psfSigma2 = molecule.getParam(LABEL_SIGMA, Units.NANOMETER)  * molecule.getParam(LABEL_SIGMA, Units.NANOMETER);
            } else if(molecule.hasParam(LABEL_SIGMA1) && molecule.hasParam(LABEL_SIGMA2)) { // 3D astigmatism (elliptic Gaussian)
                psfSigma2 = molecule.getParam(LABEL_SIGMA1, Units.NANOMETER)  * molecule.getParam(LABEL_SIGMA2, Units.NANOMETER);
            } else {
                throw new UncertaintyNotApplicableException("Missing parameter - `sigma`!");
            }
            double gain, readout;
            if (CameraSetupPlugIn.getIsEmGain()) {  // EMCCD
                gain = 2.0; // correction factor by Quan
                readout = 0.0;
            } else {    // CCD or sCMOS
                gain = 1.0;
                readout = CameraSetupPlugIn.getReadoutNoise();
            }

            double pixelSize = CameraSetupPlugIn.getPixelSize();
            double psfPhotons = molecule.getParam(LABEL_INTENSITY, Units.PHOTON) * CameraSetupPlugIn.getQuantumEfficiency();
            double bkgStd = molecule.getParam(LABEL_BACKGROUND, Units.PHOTON) * CameraSetupPlugIn.getQuantumEfficiency() + readout;
            double tau = 0.0;

            String fittingMethod = null;
            MeasurementProtocol protocol = IJResultsTable.getResultsTable().getMeasurementProtocol();
            if (protocol.analysisEstimator instanceof EllipticGaussianEstimatorUI) {    // 3D? elliptic Gauss (astigmatism)
                fittingMethod = ((EllipticGaussianEstimatorUI) protocol.analysisEstimator).getMethod();
                DaostormCalibration cal = ((EllipticGaussianEstimatorUI) protocol.analysisEstimator).getDaoCalibration();
                double l2 = abs(cal.getC1() * cal.getC2());
                double d2 = abs(cal.getD1() * cal.getD2());
                tau = 2.0 * PI * bkgStd*bkgStd * (psfSigma2*(1.0 + l2/d2) + pixelSize*pixelSize/12.0) / (psfPhotons * pixelSize*pixelSize);
            } else if (protocol.analysisEstimator instanceof SymmetricGaussianEstimatorUI) {    // 2D? Gauss or IntGauss
                fittingMethod = ((SymmetricGaussianEstimatorUI) protocol.analysisEstimator).getMethod();
                tau = 2.0 * PI * bkgStd*bkgStd * (psfSigma2 + pixelSize*pixelSize/12.0) / (psfPhotons * pixelSize*pixelSize);
            }

            if (fittingMethod != null) {
                if (fittingMethod.equals(SymmetricGaussianEstimatorUI.MLE)
                 || fittingMethod.equals(SymmetricGaussianEstimatorUI.WLSQ)) {
                    // Note: here we don't distinguish between MLE and WLSQ, however, there is a difference!
                    //       For details, see supplementary note for Mortensen 2010, Eq. (46), which shows an extra offset-dependent term!
                    return sqrt((gain * psfSigma2 + pixelSize*pixelSize/12.0) / psfPhotons * (1.0 + 4.0*tau + sqrt(2.0*tau/(1 + 4.0*tau))));
                } else if (fittingMethod.equals(SymmetricGaussianEstimatorUI.LSQ)) {
                    return sqrt((gain * psfSigma2 + pixelSize*pixelSize/12.0) / psfPhotons * (16.0/9.0 + 4.0*tau));
                }
            }
            throw new UncertaintyNotApplicableException("Unsupported fitting method! Was the measurement protocol loaded properly?");
        }

        /**
         * Returns axial uncertainty in nanometers.
         *
         * Here we always assume MLE fit --> the math has been worked out by Rieger, et al. 2014.
         * When we distinguish between (W)LSQ and MLE, we could work out the math for LSQ in a similar fashion
         * as Thompson, et al. 2002 and/or Mortensen, et al. 2010.
         */
        public static double uncertaintyZ(Molecule molecule) throws UncertaintyNotApplicableException {
            MeasurementProtocol protocol = IJResultsTable.getResultsTable().getMeasurementProtocol();
            if (!(protocol.analysisEstimator instanceof EllipticGaussianEstimatorUI)
                    || !(molecule.hasParam(LABEL_SIGMA1) && molecule.hasParam(LABEL_SIGMA2))) {
                throw new UncertaintyNotApplicableException("Axial uncertainty cannot be calculated for 2D estimate (missing sigma1, sigma2)!");
            }

            double gain, readout;
            if (CameraSetupPlugIn.getIsEmGain()) {  // EMCCD
                gain = 2.0; // correction factor by Quan
                readout = 0.0;
            } else {    // CCD or sCMOS
                gain = 1.0;
                readout = CameraSetupPlugIn.getReadoutNoise();
            }
            double pixelSize = CameraSetupPlugIn.getPixelSize();
            double psfPhotons = molecule.getParam(LABEL_INTENSITY, Units.PHOTON) * CameraSetupPlugIn.getQuantumEfficiency();
            double bkgStd = molecule.getParam(LABEL_BACKGROUND, Units.PHOTON) * CameraSetupPlugIn.getQuantumEfficiency() + readout;
            double psfSigma1 = molecule.getParam(LABEL_SIGMA1, Units.NANOMETER);
            double psfSigma2 = molecule.getParam(LABEL_SIGMA2, Units.NANOMETER);
            double zCoord = molecule.hasParam(LABEL_Z_REL)
                    ? molecule.getParam(LABEL_Z_REL, Units.NANOMETER)
                    : molecule.getParam(LABEL_Z, Units.NANOMETER);

            DaostormCalibration cal = ((EllipticGaussianEstimatorUI) protocol.analysisEstimator).getDaoCalibration();
            double l2 = abs(cal.getC1() * cal.getC2());
            double d2 = abs(cal.getD1() * cal.getD2());
            double tau = 2.0 * PI * bkgStd*bkgStd * (psfSigma1*psfSigma2*(1.0 + l2/d2) + pixelSize*pixelSize/12.0) / (psfPhotons * pixelSize*pixelSize);
            double zLimit = sqrt(l2 + d2);  // singularity in CRLB - do not evaluate at positions beyond
            if (abs(zCoord) >= zLimit) return Double.POSITIVE_INFINITY;
            //
            double compensation = (sqrt(gain * psfSigma1*psfSigma1 + pixelSize*pixelSize/12.0) / psfSigma1
                                +  sqrt(gain * psfSigma2*psfSigma2 + pixelSize*pixelSize/12.0) / psfSigma2)
                                / 2.0;  // finite pixel size and em gain compensation
            //
            double stdSigma;  // method-dependent parameter
            String fittingMethod = ((EllipticGaussianEstimatorUI) protocol.analysisEstimator).getMethod();
            if (fittingMethod != null) {
                if (fittingMethod.equals(SymmetricGaussianEstimatorUI.MLE)
                        || fittingMethod.equals(SymmetricGaussianEstimatorUI.WLSQ)) {
                    // Note: here we don't distinguish between MLE and WLSQ, however, there is a difference!
                    //       For details, see supplementary note for Mortensen 2010, Eq. (46), which shows an extra offset-dependent term!
                    stdSigma = sqrt(1 + 8.0 * tau + sqrt(9.0 * tau / (1.0 + 4.0 * tau))) * compensation / sqrt(psfPhotons);

                } else if (fittingMethod.equals(SymmetricGaussianEstimatorUI.LSQ)) {
                    stdSigma = sqrt(1 + 8.0 * tau) * compensation / sqrt(psfPhotons);
                } else {
                    throw new UncertaintyNotApplicableException("Unsupported (unknown) fitting method! Was the measurement protocol loaded properly?");
                }
            } else {
                throw new UncertaintyNotApplicableException("Unsupported (empty) fitting method! Was the measurement protocol loaded properly?");
            }
            //
            double Fsq = 4.0 * l2 * zCoord*zCoord / sqr(l2 + d2 + zCoord*zCoord);
            double stdF = sqrt(1.0 - Fsq) * stdSigma;
            double stdZ = stdF * sqr(l2 + d2 + zCoord*zCoord) / (2.0 * sqrt(l2) * (l2 + d2 - zCoord*zCoord));
            return stdZ;
        }

        public static class UncertaintyNotApplicableException extends Exception {
            public UncertaintyNotApplicableException(String message) {
                super(message);
            }

            public UncertaintyNotApplicableException() {
                super("Cannot calculate the uncertainty!");
            }
        }
    }

    public static final String LABEL_ID = "id";
    public static final String LABEL_FRAME = "frame";
    public static final String LABEL_DETECTIONS = "detections";
    public static final String LABEL_GROUND_TRUTH_ID = "gt_id";
    public static final String LABEL_DISTANCE_TO_GROUND_TRUTH_XY = "gt_dist_xy";
    public static final String LABEL_DISTANCE_TO_GROUND_TRUTH_Z = "gt_dist_z";
    public static final String LABEL_DISTANCE_TO_GROUND_TRUTH_XYZ = "gt_dist_xyz";

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
                        case PIXEL:
                            return CameraSetupPlugIn.nanometersToPixels(1000.0 * value);
                        case NANOMETER:
                            return 1000.0 * value;
                        case MICROMETER:
                            return value;
                        case UNITLESS:
                            return value;
                    }
                    break;

                case NANOMETER:
                    switch(target) {
                        case PIXEL:
                            return CameraSetupPlugIn.nanometersToPixels(value);
                        case NANOMETER:
                            return value;
                        case MICROMETER:
                            return value / 1000.0;
                        case UNITLESS:
                            return value;
                    }
                    break;

                case PIXEL:
                    switch(target) {
                        case PIXEL:
                            return value;
                        case MICROMETER:
                            return CameraSetupPlugIn.pixelsToNanometers(value) / 1000.0;
                        case NANOMETER:
                            return CameraSetupPlugIn.pixelsToNanometers(value);
                        case UNITLESS:
                            return value;
                    }
                    break;

                case MICROMETER_SQUARED:
                    switch(target) {
                        case PIXEL_SQUARED:
                            return CameraSetupPlugIn.nanometers2ToPixels2(value * 1000.0 * 1000.0);
                        case MICROMETER_SQUARED:
                            return value;
                        case NANOMETER_SQUARED:
                            return value * 1000.0 * 1000.0;
                        case UNITLESS:
                            return value;
                    }
                    break;

                case NANOMETER_SQUARED:
                    switch(target) {
                        case PIXEL_SQUARED:
                            return CameraSetupPlugIn.nanometers2ToPixels2(value);
                        case MICROMETER_SQUARED:
                            return value / 1000.0 / 1000.0;
                        case NANOMETER_SQUARED:
                            return value;
                        case UNITLESS:
                            return value;
                    }
                    break;

                case PIXEL_SQUARED:
                    switch(target) {
                        case PIXEL_SQUARED:
                            return value;
                        case MICROMETER_SQUARED:
                            return CameraSetupPlugIn.pixels2ToNanometers2(value) / 1000.0 / 1000.0;
                        case NANOMETER_SQUARED:
                            return CameraSetupPlugIn.pixels2ToNanometers2(value);
                        case UNITLESS:
                            return value;
                    }
                    break;

                case DIGITAL:
                    switch(target) {
                        case PHOTON:
                            return CameraSetupPlugIn.digitalCountsToPhotons(value);
                        case DIGITAL:
                            return value;
                        case UNITLESS:
                            return value;
                    }
                    break;

                case PHOTON:
                    switch(target) {
                        case PHOTON:
                            return value;
                        case DIGITAL:
                            return CameraSetupPlugIn.photonsToDigitalCounts(value);
                        case UNITLESS:
                            return value;
                    }
                    break;

                case DEGREE:
                    switch(target) {
                        case DEGREE:
                            return value;
                        case RADIAN:
                            return value / 180.0 * PI;
                        case UNITLESS:
                            return value;
                    }
                    break;

                case RADIAN:
                    switch(target) {
                        case DEGREE:
                            return value / PI * 180.0;
                        case RADIAN:
                            return value;
                        case UNITLESS:
                            return value;
                    }
                    break;

                case UNITLESS:
                    return value;
            }
            throw new UnsupportedOperationException("Conversion from " + toString() + " to " + target.toString() + " is not allowed!");
        }

        private static HashMap<String, Units> allUnitsNames = null;
        private static HashMap<String, Units> allUnits = null;
        private static Vector<Units>[] groups = null;
        private static EnumMap<Units, Integer> groupMap = null;

        public static Units fromString(String unit) {
            if(allUnitsNames == null) {
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
                groups[0] = new Vector<Units>(Arrays.asList(new Units[]{Units.PIXEL, Units.NANOMETER, Units.MICROMETER}));
                groups[1] = new Vector<Units>(Arrays.asList(new Units[]{Units.PIXEL_SQUARED, Units.NANOMETER_SQUARED, Units.MICROMETER_SQUARED}));
                groups[2] = new Vector<Units>(Arrays.asList(new Units[]{Units.DIGITAL, Units.PHOTON}));
                groups[3] = new Vector<Units>(Arrays.asList(new Units[]{Units.DEGREE, Units.RADIAN}));
                groups[4] = new Vector<Units>(Arrays.asList(new Units[]{Units.UNITLESS}));
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
            return groups[groupMap.get(selected)];
        }

        public static Units getDefaultUnit(String paramName) {
            if(allUnits == null) {
                allUnits = new HashMap<String, Units>();
                allUnits.put(PSFModel.Params.LABEL_X, Units.PIXEL);
                allUnits.put(PSFModel.Params.LABEL_Y, Units.PIXEL);
                allUnits.put(PSFModel.Params.LABEL_Z, Units.NANOMETER);
                allUnits.put(LABEL_SIGMA, Units.PIXEL);
                allUnits.put(LABEL_SIGMA1, Units.PIXEL);
                allUnits.put(LABEL_SIGMA2, Units.PIXEL);
                allUnits.put(LABEL_SIGMA3, Units.PIXEL);
                allUnits.put(LABEL_SIGMA4, Units.PIXEL);
                allUnits.put(LABEL_INTENSITY, Units.DIGITAL);
                allUnits.put(PSFModel.Params.LABEL_OFFSET, Units.DIGITAL);
                allUnits.put(PSFModel.Params.LABEL_OFFSET1, Units.DIGITAL);
                allUnits.put(PSFModel.Params.LABEL_OFFSET2, Units.DIGITAL);
                allUnits.put(LABEL_BACKGROUND, Units.DIGITAL);
                allUnits.put(PSFModel.Params.LABEL_ANGLE, Units.RADIAN);
                //
                allUnits.put(LABEL_ID, Units.UNITLESS);
                allUnits.put(LABEL_FRAME, Units.UNITLESS);
                allUnits.put(LABEL_DETECTIONS, Units.UNITLESS);
                allUnits.put(LABEL_GROUND_TRUTH_ID, Units.UNITLESS);
                allUnits.put(LABEL_DISTANCE_TO_GROUND_TRUTH_XY, Units.NANOMETER);
                allUnits.put(LABEL_DISTANCE_TO_GROUND_TRUTH_Z, Units.NANOMETER);
                allUnits.put(LABEL_DISTANCE_TO_GROUND_TRUTH_XYZ, Units.NANOMETER);
                //
                allUnits.put(Fitting.LABEL_UNCERTAINTY_XY, Units.NANOMETER);
                allUnits.put(Fitting.LABEL_UNCERTAINTY_Z, Units.NANOMETER);
            }
            if(allUnits.containsKey(paramName)) {
                return allUnits.get(paramName);
            } else {
                return Units.UNITLESS;
            }
        }
        
        public static Units getDigitalUnits(Units analogUnit){
            switch(analogUnit) {
                case NANOMETER:
                case MICROMETER:
                     return PIXEL;
                case NANOMETER_SQUARED:
                case MICROMETER_SQUARED:
                    return PIXEL_SQUARED;
                case PHOTON:
                    return DIGITAL;
            }
            return analogUnit;
        }
        
        public static Units getAnalogUnits(Units digitalUnit){
            switch(digitalUnit) {
                case PIXEL:
                case MICROMETER: // this is of course analog unit, but we need all units to be the same
                    return NANOMETER;
                case PIXEL_SQUARED:
                case MICROMETER_SQUARED: // this is of course analog unit, but we need all units to be the same
                    return NANOMETER_SQUARED;
                case DIGITAL:
                    return PHOTON;
            }
            return digitalUnit;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    public static enum MergingOperations {

        NONE, MIN, MAX, SUM, MEAN, COUNT, RECALC, ASSIGN_NaN;

        public static HashMap<String, MergingOperations> allParams = null;

        public static void init() {
            allParams = new HashMap<String, MergingOperations>();
            allParams.put(PSFModel.Params.LABEL_X, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_Y, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_Z, MergingOperations.MEAN);
            allParams.put(LABEL_SIGMA, MergingOperations.MEAN);
            allParams.put(LABEL_SIGMA1, MergingOperations.MEAN);
            allParams.put(LABEL_SIGMA2, MergingOperations.MEAN);
            allParams.put(LABEL_SIGMA3, MergingOperations.MEAN);
            allParams.put(LABEL_SIGMA4, MergingOperations.MEAN);
            allParams.put(LABEL_INTENSITY, MergingOperations.SUM);
            allParams.put(PSFModel.Params.LABEL_OFFSET, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_OFFSET1, MergingOperations.MEAN);
            allParams.put(PSFModel.Params.LABEL_OFFSET2, MergingOperations.MEAN);
            allParams.put(LABEL_BACKGROUND, MergingOperations.SUM);
            allParams.put(PSFModel.Params.LABEL_ANGLE, MergingOperations.MEAN);
            //
            allParams.put(LABEL_ID, MergingOperations.MIN);
            allParams.put(LABEL_FRAME, MergingOperations.MIN);
            allParams.put(LABEL_DETECTIONS, MergingOperations.COUNT);
            //
            allParams.put(Fitting.LABEL_CHI2, MergingOperations.ASSIGN_NaN);
            allParams.put(Fitting.LABEL_UNCERTAINTY_XY, MergingOperations.RECALC);
            allParams.put(Fitting.LABEL_UNCERTAINTY_Z, MergingOperations.RECALC);
        }

        // molecule <-- target
        // detections <-- source
        // paramName <-- parameter, which is this operation applied to
        public static void merge(Molecule molecule, List<Molecule> detections, String paramName) throws Exception {
            if(allParams == null) {
                init();
            }
            if (allParams.containsKey(paramName)) {
                switch (allParams.get(paramName)) {
                    case MIN:
                        molecule.setParam(paramName, VectorMath.min(Molecule.extractParamToArray(detections, paramName)));
                        break;
                    case MAX:
                        molecule.setParam(paramName, VectorMath.max(Molecule.extractParamToArray(detections, paramName)));
                        break;
                    case SUM:
                        molecule.setParam(paramName, VectorMath.sum(Molecule.extractParamToArray(detections, paramName)));
                        break;
                    case MEAN:
                        molecule.setParam(paramName, VectorMath.mean(Molecule.extractParamToArray(detections, paramName)));
                        break;
                    case COUNT:
                        molecule.setParam(paramName, detections.size());
                        break;
                    case ASSIGN_NaN:
                        if (detections.size() > 1) {
                            molecule.setParam(paramName, Double.NaN);
                        }
                        break;
                    case RECALC:
                        if (LABEL_ID.equals(paramName)) {
                            molecule.setParam(paramName, IJResultsTable.getResultsTable().getNewId());
                        } else if (Fitting.LABEL_UNCERTAINTY_XY.equals(paramName)) {
                            molecule.setParam(paramName, Units.NANOMETER, Fitting.uncertaintyXY(molecule));
                        } else if (Fitting.LABEL_UNCERTAINTY_Z.equals(paramName)) {
                            molecule.setParam(paramName, Units.NANOMETER, Fitting.uncertaintyZ(molecule));
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
}
