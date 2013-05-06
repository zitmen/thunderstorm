package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public class MaximumLikelihoodEstimator implements IEstimator, IModule {

    /* unify this for all estimators?? */
    private int fitrad;
    private int fitrad2;
    private int fitrad_2;
    
    /**
     *
     * @param fitting_radius
     */
    public MaximumLikelihoodEstimator(int fitting_radius) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        /*
        this.fitrad = fitting_radius;
        this.fitrad2 = fitting_radius * fitting_radius;
        this.fitrad_2 = fitting_radius / 2;
        */
    }
    
    /**
     *
     * @param fp
     * @param detections
     * @param initial_guess
     * @return
     */
    public Vector<Point<Double>> ExponentialGaussianEstimator(FloatProcessor fp, Vector<Point> detections, PSF initial_guess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        /*
        Vector<Point<Double>> fits = new Vector<Point<Double>>();
        
        for(int d = 0, dm = detections.size(); d < dm; d++)
        {
            Point p = detections.elementAt(d);
            
            // params = {x0,y0,Intensity,sigma,background}
            double[] init_guess = new double[]{ p.getX().doubleValue(), p.getY().doubleValue(), fp.getPixelValue(p.roundToInteger().getX().intValue(), p.roundToInteger().getY().intValue()), 1.3, 100.0 };
            double[][] x = new double[fitrad2][2];
            double[] y = new double[fitrad2];
            for (int r = 0; r < fitrad; r++) {
                for (int c = 0; c < fitrad; c++) {
                    int idx = r * 11 + c;
                    x[idx][0] = (int) init_guess[0] + c - fitrad_2;  // x
                    x[idx][1] = (int) init_guess[1] + r - fitrad_2;  // y
                    y[idx] = new Float(fp.getPixelValue((int) x[idx][0], (int) x[idx][1])).doubleValue();    // G(x,y)
                }
            }
            
            LMA lma = new LMA(new Thunder_STORM.Gaussian(), init_guess, y, x);
            lma.fit();
            
            fits.add(new Point(lma.parameters[0]+0.5, lma.parameters[1]+0.5));  // 0.5px shift to the center of each pixel
        }
        
        return fits;
        */
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "Maximizing log-likelihood";
    }

    /**
     *
     * @return
     */
    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Fitting region size: "));
        panel.add(new JTextField("Fitting region size", 20));
        return panel;
    }

    /**
     *
     */
    @Override
    public void readParameters() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     *
     * @param image
     * @param detections
     * @return
     */
    @Override
    public Vector<PSF> estimateParameters(FloatProcessor image, Vector<Point> detections) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
