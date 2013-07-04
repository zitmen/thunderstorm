package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.util.Math;
import ij.process.FloatProcessor;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;

public class RetVal {
    
    private Double val = null;
    private Double [] vec = null;
    private FloatProcessor mat = null;
    
    public RetVal(boolean value) {
        if(value) {
          val = new Double(1.0);
        } else {
          val = new Double(0.0);
        }
    }
    
    public RetVal(Double value) {
        val = value;
    }
    
    public RetVal(Double [] vector) {
      vec = vector;
    }
    
    public RetVal(FloatProcessor matrix) {
        mat = matrix;
    }
    
    public boolean isValue() {
        return (val != null);
    }
    
    public boolean isVector() {
        return (vec != null);
    }
    
    public boolean isMatrix() {
        return (mat != null);
    }

    public Object get() {
        if(isValue()) return val;
        if(isVector()) return vec;
        if(isMatrix()) return mat;
        assert(true);
        return null;
    }
    
    public RetVal add(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar + scalar
                return new RetVal(val.doubleValue() + b.val.doubleValue());
            } else if(b.isVector()) { // scalar + vector
                return new RetVal(Math.add(val.doubleValue(), b.vec));
            } else {    // scalar + matrix
                return new RetVal(ImageProcessor.add(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector + scalar
                return new RetVal(Math.add(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector + vector
                return new RetVal(Math.add(vec, b.vec));
            } else {    // vector + matrix
                throw new IllegalArgumentException("Operation vector+matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix + scalar
                return new RetVal(ImageProcessor.add(val.floatValue(), b.mat));
            } else if(b.isVector()) {   // matrix + vector
                throw new IllegalArgumentException("Operation matrix+vector is not supported!");
            } else {    // matrix + matrix
                return new RetVal(ImageProcessor.add(mat, b.mat));
            }
        }
    }
    
    public RetVal sub(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar - scalar
                return new RetVal(val.doubleValue() - b.val.doubleValue());
            } else if(b.isVector()) {   // scalar - vector
                return new RetVal(Math.sub(val, b.vec));
            } else {    // scalar - matrix
                return new RetVal(ImageProcessor.subtract(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector - scalar
                return new RetVal(Math.sub(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector - vector
                return new RetVal(Math.sub(vec, b.vec));
            } else {    // vector - matrix
                throw new IllegalArgumentException("Operation vector-matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix - scalar
                return new RetVal(ImageProcessor.subtract(b.mat, val.floatValue()));
            } else if(b.isVector()) {   // matrix - vector
                throw new IllegalArgumentException("Operation matrix-vector is not supported!");
            } else {    // matrix - matrix
                return new RetVal(ImageProcessor.subtract(mat, b.mat));
            }
        }
    }
    
    public RetVal mul(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar * scalar
                return new RetVal(val.doubleValue() * b.val.doubleValue());
            } else if(b.isVector()) {   // scalar * vector
                return new RetVal(Math.mul(val, b.vec));
            } else {    // scalar * matrix
                return new RetVal(ImageProcessor.multiply(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector * scalar
                return new RetVal(Math.mul(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector * vector
                return new RetVal(Math.mul(vec, b.vec));
            } else {    // vector * matrix
                throw new IllegalArgumentException("Operation vector*matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix * scalar
                return new RetVal(ImageProcessor.multiply(val.floatValue(), b.mat));
            } else if(b.isVector()) {   // matrix * vector
                throw new IllegalArgumentException("Operation matrix*vector is not supported!");
            } else {    // matrix * matrix
                return new RetVal(ImageProcessor.multiply(mat, b.mat));
            }
        }
    }
    
    public RetVal div(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar / scalar
                return new RetVal(val.doubleValue() / b.val.doubleValue());
            } else if(b.isVector()) {   // scalar / vector
                return new RetVal(Math.div(val, b.vec));
            } else {    // scalar / matrix
                return new RetVal(ImageProcessor.divide(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector / scalar
                return new RetVal(Math.div(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector / vector
                return new RetVal(Math.div(vec, b.vec));
            } else {    // vector / matrix
                throw new IllegalArgumentException("Operation vector/matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix / scalar
                return new RetVal(ImageProcessor.divide(b.mat, val.floatValue()));
            } else if(b.isVector()) {   // matrix / vector
                throw new IllegalArgumentException("Operation matrix/vector is not supported!");
            } else {    // matrix / matrix
                return new RetVal(ImageProcessor.divide(mat, b.mat));
            }
        }
    }
    
    public RetVal mod(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar % scalar
                return new RetVal(val.doubleValue() % b.val.doubleValue());
            } else if(b.isVector()) {   // scalar % vector
                return new RetVal(Math.mod(val, b.vec));
            } else {    // scalar % matrix
                return new RetVal(ImageProcessor.modulo(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector % scalar
                return new RetVal(Math.mod(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector % vector
                return new RetVal(Math.mod(vec, b.vec));
            } else {    // vector % matrix
                throw new IllegalArgumentException("Operation vector%matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix % scalar
                return new RetVal(ImageProcessor.modulo(b.mat, val.floatValue()));
            } else if(b.isVector()) {   // matrix % vector
                throw new IllegalArgumentException("Operation matrix%vector is not supported!");
            } else {    // matrix % matrix
                return new RetVal(ImageProcessor.modulo(mat, b.mat));
            }
        }
    }
    
    public RetVal pow(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar ^ scalar
                return new RetVal(Math.pow(val.doubleValue(), b.val.doubleValue()));
            } else if(b.isVector()) {   // scalar ^ vector
                throw new IllegalArgumentException("Operation scalar^vector is not supported!");
            } else {    // scalar ^ matrix
                throw new IllegalArgumentException("Operation scalar^matrix is not supported!");
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector ^ scalar
                return new RetVal(Math.pow(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector ^ vector
                throw new IllegalArgumentException("Operation vector^vector is not supported!");
            } else {    // vector ^ matrix
                throw new IllegalArgumentException("Operation vector^matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix ^ scalar
                return new RetVal(ImageProcessor.power(b.mat, val.floatValue()));
            } else if(b.isVector()) {   // matrix ^ vector
                throw new IllegalArgumentException("Operation matrix^vector is not supported!");
            } else {    // matrix ^ matrix
                throw new IllegalArgumentException("Operation matrix^matrix is not supported!");
            }
        }
    }
    
    public RetVal and(RetVal b) {
        if(isValue() && b.isValue()) {
            return new RetVal((val.doubleValue() != 0.0) && (b.val.doubleValue() != 0.0));
        } else if(isVector() && b.isVector()) {
            return new RetVal(Math.logAnd(vec, b.vec));
        } else if(isMatrix()&& b.isMatrix()) {
            return new RetVal(ImageProcessor.logAnd(mat, b.mat));
        } else {
            throw new IllegalArgumentException("Operator `&` can be used only with variables of the same type, i.e., scalar&scalar, vector&vector, or matrix&matrix!");
        }
    }
    
    public RetVal or(RetVal b) {
        if(isValue() && b.isValue()) {
            return new RetVal((val.doubleValue() != 0.0) || (b.val.doubleValue() != 0.0));
        } else if(isVector() && b.isVector()) {
            return new RetVal(Math.logOr(vec, b.vec));
        } else if(isMatrix()&& b.isMatrix()) {
            return new RetVal(ImageProcessor.logOr(mat, b.mat));
        } else {
            throw new IllegalArgumentException("Operator `|` can be used only with variables of the same type, i.e., scalar&scalar, vector&vector, or matrix&matrix!");
        }
    }
    
    public RetVal lt(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar < scalar
                return new RetVal(val.doubleValue() < b.val.doubleValue());
            } else if(b.isVector()) { // scalar < vector
                return new RetVal(Math.relLt(val, b.vec));
            } else {    // scalar < matrix
                return new RetVal(ImageProcessor.relLt(val, b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector < scalar
                return new RetVal(Math.relLt(vec, b.val));
            } else if(b.isVector()) {   // vector < vector
                return new RetVal(Math.relLt(vec, b.vec));
            } else {    // vector < matrix
                throw new IllegalArgumentException("Operation vector<matrix is not supported!");
            }
        } else {  // matrix
            if(b.isValue()) {   // matrix < scalar
                return new RetVal(ImageProcessor.relLt(mat, b.val));
            } else if(b.isVector()) { // matrix < vector
                throw new IllegalArgumentException("Operation matrix<vector is not supported!");
            } else {    // matrix < matrix
                return new RetVal(ImageProcessor.relLt(mat, b.mat));
            }
        }
    }
    
    public RetVal gt(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar > scalar
                return new RetVal(val.doubleValue() > b.val.doubleValue());
            } else if(b.isVector()) { // scalar > vector
                return new RetVal(Math.relGt(val, b.vec));
            } else {    // scalar > matrix
                return new RetVal(ImageProcessor.relGt(val, b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector > scalar
                return new RetVal(Math.relGt(vec, b.val));
            } else if(b.isVector()) {   // vector > vector
                return new RetVal(Math.relGt(vec, b.vec));
            } else {    // vector > matrix
                throw new IllegalArgumentException("Operation vector>matrix is not supported!");
            }
        } else {  // matrix
            if(b.isValue()) {   // matrix > scalar
                return new RetVal(ImageProcessor.relGt(mat, b.val));
            } else if(b.isVector()) { // matrix > vector
                throw new IllegalArgumentException("Operation matrix>vector is not supported!");
            } else {    // matrix > matrix
                return new RetVal(ImageProcessor.relGt(mat, b.mat));
            }
        }
    }
    
    public RetVal eq(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar == scalar
                return new RetVal(val.doubleValue() == b.val.doubleValue());
            } else if(b.isVector()) {   // scalar == vector
                return new RetVal(Math.relEq(val, b.vec));
            } else {    // scalar == matrix
                return new RetVal(ImageProcessor.relEq(val, b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector == scalar
                return new RetVal(Math.relEq(vec, b.val));
            } else if(b.isVector()) {   // vector == vector
                return new RetVal(Math.relEq(vec, b.vec));
            } else {    // vector == matrix
                throw new IllegalArgumentException("Operation vector=matrix is not supported!");
            }
        } else {
            if(b.isValue()) {  // matrix == scalar
                return new RetVal(ImageProcessor.relEq(mat, b.val));
            } else if(b.isVector()) {   // matrix == vector
                throw new IllegalArgumentException("Operation matrix=vector is not supported!");
            } else {   // matrix == matrix
                return new RetVal(ImageProcessor.relEq(mat, b.mat));
            }
        }
    }

}
