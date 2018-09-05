
package cz.cuni.lf1.lge.ThunderSTORM.UI;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.java.balloontip.BalloonTip;

import ij.IJ;

public class GUI {

	public static void setLookAndFeel() {
		// Use an appropriate Look and Feel
		try {
			String systemLAFName = UIManager.getSystemLookAndFeelClassName();
			if ("javax.swing.plaf.metal.MetalLookAndFeel".equals(systemLAFName)) {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
			}
			UIManager.setLookAndFeel(systemLAFName);
			// UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
		}
		catch (Exception ex) {
			IJ.handleException(ex);
		}
	}

	private static BalloonTip baloon = null;

	public static void showBalloonTip(JComponent attachedComponent, String tip) {
		closeBalloonTip();
		baloon = new BalloonTip(attachedComponent, tip);
	}

	public static void closeBalloonTip() {
		if (baloon != null) {
			baloon.closeBalloon();
			baloon = null;
		}
	}

	/**
	 * Throws StoppedByUserException if IJ.escapePressed flag is true. Does not
	 * reset the flag.
	 */
	public static void checkIJEscapePressed() throws StoppedByUserException {
		if (IJ.escapePressed()) {
			throw new StoppedByUserException("Escape pressed");
		}
	}

	public static void runOnUIThreadAndWait(Runnable action) {
		if (SwingUtilities.isEventDispatchThread()) {
			action.run();
		}
		else {
			try {
				SwingUtilities.invokeAndWait(action);
			}
			catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
			catch (InvocationTargetException ex) {
				throw new RuntimeException(ex.getCause());
			}
		}
	}

}
