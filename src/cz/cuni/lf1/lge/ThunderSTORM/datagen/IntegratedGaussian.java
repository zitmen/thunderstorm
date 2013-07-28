package cz.cuni.lf1.lge.ThunderSTORM.datagen;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.PI;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.exp;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqrt;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import ij.process.FloatProcessor;
import org.apache.commons.math3.random.RandomDataGenerator;
import static org.apache.commons.math3.special.Erf.erf;
import static org.apache.commons.math3.util.FastMath.log;

public class IntegratedGaussian {
        
        public static final double FWHM_factor = sqrt(2*log(2));
        public double x0, y0, I0, sig0, fwhm0;
        
        public IntegratedGaussian(RandomDataGenerator rand, double x, double y, Range energy, Range fwhm) {
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