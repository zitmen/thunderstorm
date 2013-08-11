package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// ============================================================================================= //
// TODO: pridat metadata pro fitovani, abych mohl spocitat Thompsona, pripadne neco dalsiho      //
//     + sloucit rozhrani s mergovanim molekul v post-processingu                                //
//     + zahrnout i scale na energii/peak, fotony/ADU, px/nm/um, atd.                            //
//    ++ bonbonek na konec, vkladat instance do tabulky a povolit rozbalovani sloucenych molekul //
// ============================================================================================= //
public class PSFInstance implements Iterable<Map.Entry<String, Double>> {

    public static class Units {
        public static final String LABEL_MICROMETER = "um";
        public static final String LABEL_NANOMETER = "nm";
        public static final String LABEL_PIXEL = "px";
        public static final String LABEL_DIGITAL = "ADU";
        public static final String LABEL_PHOTON = "photon";
        public static final String LABEL_DEGREE = "deg";
        public static final String LABEL_RADIAN = "rad";
        
        private static HashMap<String, String> all_units = null;

        public static String getUnit(String paramName) {
            if (all_units == null) {
                all_units = new HashMap<String, String>();
                all_units.put(Params.LABEL_X, Units.LABEL_PIXEL);
                all_units.put(Params.LABEL_Y, Units.LABEL_PIXEL);
                all_units.put(Params.LABEL_Z, Units.LABEL_NANOMETER);
                all_units.put(Params.LABEL_SIGMA, Units.LABEL_PIXEL);
                all_units.put(Params.LABEL_SIGMA1, Units.LABEL_PIXEL);
                all_units.put(Params.LABEL_SIGMA2, Units.LABEL_PIXEL);
                all_units.put(Params.LABEL_INTENSITY, Units.LABEL_DIGITAL);
                all_units.put(Params.LABEL_BACKGROUND, Units.LABEL_DIGITAL);
                all_units.put(Params.LABEL_ANGLE, Units.LABEL_DEGREE);
            }
            return all_units.get(paramName);
        }
    }
    
    public static final String LABEL_ID = "id";
    public static final String LABEL_FRAME = "frame";
    public static final String LABEL_DETECTIONS = "detections";
    
    private Params params;

    public PSFInstance(Params params) {
        assert params.hasParam(Params.X) && params.hasParam(Params.Y);
        this.params = params;
    }
    
    public boolean hasParam(int param) {
        return params.hasParam(param);
    }
    
    public boolean hasParam(String name) {
        return params.hasParam(name);
    }

    public double getParamAt(int i) {
        return params.getParamAt(i);
    }
    
    public void setParamAt(int i, double value) {
        params.setParamAt(i, value);
    }

    public String getParamNameAt(int i) {
        return params.getParamNameAt(i);
    }
    
    public double getX() {
        return params.getParam(Params.X);
    }
    
    public void setX(double value) {
        params.setParam(Params.X, value);
    }
    
    public double getY() {
        return params.getParam(Params.Y);
    }
    
    public void setY(double value) {
        params.setParam(Params.Y, value);
    }

    public double getParam(int param) {
        return params.getParam(param);
    }
    
    public void setParam(int param, double value) {
        params.setParam(param, value);
    }

    public void setParam(String name, double value) {
        params.setParam(name, value);
    }

    public double getParam(String name) {
        return params.getParam(name);
    }

    /**
     * Conversion between pixels and nanometers with known pixelsize.
     *
     * Simply multiply {
     *
     * @mathjax x[nm] = x[px] \cdot pixelsize}.
     *
     * @param pixelsize size of a single pixel in nanometers
     */
    public void convertXYToNanoMeters(double pixelsize) {
        setParam(Params.X, pixelsize * getParam(Params.X));
        setParam(Params.Y, pixelsize * getParam(Params.Y));
    }

    public int[] getParamIndices() {
        return params.indices;
    }
    
    public String[] getParamNames() {
        return params.names;
    }

    public double[] getParamArray() {
        return params.values;
    }

    @Override
    public Iterator<Map.Entry<String, Double>> iterator() {
        return new Iterator<Map.Entry<String, Double>>() {
            int position = 0;
            private AbstractMap.SimpleImmutableEntry<String, Double> retValue;

            @Override
            public boolean hasNext() {
                return position < params.getParamsCount();
            }

            @Override
            public Map.Entry<String, Double> next() {
                retValue = new AbstractMap.SimpleImmutableEntry<String, Double>(params.getParamNameAt(position), params.getParamAt(position));
                position++;
                return retValue;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Removing is not supported!");
            }
        };
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < params.getParamsCount(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(params.getParamNameAt(i));
            sb.append("=");
            sb.append(params.getParamAt(i));
        }
        sb.append("]");
        return sb.toString();
    }

    public static double[] extractParamToArray(List<PSFInstance> fits, int param) {
        double[] array = new double[fits.size()];
        for (int i = 0; i < fits.size(); i++) {
            array[i] = fits.get(i).getParam(param);
        }
        return array;
    }
    
    public static double[] extractParamToArray(List<PSFInstance> fits, String param) {
        double[] array = new double[fits.size()];
        for (int i = 0; i < fits.size(); i++) {
            array[i] = fits.get(i).getParam(param);
        }
        return array;
    }
}
