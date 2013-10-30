package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections.primitives.ArrayFloatList;
import org.apache.commons.collections.primitives.FloatList;

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
    public static void showPointsInImage(ImagePlus imp, double[] xCoord, double[] yCoord, int slice, Color c, int markerType) {
        assert (xCoord.length == yCoord.length);

        Overlay overlay = imp.getOverlay();
        if(overlay == null) {
            overlay = new Overlay();
        }
        addPointsToOverlay(xCoord, yCoord, overlay, slice, c, markerType);
        imp.setOverlay(overlay);
    }

    public static Overlay addPointsToOverlay(double[] xCoord, double[] yCoord, Overlay overlay, int slice, Color c, int markerType) {
        assert xCoord.length == yCoord.length;
        float[] xs = new float[xCoord.length];
        float[] ys = new float[yCoord.length];
        for(int i = 0; i < xCoord.length; i++) {
            xs[i] = (float) xCoord[i];
            ys[i] = (float) yCoord[i];
        }
        overlay.add(new MultiplePointsRoi(xs, ys, slice, c, markerType));
        return overlay;
    }

    /**
     * Shows all molecule locations from table in image overlay. Compound
     * molecules are expanded.
     *
     * @param rt
     * @param imp
     * @param roi
     * @param c
     * @param markerType
     */
    public static void showPointsInImage(IJResultsTable rt, ImagePlus imp, Rectangle roi, Color c, int markerType) {
        if(rt.isEmpty()) {
            return;
        }
        if(!rt.columnExists(LABEL_X) || !rt.columnExists(LABEL_Y)) {
            return;
        }
        List<Molecule> mols = new ArrayList<Molecule>(rt.getRowCount());
        for(int r = 0, rows = rt.getRowCount(); r < rows; r++) {
            Molecule m = rt.getRow(r);
            mols.add(m);
            if(!m.isSingleMolecule()) {
                mols.addAll(m.getDetections());
            }
        }
        showPointsInImage(mols.toArray(new Molecule[0]), imp, roi, c, markerType);
    }

    /**
     * Shows molecule locations in image overlay. Compound molecules are not
     * expanded - only the parent molecule is shown.
     *
     * @param mols
     * @param imp
     * @param roi
     * @param c
     * @param markerType
     */
    public static void showPointsInImage(Molecule[] mols, ImagePlus imp, Rectangle roi, Color c, int markerType) {
        Overlay overlay = imp.getOverlay();
        if(overlay == null) {
            overlay = new Overlay();
        }

        if(roi == null) {
            Roi r = imp.getRoi();
            if(r != null) {
                roi = r.getBounds();
            } else {
                roi = new Rectangle(0, 0, imp.getWidth(), imp.getHeight());
            }
        }

        Arrays.sort(mols, new Comparator<Molecule>() {
            @Override
            public int compare(Molecule o1, Molecule o2) {
                return Double.compare(o1.getParam(MoleculeDescriptor.LABEL_FRAME), o2.getParam(MoleculeDescriptor.LABEL_FRAME));
            }
        });
        int oldFrame = -1;
        FloatList xs = new ArrayFloatList();
        FloatList ys = new ArrayFloatList();
        for(Molecule mol : mols) {
            int frame = (int) mol.getParam(MoleculeDescriptor.LABEL_FRAME);
            if(frame != oldFrame) {
                if(xs.size() > 0) {
                    overlay.add(new MultiplePointsRoi(xs.toArray(), ys.toArray(), oldFrame, c, markerType));
                    xs.clear();
                    ys.clear();
                }
            }
            xs.add((float) (roi.x + mol.getParam(PSFModel.Params.LABEL_X, Units.PIXEL)));
            ys.add((float) (roi.y + mol.getParam(PSFModel.Params.LABEL_Y, Units.PIXEL)));
            oldFrame = frame;
        }
        overlay.add(new MultiplePointsRoi(xs.toArray(), ys.toArray(), oldFrame, c, markerType));
        imp.setOverlay(overlay);
    }

    static class MultiplePointsRoi extends Roi {

        float[] xs;
        float[] ys;
        int markerType;

        public MultiplePointsRoi(float[] xs, float[] ys, int slice, Color c, int markerType) {
            super(0, 0, null);
            assert xs.length == ys.length;
            this.xs = xs;
            this.ys = ys;
            setPosition(slice);
            fillColor = c;
            this.markerType = markerType;
        }

        /**
         * Draws the points on the image.
         */
        @Override
        public void draw(Graphics g) {
            for(int i = 0; i < xs.length; i++) {
                //x,y added to coords because of the WTF way of flattening overlay in newer versions of ImageJ(1.45s works fine without it)
                //x,y is set to zeros in constructors, but imageJ sets it to (width, height) while flattening the overlay to signal that the roi should not be painted (the roi is painted outside the image)
                int x = getPolygon().xpoints[0];
                int y = getPolygon().ypoints[0];
                switch(markerType) {
                    case MARKER_CIRCLE:
                        drawCircle(g, screenXD(xs[i] + x), screenYD(ys[i] + y));
                        break;
                    case MARKER_CROSS:
                        drawCross(g, screenXD(xs[i] + x), screenYD(ys[i] + y));
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown marker type!");
                }
            }
            //showStatus();
            if(updateFullWindow) {
                updateFullWindow = false;
                imp.draw();
            }
        }

        void drawCross(Graphics g, int x, int y) {
            double magnification = getMagnification();
            int halfSize = (int) Math.ceil(1 * magnification);
            g.setColor(fillColor != null ? fillColor : Color.white);
            g.drawLine(x - halfSize, y, x + halfSize, y);
            g.drawLine(x, y - halfSize, x, y + halfSize);
        }

        void drawCircle(Graphics g, int x, int y) {
            double magnification = getMagnification();
            int halfSize = (int) Math.ceil(1 * magnification);
            g.setColor(fillColor != null ? fillColor : Color.white);
            g.drawOval(x - halfSize, y - halfSize, 2 * halfSize, 2 * halfSize);
        }

        @Override
        public MultiplePointsRoi clone() {
            MultiplePointsRoi r = (MultiplePointsRoi) super.clone();
            r.markerType = markerType;
            r.xs = xs.clone();
            r.ys = ys.clone();
            return r;
        }
    }
}
