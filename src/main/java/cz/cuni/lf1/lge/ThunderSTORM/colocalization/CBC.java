package cz.cuni.lf1.lge.ThunderSTORM.colocalization;

import cz.cuni.lf1.lge.ThunderSTORM.util.Loop;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import cz.cuni.lf1.lge.ThunderSTORM.util.javaml.kdtree.KDTree;
import cz.cuni.lf1.lge.ThunderSTORM.util.javaml.kdtree.KeySizeException;
import ij.IJ;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.math3.exception.NotANumberException;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.commons.math3.util.MathArrays;

/**
 * Coordinate based colocalization.
 * <br/>
 * Original paper: Coordinate-based colocalization analysis of single-molecule
 * localization microscopy data by Sebastian Malkusch, Ulrike Endesfelder,
 * Justine Mondry, Marton Gelleri, Peter J. Verveer, Mike Heilemann
 */
public class CBC {

    private double[][] firstChannelCoords;
    private double[][] secondChannelCoords;
    public double[] firstChannelNearestNeighborDistances;
    public double[] secondChannelNearestNeighborDistances;
    public double[][] firstChannelNeighborsInDistance;
    public double[][] secondChannelNeighborsInDistance;
    private KDTree<double[]> firstChannelTree;
    private KDTree<double[]> secondChannelTree;
    public double[] squaredRadiusDomain;
    private double radiusStep;
    private int stepCount;

    public CBC(double[][] firstChannelCoords, double[][] secondChannelCoords, double radiusStep, int stepCount) {
        this.firstChannelCoords = firstChannelCoords;
        this.secondChannelCoords = secondChannelCoords;
        this.radiusStep = radiusStep;
        this.stepCount = stepCount;

        fillRadiusDomain();
        buildKdTrees(secondChannelCoords);
    }

    public double[] calculateFirstChannelCBC() {
        firstChannelNeighborsInDistance = new double[squaredRadiusDomain.length][firstChannelCoords.length];
        firstChannelNearestNeighborDistances = new double[firstChannelCoords.length];
        return calc(firstChannelCoords, firstChannelTree, secondChannelTree, firstChannelNeighborsInDistance, firstChannelNearestNeighborDistances);
    }

    public double[] calculateSecondChannelCBC() {
        secondChannelNeighborsInDistance = new double[squaredRadiusDomain.length][secondChannelCoords.length];
        secondChannelNearestNeighborDistances = new double[secondChannelCoords.length];
        return calc(secondChannelCoords, secondChannelTree, firstChannelTree, secondChannelNeighborsInDistance, secondChannelNearestNeighborDistances);
    }

    /**
     * If channel1 == channel2 (both res-tables or both gt-tables), avoid self-counting, i.e., distance to nearest neighbor must not be 0!
     * On the other hand if comparing res-table with gt-table then self-counting is allowed even if the data in the tables are the same.
     * */
    private double[] calc(final double[][] mainChannelCoords, final KDTree<double[]> mainChannelTree, final KDTree<double[]> otherChannelTree, final double [][] neighborsInDistance, final double [] nearestNeighborDistances) {

        final int lastRadiusIndex = squaredRadiusDomain.length - 1;
        final double maxSquaredRadius = squaredRadiusDomain[lastRadiusIndex];

        final double[] cbcResults = new double[mainChannelCoords.length];
        final AtomicInteger count = new AtomicInteger(0);
        IJ.showProgress(0);
        Loop.withIndex(0, mainChannelCoords.length, new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                try {
                    double[] counts = calcNeighborCount(mainChannelCoords[i], mainChannelTree, squaredRadiusDomain, (firstChannelCoords == secondChannelCoords));
                    for(int j = 0; j < counts.length; j++) {
                        counts[j] = counts[j] / counts[lastRadiusIndex] * maxSquaredRadius / squaredRadiusDomain[j];
                    }

                    double[] counts2 = calcNeighborCount(mainChannelCoords[i], otherChannelTree, squaredRadiusDomain, (firstChannelCoords == secondChannelCoords));
                    nearestNeighborDistances[i] = getDistanceToNearestNeighbor(mainChannelCoords[i], otherChannelTree, (firstChannelCoords == secondChannelCoords));
                    double maxCount = counts2[lastRadiusIndex];

                    for(int j = 0; j < counts2.length; j++) {
                        neighborsInDistance[j][i] = counts2[j];
                        if(maxCount == 0) {
                            counts2[j] = 0;
                        } else {
                            counts2[j] = counts2[j] / maxCount * maxSquaredRadius / squaredRadiusDomain[j];
                        }
                    }

                    SpearmansCorrelation correlator = new SpearmansCorrelation();
                    double correlation;
                    try {
                        correlation = correlator.correlation(counts, counts2);
                    } catch (NotANumberException e) {
                        correlation = Double.NaN;
                    }
                    double[] nearestNeighbor = otherChannelTree.nearest(mainChannelCoords[i]);
                    double nnDistance = MathArrays.distance(nearestNeighbor, mainChannelCoords[i]);

                    double result = correlation * MathProxy.exp(-nnDistance / MathProxy.sqrt(maxSquaredRadius));
                    cbcResults[i] = result;
                    if(i % 1024 == 0) {
                        IJ.showProgress((double)count.addAndGet(1024) / (double)(mainChannelCoords.length));
                    }
                } catch(KeySizeException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        IJ.showProgress(1);
        return cbcResults;
    }

    private double[] calcNeighborCount(double[] queryPoint, KDTree<double[]> kdtree, double[] squaredRadiusValues, boolean checkNotSelf) {
        assert queryPoint != null;
        assert squaredRadiusValues != null;
        assert kdtree != null;

        List<KDTree.DistAndValue<double[]>> neighbors = kdtree.ballQuery(queryPoint, Math.sqrt(squaredRadiusValues[squaredRadiusValues.length - 1]));

        double[] result = new double[squaredRadiusValues.length];
        for(KDTree.DistAndValue<double[]> neighbor : neighbors) {
            if (checkNotSelf && eqCoords(neighbor.value, queryPoint)) {
                continue;
            }
            double distance = neighbor.dist;
            int bin = (int) Math.floor(distance / radiusStep);
            result[bin]++;
        }

        VectorMath.cumulativeSum(result, false);
        return result;
    }

    private boolean eqCoords(double[] a, double[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
    
    private double getDistanceToNearestNeighbor(double[] queryPoint, KDTree<double[]> kdtree, boolean checkNotSelf) {
        assert queryPoint != null;
        assert kdtree != null;

        List<double[]> knn = kdtree.nearest(queryPoint, 2);
        double[] nn = knn.get(0);
        if (checkNotSelf && eqCoords(nn, queryPoint)) {
            nn = knn.get(1);
        }
        return MathProxy.euclidDist(nn, queryPoint);
    }

    private void fillRadiusDomain() {
        squaredRadiusDomain = new double[stepCount];
        for(int i = 0; i < stepCount; i++) {
            squaredRadiusDomain[i] = MathProxy.sqr((i + 1) * radiusStep);
        }
    }

    private void buildKdTrees(double[][] secondChannelCoords) throws RuntimeException {
        //build one tree in separate thread, use Loop's executor so we do not create additional threads
        Future<?> future = Loop.getExecutor().submit(new Runnable() {
            @Override
            public void run() {
                firstChannelTree = buildTree(CBC.this.firstChannelCoords);
            }
        });
        secondChannelTree = buildTree(secondChannelCoords);
        try {
            future.get();
        } catch(InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch(ExecutionException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    private KDTree<double[]> buildTree(double[][] coords) {
        assert coords != null;
        KDTree<double[]> kdtree = new KDTree<double[]>(coords[0].length);
        for(double[] dataPoint : coords) {

            assert dataPoint != null;
            kdtree.insert(dataPoint, dataPoint);

        }
        return kdtree;
    }

}
