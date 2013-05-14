package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import java.util.Arrays;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Apply a median filter on an input image.
 */
public final class MedianFilter implements IFilter, IModule {

    /**
     * Setting the cross pattern will calculate the median of 5 values (center, left, right, top, and bottom).
     * 
     * Cross pattern:
     * <pre>
     * {@code
     * .#.
     * ###
     * .#.}
     * </pre>
     */
    public static final int CROSS = 4;
    
    /**
     * Setting the cross pattern will calculate the median of all 9 values.
     * 
     * Box pattern:
     * <pre>
     * {@code
     * ###
     * ###
     * ###}
     * </pre>
     */
    public static final int BOX = 8;
    
    private int pattern;
    private int size;
    
    private JTextField sizeTextField;
    private JRadioButton patternCrossRadioButton, patternBoxRadioButton;

    /**
     * Initialize the filter.
     * 
     * @param pattern one of the pre-defined patterns ({@code BOX or CROSS})
     * @param size size of the median filter, typically 3, 5, or 7, which selects
     *             points from a box of size 3x3, 5x5, or 7x7 respectively
     */
    public MedianFilter(int pattern, int size) {
        assert ((pattern == BOX) || (pattern == CROSS));

        this.pattern = pattern;
        this.size = size;
    }

    /**
     * Go through the input {@code image}, calculate median at each position,
     * and save the result into the output image at the same position as the
     * median was calculated at.
     *
     * @param image an input image
     * @return a <strong>new instance</strong> of FloatProcessor that contains
     *         the filtered image (matrix of medians)
     */
    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        FloatProcessor result = new FloatProcessor(image.getWidth(), image.getHeight());
        if (pattern == BOX) {
            float [] items = new float[size*size];
            
                for (int y = 0, ym = image.getHeight(); y < ym; y++) {
                    for (int x = 0, xm = image.getWidth(); x < xm; x++) {
                    int ii = 0;
                    for(int i = x - size/2, im = i + size; i < im; i++) {
                        for(int j = y - size/2, jm = j + size; j < jm; j++) {
                            if((i >= 0) && (i < xm) && (j >= 0) && (j < ym)) {
                                items[ii] = image.getPixelValue(i, j);
                                ii++;
                            }
                        }
                    }
                    Arrays.sort(items, 0, ii);
                    result.setf(x, y, ((ii%2==1) ? items[ii/2] : ((items[(ii-1)/2] + items[ii/2]) / 2.0f)));    // median evaluation, same as in Matlab
                }
            }
        } else {
            float [] items = new float[2*size-1];
            for (int x = 0, xm = image.getWidth(); x < xm; x++) {
                for (int y = 0, ym = image.getHeight(); y < ym; y++) {
                    int ii = 0;
                    for(int i = x - size/2, im = i + size; i < im; i++) {
                        if((i >= 0) && (i < xm)) {  // check for boudaries
                            items[ii] = image.getPixelValue(i, y);
                            ii++;
                        }
                    }
                    for(int j = y - size/2, jm = j + size; j < jm; j++) {
                        if((j >= 0) && (j < ym) && (j != y)) {  // check for boundaries and skip the center of the cross to avoid storing one value multiple times
                            items[ii] = image.getPixelValue(x, j);
                            ii++;
                        }
                    }
                    Arrays.sort(items, 0, ii);
                    result.setf(x, y, ((ii%2==1) ? items[ii/2] : ((items[(ii-1)/2] + items[ii/2]) / 2.0f)));    // median evaluation, same as in Matlab
                }
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "Median filter";
    }

    @Override
    public JPanel getOptionsPanel() {
        patternBoxRadioButton = new JRadioButton("box");
        patternCrossRadioButton = new JRadioButton("cross");
        sizeTextField = new JTextField(Integer.toString(size), 20);
        //
        patternBoxRadioButton.setSelected(pattern == BOX);
        patternCrossRadioButton.setSelected(pattern == CROSS);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Pattern: "), GridBagHelper.pos(0, 0));
        panel.add(patternBoxRadioButton, GridBagHelper.pos(1, 0));
        panel.add(patternCrossRadioButton, GridBagHelper.pos(1, 1));
        panel.add(new JLabel("Size: "), GridBagHelper.pos(0, 2));
        panel.add(sizeTextField, GridBagHelper.pos(1, 2));
        return panel;
    }

    @Override
    public void readParameters() {
        try {
            size = Integer.parseInt(sizeTextField.getText());
            if(patternBoxRadioButton.isSelected()) pattern = BOX;
            if(patternCrossRadioButton.isSelected()) pattern = CROSS;
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }
}
