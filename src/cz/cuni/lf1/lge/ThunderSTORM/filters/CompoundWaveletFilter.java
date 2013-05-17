package cz.cuni.lf1.lge.ThunderSTORM.filters;

import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.subtractImage;
import static cz.cuni.lf1.lge.ThunderSTORM.util.ImageProcessor.cropImage;
import cz.cuni.lf1.lge.ThunderSTORM.util.Padding;
import ij.process.FloatProcessor;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

/**
 * This wavelet filter is implemented as an undecimated wavelet transform using B-spline of third order.
 * 
 * This filter uses the separable kernel feature.
 * Note that in the convolution with the wavelet kernels we use padding twice to
 * simulate {@code conv2} function from Matlab to keep the results identical to
 * the results we got in Matlab version of ThunderSTORM.
 * 
 * @see WaveletFilter
 * @see ConvolutionFilter
 */
public final class CompoundWaveletFilter implements IFilter {

    private int margin;
    private boolean third_plane;
    
    private WaveletFilter w1, w2, w3;
    
    private JCheckBox thirdCheckBox;

  public CompoundWaveletFilter() {
    this(false);
  }
    
    /**
     * Initialize the filter with all the wavelet kernels needed to create the wavelet transform.
     *
     * @param third_plane if {@code true} use 3rd plane for detection; otherwise use 2nd plane instead
     */
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
        third_plane = thirdCheckBox.isSelected();
    }
    
}
