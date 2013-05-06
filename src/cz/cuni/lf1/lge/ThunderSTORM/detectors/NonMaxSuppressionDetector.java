package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Morphology;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public final class NonMaxSuppressionDetector implements IDetector, IModule {

    private int radius;
    private double threshold;
    
    private JTextField thrTextField;
    private JTextField radiusTextField;
    
    /**
     *
     * @param radius
     * @param threshold
     */
    public NonMaxSuppressionDetector(int radius, double threshold) {
        this.radius = radius;
        this.threshold = threshold;
    }

    /**
     *
     * @param image
     * @return
     */
    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) {
        Vector<Point> detections = new Vector<Point>();
        
        FloatProcessor mx = Morphology.dilateBox(image, radius);
        
        float imval, mxval;
        for(int x = radius/2, xm = image.getWidth()-radius/2; x < xm; x++) {
            for(int y = radius/2, ym = image.getHeight()-radius/2; y < ym; y++) {
                imval = image.getf(x, y);
                mxval = mx.getf(x, y);
                if((mxval == imval) && (imval >= threshold))
                    detections.add(new Point(x, y, imval));
            }
        }
        
        return detections;
    }

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "Non-maxima suppression";
    }

    /**
     *
     * @return
     */
    @Override
    public JPanel getOptionsPanel() {
        thrTextField = new JTextField(Double.toString(threshold), 20);
        radiusTextField = new JTextField(Integer.toString(radius), 20);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Threshold: "), GridBagHelper.pos(0, 0));
        panel.add(thrTextField, GridBagHelper.pos(1, 0));
        panel.add(new JLabel("Radius: "), GridBagHelper.pos(0, 1));
        panel.add(radiusTextField, GridBagHelper.pos(1, 1));
        return panel;
    }

    /**
     *
     */
    @Override
    public void readParameters() {
        try {
            threshold = Double.parseDouble(thrTextField.getText());
            radius = Integer.parseInt(radiusTextField.getText());
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }

}
