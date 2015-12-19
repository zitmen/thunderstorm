package cz.cuni.lf1.lge.ThunderSTORM.datagen;

import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.max;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params;
import ij.process.FloatProcessor;
import java.awt.Rectangle;

public class EmitterModel {
    
    public PSFModel model;
    public Molecule molecule;
    public double region;
    
    public EmitterModel(PSFModel model, Molecule molecule) {
        this.model = model;
        this.molecule = molecule;
        //
        region = 0;
        if(molecule.hasParam(Params.LABEL_SIGMA)) {
            region = max(region, molecule.getParam(Params.LABEL_SIGMA));
        }
        if(molecule.hasParam(Params.LABEL_SIGMA1)) {
            region = max(region, molecule.getParam(Params.LABEL_SIGMA1));
        } 
        if(molecule.hasParam(Params.LABEL_SIGMA2)) {
            region = max(region, molecule.getParam(Params.LABEL_SIGMA2));
        } 
        region *= 5;
    }
    
    public void moveXY(double dx, double dy) {
        molecule.setX(molecule.getX() + dx);
        molecule.setY(molecule.getY() + dy);
    }
    
    public boolean isOutOfRoi(Rectangle roi) {
        return (!roi.contains(molecule.getX(), molecule.getY()));
    }
    
    public void generate(FloatProcessor img) {
        double [] params = new double[molecule.values.length];
        for(int i = 0; i < params.length; i++) {
            params[i] = molecule.values[i];
        }
        //
        int width = img.getWidth(), height = img.getHeight();
        for(int x = (int)(molecule.getX() - region), xm = (int)(molecule.getX() + region); x <= xm; x++) {
            if((x < 0) || (x >= width)) continue;
            for(int y = (int)(molecule.getY() - region), ym = (int)(molecule.getY() + region); y <= ym; y++) {
                if((y < 0) || (y >= height)) continue;
                img.setf(x, y, img.getf(x, y) + (float)model.getValue(params, x+0.5, y+0.5));
            }
        }
    }

}
