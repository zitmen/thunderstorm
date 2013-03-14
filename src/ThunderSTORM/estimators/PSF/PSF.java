package ThunderSTORM.estimators.PSF;

public abstract class PSF {
    
    public double xpos;
    public double ypos;
    public double intensity;
    public double background;
    
    public abstract PSF getGradient();
    public abstract double getValueAt(PSF where);
    
}
