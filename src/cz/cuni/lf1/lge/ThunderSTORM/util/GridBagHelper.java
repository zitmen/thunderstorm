package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.awt.GridBagConstraints;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public class GridBagHelper {
    
    private static GridBagConstraints gbc = new GridBagConstraints();
    
    private static GridBagConstraints width(int w, int h) {
        gbc.gridwidth = w;
        gbc.gridheight = h;
        return gbc;
    }
    
    /**
     *
     * @param x
     * @param y
     * @return
     */
    public static GridBagConstraints pos(int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        width(1, 1);
        return gbc;
    }
    
    /**
     *
     * @param x
     * @param y
     * @param w
     * @param h
     * @return
     */
    public static GridBagConstraints pos_width(int x, int y, int w, int h) {
        pos(x, y);
        width(w, h);
        return gbc;
    }
    
}
