package cz.cuni.lf1.lge.ThunderSTORM.util;

import au.com.bytecode.opencsv.CSVReader;
import com.sun.media.sound.InvalidDataException;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.GaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import ij.process.FloatProcessor;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

public class CSV {
    
    public static FloatProcessor csv2fp(String fname) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(fname));
        List<String[]> lines = csvReader.readAll();
        csvReader.close();
        
        if(lines.size() < 1) throw new InvalidDataException("CSV data have to be in a full square/rectangle matrix!");
        if(lines.get(0).length < 1) throw new InvalidDataException("CSV data have to be in a full square/rectangle matrix!");
        
        float [][] array = new float[lines.get(0).length][lines.size()];
        for(int c = 0; c < array.length; c++) {
            for(int r = 0; r < array[c].length; r++) {
                array[c][r] = Float.parseFloat(lines.get(r)[c]);
            }
        }
        return new FloatProcessor(array);
    }

    public static Vector<PSF> csv2psf(String fname, int start_row, int start_col) throws IOException {
        CSVReader csvReader = new CSVReader(new FileReader(fname));
        List<String[]> lines = csvReader.readAll();
        csvReader.close();
        
        if(lines.size() < 1) throw new InvalidDataException("CSV data have to be in a full square/rectangle matrix!");
        if(lines.get(0).length < 1) throw new InvalidDataException("CSV data have to be in a full square/rectangle matrix!");
        
        Vector<PSF> loc = new Vector<PSF>();
        for(int r = start_row, rm = lines.size(); r < rm; r++) {
            loc.add(new GaussianPSF(
                Float.parseFloat(lines.get(r)[start_col+0]),    // x
                Float.parseFloat(lines.get(r)[start_col+1]),    // y
                Float.parseFloat(lines.get(r)[start_col+3]),    // I
                Float.parseFloat(lines.get(r)[start_col+2]),    // s
                0.0                                             // b
            ));
        }
        return loc;
    }

    public static Vector<Point> csv2point(String fname, int start_row, int start_col) throws IOException {
        Vector<PSF> list = csv2psf(fname, start_row, start_col);
        Vector<Point> points = new Vector<Point>();
        for(PSF psf : list) {
            points.add(new Point(psf.xpos, psf.ypos));
        }
        return points;
    }
}
