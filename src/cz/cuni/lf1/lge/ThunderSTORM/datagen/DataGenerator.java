package cz.cuni.lf1.lge.ThunderSTORM.datagen;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.filters.BoxFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.ceil;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import ij.process.FloatProcessor;
import org.apache.commons.math3.random.RandomDataGenerator;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqrt;
import ij.process.ShortProcessor;
import java.util.Arrays;
import java.util.Vector;

public class DataGenerator {
    
    private RandomDataGenerator rand;
    private Vector<IntegratedGaussian> deleteLater;
    
    public DataGenerator() {
        rand = new RandomDataGenerator();
        rand.reSeed();
        deleteLater = new Vector<IntegratedGaussian>();
    }
    
    public FloatProcessor generatePoissonNoise(int width, int height, double variance) {
        FloatProcessor img = new FloatProcessor(width, height);
        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
                img.setf(x, y, (float)rand.nextPoisson(variance));
        return img;
    }
    
    public FloatProcessor generateGaussianNoise(int width, int height, double mean, double variance) {
        double sigma = sqrt(variance);
        FloatProcessor img = new FloatProcessor(width, height);
        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
                img.setf(x, y, (float)rand.nextGaussian(mean, sigma));
        return img;
    }
    
    public FloatProcessor generateBackground(int width, int height, Drift drift, Range bkg) {
        // padd the background image; crop the center of the image later, after the drift is applied
        FloatProcessor img = new FloatProcessor(width + 2*(int)ceil(drift.dist), height + 2*(int)ceil(drift.dist));
        for(int x = 0, w = img.getWidth(); x < w; x++)
            for(int y = 0, h = img.getHeight(); y < h; y++)
                img.setf(x, y, (float)rand.nextUniform(bkg.from, bkg.to, true));
        IFilter filter = new BoxFilter(1+2*(int)(((double)Math.min(width, width))/8.0));
        return filter.filterImage(img);
    }
    
    public Vector<IntegratedGaussian> generateMolecules(int width, int height, FloatProcessor mask, double density, Range intensity, Range fwhm) {
        Vector<IntegratedGaussian> molist = new Vector<IntegratedGaussian>();
        double gPpx = Units.NANOMETER_SQUARED.convertTo(Units.MICROMETER_SQUARED, sqr(CameraSetupPlugIn.pixelSize)) * density, p_px, p, dx, dy;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                p_px = gPpx * mask.getf(x, y);  // probability that a molecule appears inside the pixel
                p = rand.nextUniform(0.0, 1.0);
                while(p <= p_px) {
                    dx = rand.nextUniform(-0.5, +0.5);
                    dy = rand.nextUniform(-0.5, +0.5);
                    molist.add(new IntegratedGaussian(rand, x+0.5+dx, y+0.5+dy, intensity, fwhm));
                    p_px -= 1.0;
                }
            }
        }
        return molist;
    }

    public ShortProcessor renderFrame(int width, int height, int frame_no, Drift drift, Vector<IntegratedGaussian> molecules, /*FloatProcessor bkg, */FloatProcessor add_noise) {
        // 1. acquisition (with drift)
        double dx = drift.getDriftX(frame_no), dy = drift.getDriftY(frame_no);
        //FloatProcessor frame = (FloatProcessor)bkg.duplicate();
        //frame.setInterpolationMethod(BILINEAR);
        //frame.translate(dx, dy);
        //frame.setRoi((int)ceil(drift.dist), (int)ceil(drift.dist), width, height);    // see generateBackground
        //frame = (FloatProcessor)frame.crop();
        float [] zeros = new float[width*height];
        Arrays.fill(zeros, 0.0f);
        FloatProcessor frame = new FloatProcessor(width, height, zeros);
        for(IntegratedGaussian mol : molecules) {
            mol.moveXY(dx, dy);
            if(mol.isOutOfRoi(frame.getRoi())) {    // does the molecule get out of ROI due to the drift?
                deleteLater.add(mol);
            } else {
                mol.generate(frame);
            }
        }
        // remote the out-of-roi molecules
        for(IntegratedGaussian mol : deleteLater) {
            molecules.remove(mol);
        }
        deleteLater.clear();
        // Additive Poisson-distributed noise...we stopped distinguishing read-out
        // and sample noise, because it might be confusing and it would not change
        // the results of simulation anyway.
        frame = ImageProcessor.add(frame, add_noise);
        // 2. em gain
        if(CameraSetupPlugIn.isEmGain) {
            frame = ImageProcessor.multiply((float)CameraSetupPlugIn.gain, frame);
        }
        // 3. read-out & camera base-level
        frame = ImageProcessor.add((float)CameraSetupPlugIn.offset, frame);
        //frame = ImageProcessor.add(frame, add_noise);
        //
        return (ShortProcessor)frame.convertToShort(false);
    }

}
