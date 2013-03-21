package ThunderSTORM.estimators.PSF;

import static ThunderSTORM.utils.Math.sqr;

public class GaussianPSF extends PSF {
    
    public double sigma_x;
    public double sigma_y;
    public double angle;    // [radians]
    
    // pre-allocated arrays
    private final double[] gradient = new double[8];
    private final double[] params = new double[8];
    private final static String[] titles = new String[] { "x", "y", "z", "I", "b", "sigma_x", "sigma_y", "angle" };

    // TODO: rozlisovat sigma_x, sigma_y a pridat rotaci o uhel (staci rotacni matice? mela by!)!!
    @Override
    public double getValueAt(PSF where) {
        return intensity/2.0/Math.PI/sqr(sigma_x) * Math.exp(-(sqr(xpos-where.xpos) + sqr(ypos-where.ypos)) / 2.0 / sqr(sigma_x)) + background;
    }

    // TODO: rozlisovat sigma_x, sigma_y a pridat rotaci o uhel (staci rotacni matice? mela by!)!!
    @Override
    public double[] getGradient(PSF where) {
        double arg = sqr(xpos - where.xpos) + sqr(ypos - where.ypos);
        gradient[0] = intensity/2.0/Math.PI/Math.pow(sigma_x,4) * (xpos-where.xpos) * Math.exp(-arg/2.0/sqr(sigma_x)); // x0
        gradient[1] = intensity/2.0/Math.PI/Math.pow(sigma_x,4) * (ypos-where.ypos) * Math.exp(-arg/2.0/sqr(sigma_x)); // y0
        // TODO [2] -- z
        gradient[3] = Math.exp(-arg/2.0/sqr(sigma_x)) / 2.0 / Math.PI / sqr(sigma_x); // Intensity
        gradient[4] = 1.0; // background
        gradient[5] = intensity/2.0/Math.PI/Math.pow(sigma_x,5) * (arg - 2.0 * sqr(sigma_x)) * Math.exp(-arg/2.0/sqr(sigma_x)); // sigma_x -- TODO!!
        gradient[6] = intensity/2.0/Math.PI/Math.pow(sigma_x,5) * (arg - 2.0 * sqr(sigma_x)) * Math.exp(-arg/2.0/sqr(sigma_x)); // sigma_y -- TODO!!
        return gradient;
    }

    @Override
    public double[] getParams() {
        params[0] = xpos;
        params[1] = ypos;
        params[2] = zpos;
        params[3] = intensity;
        params[4] = background;
        params[5] = sigma_x;
        params[6] = sigma_y;
        params[7] = angle;
        return params;
    }

    @Override
    public String[] getTitles() {
        return titles;
    }
    
}
