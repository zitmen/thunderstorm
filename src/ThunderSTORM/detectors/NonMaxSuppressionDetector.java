package ThunderSTORM.detectors;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.Morphology;
import ThunderSTORM.utils.Point;
import ij.process.FloatProcessor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class NonMaxSuppressionDetector implements IDetector, IModule {

    private int radius;
    private double threshold;
    
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
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Threshold: "), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField("Threshold", 20), gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Radius: "), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField("Radius", 20), gbc);
        return panel;
    }
    
}
