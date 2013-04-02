package ThunderSTORM.UI;

import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.Line;
import ij.gui.Overlay;
import java.awt.Color;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class RenderingOverlay {

    public static final int CROSS = 1;
    public static final int CIRCLE = 2;

    public static void showPointsInImage(ImagePlus imp, double[] xCoord, double[] yCoord, Color c, int markerType) {
        Overlay overlay = imp.getOverlay();
        if (overlay == null) {
            overlay = new Overlay();
        }
        addPointsToOverlay(xCoord, yCoord, overlay, 0, c, markerType);
        imp.setOverlay(overlay);
    }

    public static void showPointsInImageSlice(ImagePlus imp, double[] xCoord, double[] yCoord, int slice, Color c, int markerType) {
        Overlay overlay = imp.getOverlay();
        if (overlay == null) {
            overlay = new Overlay();
        }
        addPointsToOverlay(xCoord, yCoord, overlay, slice, c, markerType);
        imp.setOverlay(overlay);
    }

    /**
     * point with coordinates [0,0] will show in the center of top left pixel
     * this is different from imageJ default behavior, where the center of the
     * top left pixel would have coordinates [0.5 0.5]
     */
    static Overlay addPointsToOverlay(double[] xCoord, double[] yCoord, Overlay overlay, int slice, Color c, int markerType) {
        double halfSize = 1;
        for (int i = 0; i < xCoord.length; i++) {
            switch (markerType) {
                case CROSS:
                    Line horizontalLine = new Line(0.5 + xCoord[i] - halfSize, 0.5 + yCoord[i], 0.5 + xCoord[i] + halfSize, 0.5 + yCoord[i]);
                    Line verticalLine = new Line(0.5 + xCoord[i], 0.5 + yCoord[i] - halfSize, 0.5 + xCoord[i], 0.5 + yCoord[i] + halfSize);
                    if (c != null) {
                        verticalLine.setStrokeColor(c);
                        horizontalLine.setStrokeColor(c);
                    }
                    verticalLine.setName("" + (i + 1));
                    horizontalLine.setName("");
                    horizontalLine.setPosition(slice);
                    verticalLine.setPosition(slice);
                    overlay.add(horizontalLine);
                    overlay.add(verticalLine);
                    break;
                case CIRCLE:
                    EllipseRoi ellipse = new EllipseRoi(0.5 + xCoord[i] - halfSize, 0.5 + yCoord[i], 0.5 + xCoord[i] + halfSize, 0.5 + yCoord[i], 1);
                    ellipse.setName("" + (i + 1));
                    ellipse.setPosition(slice);
                    if (c != null) {
                        ellipse.setStrokeColor(c);
                    }
                    overlay.add(ellipse);
                    break;
                default:
                    throw new IllegalArgumentException("unknown marker type");
            }
        }
        return overlay;
    }
}
