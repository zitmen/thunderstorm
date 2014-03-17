package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.drift.CrossCorrelationDriftCorrection;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.optimizers.NelderMead;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.gui.Plot;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.commons.math3.util.MathArrays;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class FiducialDriftCorrection extends PostProcessingModule {

    @Override
    public String getMacroName() {
        return "fiducial";
    }

    @Override
    public String getTabName() {
        return "Fiducial Drift correction";
    }

    @Override
    protected JPanel createUIPanel() {
        JPanel panel = new JPanel();
        JButton runButton = new JButton("run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                run();
            }
        });
        panel.add(runButton);
        return panel;
    }

    @Override
    protected void runImpl() {

        FrameSequence grouping = new FrameSequence();
        MoleculeDescriptor clonedDescriptor = model.cloneDescriptor();
        for(int i = 0; i < model.getRowCount(); i++) {
            grouping.InsertMolecule(model.getRow(i).clone(clonedDescriptor));
        }
        grouping.matchMolecules(MathProxy.sqr(40),
                new FrameSequence.RelativeToDetectionCount(2),
                new FrameSequence.LastFewDetectionsMean(5),
                0);
        List<Molecule> groupedMolecules = grouping.getAllMolecules();
        Collections.sort(groupedMolecules, new Comparator<Molecule>() {
            @Override
            public int compare(Molecule o1, Molecule o2) {
                return -Double.compare(o1.getParam(MoleculeDescriptor.LABEL_DETECTIONS), o2.getParam(MoleculeDescriptor.LABEL_DETECTIONS));
            }
        });
        int numMarkers = 5;
        List<Molecule> fiducialMarkers = new ArrayList<Molecule>();
        for(int i = 0; i < numMarkers && i < groupedMolecules.size(); i++) {
            fiducialMarkers.add(groupedMolecules.get(i));
        }

        //combine data points from multiple fiducial markers
        int dataPoints = 0;
        for(Molecule mol : fiducialMarkers) {
            dataPoints += mol.getDetections().size();
        }
        double[] combinedFrames = new double[dataPoints];
        double[] combinedX = new double[dataPoints];
        double[] combinedY = new double[dataPoints];
        int lastIndex = 0;
        for(Molecule mol : fiducialMarkers) {
            List<Molecule> detections = mol.getDetections();
            double[] frame = extractParamAsArray(detections, detections.get(0).descriptor.getParamIndex(MoleculeDescriptor.LABEL_FRAME));
            System.arraycopy(frame, 0, combinedFrames, lastIndex, frame.length);
            lastIndex += frame.length;
        }
        double[] markerOffsetsInX = findFiducialsOffsets(fiducialMarkers, combinedFrames, PSFModel.Params.LABEL_X);
        double[] markerOffsetsInY = findFiducialsOffsets(fiducialMarkers, combinedFrames, PSFModel.Params.LABEL_Y);
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
        MathArrays.sortInPlace(combinedFrames, combinedX, combinedY);
        
        //subtract first frame coordinates so that drift at first frame is zero
        VectorMath.add(combinedX, -combinedX[0]);
        VectorMath.add(combinedY, -combinedY[0]);

        //smooth & interpolate
        int minFrame = (int) VectorMath.min(combinedFrames);
        int maxFrame = (int) VectorMath.max(combinedFrames);
        ModifiedLoess interpolator = new ModifiedLoess(0.1, 0);
        PolynomialSplineFunction xFunction = CrossCorrelationDriftCorrection.addLinearExtrapolationToBorders(interpolator.interpolate(combinedFrames, combinedX), minFrame, maxFrame);
        PolynomialSplineFunction yFunction = CrossCorrelationDriftCorrection.addLinearExtrapolationToBorders(interpolator.interpolate(combinedFrames, combinedY), minFrame, maxFrame);

        int gridTicks = 200;
        double tickStep = (maxFrame - minFrame) / (double) gridTicks;
        double[] grid = new double[gridTicks];
        double[] driftX = new double[gridTicks];
        double[] driftY = new double[gridTicks];
        for(int i = 0; i < gridTicks; i++) {
            grid[i] = i * tickStep + minFrame;
            driftX[i] = xFunction.value(grid[i]);
            driftY[i] = yFunction.value(grid[i]);
        }

        Plot plot = new Plot("Drift", "frame", "drift", (float[]) null, null);
        plot.setFrameSize(1280, 720);
        plot.setLimits(minFrame, maxFrame,
                MathProxy.min(VectorMath.min(combinedX), VectorMath.min(combinedY)),
                MathProxy.max(VectorMath.max(combinedX), VectorMath.max(combinedY)));
        plot.setColor(new Color(255,128,128));
        plot.addPoints(combinedFrames, combinedX, Plot.CROSS);
        plot.draw();
        plot.setColor(Color.red);
        plot.addPoints(grid, driftX, Plot.LINE);
        plot.setColor(new Color(128, 255, 128));
        plot.addPoints(combinedFrames, combinedY, Plot.CROSS);
        plot.setColor(Color.green);
        plot.addPoints(grid, driftY, Plot.LINE);
        plot.show();
    }

    public double[] findFiducialsOffsets(final List<Molecule> fiducials, final double[] combinedFrames, String param) {

        //frame to detection maps
        final List<Map<Double, Double>> maps = new ArrayList<Map<Double, Double>>();
        for(Molecule fiducial : fiducials) {
            Map<Double, Double> detectionsByFrame = new HashMap<Double, Double>();
            maps.add(detectionsByFrame);
            for(Molecule detection : fiducial.getDetections()) {
                detectionsByFrame.put(detection.getParam(MoleculeDescriptor.LABEL_FRAME), detection.getParam(param));
            }
        }
        NelderMead nm = new NelderMead();

        MultivariateFunction fun = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double cost = 0;
                for(double frame : combinedFrames) {
                    List<Double> drifts = new ArrayList<Double>();
                    for(int i = 0; i < fiducials.size(); i++) {
                        Double val = maps.get(i).get(frame);
                        if(val != null) {
                            drifts.add(val - point[i]);
                        }
                    }
                    if(drifts.size() > 1) {
                        double sum = 0;
                        for(Double d : drifts) {
                            sum += d;
                        }
                        double avg = sum / drifts.size();

                        for(Double d : drifts) {
                            cost += MathProxy.sqr(d - avg);
                        }
                    }
                }
                return Math.sqrt(cost);
            }
        };
        double[] guess = new double[fiducials.size()];
        for(int i = 0; i < guess.length; i++) {
            fiducials.get(i).getDetections().get(0).getX();
        }
        double[] initialSimplex = new double[fiducials.size()];
        Arrays.fill(initialSimplex, 5);
        int maxIter = 5000;
        nm.optimize(fun,
                NelderMead.Objective.MINIMIZE, guess, 1e-8, initialSimplex, 10, maxIter);
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
}
