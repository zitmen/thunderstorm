package cz.cuni.lf1.lge.ThunderSTORM.UI;

import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.Line;
import ij.gui.Overlay;
import java.awt.Color;

/**
 * Overlay for preview of results.
 */
public class RenderingOverlay {

    /**
     * Cross.
     */
    public static final int MARKER_CROSS = 1;
    
    /**
     * Circle.
     */
    public static final int MARKER_CIRCLE = 2;

    /**
     * Put markers at the positions of positions of molecules specified by X,Y coordinates on to an input image.
     * 
     * If the input {@code ImagePlus} instance contains a stack, the marker will be put
     * into one global Overlay, which is shown for each slice of the stack.
     *
     * @param imp image window to which the markers will be drawn
     * @param xCoord array of X coordinates
     * @param yCoord array of Y coordinates
     * @param c color of markers
     * @param markerType one of the predefined marker-types ({@code MARKER_CIRCLE} or {@code MARKER_CROSS})
     */
    public static void showPointsInImage(ImagePlus imp, double[] xCoord, double[] yCoord, Color c, int markerType) {
        assert(xCoord.length == yCoord.length);
        
        Overlay overlay = imp.getOverlay();
        if (overlay == null) {
            overlay = new Overlay();
        }
        addPointsToOverlay(xCoord, yCoord, overlay, 0, c, markerType);
        imp.setOverlay(overlay);
    }

    /**
     * Put markers at the positions of positions of molecules specified by X,Y coordinates on to a specific slice of a stack.
     * 
     * @param imp image window to which the markers will be drawn
     * @param xCoord array of X coordinates
     * @param yCoord array of Y coordinates
     * @param slice slice number (indexing starts from 1). If the slice number is 0, the markers
     *              will be put into one global Overlay, which is shown for each slice of the stack.
     * @param c color of markers
     * @param markerType one of the predefined marker-types ({@code MARKER_CIRCLE} or {@code MARKER_CROSS})
     */
    public static void showPointsInImageSlice(ImagePlus imp, double[] xCoord, double[] yCoord, int slice, Color c, int markerType) {
        assert(xCoord.length == yCoord.length);
        
        Overlay overlay = imp.getOverlay();
        if (overlay == null) {
            overlay = new Overlay();
        }
        addPointsToOverlay(xCoord, yCoord, overlay, slice, c, markerType);
        imp.setOverlay(overlay);
    }

    private static Overlay addPointsToOverlay(double[] xCoord, double[] yCoord, Overlay overlay, int slice, Color c, int markerType) {
        double halfSize = 1;
        for (int i = 0; i < xCoord.length; i++) {
            switch (markerType) {
                case MARKER_CROSS:
                    Line horizontalLine = new Line(xCoord[i] - halfSize, yCoord[i], xCoord[i] + halfSize, yCoord[i]);
                    Line verticalLine = new Line(xCoord[i], yCoord[i] - halfSize, xCoord[i], yCoord[i] + halfSize);
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
                    
                case MARKER_CIRCLE:
                    EllipseRoi ellipse = new EllipseRoi(xCoord[i] - halfSize, yCoord[i], xCoord[i] + halfSize, yCoord[i], 1);
                    ellipse.setName("" + (i + 1));
                    ellipse.setPosition(slice);
                    if (c != null) {
                        ellipse.setStrokeColor(c);
                    }
                    overlay.add(ellipse);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Unknown marker type!");
            }
        }
        return overlay;
    }
}
