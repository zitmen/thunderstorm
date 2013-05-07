package cz.cuni.lf1.lge.ThunderSTORM.util;

import java.awt.GridBagConstraints;

/**
 * A very simple helper class for working with java.awt.GridBagLayout class.
 * 
 * When adding control elements (labels, buttons, etc.) into a panel
 * ({@code JPanel}) using the {@code GridBagLayout} it is necessary to provide
 * an instance of {@code GridBagLayoutContraints} class for every single
 * control element to specify its position and size in the grid of
 * {@code GridBagLayout}. And this is exactly what this helper provides.
 * So the whole thing simplifies to:
 * <pre>
 * {@code
 * JPanel panel = new JPanel(new GridBagLayout());
 * panel.add(new JLabel("Hello!"), GridBagHelper.pos(0, 0));}
 * </pre>
 * So the whole call of {@code panel.add} can be in a single line without
 * filling the {@code GridBagConstraints} class every single call.
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
     * Get a pre-allocated {@code GridBagConstraints} instance filled with X,Y
     * coordinates of a control element in a grid.
     * 
     * <strong>Width and height is always set to 1!</strong>
     * For changing the width and/or the height use {@code pos_size} method.
     * 
     * @param x X coordinate of a control element in a grid
     * @param y Y coordinate of a control element in a grid
     * @return the pre-allocated instance of{@code GridBagConstraints} class
     *         filled with {@code x,y} values specifying the position
     *         of a control element in a grid.
     */
    public static GridBagConstraints pos(int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        width(1, 1);
        return gbc;
    }
    
    /**
     * Get a pre-allocated {@code GridBagConstraints} instance filled with X,Y
     * coordinates, width, and height of a control element in a grid.
     * 
     * @param x X coordinate of a control element in a grid
     * @param y Y coordinate of a control element in a grid
     * @param w width of a control element in a grid
     * @param h height of a control element in a grid
     * @return the pre-allocated instance of {@code GridBagConstraints} class
     *         filled with {@code x,y} and {@code w,h} values specifying the
     *         position, width, and height of a control element in a grid.
     */
    public static GridBagConstraints pos_size(int x, int y, int w, int h) {
        pos(x, y);
        width(w, h);
        return gbc;
    }
    
}
