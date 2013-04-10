package fi.utu.jaolho.LMA;

import gov.nist.math.Jama.Matrix;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;

/**
 * The matrix to be used in LMA.
 * 
 * Note: the original interface provided by LMA lib was replaced by its
 *       implementation
 */
public class LMAMatrix extends Matrix {

    public static class InvertException extends RuntimeException {

        public InvertException(String message) {
            super(message);
        }
    }

    public LMAMatrix(double[][] elements) {
        super(elements);
    }

    public LMAMatrix(int rows, int cols) {
        super(rows, cols);
    }

    /**
     * Inverts the matrix for solving linear equations for parameter increments.
     */
    public void invert() throws LMAMatrix.InvertException {
        try {
            Matrix m = inverse();
            setMatrix(0, this.getRowDimension() - 1, 0, getColumnDimension() - 1, m);
        } catch (RuntimeException e) {
            StringWriter s = new StringWriter();
            PrintWriter p = new PrintWriter(s);
            p.println(e.getMessage());
            p.println("Inversion failed for matrix:");
            this.print(p, NumberFormat.getInstance(), 5);
            throw new LMAMatrix.InvertException(s.toString());
        }
    }

    /**
     * Set the value of a matrix element.
     */
    public void setElement(int row, int col, double value) {
        set(row, col, value);
    }

    /**
     * Get the value of a matrix element.
     */
    public double getElement(int row, int col) {
        return get(row, col);
    }

    /**
     * Multiplies this matrix with an array (result = this * vector). The
     * lengths of the arrays must be equal to the number of rows in the matrix.
     *
     * @param vector The array to be multiplied with the matrix.
     * @param result The result of the multiplication will be put here.
     */
    public void multiply(double[] vector, double[] result) {
        for (int i = 0; i < this.getRowDimension(); i++) {
            result[i] = 0;
            for (int j = 0; j < this.getColumnDimension(); j++) {
                result[i] += this.getElement(i, j) * vector[j];
            }
        }
    }
}
