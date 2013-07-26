package cz.cuni.lf1.lge.ThunderSTORM.datagen;

import cz.cuni.lf1.lge.ThunderSTORM.filters.BoxFilter;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.PI;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.ceil;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import ij.process.FloatProcessor;
import org.apache.commons.math3.random.RandomDataGenerator;
import static org.apache.commons.math3.special.Erf.erf;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.exp;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqrt;
import static ij.process.ImageProcessor.BILINEAR;
import ij.process.ShortProcessor;
import java.util.Vector;
import static org.apache.commons.math3.util.FastMath.cos;
import static org.apache.commons.math3.util.FastMath.log;
import static org.apache.commons.math3.util.FastMath.sin;

public class DataGenerator {
    
    private static RandomDataGenerator rand = new RandomDataGenerator();
    
    public static FloatProcessor generatePoissonNoise(int width, int height, double variance) {
        rand.reSeed();
        FloatProcessor img = new FloatProcessor(width, height);
        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
                img.setf(x, y, (float)rand.nextPoisson(variance));
        return img;
    }
    
    public static FloatProcessor generateGaussianNoise(int width, int height, double mean, double variance) {
        rand.reSeed();
        double sigma = sqrt(variance);
        FloatProcessor img = new FloatProcessor(width, height);
        for(int x = 0; x < width; x++)
            for(int y = 0; y < height; y++)
                img.setf(x, y, (float)rand.nextGaussian(mean, sigma));
        return img;
    }
    
    public static FloatProcessor generateBackground(int width, int height, Drift drift, Range bkg) {
        rand.reSeed();
        // padd the background image; crop the center of the image later, after the drift is applied
        FloatProcessor img = new FloatProcessor(width + 2*(int)ceil(drift.dist), height + 2*(int)ceil(drift.dist));
        for(int x = 0, w = img.getWidth(); x < w; x++)
            for(int y = 0, h = img.getHeight(); y < h; y++)
                img.setf(x, y, (float)rand.nextUniform(bkg.from, bkg.to, true));
        IFilter filter = new BoxFilter((int)(((double)Math.min(width, width))/4.0));
        return filter.filterImage(img);
    }
    
    public static Vector<IntegratedGaussian> generateMolecules(int width, int height, FloatProcessor mask, double pixelsize, double density, Range energy, Range fwhm) {
        rand.reSeed();
        Vector<IntegratedGaussian> molist = new Vector<IntegratedGaussian>();
        double gPpx = sqr(pixelsize) * density, p_px, p, dx, dy;
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                p_px = gPpx * mask.getf(x, y);  // probability that a molecule appears inside the pixel
                p = rand.nextUniform(0.0, 1.0);
                while(p <= p_px) {
                    dx = rand.nextUniform(-0.5, +0.5);
                    dy = rand.nextUniform(-0.5, +0.5);
                    molist.add(new IntegratedGaussian(x+0.5+dx, y+0.5+dy, energy, fwhm));
                    p_px *= p_px;
                }
            }
        }
        return molist;
    }

    public static ShortProcessor renderFrame(int width, int height, int frame_no, Drift drift, Vector<IntegratedGaussian> molecules, FloatProcessor bkg, FloatProcessor add_noise, FloatProcessor mul_noise) {
        // 1. acquisition (with drift)
        double dx = drift.getDriftX(frame_no), dy = drift.getDriftY(frame_no);
        FloatProcessor frame = (FloatProcessor)bkg.duplicate();
        frame.setInterpolationMethod(BILINEAR);
        frame.translate(dx, dy);
        frame.setRoi((int)ceil(drift.dist), (int)ceil(drift.dist), width, height);    // see generateBackground
        frame = (FloatProcessor)frame.crop();
        for(IntegratedGaussian mol : molecules) {
            mol.moveXY(dx, dy);
            mol.generate(frame);
        }
        // 2. read-out
        frame = ImageProcessor.add(frame, add_noise);
        // 3. gain
        frame = ImageProcessor.multiply(frame, mul_noise);
        //
        return (ShortProcessor)frame.convertToShort(false);
    }
    
    public static class Drift {
        
        public double dist;     // [pixels]
        public double angle;    // [radians]
        public double dist_step;    // assuming linear drift
        
        public Drift(double dist, double angle, boolean angle_in_rad, int nframes) {
            this.dist = dist;
            if(angle_in_rad) {
                this.angle = angle;
            } else {
                this.angle = angle / 180.0 * PI;
            }
            if(nframes > 1) {
                dist_step = dist / (double)(nframes-1); // nframes-1, because there is no drift in the first frame
            } else {
                dist_step = 0.0;
            }
        }
        
        public double getDriftX(int frame) {    // indexing from zero
            return ((double)frame * dist_step) * cos(angle);
        }
        
        public double getDriftY(int frame) {    // indexing from zero
            return ((double)frame * dist_step) * sin(angle);
        }
        
    }
    
    public static class IntegratedGaussian {
        
        public final double FWHM_factor = sqrt(2*log(2));
        public double x0, y0, I0, sig0, fwhm0;
        
        public IntegratedGaussian(double x, double y, Range energy, Range fwhm) {
            x0 = x;
            y0 = y;
            I0 = rand.nextUniform(energy.from, energy.to);
            fwhm0 = rand.nextUniform(fwhm.from, fwhm.to);
            sig0 = fwhm0 / FWHM_factor;
        }
        
        public void moveXY(double dx, double dy) {
            x0 += dx;
            y0 += dy;
        }
        
        public void generate(FloatProcessor img) {
            int width = img.getWidth(), height = img.getHeight();
            for(int x = (int)(x0 - 2*fwhm0), xm = (int)(x0 + 2*fwhm0); x <= xm; x++) {
                if((x < 0) || (x >= width)) continue;
                for(int y = (int)(y0 - 2*fwhm0), ym = (int)(y0 + 2*fwhm0); y <= ym; y++) {
                    if((y < 0) || (y >= height)) continue;
                    img.setf(x, y, img.getf(x, y) + (float)evalAtPixel(x, y));
                }
            }
        }
        
        private double evalAtPixel(double x, double y) {
            return I0/2/PI/sqr(sig0) * exp(-(sqr(x-x0)+sqr(y-y0))/2/sqr(sig0));
            //return I0 * dExy(x, x0) * dExy(y, y0);
        }

        private double dExy(double xy, double xy0) {   // `xy`, because the same equation applies for `x` and `y`
            return 0.5*erf((xy-xy0+0.5) / (2*sqr(sig0))) - 0.5*erf((xy-xy0-0.5) / (2*sqr(sig0)));
        }
    
    }

}
