package ThunderSTORM.detectors;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.Graph;
import static ThunderSTORM.utils.ImageProcessor.applyMask;
import static ThunderSTORM.utils.ImageProcessor.threshold;
import ThunderSTORM.utils.Point;
import Watershed.WatershedAlgorithm;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class WatershedDetector implements IDetector, IModule {

    private boolean upsample;
    private double threshold;
    
    public WatershedDetector(boolean upsample, double threshold) {
        this.upsample = upsample;
        this.threshold = threshold;
    }

    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) {
        // thresholding first to make the image binary
        threshold(image, (float) threshold, 1.0f, 0.0f); // these are in reverse (1=low,0=high) on purpose!
                                                         //the result is negated image, which is exactly what i need
        // watershed transform with[out] upscaling
        if (upsample) {
            image.setInterpolationMethod(FloatProcessor.NEAREST_NEIGHBOR);
            image = (FloatProcessor) image.resize(image.getWidth() * 2);
        }
        // run the watershed algorithm - it works only with ByteProcessor! that's all I need though
        FloatProcessor w = (FloatProcessor) WatershedAlgorithm.run((ByteProcessor) image.convertToByte(false)).convertToFloat();
        image = applyMask(w, image);
        if (upsample) {
            image = (FloatProcessor) image.resize(image.getWidth() / 2);
        }

        // finding a center of gravity (with subpixel precision)
        Vector<Point> detections = new Vector<Point>();
        for (Graph.ConnectedComponent c : Graph.getConnectedComponents((ImageProcessor) image, Graph.CONNECTIVITY_8)) {
            detections.add(c.centroid());
            detections.lastElement().val = null;
        }

        return detections;
    }

    @Override
    public String getName() {
        return "Watershed transform";
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
        gbc.gridwidth = 2;
        panel.add(new JCheckBox("upsample"), gbc);
        return panel;
    }
    
}
