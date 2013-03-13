package ThunderSTORM.utils;

import static java.lang.Math.round;

public class Point<T extends Number> {

    public T x, y, val;

    public Point() {
        this.x = null;
        this.y = null;
        this.val = null;
    }

    public Point(T x, T y, T val) {
        this.x = x;
        this.y = y;
        this.val = val;
    }

    public Point(T x, T y) {
        this.x = x;
        this.y = y;
        this.val = null;
    }
    
    public T getX() {
        return x;
    }

    public T getY() {
        return y;
    }

    public T getVal() {
        return val;
    }

    public void setX(T x) {
        this.x = x;
    }

    public void setY(T y) {
        this.y = y;
    }

    public void setVal(T val) {
        this.val = val;
    }

    public void setLocation(T x, T y) {
        this.x = x;
        this.y = y;
    }

    public void set(T x, T y, T val) {
        this.x = x;
        this.y = y;
        this.val = val;
    }
    
    public Point<Integer> roundToInteger() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return new Point<>(round(this.x.floatValue()), round(this.y.floatValue()));
        } else {
            return new Point<>(round(this.x.floatValue()), round(this.y.floatValue()), round(this.val.floatValue()));
        }
    }
    
    public Point<Long> roundToLong() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return new Point<>(round(this.x.doubleValue()), round(this.y.doubleValue()));
        } else {
            return new Point<>(round(this.x.doubleValue()), round(this.y.doubleValue()), round(this.val.doubleValue()));
        }
    }

    public Point<Integer> toInteger() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return new Point<>(this.x.intValue(), this.y.intValue());
        } else {
            return new Point<>(this.x.intValue(), this.y.intValue(), this.val.intValue());
        }
    }

    public Point<Float> toFloat() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return new Point<>(this.x.floatValue(), this.y.floatValue());
        } else {
            return new Point<>(this.x.floatValue(), this.y.floatValue(), this.val.floatValue());
        }
    }

    public Point<Byte> toByte() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return new Point<>(this.x.byteValue(), this.y.byteValue());
        } else {
            return new Point<>(this.x.byteValue(), this.y.byteValue(), this.val.byteValue());
        }
    }

    public Point<Double> toDouble() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return new Point<>(this.x.doubleValue(), this.y.doubleValue());
        } else {
            return new Point<>(this.x.doubleValue(), this.y.doubleValue(), this.val.doubleValue());
        }
    }

    public Point<Long> toLong() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return new Point<>(this.x.longValue(), this.y.longValue());
        } else {
            return new Point<>(this.x.longValue(), this.y.longValue(), this.val.longValue());
        }
    }

    public Point<Short> toShort() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return new Point<>(this.x.shortValue(), this.y.shortValue());
        } else {
            return new Point<>(this.x.shortValue(), this.y.shortValue(), this.val.shortValue());
        }
    }

    @Override
    public String toString() {
        assert ((x != null) && (y != null));

        if (val == null) {
            return "[" + x.toString() + "," + y.toString() + "]";
        } else {
            return "[" + x.toString() + "," + y.toString() + "]=" + val.toString();
        }
    }
}
