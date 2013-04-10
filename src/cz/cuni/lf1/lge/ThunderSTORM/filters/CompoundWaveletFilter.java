package cz.cuni.lf1.lge.ThunderSTORM.filters;

import static cz.cuni.lf1.lge.ThunderSTORM.utils.ImageProcessor.subtractImage;
import static cz.cuni.lf1.lge.ThunderSTORM.utils.ImageProcessor.cropImage;
import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.utils.Padding;
import ij.IJ;
import ij.process.FloatProcessor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

// Here we use double padding to simulate conv2 function and to keep the results identical to the Matlab version.
public final class CompoundWaveletFilter implements IFilter, IModule {

    private int margin;
    private boolean third_plane;
    
    private WaveletFilter w1, w2, w3;
    
    private JCheckBox thirdCheckBox;
    
    public CompoundWaveletFilter(boolean third_plane) {
        this.third_plane = third_plane;
        //
        w1 = new WaveletFilter(1, Padding.PADDING_ZERO);
        w2 = new WaveletFilter(2, Padding.PADDING_ZERO);
        w3 = new WaveletFilter(3, Padding.PADDING_ZERO);
        //
        this.margin = w3.getKernelX().getWidth() / 2;
    }
    
    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        FloatProcessor padded = Padding.addBorder(image, Padding.PADDING_DUPLICATE, margin);
        FloatProcessor V1 = w1.filterImage(padded);
        FloatProcessor V2 = w2.filterImage(V1);
        
        //double stddev = cropImage(subtractImage(image, V1), margin, margin, image.getWidth(), image.getHeight()).getStatistics().stdDev;
        
        if (third_plane) {
            FloatProcessor V3 = w3.filterImage(V2);
            return cropImage(subtractImage(V2, V3), margin, margin, image.getWidth(), image.getHeight());
        }
        return cropImage(subtractImage(V1, V2), margin, margin, image.getWidth(), image.getHeight());
    }
    
    @Override
    public String getName() {
        return "Wavelet filter";
    }

    @Override
    public JPanel getOptionsPanel() {
        thirdCheckBox = new JCheckBox("third plane");
        thirdCheckBox.setSelected(third_plane);
        //
        JPanel panel = new JPanel();
        panel.add(thirdCheckBox);
        return panel;
    }

    @Override
    public void readParameters() {
        try {
            third_plane = thirdCheckBox.isSelected();
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }
    
}
