package cz.cuni.lf1.lge.ThunderSTORM.datagen;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.IntegratedSymmetricGaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
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
import static org.apache.commons.math3.util.FastMath.log;

public class DataGenerator {
    
    public static final double FWHM_factor = 2*sqrt(2*log(2));
    
    private RandomDataGenerator rand;
    private Vector<EmitterModel> deleteLater;
    
    public DataGenerator() {
        rand = new RandomDataGenerator();
        rand.reSeed();
        deleteLater = new Vector<EmitterModel>();
    }
    
    public FloatProcessor generatePoissonNoise(int width, int height, double stddev_photons) {
        FloatProcessor img = new FloatProcessor(width, height);
        if(CameraSetupPlugIn.isEmGain) {
            for(int x = 0; x < width; x++)
                for(int y = 0; y < height; y++)
                    img.setf(x, y, (float)rand.nextGamma(rand.nextPoisson(stddev_photons)+0.001, CameraSetupPlugIn.gain) / (float)CameraSetupPlugIn.photons2ADU);
        } else {
            for(int x = 0; x < width; x++)
                for(int y = 0; y < height; y++)
                    img.setf(x, y, (float)rand.nextPoisson(stddev_photons) * (float)CameraSetupPlugIn.photons2ADU);
        }
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
    
    public Vector<EmitterModel> generateMolecules(int width, int height, FloatProcessor mask, double density, Range intensity_photons, Range fwhm) {
        double [] params = new double[PSFModel.Params.PARAMS_LENGTH];
        Arrays.fill(params, 0.0);
        Vector<EmitterModel> molist = new Vector<EmitterModel>();
        double gPpx = Units.NANOMETER_SQUARED.convertTo(Units.MICROMETER_SQUARED, sqr(CameraSetupPlugIn.pixelSize)) * density, p_px, p, fwhm0;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                p_px = gPpx * mask.getf(x, y);  // probability that a molecule appears inside the pixel
                p = rand.nextUniform(0.0, 1.0);
                while(p <= p_px) {
                    fwhm0 = rand.nextUniform(fwhm.from, fwhm.to);
                    params[PSFModel.Params.X] = x + 0.5 + rand.nextUniform(-0.5, +0.5);
                    params[PSFModel.Params.Y] = y + 0.5 + rand.nextUniform(-0.5, +0.5);
                    params[PSFModel.Params.SIGMA] = fwhm0 / FWHM_factor;
                    if(CameraSetupPlugIn.isEmGain) {
                        params[PSFModel.Params.INTENSITY] = rand.nextGamma(rand.nextUniform(intensity_photons.from, intensity_photons.to), CameraSetupPlugIn.gain) / CameraSetupPlugIn.photons2ADU;
                    } else {
                        params[PSFModel.Params.INTENSITY] = rand.nextUniform(intensity_photons.from, intensity_photons.to) / CameraSetupPlugIn.photons2ADU;
                    }
                    PSFModel model = new IntegratedSymmetricGaussianPSF(params[PSFModel.Params.SIGMA]);
                    molist.add(new EmitterModel(model, model.newInstanceFromParams(params), fwhm0));
                    //
                    p_px -= 1.0;
                }
            }
        }
        return molist;
    }

    public ShortProcessor renderFrame(int width, int height, int frame_no, Drift drift, Vector<EmitterModel> molecules, /*FloatProcessor bkg, */FloatProcessor add_noise) {
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
        for(EmitterModel mol : molecules) {
            mol.moveXY(dx, dy);
            if(mol.isOutOfRoi(frame.getRoi())) {    // does the molecule get out of ROI due to the drift?
                deleteLater.add(mol);
            } else {
                mol.generate(frame);
            }
        }
        // remote the out-of-roi molecules
        for(EmitterModel mol : deleteLater) {
            molecules.remove(mol);
        }
        deleteLater.clear();
        // Additive Poisson-distributed noise...we stopped distinguishing read-out
        // and sample noise, because it might be confusing and it would not change
        // the results of simulation anyway.
        frame = ImageProcessor.add((float)CameraSetupPlugIn.offset, frame);
        frame = ImageProcessor.add(frame, add_noise);
        //
        return (ShortProcessor)frame.convertToShort(false);
    }

}
