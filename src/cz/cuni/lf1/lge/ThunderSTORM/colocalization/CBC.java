package cz.cuni.lf1.lge.ThunderSTORM.colocalization;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.DistanceFunction;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import cz.cuni.lf1.lge.ThunderSTORM.util.Loop;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

/**
 * Coordinate based colocalization.
 * <br/>
 * Original paper: Coordinate-based colocalization analysis of single-molecule
 * localization microscopy data by Sebastian Malkusch, Ulrike Endesfelder,
 * Justine Mondry, Marton Gelleri, Peter J. Verveer, Mike Heilemann
 */
public class CBC {

    private double[][] firstChannelXYdata;
    private double[][] secondChannelXYdata;
    private KdTree<double[]> firstChannelTree;
    private KdTree<double[]> secondChannelTree;
    private double[] squaredRadiusDomain;
    private double radiusStep;
    private int radiusCount;
    private DistanceFunction sqrDistanceFunction;

    public CBC(double[][] firstChannelXYdata, double[][] secondChannelXYdata, double radiusStep, int radiusCount) {
        this.firstChannelXYdata = firstChannelXYdata;
        this.secondChannelXYdata = secondChannelXYdata;
        this.radiusStep = radiusStep;
        this.radiusCount = radiusCount;

        sqrDistanceFunction = new SquareEuclideanDistanceFunction();
        fillRadiusDomain();
        buildKdTrees(secondChannelXYdata);
    }

    public double[] calculateFirstChannelCBC() {
        return calc(firstChannelXYdata, firstChannelTree, secondChannelTree);
    }

    public double[] calculateSecondChannelCBC() {
        return calc(secondChannelXYdata, secondChannelTree, firstChannelTree);
    }

    private double[] calc(final double[][] mainChannelXYdata, final KdTree<double[]> mainChannelTree, final KdTree<double[]> otherChannelTree) {

        final int lastRadiusIndex = squaredRadiusDomain.length - 1;
        final double maxSquaredRadius = squaredRadiusDomain[lastRadiusIndex];

        final double[] cbcResults = new double[mainChannelXYdata.length];
        final AtomicInteger count = new AtomicInteger(0);

        Loop.withIndex(0, mainChannelXYdata.length, new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                double[] counts = calcNeighborCount(mainChannelXYdata[i], mainChannelTree, squaredRadiusDomain, sqrDistanceFunction);
                for(int j = 0; j < counts.length; j++) {
                    counts[j] = counts[j] / counts[lastRadiusIndex] * maxSquaredRadius / squaredRadiusDomain[j];
                }

                double[] counts2 = calcNeighborCount(mainChannelXYdata[i], otherChannelTree, squaredRadiusDomain, sqrDistanceFunction);
                double maxCount = counts2[lastRadiusIndex];

                for(int j = 0; j < counts2.length; j++) {
                    if(maxCount == 0) {
                        counts2[j] = 0;
                    } else {
                        counts2[j] = counts2[j] / maxCount * maxSquaredRadius / squaredRadiusDomain[j];
                    }
                }

                SpearmansCorrelation correlator = new SpearmansCorrelation();
                double correlation = correlator.correlation(counts, counts2);

                MaxHeap<double[]> nearestNeighbors = otherChannelTree.findNearestNeighbors(mainChannelXYdata[i], 1, sqrDistanceFunction);
                double nnDistance = nearestNeighbors.getMaxKey();

                double result = correlation * MathProxy.exp(-MathProxy.sqrt(nnDistance) / MathProxy.sqrt(maxSquaredRadius));
                cbcResults[i] = result;
                if(i % 1024 == 0) {
                    int done = count.addAndGet(1024);
                    IJ.showProgress(done, mainChannelXYdata.length);
                }

            }
        });
        IJ.showProgress(1);
        return cbcResults;
    }

    private double[] calcNeighborCount(double[] queryPoint, KdTree<double[]> kdtree, double[] squaredRadiusValues, DistanceFunction sqrDistanceFunction) {
        assert queryPoint != null;
        assert squaredRadiusValues != null;
        assert kdtree != null;
        assert sqrDistanceFunction != null;

        List<KdTree.DistAndValue<double[]>> neighbors = kdtree.ballQuery(queryPoint, squaredRadiusValues[squaredRadiusValues.length - 1], sqrDistanceFunction);

        double[] result = new double[squaredRadiusValues.length];
        for(KdTree.DistAndValue<double[]> neighbor : neighbors) {
            double sqrDistance = neighbor.dist;
            int bin = (int) Math.floor(MathProxy.sqrt(sqrDistance) / radiusStep);
            result[bin]++;
        }

        VectorMath.cumulativeSum(result, false);
        return result;
    }

    private void fillRadiusDomain() {
        squaredRadiusDomain = new double[radiusCount];
        for(int i = 0; i < radiusCount; i++) {
            squaredRadiusDomain[i] = MathProxy.sqr((i + 1) * radiusStep);
        }
    }

    private void buildKdTrees(double[][] secondChannelXYdata) throws RuntimeException {
        //build one tree in separate thread, use Loop's executor so we do not create additional threads
        Future<?> future = Loop.getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                firstChannelTree = buildTree(CBC.this.firstChannelXYdata);
            }
        });
        secondChannelTree = buildTree(secondChannelXYdata);
        try {
            future.get();
        } catch(InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch(ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private KdTree<double[]> buildTree(double[][] xy) {
        assert xy != null;

        KdTree<double[]> kdtree = new KdTree<double[]>(2);
        for(double[] dataPoint : xy) {
            assert dataPoint != null;
            kdtree.addPoint(dataPoint, dataPoint);
        }
        return kdtree;
    }

}
