package LMA;

/*
 * Changed 2013-03-03 by Martin Ovesny to fix the indexing error in `combine*` methods
 */


public class ArrayConverter {
	public static class SeparatedData {
		public double[] yDataPoints;
		public double[][] xDataPoints;	
	}
	
	
	public static double[][] asDoubleArray(float[][] a) {
		double[][] result = new double[a.length][];
		for (int i = 0; i < a.length; i++) {
			//a[i].class.getName()
			//if (a[i].getClass().getName())
			result[i] = new double[a[i].length];
			for (int j = 0; j < a[i].length; j++) {
				result[i][j] = (float) a[i][j];
			}
		}
		return result;
	}
	
	public static double[] asDoubleArray(float[] a) {
		if (a == null) return null;
		return asDoubleArray(new float[][] {a})[0];
	}
	
	public static float[] asFloatArray(double[] a) {
		float[] result = new float[a.length];
		for (int i = 0; i < a.length; i++) {
			result[i] = (float) a[i];
		}
		return result;
	}
	
	/**
	 * Separates data from dataPoints[N][K+1] to yDataPoints[N] and xDatapoints[N][K].
	 * <p>
	 * dataPoints[j] = yj xj0 xj1 xj2 ... xj[K-1]<br>
	 * =><br>
	 * yDataPoints    = y0 y1 y2 ... yN<br>
	 * xDataPoints[i] = xi0 xi1 xi2 ... xi[K-1]<br>
	 */
	public static SeparatedData separateMultiDimDataToXY(double[][] dataPoints) {
		SeparatedData result = new SeparatedData();
		result.yDataPoints = new double[dataPoints.length];
		result.xDataPoints = new double[dataPoints.length][dataPoints[0].length - 1];
		for (int i = 0; i < dataPoints.length; i++) {
			result.yDataPoints[i] = dataPoints[i][0];
			for (int j = 1; j < dataPoints[i].length; j++) {
				result.xDataPoints[i][j - 1] = dataPoints[i][j]; 
			}
		}
		return result;
	}
	
	/**
	 * Combines K-dimensional input data into one 2d-array:
	 * <p>
	 * yDataPoints    = y0 y1 y2 ... yN<br>
	 * xDataPoints[i] = xi0 xi1 xi2 ... xi[K-1]<br>
	 * =><br>
	 * result[j] = yj xj0 xj1 xj2 ... xj[K-1]<br>
	 */
	public static double[][] combineMultiDimDataPoints(double[] yDataPoints, double[][] xDataPoints) {
		double[][] result = new double[yDataPoints.length][xDataPoints[0].length + 1];
		for (int i = 0; i < result.length; i++) {
			result[i][0] = yDataPoints[i];
			for (int j = 1; j < result[i].length; j++) {
				result[i][j] = xDataPoints[i][j - 1];
			}
		}
		return result;
	}
	
	/**
	 * Combines K-dimensional input data into one 2d-array:
	 * <p>
	 * yDataPoints    = y0 y1 y2 ... yN<br>
	 * xDataPoints[i] = xi0 xi1 xi2 ... xi[K-1]<br>
	 * =><br>
	 * result[j] = yj xj0 xj1 xj2 ... xj[K-1]<br>
	 */
	public static double[][] combineMultiDimDataPoints(float[] yDataPoints, float[][] xDataPoints) {
		double[][] result = new double[yDataPoints.length][xDataPoints[0].length + 1];
		for (int i = 0; i < result.length; i++) {
			result[i][0] = yDataPoints[i];
			for (int j = 1; j < result[i].length; j++) {
				result[i][j] = xDataPoints[i][j - 1];
			}
		}
		return result;
	}
	
	public static double[][] transpose(double[] a) {
		double[][] result = new double[a.length][1];
		for (int i = 0; i < a.length; i++) {
			result[i][0] = a[i];
		}
		return result;
	}

}
