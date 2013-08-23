package cz.cuni.lf1.lge.ThunderSTORM.datagen;

import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqrt;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import ij.process.FloatProcessor;
import java.awt.Rectangle;
import java.util.HashMap;
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
        //
        dExyTLMap.get().clear();
    }

    public void moveXY(double dx, double dy) {
        x0 += dx;
        y0 += dy;
    }
    
    public boolean isOutOfRoi(Rectangle roi) {
        return (roi.contains(x0, y0) == false);
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
    
    // -----------------
    private static final double sqrt2 = sqrt(2);
    private static final ThreadLocal<HashMap<Double,Double>> dExyTLMap = new ThreadLocal<HashMap<Double,Double>>() {
        @Override
        protected synchronized HashMap<Double,Double> initialValue() {
            return new HashMap<Double,Double>();
        }
    };

    private double evalAtPixel(double x, double y) {
        HashMap<Double,Double> dExyValues = dExyTLMap.get();
        //
        double dEx;
        if(dExyValues.containsKey(x-x0)) {
            dEx = dExyValues.get(x-x0);
        } else {
            dEx = dExy(x, x0);
            dExyValues.put(x-x0, dEx);
        }
        double dEy;
        if(dExyValues.containsKey(y-y0)) {
            dEy = dExyValues.get(y-y0);
        } else {
            dEy = dExy(y, y0);
            dExyValues.put(y-y0, dEy);
        }
        return I0 * dEx * dEy;
    }

    private double dExy(double xy, double xy0) {   // `xy`, because the same equation applies for `x` and `y`
        return 0.5*erf((xy-xy0+0.5) / (sqrt2*sig0)) - 0.5*erf((xy-xy0-0.5) / (sqrt2*sig0));
    }
    
}