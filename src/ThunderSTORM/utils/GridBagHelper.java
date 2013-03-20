package ThunderSTORM.utils;

import java.awt.GridBagConstraints;

public class GridBagHelper {
    
    private static GridBagConstraints gbc = new GridBagConstraints();
    
    public static GridBagConstraints pos(int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
    }
    
    public static GridBagConstraints width(int w, int h) {
        gbc.gridwidth = w;
        gbc.gridheight = h;
        return gbc;
    }
    
}
