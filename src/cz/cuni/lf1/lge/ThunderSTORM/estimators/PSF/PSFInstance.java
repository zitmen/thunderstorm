package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 */
public class PSFInstance implements Iterable<Map.Entry<String, Double>> {

  private final String[] paramNames;
  private final double[] params;
  public static final String X = "x";
  public static final String Y = "y";
  public static final String Z = "z";
  public static final String SIGMA = "\u03C3";
  public static final String SIGMA2 = "\u03C32";
  public static final String INTENSITY = "intensity";
  public static final String BACKGROUND = "background";

  public PSFInstance(String[] paramNames, double[] params) {
    assert paramNames.length == params.length : "names and values array lengths must be the same";
    this.paramNames = paramNames;
    this.params = params;
    assert contains(X) && contains(Y);
  }

  public double getX() {
    return getParam(X);
  }

  public void setX(double value) {
    setParam(X, value);
  }

  public double getY() {
    return getParam(Y);
  }

  public void setY(double value) {
    setParam(Y, value);
  }

  public double getParamAt(int i) {
    return params[i];
  }

  public String getParamName(int i) {
    return paramNames[i];
  }

  public double getParam(String name) {
    return params[getParamIndex(name)];
  }

  public int getParamIndex(String name) {
    for (int i = 0; i < paramNames.length; i++) {
      if (paramNames[i].equals(name)) {
        return i;
      }
    }
    throw new IllegalArgumentException("This instance does not contain the requested parameter: " + name);
  }

  public void setParamAt(int pos, double value) {
    params[pos] = value;
  }

  public void setParam(String name, double value) {
    for (int i = 0; i < paramNames.length; i++) {
      if (paramNames[i].equals(name)) {
        params[i] = value;
        return;
      }
    }
    throw new IllegalArgumentException("This instance does not contain the requested parameter: " + name);
  }

  public final boolean hasParam(String name) {
    return contains(name);
  }

  private boolean contains(String name) {
    for (int i = 0; i < paramNames.length; i++) {
      if (paramNames[i].equals(name)) {
        return true;
      }
    }
    return false;
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
    int xIndex = getParamIndex(X);
    int yIndex = getParamIndex(Y);
    setParamAt(xIndex, pixelsize * getParamAt(xIndex));
    setParamAt(yIndex, pixelsize * getParamAt(yIndex));
  }

  public String[] getParamNames() {
    return paramNames;
  }

  public double[] getParamArray() {
    return params;
  }

  @Override
  public Iterator<Map.Entry<String, Double>> iterator() {
    return new Iterator<Map.Entry<String, Double>>() {
      int position = 0;
      private AbstractMap.SimpleImmutableEntry<String, Double> retValue;

      @Override
      public boolean hasNext() {
        return position < paramNames.length;
      }

      @Override
      public Map.Entry<String, Double> next() {
        retValue = new AbstractMap.SimpleImmutableEntry<String, Double>(paramNames[position], params[position]);
        position++;
        return retValue;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("Removing not supported.");
      }
    };
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < paramNames.length; i++) {
      if (i != 0) {
        sb.append(", ");
      }
      sb.append(paramNames[i]);
      sb.append("=");
      sb.append(params[i]);
    }
    sb.append("]");
    return sb.toString();
  }
}
