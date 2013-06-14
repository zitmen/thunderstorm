package cz.cuni.lf1.lge.ThunderSTORM.util;

import au.com.bytecode.opencsv.CSVReader;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.GaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import ij.process.FloatProcessor;
import java.io.FileReader;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.util.List;
import java.util.Vector;

/**
 * Importing CSV files and translating them into internal plugin objects (FloatProcessor, PSFModel, Point).
 */
public class CSV {
    
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
        CSVReader csvReader = new CSVReader(new FileReader(fname));
        List<String[]> lines = csvReader.readAll();
        csvReader.close();
        
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
    public static Vector<PSFInstance> csv2psf(String fname, int start_row, int start_col) throws IOException, InvalidObjectException {
        CSVReader csvReader = new CSVReader(new FileReader(fname));
        List<String[]> lines = csvReader.readAll();
        csvReader.close();
        
        if(lines.size() < 1) throw new InvalidObjectException("CSV data have to be in a full square/rectangle matrix!");
        if(lines.get(0).length < 1) throw new InvalidObjectException("CSV data have to be in a full square/rectangle matrix!");
        
        Vector<PSFInstance> loc = new Vector<PSFInstance>();
        String[] names = new String[]{PSFInstance.X, PSFInstance.Y, "Intensity", PSFInstance.SIGMA, "Background"};
        for(int r = start_row, rm = lines.size(); r < rm; r++) {
            loc.add(new PSFInstance(names, new double[]{
                Float.parseFloat(lines.get(r)[start_col+0]),    // x
                Float.parseFloat(lines.get(r)[start_col+1]),    // y
                Float.parseFloat(lines.get(r)[start_col+3]),    // I
                Float.parseFloat(lines.get(r)[start_col+2]),    // s
                0.0}                                             // b
            ));
        }
        return loc;
    }

    /**
     * Read an input CSV file and interpret the data as a set of Points.
     * 
     * This method actually calls the {@code csv2psf} method first and then
     * converts all {@code PSFModel}s to {@code Point}s, i.e., fills the
     * {@code x,y} coordinates.
     *
     * @param fname path to an input CSV file
     * @param start_row row offset from which we want to read the data
     * @param start_col column offset from which we want to read the data
     * @return a Vector of PSFs initialized based on the data in CSV file
     * 
     * @throws IOException if the input file specified by {@fname was not found or cannot be opened for reading}
     * @throws InvalidObjectException if the input file does not contain any data
     * 
     * @see Point
     */
    public static Vector<Point> csv2point(String fname, int start_row, int start_col) throws IOException {
        Vector<PSFInstance> list = csv2psf(fname, start_row, start_col);
        Vector<Point> points = new Vector<Point>();
        for(PSFInstance psf : list) {
            points.add(new Point(psf.getX(), psf.getY()));
        }
        return points;
    }
}
