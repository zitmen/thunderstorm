package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.ImagePlus;
import ij.gui.EllipseRoi;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Roi;
import java.awt.Color;
import java.awt.Rectangle;

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
     * Put markers at the positions of positions of molecules specified by X,Y
     * coordinates on to an input image.
     *
     * If the input {@code ImagePlus} instance contains a stack, the marker will
     * be put into one global Overlay, which is shown for each slice of the
     * stack.
     *
     * @param imp image window to which the markers will be drawn
     * @param xCoord array of X coordinates
     * @param yCoord array of Y coordinates
     * @param c color of markers
     * @param markerType one of the predefined marker-types
     * ({@code MARKER_CIRCLE} or {@code MARKER_CROSS})
     */
    public static void showPointsInImage(ImagePlus imp, double[] xCoord, double[] yCoord, Color c, int markerType) {
        assert (xCoord.length == yCoord.length);

        Overlay overlay = imp.getOverlay();
        if(overlay == null) {
            overlay = new Overlay();
        }
        addPointsToOverlay(xCoord, yCoord, overlay, 0, c, markerType);
        imp.setOverlay(overlay);
    }

    /**
     * Put markers at the positions of positions of molecules specified by X,Y
     * coordinates on to a specific slice of a stack.
     *
     * @param imp image window to which the markers will be drawn
     * @param xCoord array of X coordinates
     * @param yCoord array of Y coordinates
     * @param slice slice number (indexing starts from 1). If the slice number
     * is 0, the markers will be put into one global Overlay, which is shown for
     * each slice of the stack.
     * @param c color of markers
     * @param markerType one of the predefined marker-types
     * ({@code MARKER_CIRCLE} or {@code MARKER_CROSS})
     */
    public static void showPointsInImageSlice(ImagePlus imp, double[] xCoord, double[] yCoord, int slice, Color c, int markerType) {
        assert (xCoord.length == yCoord.length);

        Overlay overlay = imp.getOverlay();
        if(overlay == null) {
            overlay = new Overlay();
        }
        addPointsToOverlay(xCoord, yCoord, overlay, slice, c, markerType);
        imp.setOverlay(overlay);
    }

    public static Overlay addPointsToOverlay(double[] xCoord, double[] yCoord, Overlay overlay, int slice, Color c, int markerType) {
        switch(markerType) {
            case MARKER_CROSS:
                for(int i = 0; i < xCoord.length; i++) {
                    drawCross(i + 1, xCoord[i], yCoord[i], slice, overlay, c);
                }
                break;

            case MARKER_CIRCLE:
                for(int i = 0; i < xCoord.length; i++) {
                    drawCircle(i + 1, xCoord[i], yCoord[i], slice, overlay, c, 1.0);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown marker type!");
        }
        return overlay;
    }
    
    public static void showPointsInImage(IJResultsTable rt, ImagePlus imp, Rectangle roi, Color c, int markerType) {
        Overlay overlay = imp.getOverlay();
        if(overlay == null) {
            overlay = new Overlay();
        }
        Molecule mol;
        Units unitsX = rt.getColumnUnits(PSFModel.Params.LABEL_X);
        Units unitsY = rt.getColumnUnits(PSFModel.Params.LABEL_Y);
        Units target = MoleculeDescriptor.Units.PIXEL;
        if(roi == null) {
            Roi r = imp.getRoi();
            if(r != null) {
                roi = r.getBounds();
            } else {
                roi = new Rectangle(0, 0, imp.getWidth(), imp.getHeight());
            }
        }
        switch(markerType) {
            case MARKER_CROSS:
                for(int r = 0, rows = rt.getRowCount(); r < rows; r++) {
                    mol = rt.getRow(r);
                    drawCross((int)mol.getParam(MoleculeDescriptor.LABEL_ID),
                            roi.x + unitsX.convertTo(target, mol.getParam(PSFModel.Params.LABEL_X)),
                            roi.y + unitsY.convertTo(target, mol.getParam(PSFModel.Params.LABEL_Y)),
                            (int)mol.getParam(MoleculeDescriptor.LABEL_FRAME),
                            overlay, c);
                }
                break;

            case MARKER_CIRCLE:
                for(int r = 0, rows = rt.getRowCount(); r < rows; r++) {
                    mol = rt.getRow(r);
                    drawCircle((int)mol.getParam(MoleculeDescriptor.LABEL_ID),
                            roi.x + unitsX.convertTo(target, mol.getParam(PSFModel.Params.LABEL_X)),
                            roi.y + unitsY.convertTo(target, mol.getParam(PSFModel.Params.LABEL_Y)),
                            (int)mol.getParam(MoleculeDescriptor.LABEL_FRAME),
                            overlay, c, 1.0);
                }
                break;

            default:
                throw new IllegalArgumentException("Unknown marker type!");
        }
        imp.setOverlay(overlay);
    }

    public static void drawCross(int id, double xCoord, double yCoord, int slice, Overlay overlay, Color c) {
        double halfSize = 1;
        Line horizontalLine = new Line(xCoord - halfSize, yCoord, xCoord + halfSize, yCoord);
        Line verticalLine = new Line(xCoord, yCoord - halfSize, xCoord, yCoord + halfSize);
        if(c != null) {
            verticalLine.setStrokeColor(c);
            horizontalLine.setStrokeColor(c);
        }
        verticalLine.setName(Integer.toString(id));
        horizontalLine.setName("");
        horizontalLine.setPosition(slice);
        verticalLine.setPosition(slice);
        overlay.add(horizontalLine);
        overlay.add(verticalLine);
    }

    public static void drawCircle(int id, double xCoord, double yCoord, int slice, Overlay overlay, Color c, double radius) {
        EllipseRoi ellipse = new EllipseRoi(xCoord - radius, yCoord, xCoord + radius, yCoord, 1);
        ellipse.setName(Integer.toString(id));
        ellipse.setPosition(slice);
        if(c != null) {
            ellipse.setStrokeColor(c);
        }
        overlay.add(ellipse);
    }
}
