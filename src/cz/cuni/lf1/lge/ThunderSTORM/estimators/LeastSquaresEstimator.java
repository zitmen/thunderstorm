package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import fi.utu.jaolho.LMA.LMA;
import fi.utu.jaolho.LMA.LMAMultiDimFunction;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.exp;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.pow;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqrt;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.PI;
import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.GaussianPSF;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import ij.IJ;
import java.util.Arrays;

public class LeastSquaresEstimator implements IEstimator, IModule {
    
    private int fitrad, fitrad2, fitrad_2;
    
    private JTextField fitregsizeTextField;
    
    private void updateFittingRadius(int fitting_region_size) {
        this.fitrad = fitting_region_size;
        this.fitrad2 = fitting_region_size * fitting_region_size;
        this.fitrad_2 = fitting_region_size / 2;
    }

    private boolean checkDist2(Point fit, Point detection) {
        return ((sqr(fit.x.doubleValue() - detection.x.doubleValue()) + sqr(fit.y.doubleValue() - detection.y.doubleValue())) <= (double)fitrad2);
    }
    
    public static class Gaussian extends LMAMultiDimFunction {

        @Override
        public double getY(double x[], double[] a) {
            // a = {x0,y0,Intensity,sigma,background}
            // sqr(a(2:4)) ---> transform the parameters back (see method estimateParameters)
            return sqr(a[2])/2.0/PI/sqr(sqr(a[3])) * exp(-(sqr(x[0]-a[0]) + sqr(x[1]-a[1])) / 2.0 / sqr(sqr(a[3]))) + sqr(a[4]);
        }

        @Override
        public double getPartialDerivate(double x[], double[] a, int parameterIndex) {
            // sqr(a[2:4]) ---> transform the parameters back (see method estimateParameters)
            //  --> I am not sure if it really matters here...?
            double arg = sqr(x[0] - a[0]) + sqr(x[1] - a[1]);
            switch (parameterIndex) {
                case 0: return sqr(a[2])/2.0/PI/pow(sqr(a[3]),4) * (x[0]-a[0]) * exp(-arg/2.0/sqr(sqr(a[3]))); // x0
                case 1: return sqr(a[2])/2.0/PI/pow(sqr(a[3]),4) * (x[1]-a[1]) * exp(-arg/2.0/sqr(sqr(a[3]))); // y0
                case 2: return exp(-arg/2.0/sqr(sqr(a[3]))) / 2.0 / PI / sqr(sqr(a[3])); // Intensity
                case 3: return sqr(a[2])/2.0/PI/pow(sqr(a[3]),5) * (arg - 2.0 * sqr(sqr(a[3]))) * exp(-arg/2.0/sqr(sqr(a[3]))); // sigma
                case 4: return 1.0; // background
            }
            throw new RuntimeException("No such parameter index: " + parameterIndex);
        }
    }
    
    public LeastSquaresEstimator(int fitting_region_size) {
        updateFittingRadius(fitting_region_size);
    }
    
    @Override
    public Vector<PSF> estimateParameters(FloatProcessor image, Vector<Point> detections) {
        Vector<PSF> fits = new Vector<PSF>();
        Point p_fit = new Point();
        
        for(int d = 0, dm = detections.size(); d < dm; d++) {
            Point p = detections.elementAt(d);
            
            // [GaussianPSF] params = {x0,y0,Intensity,sigma,background}
            double[] init_guess = new double[]{ p.getX().doubleValue(), p.getY().doubleValue(), image.getf(p.roundToInteger().getX().intValue(), p.roundToInteger().getY().intValue()), 1.6, Double.MAX_VALUE };
            
            // extract the fitting area of a certain radius
            double[][] x = new double[fitrad2][2];
            double[] y = new double[fitrad2];
            for (int r = 0; r < fitrad; r++) {
                for (int c = 0; c < fitrad; c++) {
                    int idx = r * fitrad + c;
                    x[idx][0] = (int) init_guess[0] + c - fitrad_2;  // x
                    x[idx][1] = (int) init_guess[1] + r - fitrad_2;  // y
                    y[idx] = new Float(image.getf((int)x[idx][0], (int)x[idx][1])).doubleValue();    // G(x,y)
                    if(y[idx] < init_guess[4]) init_guess[4] = y[idx];  // background = minimal value in the fitting region
                }
            }
            
            // transform the parameters to avoid negative values
            for(int i = 2; i < init_guess.length; i++)
                init_guess[i] = sqrt(init_guess[i]);
            
            // fitting by L-M algorithm
            LMA lma = new LMA(new Gaussian(), init_guess, y, x);    // Gaussian! LMA has to be adapted to work with PSF!!
            lma.fit();
            
            // transform the parameters back
            for(int i = 2; i < init_guess.length; i++)
                lma.parameters[i] = sqr(lma.parameters[i]);
            
            // 0.5px shift to the center of each pixel
            lma.parameters[0] += 0.5;
            lma.parameters[1] += 0.5;
            
            // TODO: generalize!! this should not be just GaussianPSF!!
            if(GaussianPSF.checkRange(lma.parameters))
                if(checkDist2(p_fit.setLocation(lma.parameters[0], lma.parameters[1]), p))
                    fits.add(new GaussianPSF(lma.parameters[0], lma.parameters[1], lma.parameters[2], lma.parameters[3], lma.parameters[4]));
        }
        
        return fits;
    }

    @Override
    public String getName() {
        return "Minimizing least squares error";
    }

    @Override
    public JPanel getOptionsPanel() {
        fitregsizeTextField = new JTextField(Integer.toString(fitrad), 20);
        //
        JPanel panel = new JPanel();
        panel.add(new JLabel("Fitting region size: "));
        panel.add(fitregsizeTextField);
        return panel;
    }

    @Override
    public void readParameters() {
        try {
            updateFittingRadius(Integer.parseInt(fitregsizeTextField.getText()));
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }
    
}
