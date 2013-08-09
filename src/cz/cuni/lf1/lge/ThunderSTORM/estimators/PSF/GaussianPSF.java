package cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.OneLocationFitter;

/**
 * General representation of Gaussian PSFModel model.
 * 
 * <strong>Note that this class will be completely changed in a future relase.</strong>
 * Now the class represents only 2D symmetric Gaussian model.
 */
public class GaussianPSF extends PSFModel {

    /**
     *
     * @param parameters
     * @return
     */
    public static boolean checkRange(double[] parameters) {
        // check for negative values
        for(int i = 0; i < parameters.length; i++)
            if(parameters[i] < 0)
                return false;
        
        return true;
    }
    
    /**
     *
     */
    public double sigma;
    
    double xpos;
    double ypos;
    double intensity;
    double background;
    //public double sigma_x;
    //public double sigma_y;
    //public double angle;    // [radians]
    
    // pre-allocated arrays
    //private final double[] gradient = new double[8];
    //private final double[] params = new double[8];
    //private final static String[] titles = new String[] { "x", "y", "z", "I", "b", "sigma_x", "sigma_y", "angle" };
    private final double[] gradient = new double[5];
    private final double[] params = new double[5];
    private final static String[] titles = new String[] { PSFInstance.X_POS, PSFInstance.Y_POS, PSFInstance.SIGMA, PSFInstance.INTENSITY, PSFInstance.BACKGROUND };

    /**
     *
     */
    public GaussianPSF() {
        //
    }
    
    /**
     *
     * @param x
     * @param y
     */
    public GaussianPSF(double x, double y) {
        this(x, y, 0, 0, 0);
    }
    
    /**
     *
     * @param x
     * @param y
     * @param I
     * @param s
     * @param b
     */
    public GaussianPSF(double x, double y, double I, double s, double b) {
        this.xpos = x;
        this.ypos = y;
        this.intensity = I;
        this.sigma = s;
        this.background = b;
    }

    /**
     *
     * @return
     */
    @Override
    public String[] getParamNames() {
        return titles;
    }

  @Override
  public double getValue(double[] params, double x, double y) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public double[] getInitialParams(OneLocationFitter.SubImage subImage) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public PSFInstance newInstanceFromParams(double[] params) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public double[] getInitialSimplex() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
    
}
