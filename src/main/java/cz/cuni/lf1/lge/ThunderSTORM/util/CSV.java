package cz.cuni.lf1.lge.ThunderSTORM.util;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import ij.process.FloatProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

import java.io.File;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

// Note: this file should be replaced by a newer version; some of the things used here are deprecated

/**
 * Importing CSV files and translating them into internal plugin objects (FloatProcessor, PSFModel, Point).
 */
public class CSV {

    private static List<String[]> readCsv(String fname) {
        List<String[]> lines = new ArrayList<String[]>();
        LineIterator it = null;
        try {
            it = FileUtils.lineIterator(new File(fname), "UTF-8");
            while (it.hasNext()) lines.add(it.nextLine().split(","));
        } catch (IOException ignored) {
        } finally {
            if (it != null) LineIterator.closeQuietly(it);
        }
        return lines;
    }
    
    /**
     * Read an input CSV file and interpret the data as an image (FloatProcessor).
     *
     * @param fname path to an input CSV file
     * @return a <strong>new instance</strong> of FloatProcessor that contains data from the input CSV file
     * 
     * @throws IOException if the input file specified by {@fname was not found or cannot be opened for reading}
     * @throws InvalidObjectException if the input file does not contain any data
     */
    public static FloatProcessor csv2fp(String fname) throws IOException, InvalidObjectException {
        List<String[]> lines = readCsv(fname);
        
        if(lines.size() < 1) throw new InvalidObjectException("CSV data have to be in a full square/rectangle matrix!");
        if(lines.get(0).length < 1) throw new InvalidObjectException("CSV data have to be in a full square/rectangle matrix!");
        
        float [][] array = new float[lines.get(0).length][lines.size()];
        for(int c = 0; c < array.length; c++) {
            for(int r = 0; r < array[c].length; r++) {
                array[c][r] = Float.parseFloat(lines.get(r)[c]);
            }
        }
        return new FloatProcessor(array);
    }

    /**
     * Read an input CSV file and interpret the data as a set of PSFs (Point Spread Functions).
     * 
     * The input data are supposed to be in the following format:
     * <pre>{@code x,y,sigma,Intensity}</pre>
     * The background parameter is by default set to zero and it is not expected to be
     * found in the input file.
     *
     * @param fname path to an input CSV file
     * @param start_row row offset from which we want to read the data
     * @param start_col column offset from which we want to read the data
     * @return a Vector of PSFs initialized based on the data in CSV file
     * 
     * @throws IOException if the input file specified by {@fname was not found or cannot be opened for reading}
     * @throws InvalidObjectException if the input file does not contain any data
     * 
     * @see PSFModel
     */
    public static Vector<Molecule> csv2psf(String fname, int start_row, int start_col) throws IOException, InvalidObjectException, Exception {
        List<String[]> lines = readCsv(fname);
        
        if(lines.size() < 1) throw new InvalidObjectException("CSV data have to be in a full square/rectangle matrix!");
        if(lines.get(0).length < 1) throw new InvalidObjectException("CSV data have to be in a full square/rectangle matrix!");
        
        Vector<Molecule> loc = new Vector<Molecule>();
        int[] params = new int[]{PSFModel.Params.X, PSFModel.Params.Y, PSFModel.Params.INTENSITY, PSFModel.Params.SIGMA, PSFModel.Params.BACKGROUND};
        for(int r = start_row, rm = lines.size(); r < rm; r++) {
            loc.add(new Molecule(new Params(params, new double[]{
                Float.parseFloat(lines.get(r)[start_col+0]),    // x
                Float.parseFloat(lines.get(r)[start_col+1]),    // y
                Float.parseFloat(lines.get(r)[start_col+3]),    // I
                Float.parseFloat(lines.get(r)[start_col+2]),    // s
                0.0},                                           // b
            false)));
        }
        return loc;
    }
}
