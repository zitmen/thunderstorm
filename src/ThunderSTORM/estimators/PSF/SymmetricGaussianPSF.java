package ThunderSTORM.estimators.PSF;

public class SymmetricGaussianPSF extends GaussianPSF {
    
    public double sigma;
    
    public SymmetricGaussianPSF(double x, double y) {
        super(x, y);
    }
    
    public SymmetricGaussianPSF(double x, double y, double I, double s, double b) {
        super(x, y, I, s, b);
    }
    
    @Override
    public double getValueAt(PSF where) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        /*
        sigma_x = sigma_y = sigma;
        return super.getValueAt(where);
        */
    }

    @Override
    public double[] getGradient(PSF where) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        /*
        sigma_x = sigma_y = sigma;
        return super.getGradient(where);
        */
    }
    
    @Override
    public double[] getParams() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        /*
        params[0] = xpos;
        params[1] = ypos;
        params[2] = zpos;
        params[3] = intensity;
        params[4] = sigma_x;
        params[5] = sigma_y;
        params[6] = background;
        return params;
        */
    }

    @Override
    public String[] getTitles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
