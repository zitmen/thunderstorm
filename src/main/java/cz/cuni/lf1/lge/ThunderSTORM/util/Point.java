package cz.cuni.lf1.lge.ThunderSTORM.util;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.round;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.ceil;
import ij.gui.Roi;
import java.util.Comparator;
import java.util.List;

/**
 * The class encapsulates X,Y representation of a single point and its
 * intensity.
 *
 * @param <T> is a Number subclass, often Integer or Double
 */
public class Point<T extends Number> {

    /**
     * X coordinate
     */
    public T x;
    /**
     * Y coordinate
     */
    public T y;
    /**
     * Intensity
     */
    public T val;

    /**
     * X, Y, and intenstity are set to null.
     *
     */
    public Point() {
        this.x = null;
        this.y = null;
        this.val = null;
    }

    /**
     * @param x X coordinate
     * @param y Y coordinate
     * @param val Intensity
     */
    public Point(T x, T y, T val) {
        this.x = x;
        this.y = y;
        this.val = val;
    }

    /**
     * @param x X coordinate
     * @param y Y coordinate
     */
    public Point(T x, T y) {
        this.x = x;
        this.y = y;
        this.val = null;
    }

    /**
     * Returns X coordinate.
     *
     * @return X coordinate
     */
    public T getX() {
        return x;
    }

    /**
     * Returns Y coordinate.
     *
     * @return Y coordinate
     */
    public T getY() {
        return y;
    }

    /**
     * Returns intensity.
     *
     * @return Intensity
     */
    public T getVal() {
        return val;
    }

    /**
     * Sets X coordinate.
     *
     * @param x X coordinate
     */
    public void setX(T x) {
        this.x = x;
    }

    /**
     * Sets Y coordinate.
     *
     * @param y Y coordinate
     */
    public void setY(T y) {
        this.y = y;
    }

    /**
     * Sets intensity.
     *
     * @param val Intensity
     */
    public void setVal(T val) {
        this.val = val;
    }

    /**
     * Sets position (X,Y coordinates) of the point.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @return this
     */
    public Point setLocation(T x, T y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets position (X,Y coordinates) and intensity (value) of the point.
     *
     * @param x X coordinate
     * @param y Y coordinate
     * @param val intensity
     */
    public void set(T x, T y, T val) {
        this.x = x;
        this.y = y;
        this.val = val;
    }

    /**
     * Returns the point with its X,Y coordinates and intensity rounded to
     * Integer.
     *
     * @return the point with its X,Y coordinates and intensity rounded to
     * Integer.
     */
    public Point<Integer> roundToInteger() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return new Point<Integer>(round(this.x.floatValue()), round(this.y.floatValue()));
        } else {
            return new Point<Integer>(round(this.x.floatValue()), round(this.y.floatValue()), round(this.val.floatValue()));
        }
    }

    /**
     * Returns the point with its X,Y coordinates and intensity rounded to Long.
     *
     * @return the point with its X,Y coordinates and intensity rounded to Long.
     */
    public Point<Long> roundToLong() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return new Point<Long>(round(this.x.doubleValue()), round(this.y.doubleValue()));
        } else {
            return new Point<Long>(round(this.x.doubleValue()), round(this.y.doubleValue()), round(this.val.doubleValue()));
        }
    }

    /**
     * Returns the point with its X,Y coordinates and intensity as Integer (the
     * decimal part is cut off).
     *
     * @return the point with its X,Y coordinates and intensity rounded to
     * Integer.
     */
    public Point<Integer> toInteger() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return new Point<Integer>(this.x.intValue(), this.y.intValue());
        } else {
            return new Point<Integer>(this.x.intValue(), this.y.intValue(), this.val.intValue());
        }
    }

    /**
     * Returns the point with its X,Y coordinates and intensity as Float (single
     * precision). Clearly there may be a round off when converting from Double
     * (double precision).
     *
     * @return the point with its X,Y coordinates and intensity as Float.
     */
    public Point<Float> toFloat() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return new Point<Float>(this.x.floatValue(), this.y.floatValue());
        } else {
            return new Point<Float>(this.x.floatValue(), this.y.floatValue(), this.val.floatValue());
        }
    }

    /**
     * Returns the point with its X,Y coordinates and intensity as Byte (the
     * decimal part is cut off). Clearly there may be an overflow or underflow
     * when converting from larger type as Integer or Long.
     *
     * @return the point with its X,Y coordinates and intensity as Byte.
     */
    public Point<Byte> toByte() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return new Point<Byte>(this.x.byteValue(), this.y.byteValue());
        } else {
            return new Point<Byte>(this.x.byteValue(), this.y.byteValue(), this.val.byteValue());
        }
    }

    /**
     * Returns the point with its X,Y coordinates and intensity as Double
     * (double precision).
     *
     * @return the point with its X,Y coordinates and intensity as Double.
     */
    public Point<Double> toDouble() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return new Point<Double>(this.x.doubleValue(), this.y.doubleValue());
        } else {
            return new Point<Double>(this.x.doubleValue(), this.y.doubleValue(), this.val.doubleValue());
        }
    }

    /**
     * Returns the point with its X,Y coordinates and intensity as Long (the
     * decimal part is cut off).
     *
     * @return the point with its X,Y coordinates and intensity as Long.
     */
    public Point<Long> toLong() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return new Point<Long>(this.x.longValue(), this.y.longValue());
        } else {
            return new Point<Long>(this.x.longValue(), this.y.longValue(), this.val.longValue());
        }
    }

    /**
     * Returns the point with its X,Y coordinates and intensity as Short (the
     * decimal part is cut off). Clearly there may be an overflow or underflow
     * when converting from larger type as Integer or Long.
     *
     * @return the point with its X,Y coordinates and intensity as Short.
     */
    public Point<Short> toShort() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return new Point<Short>(this.x.shortValue(), this.y.shortValue());
        } else {
            return new Point<Short>(this.x.shortValue(), this.y.shortValue(), this.val.shortValue());
        }
    }

    @Override
    public String toString() {
        assert ((x != null) && (y != null));

        if(val == null) {
            return "[" + x.toString() + "," + y.toString() + "]";
        } else {
            return "[" + x.toString() + "," + y.toString() + "]=" + val.toString();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof Point) {
            Point pt = (Point) o;
            boolean bx = false, by = false, bval = false;
            //
            // x
            if((x != null) && (pt.x != null)) {
                if(x.equals(pt.x)) {
                    bx = true;
                }
            } else if((x == null) && (pt.x == null)) {
                bx = true;
            }
            if(bx == false) {
                return false;
            }
            //
            // y
            if((y != null) && (pt.y != null)) {
                if(y.equals(pt.y)) {
                    by = true;
                }
            } else if((y == null) && (pt.y == null)) {
                by = true;
            }
            if(by == false) {
                return false;
            }
            //
            // val
            if((val != null) && (pt.val != null)) {
                if(val.equals(pt.val)) {
                    bval = true;
                }
            } else if((val == null) && (pt.val == null)) {
                bval = true;
            }
            if(bval == false) {
                return false;
            }
            //
            return true;
        }
        return false;
    }

    // automatically generated by Netbeans IDE
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.x != null ? this.x.hashCode() : 0);
        hash = 79 * hash + (this.y != null ? this.y.hashCode() : 0);
        hash = 79 * hash + (this.val != null ? this.val.hashCode() : 0);
        return hash;
    }

    /**
     * Scaling the coordinates, e.g., for the purpose of converting between
     * pixels and nanometers. The data type T of Point&lt;T&gt; remains the same
     * as it was before calling this method.
     *
     * <ol>
     * <li>the X,Y coordinates are converted to Double</li>
     * <li>the multiplication {
     *
     * @mathjax x = x \times factor} and {
     * @mathjax y = y \times factor} is performed</li>
     * <li>the result is converted back to type T (there may be a round off or
     * overflow error)</li>
     * </ol>
     *
     * @param factor scale by which the X,Y coordinates will be multiplied with
     */
    public void scaleXY(double factor) {
        double nx = x.doubleValue() * factor;
        double ny = y.doubleValue() * factor;
        if(x instanceof Double) {
            x = (T) new Double(nx);
            y = (T) new Double(ny);
        } else if(x instanceof Float) {
            x = (T) new Float(nx);
            y = (T) new Float(ny);
        } else if(x instanceof Long) {
            x = (T) new Long((long) nx);
            y = (T) new Long((long) ny);
        } else if(x instanceof Integer) {
            x = (T) new Integer((int) nx);
            y = (T) new Integer((int) ny);
        } else if(x instanceof Short) {
            x = (T) new Short((short) nx);
            y = (T) new Short((short) ny);
        } else if(x instanceof Byte) {
            x = (T) new Byte((byte) nx);
            y = (T) new Byte((byte) ny);
        }
    }

    /**
     * Comparator class for sorting the Point&lt;T&gt; instances. This
     * comparator uses only X,Y coordinations, <strong>not</strong> the
     * intensity. The left-most point will be ordered as first. If two or more
     * points have the same X coordinate the point with the lowest value of Y
     * coordinate will be ordered as first.
     */
    public static class XYComparator implements Comparator<Point> {

        @Override
        public int compare(Point p1, Point p2) {
            double px1 = p1.x.doubleValue(), px2 = p2.x.doubleValue();
            double py1 = p1.y.doubleValue(), py2 = p2.y.doubleValue();
            if(px1 == px2) {
                return (int) ceil(py1 - py2);
            }
            return (int) ceil(px1 - px2);
        }
    }

    public static List<Point> applyRoiMask(Roi roi, List<Point> detections) {
        if(roi == null) {
            return detections;
        } else {
            for(int i = 0; i < detections.size();) {
                Point pt = detections.get(i);
                if(!roi.contains(pt.x.intValue() + roi.getBounds().x, pt.y.intValue() + roi.getBounds().y)) {
                    detections.remove(i);
                } else {
                    i++;
                }
            }
            return detections;
        }
    }
}
