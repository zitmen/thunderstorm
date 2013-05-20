package cz.cuni.lf1.lge.ThunderSTORM.thresholding.SyntaxTree;

import ij.process.FloatProcessor;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;

public class RetVal {
    
    private Float val = null;
    private FloatProcessor mat = null;
    
    public RetVal(Float value) {
        val = value;
    }
    
    public RetVal(FloatProcessor matrix) {
        mat = matrix;
    }
    
    public boolean isValue() {
        return (val != null);
    }
    
    public boolean isMatrix() {
        return (mat != null);
    }

    public Object get() {
        if(isValue()) return val;
        return mat;
    }
    
    public RetVal add(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar + scalar
                return new RetVal(val.floatValue() + b.val.floatValue());
            } else {    // scalar + matrix
                return new RetVal(ImageProcessor.add(val.floatValue(), b.mat));
            }
        } else {
            if(b.isValue()) {   // matrix + scalar
                return new RetVal(ImageProcessor.add(val.floatValue(), b.mat));
            } else {    // matrix + matrix
                return new RetVal(ImageProcessor.add(mat, b.mat));
            }
        }
    }
    
    public RetVal sub(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar - scalar
                return new RetVal(val.floatValue() - b.val.floatValue());
            } else {    // scalar - matrix
                return new RetVal(ImageProcessor.subtract(val.floatValue(), b.mat));
            }
        } else {
            if(b.isValue()) {   // matrix - scalar
                return new RetVal(ImageProcessor.subtract(b.mat, val.floatValue()));
            } else {    // matrix - matrix
                return new RetVal(ImageProcessor.subtract(mat, b.mat));
            }
        }
    }
    
    public RetVal mul(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar * scalar
                return new RetVal(val.floatValue()* b.val.floatValue());
            } else {    // scalar * matrix
                return new RetVal(ImageProcessor.multiply(val.floatValue(), b.mat));
            }
        } else {
            if(b.isValue()) {   // matrix * scalar
                return new RetVal(ImageProcessor.multiply(val.floatValue(), b.mat));
            } else {    // matrix * matrix
                return new RetVal(ImageProcessor.multiply(mat, b.mat));
            }
        }
    }
    
    public RetVal div(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar / scalar
                return new RetVal(val.floatValue() / b.val.floatValue());
            } else {    // scalar / matrix
                return new RetVal(ImageProcessor.divide(val.floatValue(), b.mat));
            }
        } else {
            if(b.isValue()) {   // matrix / scalar
                return new RetVal(ImageProcessor.divide(b.mat, val.floatValue()));
            } else {    // matrix / matrix
                return new RetVal(ImageProcessor.divide(mat, b.mat));
            }
        }
    }
    
    public RetVal pow(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar ^ scalar
                return new RetVal((float)Math.pow(val.doubleValue(), b.val.doubleValue()));
            } else {    // scalar ^ matrix
                throw new IllegalArgumentException("Operation scalar^matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix ^ scalar
                return new RetVal(ImageProcessor.power(b.mat, val.floatValue()));
            } else {    // matrix ^ matrix
                throw new IllegalArgumentException("Operation matrix^matrix is not supported!");
            }
        }
    }

}
