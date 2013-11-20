package cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.SyntaxTree;

import cz.cuni.lf1.lge.ThunderSTORM.FormulaParser.FormulaParserException;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import ij.process.FloatProcessor;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageMath;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;

public class RetVal {

    private Double val = null;
    private Double[] vec = null;
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

    public RetVal(Double[] vector) {
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
        if(isValue()) {
            return val;
        }
        if(isVector()) {
            return vec;
        }
        if(isMatrix()) {
            return mat;
        }
        assert (true);
        return null;
    }

    public RetVal add(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar + scalar
                return new RetVal(val.doubleValue() + b.val.doubleValue());
            } else if(b.isVector()) { // scalar + vector
                return new RetVal(MathProxy.add(val.doubleValue(), b.vec));
            } else {    // scalar + matrix
                return new RetVal(ImageMath.add(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector + scalar
                return new RetVal(VectorMath.add(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector + vector
                return new RetVal(VectorMath.add(vec, b.vec));
            } else {    // vector + matrix
                throw new FormulaParserException("Operation vector+matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix + scalar
                return new RetVal(ImageMath.add(val.floatValue(), b.mat));
            } else if(b.isVector()) {   // matrix + vector
                throw new FormulaParserException("Operation matrix+vector is not supported!");
            } else {    // matrix + matrix
                return new RetVal(ImageMath.add(mat, b.mat));
            }
        }
    }

    public RetVal sub(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar - scalar
                return new RetVal(val.doubleValue() - b.val.doubleValue());
            } else if(b.isVector()) {   // scalar - vector
                return new RetVal(VectorMath.sub(val, b.vec));
            } else {    // scalar - matrix
                return new RetVal(ImageMath.subtract(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector - scalar
                return new RetVal(VectorMath.sub(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector - vector
                return new RetVal(VectorMath.sub(vec, b.vec));
            } else {    // vector - matrix
                throw new FormulaParserException("Operation vector-matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix - scalar
                return new RetVal(ImageMath.subtract(mat, b.val.floatValue()));
            } else if(b.isVector()) {   // matrix - vector
                throw new FormulaParserException("Operation matrix-vector is not supported!");
            } else {    // matrix - matrix
                return new RetVal(ImageMath.subtract(mat, b.mat));
            }
        }
    }

    public RetVal mul(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar * scalar
                return new RetVal(val.doubleValue() * b.val.doubleValue());
            } else if(b.isVector()) {   // scalar * vector
                return new RetVal(VectorMath.mul(val, b.vec));
            } else {    // scalar * matrix
                return new RetVal(ImageMath.multiply(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector * scalar
                return new RetVal(VectorMath.mul(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector * vector
                return new RetVal(VectorMath.mul(vec, b.vec));
            } else {    // vector * matrix
                throw new FormulaParserException("Operation vector*matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix * scalar
                return new RetVal(ImageMath.multiply(val.floatValue(), b.mat));
            } else if(b.isVector()) {   // matrix * vector
                throw new FormulaParserException("Operation matrix*vector is not supported!");
            } else {    // matrix * matrix
                return new RetVal(ImageMath.multiply(mat, b.mat));
            }
        }
    }

    public RetVal div(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar / scalar
                return new RetVal(val.doubleValue() / b.val.doubleValue());
            } else if(b.isVector()) {   // scalar / vector
                return new RetVal(VectorMath.div(val, b.vec));
            } else {    // scalar / matrix
                return new RetVal(ImageMath.divide(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector / scalar
                return new RetVal(VectorMath.div(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector / vector
                return new RetVal(VectorMath.div(vec, b.vec));
            } else {    // vector / matrix
                throw new FormulaParserException("Operation vector/matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix / scalar
                return new RetVal(ImageMath.divide(mat, b.val.floatValue()));
            } else if(b.isVector()) {   // matrix / vector
                throw new FormulaParserException("Operation matrix/vector is not supported!");
            } else {    // matrix / matrix
                return new RetVal(ImageMath.divide(mat, b.mat));
            }
        }
    }

    public RetVal mod(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar % scalar
                return new RetVal(val.doubleValue() % b.val.doubleValue());
            } else if(b.isVector()) {   // scalar % vector
                return new RetVal(VectorMath.mod(val, b.vec));
            } else {    // scalar % matrix
                return new RetVal(ImageMath.modulo(val.floatValue(), b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector % scalar
                return new RetVal(VectorMath.mod(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector % vector
                return new RetVal(VectorMath.mod(vec, b.vec));
            } else {    // vector % matrix
                throw new FormulaParserException("Operation vector%matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix % scalar
                return new RetVal(ImageMath.modulo(mat, b.val.floatValue()));
            } else if(b.isVector()) {   // matrix % vector
                throw new FormulaParserException("Operation matrix%vector is not supported!");
            } else {    // matrix % matrix
                return new RetVal(ImageMath.modulo(mat, b.mat));
            }
        }
    }

    public RetVal pow(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar ^ scalar
                return new RetVal(MathProxy.pow(val.doubleValue(), b.val.doubleValue()));
            } else if(b.isVector()) {   // scalar ^ vector
                throw new FormulaParserException("Operation scalar^vector is not supported!");
            } else {    // scalar ^ matrix
                throw new FormulaParserException("Operation scalar^matrix is not supported!");
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector ^ scalar
                return new RetVal(VectorMath.pow(vec, b.val.doubleValue()));
            } else if(b.isVector()) { // vector ^ vector
                throw new FormulaParserException("Operation vector^vector is not supported!");
            } else {    // vector ^ matrix
                throw new FormulaParserException("Operation vector^matrix is not supported!");
            }
        } else {
            if(b.isValue()) {   // matrix ^ scalar
                return new RetVal(ImageMath.power(mat, b.val.floatValue()));
            } else if(b.isVector()) {   // matrix ^ vector
                throw new FormulaParserException("Operation matrix^vector is not supported!");
            } else {    // matrix ^ matrix
                throw new FormulaParserException("Operation matrix^matrix is not supported!");
            }
        }
    }

    public RetVal and(RetVal b) {
        if(isValue() && b.isValue()) {
            return new RetVal((val.doubleValue() != 0.0) && (b.val.doubleValue() != 0.0));
        } else if(isVector() && b.isVector()) {
            return new RetVal(VectorMath.logAnd(vec, b.vec));
        } else if(isMatrix() && b.isMatrix()) {
            return new RetVal(ImageMath.logAnd(mat, b.mat));
        } else {
            throw new FormulaParserException("Operator `&` can be used only with variables of the same type, i.e., scalar&scalar, vector&vector, or matrix&matrix!");
        }
    }

    public RetVal or(RetVal b) {
        if(isValue() && b.isValue()) {
            return new RetVal((val.doubleValue() != 0.0) || (b.val.doubleValue() != 0.0));
        } else if(isVector() && b.isVector()) {
            return new RetVal(VectorMath.logOr(vec, b.vec));
        } else if(isMatrix() && b.isMatrix()) {
            return new RetVal(ImageMath.logOr(mat, b.mat));
        } else {
            throw new FormulaParserException("Operator `|` can be used only with variables of the same type, i.e., scalar&scalar, vector&vector, or matrix&matrix!");
        }
    }

    public RetVal lt(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar < scalar
                return new RetVal(val.doubleValue() < b.val.doubleValue());
            } else if(b.isVector()) { // scalar < vector
                return new RetVal(VectorMath.relLt(val, b.vec));
            } else {    // scalar < matrix
                return new RetVal(ImageMath.relLt(val, b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector < scalar
                return new RetVal(VectorMath.relLt(vec, b.val));
            } else if(b.isVector()) {   // vector < vector
                return new RetVal(VectorMath.relLt(vec, b.vec));
            } else {    // vector < matrix
                throw new FormulaParserException("Operation vector<matrix is not supported!");
            }
        } else {  // matrix
            if(b.isValue()) {   // matrix < scalar
                return new RetVal(ImageMath.relLt(mat, b.val));
            } else if(b.isVector()) { // matrix < vector
                throw new FormulaParserException("Operation matrix<vector is not supported!");
            } else {    // matrix < matrix
                return new RetVal(ImageMath.relLt(mat, b.mat));
            }
        }
    }

    public RetVal gt(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar > scalar
                return new RetVal(val.doubleValue() > b.val.doubleValue());
            } else if(b.isVector()) { // scalar > vector
                return new RetVal(VectorMath.relGt(val, b.vec));
            } else {    // scalar > matrix
                return new RetVal(ImageMath.relGt(val, b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector > scalar
                return new RetVal(VectorMath.relGt(vec, b.val));
            } else if(b.isVector()) {   // vector > vector
                return new RetVal(VectorMath.relGt(vec, b.vec));
            } else {    // vector > matrix
                throw new FormulaParserException("Operation vector>matrix is not supported!");
            }
        } else {  // matrix
            if(b.isValue()) {   // matrix > scalar
                return new RetVal(ImageMath.relGt(mat, b.val));
            } else if(b.isVector()) { // matrix > vector
                throw new FormulaParserException("Operation matrix>vector is not supported!");
            } else {    // matrix > matrix
                return new RetVal(ImageMath.relGt(mat, b.mat));
            }
        }
    }

    public RetVal eq(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar == scalar
                return new RetVal(val.doubleValue() == b.val.doubleValue());
            } else if(b.isVector()) {   // scalar == vector
                return new RetVal(VectorMath.relEq(val, b.vec));
            } else {    // scalar == matrix
                return new RetVal(ImageMath.relEq(val, b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector == scalar
                return new RetVal(VectorMath.relEq(vec, b.val));
            } else if(b.isVector()) {   // vector == vector
                return new RetVal(VectorMath.relEq(vec, b.vec));
            } else {    // vector == matrix
                throw new FormulaParserException("Operation vector=matrix is not supported!");
            }
        } else {
            if(b.isValue()) {  // matrix == scalar
                return new RetVal(ImageMath.relEq(mat, b.val));
            } else if(b.isVector()) {   // matrix == vector
                throw new FormulaParserException("Operation matrix=vector is not supported!");
            } else {   // matrix == matrix
                return new RetVal(ImageMath.relEq(mat, b.mat));
            }
        }
    }

    public RetVal neq(RetVal b) {
        if(isValue()) {
            if(b.isValue()) {   // scalar != scalar
                return new RetVal(val.doubleValue() != b.val.doubleValue());
            } else if(b.isVector()) {   // scalar != vector
                return new RetVal(VectorMath.relNeq(val, b.vec));
            } else {    // scalar != matrix
                return new RetVal(ImageMath.relNeq(val, b.mat));
            }
        } else if(isVector()) {
            if(b.isValue()) {   // vector != scalar
                return new RetVal(VectorMath.relNeq(vec, b.val));
            } else if(b.isVector()) {   // vector != vector
                return new RetVal(VectorMath.relNeq(vec, b.vec));
            } else {    // vector != matrix
                throw new FormulaParserException("Operation vector=matrix is not supported!");
            }
        } else {
            if(b.isValue()) {  // matrix != scalar
                return new RetVal(ImageMath.relNeq(mat, b.val));
            } else if(b.isVector()) {   // matrix != vector
                throw new FormulaParserException("Operation matrix=vector is not supported!");
            } else {   // matrix != matrix
                return new RetVal(ImageMath.relNeq(mat, b.mat));
            }
        }
    }

}
