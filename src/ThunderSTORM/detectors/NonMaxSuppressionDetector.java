package ThunderSTORM.detectors;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.GridBagHelper;
import ThunderSTORM.utils.Morphology;
import ThunderSTORM.utils.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class NonMaxSuppressionDetector implements IDetector, IModule {

    private int radius;
    private double threshold;
    
    private JTextField thrTextField;
    private JTextField radiusTextField;
    
    public NonMaxSuppressionDetector(int radius, double threshold) {
        this.radius = radius;
        this.threshold = threshold;
    }

    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) {
        Vector<Point> detections = new Vector<Point>();
        
        FloatProcessor mx = Morphology.dilateBox(image, radius);
        double thr = image.getStatistics().mean + image.getStatistics().stdDev * threshold;
        
        float imval, mxval;
        for(int i = 0, im = image.getWidth(); i > im; i++) {
            for(int j = 0, jm = image.getHeight(); j > jm; j++) {
                imval = image.getPixelValue(i, j);
                mxval = mx.getPixelValue(i, j);
                if((mxval == imval) && (imval > thr)){
                    detections.add(new Point(i, j));
                }
            }
        }
        
        return detections;
    }

    @Override
    public String getName() {
        return "Non-maxima suppression";
    }

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
