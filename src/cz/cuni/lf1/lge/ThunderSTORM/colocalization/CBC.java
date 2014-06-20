package cz.cuni.lf1.lge.ThunderSTORM.colocalization;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor.Units;
import cz.cuni.lf1.lge.ThunderSTORM.util.Loop;
import cz.cuni.lf1.lge.ThunderSTORM.util.MathProxy;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import ij.IJ;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import net.sf.javaml.core.kdtree.KDTree;
import net.sf.javaml.core.kdtree.KeyDuplicateException;
import net.sf.javaml.core.kdtree.KeySizeException;
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

    private double[][] firstChannelXYdata;
    private double[][] secondChannelXYdata;
    public double[] firstChannelNearestNeighborDistances;
    public double[] secondChannelNearestNeighborDistances;
    public double[][] firstChannelNeighborsInDistance;
    public double[][] secondChannelNeighborsInDistance;
    private KDTree<double[]> firstChannelTree;
    private KDTree<double[]> secondChannelTree;
    public double[] squaredRadiusDomainPx;
    public double[] squaredRadiusDomainNm;
    private double radiusStepPx;
    private int radiusCount;

    public CBC(double[][] firstChannelXYdata, double[][] secondChannelXYdata, double radiusStep, int radiusCount) {
        this.firstChannelXYdata = firstChannelXYdata;
        this.secondChannelXYdata = secondChannelXYdata;
        this.radiusStepPx = Units.NANOMETER.convertTo(Units.PIXEL, radiusStep);
        this.radiusCount = radiusCount;

        fillRadiusDomain();
        buildKdTrees(secondChannelXYdata);
    }

    public double[] calculateFirstChannelCBC() {
        firstChannelNeighborsInDistance = new double[squaredRadiusDomainPx.length][firstChannelXYdata.length];
        firstChannelNearestNeighborDistances = new double[firstChannelXYdata.length];
        return calc(firstChannelXYdata, firstChannelTree, secondChannelTree, firstChannelNeighborsInDistance, firstChannelNearestNeighborDistances);
    }

    public double[] calculateSecondChannelCBC() {
        secondChannelNeighborsInDistance = new double[squaredRadiusDomainPx.length][secondChannelXYdata.length];
        secondChannelNearestNeighborDistances = new double[secondChannelXYdata.length];
        return calc(secondChannelXYdata, secondChannelTree, firstChannelTree, secondChannelNeighborsInDistance, secondChannelNearestNeighborDistances);
    }

    private double[] calc(final double[][] mainChannelXYdata, final KDTree<double[]> mainChannelTree, final KDTree<double[]> otherChannelTree, final double [][] neighborsInDistance, final double [] nearestNeighborDistances) {

        final int lastRadiusIndex = squaredRadiusDomainPx.length - 1;
        final double maxSquaredRadius = squaredRadiusDomainPx[lastRadiusIndex];

        final double[] cbcResults = new double[mainChannelXYdata.length];
        final AtomicInteger count = new AtomicInteger(0);
        
        Loop.withIndex(0, mainChannelXYdata.length, new Loop.BodyWithIndex() {
            @Override
            public void run(int i) {
                try {
                    double[] counts = calcNeighborCount(mainChannelXYdata[i], mainChannelTree, squaredRadiusDomainPx);
                    for(int j = 0; j < counts.length; j++) {
                        counts[j] = counts[j] / counts[lastRadiusIndex] * maxSquaredRadius / squaredRadiusDomainPx[j];
                    }

                    double[] counts2 = calcNeighborCount(mainChannelXYdata[i], otherChannelTree, squaredRadiusDomainPx);
                    nearestNeighborDistances[i] = getDistanceToNearestNeighbor(mainChannelXYdata[i], otherChannelTree);
                    double maxCount = counts2[lastRadiusIndex];

                    for(int j = 0; j < counts2.length; j++) {
                        neighborsInDistance[j][i] = counts2[j];
                        if(maxCount == 0) {
                            counts2[j] = 0;
                        } else {
                            counts2[j] = counts2[j] / maxCount * maxSquaredRadius / squaredRadiusDomainPx[j];
                        }
                    }

                    SpearmansCorrelation correlator = new SpearmansCorrelation();
                    double correlation = correlator.correlation(counts, counts2);

                    double[] nearestNeighbor = otherChannelTree.nearest(mainChannelXYdata[i]);
                    double nnDistance = MathArrays.distance(nearestNeighbor, mainChannelXYdata[i]);

                    double result = correlation * MathProxy.exp(-nnDistance / MathProxy.sqrt(maxSquaredRadius));
                    cbcResults[i] = result;
                    if(i % 1024 == 0) {
                        int done = count.addAndGet(1024);
                        IJ.showProgress(done, mainChannelXYdata.length);
                    }
                } catch(KeySizeException ex) {
                    throw new RuntimeException(ex);
                }

            }
        });
        IJ.showProgress(1);
        return cbcResults;
    }

    private double[] calcNeighborCount(double[] queryPoint, KDTree<double[]> kdtree, double[] squaredRadiusValues) {
        assert queryPoint != null;
        assert squaredRadiusValues != null;
        assert kdtree != null;

        List<KDTree.DistAndValue<double[]>> neighbors = kdtree.ballQuery(queryPoint, Math.sqrt(squaredRadiusValues[squaredRadiusValues.length - 1]));

        double[] result = new double[squaredRadiusValues.length];
        for(KDTree.DistAndValue<double[]> neighbor : neighbors) {
            double distance = neighbor.dist;
            int bin = (int) Math.floor(distance / radiusStepPx);
            result[bin]++;
        }

        VectorMath.cumulativeSum(result, false);
        return result;
    }
    
    private double getDistanceToNearestNeighbor(double[] queryPoint, KDTree<double[]> kdtree) {
        assert queryPoint != null;
        assert kdtree != null;

        double[] nn = kdtree.nearest(queryPoint);
        return MathProxy.sqrt(MathProxy.sqr(nn[0]-queryPoint[0])+MathProxy.sqr(nn[1]-queryPoint[1]));
    }

    private void fillRadiusDomain() {
        squaredRadiusDomainPx = new double[radiusCount];
        squaredRadiusDomainNm = new double[radiusCount];
        for(int i = 0; i < radiusCount; i++) {
            squaredRadiusDomainPx[i] = MathProxy.sqr((i + 1) * radiusStepPx);
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

    private KDTree<double[]> buildTree(double[][] xy) {
        assert xy != null;
        KDTree<double[]> kdtree = new KDTree<double[]>(2);
        for(double[] dataPoint : xy) {

            assert dataPoint != null;
            kdtree.insert(dataPoint, dataPoint);

        }
        return kdtree;
    }

}
