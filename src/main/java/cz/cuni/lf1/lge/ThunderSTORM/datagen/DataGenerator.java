package cz.cuni.lf1.lge.ThunderSTORM.datagen;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.ui.IPsfUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.BoxFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageMath;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.ceil;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import ij.process.FloatProcessor;
import org.apache.commons.math3.random.RandomDataGenerator;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy.sqrt;
import ij.process.ShortProcessor;
import java.util.Vector;

public class DataGenerator {
    
    private final RandomDataGenerator rand;
    private final Vector<EmitterModel> deleteLater;
    
    private double getNextUniform(double lower, double upper) {
        //return ((lower == upper) ? lower : rand.nextUniform(lower, upper));
        if(lower == upper) {
            return lower;
        } else {
            try {
                return rand.nextUniform(lower, upper);
            } catch(Exception ex) {
                int a = 5;
                return a;
            }
        }
    }
    
    public DataGenerator() {
        rand = new RandomDataGenerator();
        rand.reSeed();
        deleteLater = new Vector<EmitterModel>();
    }
    
    public FloatProcessor generatePoissonNoise(int width, int height, double stddev_photons) {
        FloatProcessor img = new FloatProcessor(width, height);
            for(int x = 0; x < width; x++)
                for(int y = 0; y < height; y++)
                    img.setf(x, y, (float)(rand.nextPoisson(stddev_photons))) ;
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
                img.setf(x, y, (float)getNextUniform(bkg.from, bkg.to));
        IFilter filter = new BoxFilter(1+2*(int)(((double)Math.min(width, width))/8.0));
        return filter.filterImage(img);
    }

    public Vector<EmitterModel> generateMolecules(int width, int height, FloatProcessor mask, double density, Range intensity_photons, IPsfUI psf) {
        MoleculeDescriptor descriptor = null;
        double[] params = new double[PSFModel.Params.PARAMS_LENGTH];
        Vector<EmitterModel> molist = new Vector<EmitterModel>();
        double gPpx = Units.NANOMETER_SQUARED.convertTo(Units.MICROMETER_SQUARED, sqr(CameraSetupPlugIn.getPixelSize())*width*height/(mask.getWidth()*mask.getHeight())) * density, p_px, p, fwhm0;
        double zFrom = psf.getZRange().from, zTo = psf.getZRange().to;
        for(int x = 0; x < mask.getWidth(); x++) {
            for(int y = 0; y < mask.getHeight(); y++) {
                p_px = gPpx * mask.getf(x, y);  //expected number of molecules inside a pixel
                int nMols = p_px > 0 ? (int) rand.nextPoisson(p_px) : 0; //actual number of molecules inside a pixel
                for(int i = 0; i < nMols; i++) {
                    double z = getNextUniform(zFrom, zTo);
                    params[PSFModel.Params.X] = (x + 0.5 + getNextUniform(-0.5, +0.5)) * width / mask.getWidth();
                    params[PSFModel.Params.Y] = (y + 0.5 + getNextUniform(-0.5, +0.5)) * height / mask.getHeight();
                    params[PSFModel.Params.SIGMA] = psf.getSigma1(z);
                    params[PSFModel.Params.SIGMA1] = psf.getSigma1(z);
                    params[PSFModel.Params.SIGMA2] = psf.getSigma2(z);
                    params[PSFModel.Params.INTENSITY] = getNextUniform(intensity_photons.from, intensity_photons.to);
                    params[PSFModel.Params.ANGLE] = psf.getAngle();
                    PSFModel model = psf.getImplementation();
                    Molecule mol = model.newInstanceFromParams(params, Units.PHOTON, false);
                    if(psf.is3D()) {
                        mol.addParam(PSFModel.Params.LABEL_Z, Units.NANOMETER, z);
                    }
                    
                    //set a common MoleculeDescriptor for all molecules in a frame to save memory
                    if(descriptor != null){
                        mol.descriptor = descriptor;
                    }else{
                        descriptor = mol.descriptor;
                    }
                    molist.add(new EmitterModel(model, mol));
                }
            }
        }
        return molist;
    }
    
    public Vector<EmitterModel> generateSingleFixedMolecule(int width, int height, double xOffset, double yOffset, Range intensity_photons, IPsfUI psf) {
        double[] params = new double[PSFModel.Params.PARAMS_LENGTH];
        Vector<EmitterModel> molist = new Vector<EmitterModel>();
        double z = getNextUniform(psf.getZRange().from, psf.getZRange().to);
        params[PSFModel.Params.X] = (xOffset + 0.5 + width/2.0);
        params[PSFModel.Params.Y] = (yOffset + 0.5 + height/2.0);
        params[PSFModel.Params.SIGMA] = psf.getSigma1(z);
        params[PSFModel.Params.SIGMA1] = psf.getSigma1(z);
        params[PSFModel.Params.SIGMA2] = psf.getSigma2(z);
        params[PSFModel.Params.INTENSITY] = getNextUniform(intensity_photons.from, intensity_photons.to);
        params[PSFModel.Params.ANGLE] = psf.getAngle();
        PSFModel model = psf.getImplementation();
        Molecule mol = model.newInstanceFromParams(params, Units.PHOTON, false);
        if(psf.is3D()) {
            mol.addParam(PSFModel.Params.LABEL_Z, Units.NANOMETER, z);
        }
        molist.add(new EmitterModel(model, mol));
        return molist;
    }
    
    /**
     * Replaces each pixel value with a sample from a poisson distribution with mean value equal to the pixel original value.
     */
    FloatProcessor samplePoisson(FloatProcessor fp){
        for(int i = 0; i < fp.getPixelCount(); i ++){
            float mean =  fp.getf(i);

            double value = mean > 0 ? (rand.nextPoisson(mean)) : 0;
            fp.setf(i, (float)value);
        }
        return fp;
    }
    
    /**
     * Replaces each pixel value with a sample from a Gamma distribution with shape equal to the original pixel value and scale equal to the gain parameter.
     */
    FloatProcessor sampleGamma(FloatProcessor fp, double gain){
        for(int i = 0; i < fp.getPixelCount(); i ++){
            double value = fp.getf(i);
            value = rand.nextGamma(value + 1e-10, gain);
            fp.setf(i, (float)value);
        }
        return fp;
    }

    public ShortProcessor renderFrame(int width, int height, int frame_no, Drift drift, Vector<EmitterModel> molecules, FloatProcessor backgroundMeanIntensity) {
        //get drift for current frame
        double dx = drift.getDriftX(frame_no), dy = drift.getDriftY(frame_no);

        //add expected photon count of molecules into an image
        float [] zeros = new float[width*height];
        FloatProcessor frame = new FloatProcessor(width, height, zeros, null);
        for(EmitterModel mol : molecules) {
            mol.moveXY(dx, dy);                     // add drift
            if(mol.isOutOfRoi(frame.getRoi())) {    // does the molecule get out of ROI due to the drift?
                deleteLater.add(mol);
            } else {
                mol.generate(frame);
            }
        }
        //remove the out-of-roi molecules
        for(EmitterModel mol : deleteLater) {
            molecules.remove(mol);
        }
        deleteLater.clear();
        
        //add expected photon background
        frame = ImageMath.add(frame, backgroundMeanIntensity);
        //simulates poisson distributed photon arrival (and EM gain using Gamma distribution if it is enabled)
        frame = samplePoisson(frame);
        if(CameraSetupPlugIn.getIsEmGain()){
            frame = sampleGamma(frame, CameraSetupPlugIn.getGain());
        }
        //convert to AD units
        frame = ImageMath.divide(frame, (float)CameraSetupPlugIn.getPhotons2ADU());
        frame = ImageMath.add((float)CameraSetupPlugIn.getOffset(), frame);
        //convert to integer
        return (ShortProcessor)frame.convertToShort(false);
    }

}
