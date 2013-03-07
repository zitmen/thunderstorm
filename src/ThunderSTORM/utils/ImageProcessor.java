package ThunderSTORM.utils;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ShortProcessor;

public class ImageProcessor {

    public static ij.process.ImageProcessor newImageProcessor(ij.process.ImageProcessor template, int width, int height) {
        if (template instanceof FloatProcessor) {
            return new FloatProcessor(width, height);
        } else if (template instanceof ShortProcessor) {
            return new ShortProcessor(width, height);
        } else if (template instanceof ByteProcessor) {
            return new ByteProcessor(width, height);
        } else {
            throw new UnsupportedOperationException("The only supported processors are FloatProcessor, ShortProcessor, and ByteProcessor.");
        }
    }

    // align subsequence of an array into its center
    public static void alignArray(int[] line, int start, int end, int fill_left, int fill_right) {
        assert line != null;
        assert start < end;
        assert start >= 0 && end < line.length;
        assert (line.length - (end - start)) % 2 == 0;

        int size = (line.length - (end - start)) / 2;
        for (int i = line.length - 1, im = line.length - size; i >= im; i--) {
            line[i] = fill_right;
        }
        for (int i = line.length - size - 1; i >= size; i--) {
            line[i] = line[i - size];
        }
        for (int i = 0; i < size; i++) {
            line[i] = fill_left;
        }
    }
    
    // [x,y] format
    public static int[][] replicateRow(int[] row, int rep) {
        assert rep > 0;

        int[][] mat = new int[row.length][rep];
        for (int i = 0; i < row.length; i++) {
            for (int j = 0; j < rep; j++) {
                mat[i][j] = row[i];
            }
        }
        return mat;
    }

    // [x,y] format
    public static int[][] replicateColumn(int[] col, int rep) {
        assert rep > 0;

        int[][] mat = new int[rep][col.length];
        for (int i = 0; i < col.length; i++) {
            for (int j = 0; j < rep; j++) {
                mat[j][i] = col[i];
            }
        }
        return mat;
    }
}
