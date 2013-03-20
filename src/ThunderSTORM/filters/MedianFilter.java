package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import ij.process.FloatProcessor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class MedianFilter implements IFilter, IModule {

    public static final int CROSS = 4;
    public static final int BOX = 8;
    private int pattern;
    private int size;

    public MedianFilter(int pattern, int size) {
        assert ((pattern == BOX) || (pattern == CROSS));

        this.pattern = pattern;
        this.size = size;
    }

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        FloatProcessor result = new FloatProcessor(image.getWidth(), image.getHeight());
        if (pattern == BOX) {
            float [] items = new float[size*size];
            for (int x = 0, xm = image.getWidth(); x < xm; x++) {
                for (int y = 0, ym = image.getHeight(); y < ym; y++) {
                    int ii = 0;
                    for(int i = x - size/2, im = i + size; i < im; i++) {
                        for(int j = x - size/2, jm = j + size; j < jm; j++) {
                            if((i >= 0) && (i < xm) && (j >= 0) && (j < ym)) {
                                items[ii] = image.getPixelValue(i, j);
                                ii++;
                            }
                        }
                    }
                    result.setf(x, y, items[ii/2]);
                }
            }
        } else {
            float [] items = new float[2*size];
            for (int x = 0, xm = image.getWidth(); x < xm; x++) {
                for (int y = 0, ym = image.getHeight(); y < ym; y++) {
                    int ii = 0;
                    for(int i = x - size/2, im = i + size; i < im; i++) {
                        if((i >= 0) && (i < xm)) {
                            items[ii] = image.getPixelValue(i, y);
                            ii++;
                        }
                    }
                    for(int j = y - size/2, jm = j + size; j < jm; j++) {
                        if((j >= 0) && (j < ym)) {
                            items[ii] = image.getPixelValue(x, j);
                            ii++;
                        }
                    }
                    result.setf(x, y, items[ii/2]);
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
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Pattern: "), gbc);
        gbc.gridx = 1;
        panel.add(new JRadioButton("box"), gbc);
        gbc.gridy = 1;
        panel.add(new JRadioButton("cross"), gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Size: "), gbc);
        gbc.gridx = 1;
        panel.add(new JTextField("Size", 20), gbc);
        return panel;
    }
}
