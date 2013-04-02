package ThunderSTORM.estimators.PSF;

import static ThunderSTORM.utils.Math.sqr;

// Note: this is now a rotationaly symmetric 2D Gaussian function
public class GaussianPSF extends PSF {
    
    public double sigma;
    //public double sigma_x;
    //public double sigma_y;
    //public double angle;    // [radians]
    
    // pre-allocated arrays
    //private final double[] gradient = new double[8];
    //private final double[] params = new double[8];
    //private final static String[] titles = new String[] { "x", "y", "z", "I", "b", "sigma_x", "sigma_y", "angle" };
    private final double[] gradient = new double[5];
    private final double[] params = new double[5];
    private final static String[] titles = new String[] { "x", "y", "I", "sigma", "b" };

    public GaussianPSF() {
        //
    }
    
    public GaussianPSF(double x, double y) {
        super.xpos = x;
        super.ypos = y;
    }
    
    public GaussianPSF(double x, double y, double I, double s, double b) {
        super.xpos = x;
        super.ypos = y;
        super.intensity = I;
        this.sigma = s;
        super.background = b;
    }

    // TODO: rozlisovat sigma_x, sigma_y a pridat rotaci o uhel (staci rotacni matice? mela by!)!!
    @Override
    public double getValueAt(PSF where) {
        return intensity/2.0/Math.PI/sqr(sigma) * Math.exp(-(sqr(xpos-where.xpos) + sqr(ypos-where.ypos)) / 2.0 / sqr(sigma)) + background;
    }

    // TODO: rozlisovat sigma_x, sigma_y a pridat rotaci o uhel (staci rotacni matice? mela by!)!!
    @Override
    public double[] getGradient(PSF where) {
        double arg = sqr(xpos - where.xpos) + sqr(ypos - where.ypos);
        gradient[0] = intensity/2.0/Math.PI/Math.pow(sigma,4) * (xpos-where.xpos) * Math.exp(-arg/2.0/sqr(sigma)); // x0
        gradient[1] = intensity/2.0/Math.PI/Math.pow(sigma,4) * (ypos-where.ypos) * Math.exp(-arg/2.0/sqr(sigma)); // y0
        gradient[2] = Math.exp(-arg/2.0/sqr(sigma)) / 2.0 / Math.PI / sqr(sigma); // Intensity
        gradient[3] = intensity/2.0/Math.PI/Math.pow(sigma,5) * (arg - 2.0 * sqr(sigma)) * Math.exp(-arg/2.0/sqr(sigma));
        gradient[4] = 1.0; // background
        return gradient;
    }

    @Override
    public double[] getParams() {
        params[0] = xpos;
        params[1] = ypos;
        params[2] = intensity;
        params[3] = sigma;
        params[4] = background;
        return params;
    }

    @Override
    public String[] getTitles() {
        return titles;
    }
    
}
