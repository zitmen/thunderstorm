package cz.cuni.lf1.lge.ThunderSTORM.UI;

import ij.IJ;
import javax.swing.JComponent;
import javax.swing.UIManager;
import net.java.balloontip.BalloonTip;

public class GUI {

    public static void setLookAndFeel() {
        // Use an appropriate Look and Feel
        try {
            String systemLAFName = UIManager.getSystemLookAndFeelClassName();
            if("javax.swing.plaf.metal.MetalLookAndFeel".equals(systemLAFName)) {
                UIManager.put("swing.boldMetal", Boolean.FALSE);
            }
            UIManager.setLookAndFeel(systemLAFName);
            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
        } catch(Exception ex) {
            IJ.handleException(ex);
        }
    }
    private static BalloonTip baloon = null;

    public static void showBalloonTip(JComponent attachedComponent, String tip) {
        closeBalloonTip();
        baloon = new BalloonTip(attachedComponent, tip);
    }

    public static void closeBalloonTip() {
        if(baloon != null) {
            baloon.closeBalloon();
            baloon = null;
        }
    }
}
