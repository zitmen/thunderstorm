package ThunderSTORM.estimators;

import LMA.LMA;
import LMA.LMAMultiDimFunction;
import ThunderSTORM.IModule;
import ThunderSTORM.estimators.PSF.GaussianPSF;
import ThunderSTORM.estimators.PSF.PSF;
import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import static ThunderSTORM.utils.Math.sqr;
import ij.IJ;

public class LeastSquaresEstimator implements IEstimator, IModule {
    
    private int fitrad, fitrad2, fitrad_2;
    
    private JTextField fitregsizeTextField;

    private void updateFittingRadius(int fitting_region_size) {
        this.fitrad = fitting_region_size;
        this.fitrad2 = fitting_region_size * fitting_region_size;
        this.fitrad_2 = fitting_region_size / 2;
    }
    
    public static class Gaussian extends LMAMultiDimFunction {

        @Override
        public double getY(double x[], double[] a) {
            // a = {x0,y0,Intensity,sigma,background}
            return a[2]/2.0/Math.PI/sqr(a[3]) * Math.exp(-(sqr(x[0]-a[0]) + sqr(x[1]-a[1])) / 2.0 / sqr(a[3])) + a[4];
        }

        @Override
        public double getPartialDerivate(double x[], double[] a, int parameterIndex) {
            double arg = sqr(x[0] - a[0]) + sqr(x[1] - a[1]);
            switch (parameterIndex) {
                case 0: return a[2]/2.0/Math.PI/Math.pow(a[3],4) * (x[0]-a[0]) * Math.exp(-arg/2.0/sqr(a[3])); // x0
                case 1: return a[2]/2.0/Math.PI/Math.pow(a[3],4) * (x[1]-a[1]) * Math.exp(-arg/2.0/sqr(a[3])); // y0
                case 2: return Math.exp(-arg/2.0/sqr(a[3])) / 2.0 / Math.PI / sqr(a[3]); // Intensity
                case 3: return a[2]/2.0/Math.PI/Math.pow(a[3],5) * (arg - 2.0 * sqr(a[3])) * Math.exp(-arg/2.0/sqr(a[3])); // sigma
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
        
        for(int d = 0, dm = detections.size(); d < dm; d++) {
            Point p = detections.elementAt(d);
            
            // [GaussianPSF] params = {x0,y0,Intensity,sigma,background}
            double[] init_guess = new double[]{ p.getX().doubleValue(), p.getY().doubleValue(), image.getPixelValue(p.roundToInteger().getX().intValue(), p.roundToInteger().getY().intValue()), 1.6, 100.0 };
            double[][] x = new double[fitrad2][2];
            double[] y = new double[fitrad2];
            for (int r = 0; r < fitrad; r++) {
                for (int c = 0; c < fitrad; c++) {
                    int idx = r * 11 + c;
                    x[idx][0] = (int) init_guess[0] + c - fitrad_2;  // x
                    x[idx][1] = (int) init_guess[1] + r - fitrad_2;  // y
                    y[idx] = new Float(image.getPixelValue((int) x[idx][0], (int) x[idx][1])).doubleValue();    // G(x,y)
                }
            }
            
            LMA lma = new LMA(new Gaussian(), init_guess, y, x);    // Gaussian! LMA has to be adapted to work with PSF!!
            lma.fit();
            
            // TODO: generalize!! this should not be just GaussianPSF!!
            fits.add(new GaussianPSF(lma.parameters[0]+0.5, lma.parameters[1]+0.5, lma.parameters[2], lma.parameters[3], lma.parameters[4]));  // 0.5px shift to the center of each pixel
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
