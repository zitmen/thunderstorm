package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.applyMask;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.threshold;
import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.util.Graph;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.plugin.filter.EDM;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public final class CentroidOfConnectedComponentsDetector implements IDetector, IModule {

    private boolean upsample;
    private double threshold;
    
    private JTextField thrTextField;
    private JCheckBox upCheckBox;
    
    public CentroidOfConnectedComponentsDetector(boolean upsample, double threshold) {
        this.upsample = upsample;
        this.threshold = threshold;
    }
    
    public void updateThreshol(double threshold) {
        this.threshold = threshold;
    }
    
    public void updateUpsample(boolean upsample) {
        this.upsample = upsample;
    }

    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) {
        // thresholding first to make the image binary
        threshold(image, (float) threshold, 0.0f, 255.0f);
        // watershed transform with[out] upscaling
        if (upsample) {
            image.setInterpolationMethod(FloatProcessor.NEAREST_NEIGHBOR);
            image = (FloatProcessor) image.resize(image.getWidth() * 2);
        }
        // run the watershed algorithm - it works only with ByteProcessor! that's all I need though
        ByteProcessor w = (ByteProcessor) image.convertToByte(false);
        EDM edm = new EDM();
        edm.setup("watershed", null);
        edm.run(w);
        image = applyMask((FloatProcessor)w.convertToFloat(), image);
        // finding a center of gravity (with subpixel precision)
        Vector<Point> detections = new Vector<Point>();
        for (Graph.ConnectedComponent c : Graph.getConnectedComponents((ImageProcessor) image, Graph.CONNECTIVITY_8)) {
            detections.add(c.centroid());
            if(upsample) detections.lastElement().scaleXY(0.5);
            detections.lastElement().val = null;
        }
        return detections;
    }

    @Override
    public String getName() {
        return "Centroid of connected components";
    }

    @Override
    public JPanel getOptionsPanel() {
        thrTextField = new JTextField(Double.toString(threshold), 20);
        upCheckBox = new JCheckBox("upsample");
        upCheckBox.setSelected(upsample);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Threshold: "), GridBagHelper.pos(0, 0));
        panel.add(thrTextField, GridBagHelper.pos(1, 0));
        panel.add(upCheckBox, GridBagHelper.pos_width(0, 1, 2, 1));
        return panel;
    }

    @Override
    public void readParameters() {
        try {
            threshold = Double.parseDouble(thrTextField.getText());
            upsample = upCheckBox.isSelected();
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }
    
}
