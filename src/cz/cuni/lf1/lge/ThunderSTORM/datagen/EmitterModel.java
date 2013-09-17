package cz.cuni.lf1.lge.ThunderSTORM.datagen;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import ij.process.FloatProcessor;
import java.awt.Rectangle;

public class EmitterModel {
    
    public double fwhm;
    public PSFModel model;
    public Molecule molecule;
    
    public EmitterModel(PSFModel model, Molecule molecule, double fwhm) {
        this.model = model;
        this.molecule = molecule;
        this.fwhm = fwhm;
    }
    
    public void moveXY(double dx, double dy) {
        molecule.setX(molecule.getX() + dx);
        molecule.setY(molecule.getY() + dy);
    }
    
    public boolean isOutOfRoi(Rectangle roi) {
        return (roi.contains(molecule.getX(), molecule.getY()) == false);
    }
    
    public void generate(FloatProcessor img) {
        double [] params = new double[molecule.values.size()];
        for(int i = 0; i < params.length; i++) {
            params[i] = molecule.values.elementAt(i);
        }
        //
        int width = img.getWidth(), height = img.getHeight();
        for(int x = (int)(molecule.getX() - 2*fwhm), xm = (int)(molecule.getX() + 2*fwhm); x <= xm; x++) {
            if((x < 0) || (x >= width)) continue;
            for(int y = (int)(molecule.getY() - 2*fwhm), ym = (int)(molecule.getY() + 2*fwhm); y <= ym; y++) {
                if((y < 0) || (y >= height)) continue;
                img.setf(x, y, img.getf(x, y) + (float)model.getValue(params, x+0.5, y+0.5));
            }
        }
    }

}
