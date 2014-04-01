package cz.cuni.lf1.lge.ThunderSTORM.drift;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.results.*;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.optimizers.NelderMead;
import cz.cuni.lf1.lge.ThunderSTORM.util.IJProgressTracker;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.util.MathArrays;

public class FiducialDriftEstimator {

    public DriftResults estimateDrift(List<Molecule> molecules, double distanceThr, double onTimeRatio, double smoothingBandwidth) {
        int minFrame = (int) getMinFrame(molecules);
        int maxFrame = (int) getMaxFrame(molecules);

        //group molecules appearing in subsequent frames
        IJ.showStatus("Grouping molecules...");
        List<Molecule> groupedMolecules = groupMolecules(molecules, distanceThr);

        //select fiducial markers (molecules that are on for many frames)
        List<Molecule> fiducialMarkers = new ArrayList<Molecule>();
        for(Molecule mol : groupedMolecules) {
            if(mol.getParam(MoleculeDescriptor.LABEL_DETECTIONS) > onTimeRatio * (maxFrame - minFrame)) {
                fiducialMarkers.add(mol);
            }
        }
        if(fiducialMarkers.isEmpty()) {
            throw new RuntimeException("No fiducial markers found.");
        }
        //combine data points from multiple fiducial markers
        int dataPoints = countDetections(fiducialMarkers);
        double[] combinedFrames = new double[dataPoints];
        double[] combinedX = new double[dataPoints];
        double[] combinedY = new double[dataPoints];
        //combine frame data values
        int lastIndex = 0;
        for(Molecule mol : fiducialMarkers) {
            List<Molecule> detections = mol.getDetections();
            double[] frame = extractParamAsArray(detections, detections.get(0).descriptor.getParamIndex(MoleculeDescriptor.LABEL_FRAME));
            System.arraycopy(frame, 0, combinedFrames, lastIndex, frame.length);
            lastIndex += frame.length;
        }
        //find offsets for each fiducial marker (to get relative drift out of absolute coordinates)
        IJ.showStatus("Finding marker offsets (x)...");
        double[] markerOffsetsInX = findFiducialsOffsets(fiducialMarkers, combinedFrames, PSFModel.Params.LABEL_X);
        IJ.showProgress(0.875);
        GUI.checkIJEscapePressed();
        IJ.showStatus("Finding marker offsets (y)...");
        double[] markerOffsetsInY = findFiducialsOffsets(fiducialMarkers, combinedFrames, PSFModel.Params.LABEL_Y);
        IJ.showProgress(0.95);
        GUI.checkIJEscapePressed();
        //combine x,y, while subtracting the found offsets
        lastIndex = 0;
        for(int i = 0; i < fiducialMarkers.size(); i++) {
            List<Molecule> detections = fiducialMarkers.get(i).getDetections();
            double[] x = extractParamAsArray(detections, detections.get(0).descriptor.getParamIndex(PSFModel.Params.LABEL_X));
            double[] y = extractParamAsArray(detections, detections.get(0).descriptor.getParamIndex(PSFModel.Params.LABEL_Y));

            VectorMath.add(x, -markerOffsetsInX[i]);
            VectorMath.add(y, -markerOffsetsInY[i]);

            System.arraycopy(x, 0, combinedX, lastIndex, x.length);
            System.arraycopy(y, 0, combinedY, lastIndex, y.length);

            lastIndex += x.length;
        }
        //sort, because loess interpolation needs non descending domain values
        MathArrays.sortInPlace(combinedFrames, combinedX, combinedY);

        //subtract first frame coordinates so that drift at first frame is zero
        //Could be a problem when first frame drift is off. ??
        VectorMath.add(combinedX, -combinedX[0]);
        VectorMath.add(combinedY, -combinedY[0]);

        //smooth & interpolate
        IJ.showStatus("Smoothing and interpolating drift...");
        ModifiedLoess interpolator = new ModifiedLoess(smoothingBandwidth, 0);
        PolynomialSplineFunction xFunction = CorrelationDriftEstimator.addLinearExtrapolationToBorders(interpolator.interpolate(combinedFrames, combinedX), minFrame, maxFrame);
        PolynomialSplineFunction yFunction = CorrelationDriftEstimator.addLinearExtrapolationToBorders(interpolator.interpolate(combinedFrames, combinedY), minFrame, maxFrame);
        IJ.showProgress(1);
        //same units as input
        MoleculeDescriptor.Units units = molecules.get(0).getParamUnits(PSFModel.Params.LABEL_X);
        return new DriftResults(xFunction, yFunction, combinedFrames, combinedX, combinedY, minFrame, maxFrame, units);
    }

    private int countDetections(List<Molecule> fiducialMarkers) {
        int dataPoints = 0;
        for(Molecule mol : fiducialMarkers) {
            dataPoints += mol.getDetections().size();
        }
        return dataPoints;
    }

    private List<Molecule> groupMolecules(List<Molecule> molecules, double distanceThr) {
        FrameSequence grouping = new FrameSequence();
        for(Molecule mol : molecules) {
            grouping.InsertMolecule(mol);
        }
        IJProgressTracker tracker = new IJProgressTracker(0, 0.8);
        grouping.matchMolecules(MathProxy.sqr(distanceThr),
                new FrameSequence.RelativeToDetectionCount(2),
                new FrameSequence.LastFewDetectionsMean(5),
                0,
                tracker);
        List<Molecule> groupedMolecules = grouping.getAllMolecules();
        return groupedMolecules;
    }

    public double[] findFiducialsOffsets(List<Molecule> fiducials, double[] combinedFrames, String param) {
        //first, restructure the required data in a data structure that can be efficiently used in the optimization process

        //a helper class that holds a detection coordinate and an index of fiducial marker the detection belongs to
        class ValAndMarkerIndex {

            double val;
            int index;

            public ValAndMarkerIndex(double val, int index) {
                this.val = val;
                this.index = index;
            }
        }
        //create a map from frame to a list of fiducial marker detections in that frame
        Map<Double, List<ValAndMarkerIndex>> values = new HashMap<Double, List<ValAndMarkerIndex>>();
        for(int i = 0; i < fiducials.size(); i++) {
            Molecule fiducial = fiducials.get(i);
            for(Molecule detection : fiducial.getDetections()) {
                double frame = detection.getParam(MoleculeDescriptor.LABEL_FRAME);
                List<ValAndMarkerIndex> list = values.get(frame);
                if(list == null) {
                    list = new ArrayList<ValAndMarkerIndex>();
                    values.put(frame, list);
                }
                list.add(new ValAndMarkerIndex(detection.getParam(param), i));
            }
        }

        //prune frames with less than two detections
        for(Iterator<Map.Entry<Double, List<ValAndMarkerIndex>>> it = values.entrySet().iterator(); it.hasNext();) {
            List<ValAndMarkerIndex> list = it.next().getValue();
            if(list.size() < 2) {
                it.remove();
            }
        }
        //copy the values collection to a list
        //this is the final data structure used in optimization
        final List<List<ValAndMarkerIndex>> detectionsInFrames = new ArrayList<List<ValAndMarkerIndex>>(values.values());

        NelderMead nm = new NelderMead();
        //cost function:
        //for each frame where multiple drift values are present
        // cost += square of difference between each drift value and mean drift value for that frame
        MultivariateFunction fun = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                GUI.checkIJEscapePressed();
                double cost = 0;
                for(List<ValAndMarkerIndex> oneFrameDetections : detectionsInFrames) {
                    double mean = 0;
                    for(ValAndMarkerIndex detection : oneFrameDetections) {
                        mean += detection.val - point[detection.index];
                    }
                    mean /= oneFrameDetections.size();

                    for(ValAndMarkerIndex detection : oneFrameDetections) {
                        cost += MathProxy.sqr(detection.val - point[detection.index] - mean);
                    }
                }
                return Math.sqrt(cost);
            }
        };
        //values for first iteration:  first detection coords
        double[] guess = new double[fiducials.size()];
        for(int i = 0; i < guess.length; i++) {
            guess[i] = fiducials.get(i).getDetections().get(0).getParam(param);
        }
        //first simplex step size, ?????
        double[] initialSimplex = new double[fiducials.size()];
        Arrays.fill(initialSimplex, 50);
        int maxIter = 5000;

        nm.optimize(fun, NelderMead.Objective.MINIMIZE, guess, 1e-8, initialSimplex, 10, maxIter);
        double[] fittedParameters = nm.xmin;

        return fittedParameters;
    }

    static double[] extractParamAsArray(List<Molecule> mols, int index) {
        double[] arr = new double[mols.size()];
        for(int i = 0; i < mols.size(); i++) {
            arr[i] = mols.get(i).getParamAt(index);
        }
        return arr;
    }

    private double getMinFrame(List<Molecule> molecules) {
        double min = molecules.get(0).getParam(MoleculeDescriptor.LABEL_FRAME);
        for(Molecule mol : molecules) {
            double frame = mol.getParam(MoleculeDescriptor.LABEL_FRAME);
            if(frame < min) {
                min = frame;
            }
        }
        return min;
    }

    private double getMaxFrame(List<Molecule> molecules) {
        double max = molecules.get(0).getParam(MoleculeDescriptor.LABEL_FRAME);
        for(Molecule mol : molecules) {
            double frame = mol.getParam(MoleculeDescriptor.LABEL_FRAME);
            if(frame > max) {
                max = frame;
            }
        }
        return max;
    }
}
