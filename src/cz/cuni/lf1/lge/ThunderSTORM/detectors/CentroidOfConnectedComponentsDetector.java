package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.applyMask;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.threshold;
import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.util.Graph;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public final class CentroidOfConnectedComponentsDetector implements IDetector, IModule {

    private boolean upsample;
    private double threshold;
    
    private JTextField thrTextField;
    private JCheckBox upCheckBox;
    
    /**
     *
     * @param upsample
     * @param threshold
     */
    public CentroidOfConnectedComponentsDetector(boolean upsample, double threshold) {
        this.upsample = upsample;
        this.threshold = threshold;
    }
    
    /**
     *
     * @param threshold
     */
    public void updateThreshol(double threshold) {
        this.threshold = threshold;
    }
    
    /**
     *
     * @param upsample
     */
    public void updateUpsample(boolean upsample) {
        this.upsample = upsample;
    }

    /**
     *
     * @param image
     * @return
     */
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
        ImageJ_EDM edm = new ImageJ_EDM();  // use this version instead of built-in ImageJ version,
        edm.setup("watershed", null);       // because the build-in version uses showStatus and
        edm.run(w);                         // showProgress methods and it disturbs my progress/status info
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

    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "Centroid of connected components";
    }

    /**
     *
     * @return
     */
    @Override
    public JPanel getOptionsPanel() {
        thrTextField = new JTextField(Double.toString(threshold), 20);
        upCheckBox = new JCheckBox("upsample");
        upCheckBox.setSelected(upsample);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Threshold: "), GridBagHelper.pos(0, 0));
        panel.add(thrTextField, GridBagHelper.pos(1, 0));
        panel.add(upCheckBox, GridBagHelper.pos_size(0, 1, 2, 1));
        return panel;
    }

    /**
     *
     */
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
