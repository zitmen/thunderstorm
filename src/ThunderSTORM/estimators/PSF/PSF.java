package ThunderSTORM.estimators.PSF;

public abstract class PSF {
    
    public double xpos;
    public double ypos;
    public double zpos;
    public double intensity;
    public double background;
    
    public abstract double[] getGradient(PSF where);
    public abstract double getValueAt(PSF where);
    public abstract double[] getParams();
    public abstract String[] getTitles();
    
}
