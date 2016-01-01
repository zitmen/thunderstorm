package cz.cuni.lf1.lge.ThunderSTORM.estimators;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.StoppedByUserException;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.Homography;
import cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MaxCountExceededException;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class MultipleLocationsBiplaneFitting implements IBiplaneEstimator {

    FloatProcessor plane1, plane2;
    List<Pair<Point, Point>> locations;
    double distThrPx;
    double[] subxgrid1;
    double[] subygrid1;
    double[] subxgrid2;
    double[] subygrid2;
    double[] subimageData1;
    double[] subimageData2;
    int subimageSize;
    int bigSubImageSize;
    double[] xgrid1;
    double[] ygrid1;
    double[] xgrid2;
    double[] ygrid2;
    Vector<Molecule> results;
    final IOneLocationBiplaneFitter fitter;
    MoleculeDescriptor moleculeDescriptor;
    Homography.TransformationMatrix homography, homographyInverse;
    PSFSeparator.Position mPos;
    List<PSFSeparator.Position> mPositions;

    public MultipleLocationsBiplaneFitting(int fittingRadius, double distThrPx, Homography.TransformationMatrix homography, IOneLocationBiplaneFitter fitter) {
        this.subimageSize = fittingRadius;
        this.distThrPx = distThrPx;
        this.homography = homography;
        this.homographyInverse = homography.inverse();
        this.fitter = fitter;

        bigSubImageSize = 2 * fittingRadius + 1;
        subxgrid1 = new double[bigSubImageSize * bigSubImageSize];  // to prevent multiple allocations
        subxgrid2 = new double[bigSubImageSize * bigSubImageSize];  // to prevent multiple allocations
        subygrid1 = new double[bigSubImageSize * bigSubImageSize];  // to prevent multiple allocations
        subygrid2 = new double[bigSubImageSize * bigSubImageSize];  // to prevent multiple allocations
        subimageData1 = new double[bigSubImageSize * bigSubImageSize];  // to prevent multiple allocations
        subimageData2 = new double[bigSubImageSize * bigSubImageSize];  // to prevent multiple allocations

        mPos = new PSFSeparator.Position();
        mPositions = new ArrayList<PSFSeparator.Position>();
        mPositions.add(mPos);
    }

    private void initializeGrid() {
        if (plane1 == null || xgrid1 != null) return;

        xgrid1 = new double[plane1.getWidth() * plane1.getHeight()];
        ygrid1 = new double[plane1.getWidth() * plane1.getHeight()];
        xgrid2 = new double[plane2.getWidth() * plane2.getHeight()];
        ygrid2 = new double[plane2.getWidth() * plane2.getHeight()];

        for(int i = 0, idx = 0; i < plane1.getHeight(); i++) {
            for(int j = 0; j < plane1.getWidth(); j++, idx++) {
                xgrid1[idx] = j + 0.5; ygrid1[idx] = i + 0.5;
                PSFSeparator.Position pos = transformPos(homography, xgrid1[idx], ygrid1[idx]);
                xgrid2[idx] = pos.getX(); ygrid2[idx] = pos.getY();
            }
        }
    }

    private PSFSeparator.Position transformPos(Homography.TransformationMatrix transform, double x, double y) {
        mPos.setX(x);
        mPos.setY(y);
        return Homography.transformPositions(transform, mPositions, plane1.getWidth(), plane1.getHeight()).get(0);
    }

    public boolean extractSubGrid(double[] /*[out]*/subxgrid, double[] /*[out]*/subygrid, double[] /*[out]*/data, double[] xgrid, double[] ygrid, FloatProcessor image, double x, double y) {
        int w = image.getWidth();
        int h = image.getHeight();
        // find the center
        int xi = 0, yi = 0;
        double minDist = Double.MAX_VALUE;
        for (int i = 0, index = 0; i < h; i++) {
            for (int j = 0; j < w; j++, index++) {
                double tmp = MathProxy.sqr(xgrid[index] - x) + MathProxy.sqr(ygrid[index] - y);
                if (tmp < minDist) {
                    minDist = tmp;
                    xi = j;
                    yi = i;
                }
            }
        }
        // check the boundaries
        if ((xi - subimageSize < 0) || (xi + subimageSize >= w)) return false;
        if ((yi - subimageSize < 0) || (yi + subimageSize >= h)) return false;
        // fill the subimage arrays
        float[] pixels = (float[]) image.getPixels();
        for (int ys = yi - subimageSize, ysm = yi + subimageSize, index = 0; ys <= ysm; ys++) {
            for (int xs = xi - subimageSize, xsm = xi + subimageSize; xs <= xsm; xs++, index++) {
                int idx = ys*w+xs;
                subxgrid[index] = xgrid[idx];
                subygrid[index] = ygrid[idx];
                data[index] = pixels[idx];
            }
        }
        return true;
    }

    public void run() throws StoppedByUserException {

        for (Pair<Point, Point> location : locations) {
            GUI.checkIJEscapePressed();

            PSFSeparator.Position pos = transformPos(homographyInverse,
                    location.second.getX().doubleValue() + 0.5,
                    location.second.getY().doubleValue() + 0.5);
            double posX = (pos.getX() + location.first.getX().doubleValue() + 0.5) / 2.0;
            double posY = (pos.getY() + location.first.getY().doubleValue() + 0.5) / 2.0;

            if (!isCloseToBorder(plane1, location.first) && !isCloseToBorder(plane2, location.second)
                && extractSubGrid(subxgrid1, subygrid1, subimageData1, xgrid1, ygrid1, plane1, posX, posY)
                && extractSubGrid(subxgrid2, subygrid2, subimageData2, xgrid2, ygrid2, plane2, posX, posY)) {
                try {
                    SubImage subImage1 = new SubImage(
                            bigSubImageSize, bigSubImageSize,
                            subxgrid1, subygrid1, subimageData1,
                            posX, posY);

                    SubImage subImage2 = new SubImage(
                            bigSubImageSize, bigSubImageSize,
                            subxgrid2, subygrid2, subimageData2,
                            posX, posY);

                    Molecule psf = fitter.fit(subImage1, subImage2);
                    //replace molecule descriptor to a common one for all molecules
                    if (moleculeDescriptor != null) {
                        moleculeDescriptor.validateMolecule(psf);
                        psf.descriptor = moleculeDescriptor;
                    } else {
                        moleculeDescriptor = psf.descriptor;
                    }
                    if (checkIsInSubimage(posX, posY, psf.getX(), psf.getY())) {
                        psf.setDetections(null);
                        results.add(psf);
                    }
                } catch (MaxCountExceededException ex) {
                    // maximum number of iterations has been exceeded (it is set very high, so it usually means trouble)
                    IJ.log("Warning: the fitter couldn't converge (max. count of iterations was exceeded)! The molecule candidate has been thrown away.");
                } catch (ConvergenceException ex) {
                    // exception: "org.apache.commons.math3.exception.ConvergenceException: illegal state:
                    //             unable to perform Q.R decomposition on the 49x11 jacobian matrix"
                    // -> probably NaN or Inf value in one of the estimated parameter
                    //    or during the evaluation of PSF model or ots derivative
                    // -> another possible reason is that camera offset is set too high, which in combination with WLSQ
                    //    fitting may lead to division by zero, since intensities are used for weighting!
                    IJ.log("Warning: the fitter couldn't converge (probably NaN or Inf occurred in calculations; if you use WLSQ fitting, check if camera offset isn't too high correctly; if not try MLE or LSQ fitting)! The molecule candidate has been thrown away.");
                } catch (Exception ex) {
                    IJ.log("Error: " + ex.getMessage());
                }
            }
        }
    }

    private boolean isCloseToBorder(FloatProcessor image, Point pos) {
        double x = pos.getX().doubleValue(), y = pos.getY().doubleValue();
        return x < (double)subimageSize || x > (double)(image.getWidth() - subimageSize)
            || y < (double)subimageSize || y > (double)(image.getHeight() - subimageSize);
    }

    private boolean checkIsInSubimage(double xinit, double yinit, double x, double y) {
        return !(Math.abs(x - xinit) > ((double)subimageSize + 0.5) || Math.abs(y - yinit) > ((double)subimageSize + 0.5));
    }

    @Override
    public Vector<Molecule> estimateParameters(FloatProcessor plane1, FloatProcessor plane2,
                                               List<Point> detections1, List<Point> detections2) throws StoppedByUserException{
        this.plane1 = plane1;
        this.plane2 = plane2;
        this.locations = Homography.mergePositions(plane1.getWidth(), plane1.getHeight(),
                                    homography, detections1, detections2, distThrPx * distThrPx);
        initializeGrid();
        results = new Vector<Molecule>();
        run();
        return extractMacroMolecules(results);
    }

    private Vector<Molecule> extractMacroMolecules(Vector<Molecule> macroMolecules) {
        Vector<Molecule> extracted = new Vector<Molecule>();
        for(Molecule mol : macroMolecules) {
            if(mol.isSingleMolecule()) {
                extracted.add(mol);
            } else {
                extracted.addAll(mol.getDetections());
            }
        }
        return extracted;
    }
}
