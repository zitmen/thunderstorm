package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA1;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_SIGMA2;

import cz.cuni.lf1.lge.ThunderSTORM.util.Pair;
import cz.cuni.lf1.lge.ThunderSTORM.util.StableMatching;
import cz.cuni.lf1.lge.ThunderSTORM.util.VectorMath;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

import java.util.*;

import static cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;

/**
 * This implementation is directly re-written from Matlab without any optimizations!
 */
public class Homography {

    private static final double dist2thr = 10.0;

    public static TransformationMatrix estimateTransform(int w1, int h1, PSFSeparator fits1, int w2, int h2, PSFSeparator fits2) {
        List<Position> pt1 = moveToCenterXY(fits1, w1, h1);
        List<Position> pt2 = moveToCenterXY(fits2, w2, h2);
        return findHomography(findTranslationAndFlip(pt1, pt2), pt1, pt2);
    }

    /**
     * 1) Based on the transformation, pair up the fits[1|2] together
     * 2) For each fit in matched subset of fits1 select only the subset of Molecules (z-slice) present both in fits[1|2]
     * 3) For each pair of Molecules assign sigma1 and sigma2 for the moleule from fits1
     * 4) Return the List of Positions containing the Molecules with sigmas properly set
     *    --> these are used for estimation of calibration curves
     */
    public static Collection<Position> mergePositions(TransformationMatrix transform, PSFSeparator fits1, PSFSeparator fits2) {
        // 1)
        List<Position> p1 = applyH(transform, fits1.getPositions());
        List<Position> p2 = fits2.getPositions();
        for (Position p : p1) {
            p.addNeighbors(p2, dist2thr);
        }
        Map<Position, Position> pairs = StableMatching.match(p1);
        // 2)
        for (Map.Entry<Position, Position> pair : pairs.entrySet()) {
            Position pos1 = pair.getKey();
            Position pos2 = pair.getValue();
            pos1.discardFitsByFrameSet(pos2.getFramesAsSet());
            pos2.discardFitsByFrameSet(pos1.getFramesAsSet());
            // 3)
            pos1.setFromArray(LABEL_SIGMA1, pos1.getAsArray(LABEL_SIGMA));
            pos1.setFromArray(LABEL_SIGMA2, pos2.getAsArray(LABEL_SIGMA));
            // -> [optional] recalculate the position as it has been transformed by the transform
            pos1.recalculateCentroid();
        }
        // 4)
        return pairs.keySet();
    }

    public static List<PSFSeparator.Position> transformPositions(TransformationMatrix transformationMatrix, List<PSFSeparator.Position> positions) {
        return applyH(transformationMatrix, positions);
    }

    public static class TransformationMatrix {
        private RealMatrix matrix;

        private TransformationMatrix() {
            matrix = null;
        }

        public Position transform(RealMatrix vec) {
            RealMatrix res = matrix.multiply(vec);  // 1x3 matrix
            Position pos = new Position();
            pos.centroidX = res.getEntry(0, 0) / res.getEntry(0, 2);
            pos.centroidY = res.getEntry(0, 1) / res.getEntry(0, 2);
            return pos;
        }

        public static TransformationMatrix createIdentity() {
            TransformationMatrix tm = new TransformationMatrix();
            tm.matrix = new Array2DRowRealMatrix(new double[][] {{1,0,0},{0,1,0},{0,0,1}});    // 3x3 identity matrix
            return tm;
        }

        public TransformationMatrix shift(double x, double y) {
            matrix.setEntry(0, 2, x);
            matrix.setEntry(1, 2, y);
            return this;
        }

        public TransformationMatrix flipX() {
            matrix.setEntry(0, 0, -matrix.getEntry(0, 0));
            return this;
        }

        public TransformationMatrix flipY() {
            matrix.setEntry(1, 1, -matrix.getEntry(1, 1));
            return this;
        }

        public static TransformationMatrix createFrom(RealMatrix matrix) {
            TransformationMatrix H = new TransformationMatrix();
            H.matrix = matrix;
            return H;
        }
    }

    private static List<Position> moveToCenterXY(PSFSeparator fits, int width, int height) {
        double shiftX = ((double) width) / 2.0;
        double shiftY = ((double) height) / 2.0;
        List<Position> posIn = fits.getPositions();
        List<Position> posOut = new ArrayList<Position>(posIn.size());
        for (Position p : posIn) {
            Position pos = new Position();
            pos.centroidX = p.centroidX - shiftX;
            pos.centroidY = p.centroidY - shiftY;
            posOut.add(pos);
        }
        return posOut;
    }

    private static List<Position> moveToBoundaryXY(PSFSeparator fits, int width, int height) {
        double shiftX = ((double) width) / 2.0;
        double shiftY = ((double) height) / 2.0;
        List<Position> posIn = fits.getPositions();
        List<Position> posOut = new ArrayList<Position>(posIn.size());
        for (Position p : posIn) {
            Position pos = new Position();
            pos.centroidX = p.centroidX + shiftX;
            pos.centroidY = p.centroidY + shiftY;
            posOut.add(pos);
        }
        return posOut;
    }

    // Find the translation between two planes using a set of corresponding points (RANSAC method is used).
    private static TransformationMatrix findTranslationAndFlip(List<Position> p1, List<Position> p2) {
        RansacConfig config = new RansacConfig();
        config.minPtNum = 2;
        config.iterNum = 1000;
        config.thDist = 15;
        config.thInlr = Math.max(2.0, Math.round(0.1 * Math.min(p1.size(), p2.size())));    // 10%
        config.pairs = false;
        config.thAllowedTransformChange = Double.POSITIVE_INFINITY;    // don't check, since this is a simple transform

        IRansacFunctions functions = new IRansacFunctions() {
            @Override
            public TransformationMatrix findTransform(List<Position> p1, List<Position> p2) {
                return solveTranslationAndFlip(p1, p2);
            }

            @Override
            public List<Pair<Double, Integer>> distance(TransformationMatrix t, List<Position> p1, List<Position> p2) {
                return calcDist(t, p1, p2);
            }

            @Override
            public boolean isResultValid(TransformationMatrix t) {
                return validateH(Double.POSITIVE_INFINITY, t, t);
            }
        };

        return ransac(p1, p2, config, functions);
    }

    // Find the homography between two planes using a set of corresponding points. RANSAC method is used.
    private static TransformationMatrix findHomography(final TransformationMatrix initialGuess, List<Position> p1, List<Position> p2) {
        RansacConfig config = new RansacConfig();
        config.minPtNum = 4;
        config.iterNum = 1000;
        config.thDist = 1;  // precision [px]; usualy the precision is much higher (~1e-6 px)
        config.thInlr = Math.max(2.0, Math.round(0.5 * Math.min(p1.size(), p2.size())));    // 50%
        config.pairs = true;
        config.thPairDist = 15;   // mutual distance of paired molecules [px] (should be same as `coef.thDist` set in `findTranslationAndFlip`)
        config.thAllowedTransformChange = 100.0;    // point [1,1] after applying T can't move further than 100px,
                                                    // otherwise something is wrong, since this step should be just
                                                    // for fine-tuning! (this value is large on purpose, so the test
                                                    // does not reject a valid transformation)

        IRansacFunctions functions = new IRansacFunctions() {
            @Override
            public TransformationMatrix findTransform(List<Position> p1, List<Position> p2) {
                return solveHomography(p1, p2);
            }

            @Override
            public List<Pair<Double, Integer>> distance(TransformationMatrix t, List<Position> p1, List<Position> p2) {
                return calcDist(t, p1, p2);
            }

            @Override
            public boolean isResultValid(TransformationMatrix t) {
                return validateH(100.0, initialGuess, t);
            }
        };

        Pair<List<Position>, List<Position>> pairs = initialPairing(config.thPairDist, initialGuess, p1, p2);
        return ransac(pairs.first, pairs.second, config, functions);
    }

    private static double calcDist2(Position p1, Position p2) {
        return Math.pow(p1.centroidX - p2.centroidX, 2) + Math.pow(p1.centroidY - p2.centroidY, 2);
    }

    // Project pts1 to P1 using H, then calcultate the distances between P1 and P2; I is index into P2/pts2
    private static List<Pair<Double, Integer>> calcDist(TransformationMatrix t, List<Position> p1, List<Position> p2) {
        List<Position> pts1 = applyH(t, p1);
        List<Position> pts2 = p2;

        List<Pair<Double, Integer>> distIdx1 = pDist2(pts1, pts2);
        List<Pair<Double, Integer>> distIdx2 = pDist2(pts2, pts1);

        boolean[] bi = new boolean[distIdx2.size()];
        Arrays.fill(bi, false);
        for (int i = 0; i < bi.length; i++) {
            bi[i] = distIdx1.get(distIdx2.get(i).second).second == i;
            if (!bi[i]) {
                distIdx2.get(i).first = Double.POSITIVE_INFINITY;
            }
        }

        return distIdx2;
    }

    private static List<Pair<Double, Integer>> pDist2(List<Position> pts1, List<Position> pts2) {
        Pair<double[][], int[][]> map = pDist2All(pts1, pts2);
        List<Pair<Double, Integer>> ret = new ArrayList<Pair<Double, Integer>>(pts1.size());
        for (int i = 0; i < pts1.size(); i++) {
            int idx = map.second[i][0];
            double dist = map.first[i][idx];
            ret.add(new Pair<Double, Integer>(dist, idx));
        }
        return ret;
    }

    private static Pair<double[][], int[][]> pDist2All(List<Position> pts1, List<Position> pts2) {
        final double[][] distMap = new double[pts1.size()][pts2.size()];
        for (int i = 0; i < pts1.size(); i++) {
            for (int j = 0; j < pts2.size() && j < i; j++) {
                distMap[i][j] = calcDist2(pts1.get(i), pts2.get(j));
            }
            if (i < pts2.size()) {
                distMap[i][i] = Double.POSITIVE_INFINITY;
            }
            for (int j = i + 1; j < pts2.size(); j++) {
                distMap[i][j] = distMap[j][i];
            }
        }
        int[][] idxMap = new int[pts1.size()][pts2.size()];
        for (int i = 0; i < pts1.size(); i++) {
            final int ii = i;
            Integer[] indices = getIndices(pts2.size());
            Arrays.sort(indices, new Comparator<Integer>() {
                @Override
                public int compare(Integer o1, Integer o2) {
                    double diff = distMap[ii][o1] - distMap[ii][o2];
                    return diff > 0 ? +1 : diff == 0 ? 0 : -1;
                }
            });
            unboxIntArray(indices, idxMap[i]);
        }
        return new Pair<double[][], int[][]>(distMap, idxMap);
    }

    private static void unboxIntArray(Integer[] src, int[] dest) {
        for (int i = 0; i < src.length; i++) {
            dest[i] = src[i];
        }
    }

    private static Integer[] getIndices(int size) {
        Integer[] indices = new Integer[size];
        for (int i = 0; i < size; i++) {
            indices[i] = i;
        }
        return indices;
    }

    private static List<Position> applyH(TransformationMatrix t, List<Position> pIn) {
        List<Position> pOut = new ArrayList<Position>(pIn.size());
        for (Position p : pIn) {
            RealMatrix pMat = new Array2DRowRealMatrix(new double[][]{{p.centroidX}, {p.centroidY}, {1.0}});   // 3x1 matrix
            pOut.add(t.transform(pMat));
        }
        return pOut;
    }

    // T is 2x1, T+[pts1(:,i);1] ~ [pts2(:,i);1]
    private static TransformationMatrix solveTranslationAndFlip(List<Position> p1, List<Position> p2) {
        if (p1.size() != p2.size()) {
            throw new IllegalArgumentException("`p1` and `p2` must have same number of elements!");
        }

        // Translate Only
        List<Position> DO = subtract(p2, p1);
        Position TO = mean(DO);

        // Flip over X axis and translate
        List<Position> DFX = subtract(p2, flipX(p1));
        Position TFX = mean(DFX);

        // Flip over Y axis and translate
        List<Position> DFY = subtract(p2, flipY(p1));
        Position TFY = mean(DFY);

        double mo = mse(DO, TO);
        double mfx = mse(DFX, TFX);
        double mfy = mse(DFY, TFY);
        if (mo <= mfx) {
            if (mo <= mfy) {
                return TransformationMatrix.createIdentity().shift(TO.centroidX, TO.centroidY);
            } else {    // mfy < mo
                return TransformationMatrix.createIdentity().shift(TFY.centroidX, TFY.centroidY).flipY();
            }
        } else { // mo > mfx
            if (mfx <= mfy) {
                return TransformationMatrix.createIdentity().shift(TFX.centroidX, TFX.centroidY).flipX();
            } else {    // mfy < mfx
                return TransformationMatrix.createIdentity().shift(TFY.centroidX, TFY.centroidY).flipY();
            }
        }
    }

    private static List<Position> flipX(List<Position> positions) {
        List<Position> res = new ArrayList<Position>(positions.size());
        for (Position pos : positions) {
            Position p = new Position();
            p.centroidX = -pos.centroidX;
            p.centroidY = +pos.centroidY;
            res.add(p);
        }
        return res;
    }

    private static List<Position> flipY(List<Position> positions) {
        List<Position> res = new ArrayList<Position>(positions.size());
        for (Position pos : positions) {
            Position p = new Position();
            p.centroidX = +pos.centroidX;
            p.centroidY = -pos.centroidY;
            res.add(p);
        }
        return res;
    }

    private static Position mean(List<Position> p) {
        Position res = new Position();
        for (Position pos : p) {
            res.centroidX += pos.centroidX;
            res.centroidY += pos.centroidY;
        }
        res.centroidX /= (double) p.size();
        res.centroidY /= (double) p.size();
        return res;
    }

    private static List<Position> subtract(List<Position> p1, List<Position> p2) {
        List<Position> res = new ArrayList<Position>(p1.size());
        for (int i = 0; i < p1.size(); i++) {
            Position pos = new Position();
            pos.centroidX = p1.get(i).centroidX - p2.get(i).centroidX;
            pos.centroidY = p1.get(i).centroidY - p2.get(i).centroidY;
            res.add(pos);
        }
        return res;
    }

    private static double mse(List<Position> d, Position t) {
        double avg = 0.0;
        for (Position pos : d) {
            avg += calcDist2(pos, t);
        }
        avg /= (double) d.size();
        return avg;
    }

    /**
     * to enhance the results, we pair together all points with
     * mutual distance less than `thPairDist`, instead of just points with
     * minimum distance; this brings a certain redundancy to the data
     * thus we need more iterations of ransac but the result is obviously
     * much better since there might be areas with dense distribution of
     * molecules in the data and it could cause many mismatched pairs
     */
    private static Pair<List<Position>, List<Position>> initialPairing(double thPairDist, TransformationMatrix H, List<Position> pts1, List<Position> pts2) {
        Pair<double[][], int[][]> DI = pDist2All(pts2, applyH(H, pts1));
        double thr2 = thPairDist * thPairDist;
        List<Position> X = new ArrayList<Position>();
        List<Position> Y = new ArrayList<Position>();
        for (int i = 0; i < DI.first.length; i++){
            for (int j = 0; j < DI.first[i].length && j < i; j++){
                if (DI.first[i][j] < thr2) {
                    X.add(pts1.get(j));
                    Y.add(pts2.get(i));
                }
            }
        }
        return new Pair<List<Position>, List<Position>>(X, Y);
    }

    // H is 3x3, H*[pts1(:,i);1] ~ [pts2(:,i);1], H(3,3) = 1
    private static TransformationMatrix solveHomography(List<Position> p1, List<Position> p2) {
        double[][] A = zeros(2*p1.size(), 9);
        for (int i = 0; i < p1.size(); i++) {
            A[2*i][0] = p1.get(i).centroidX;
            A[2*i][1] = p1.get(i).centroidY;
            A[2*i][3] = 1.0;
            A[2*i+1][4] = p1.get(i).centroidX;
            A[2*i+1][5] = p1.get(i).centroidY;
            A[2*i+1][6] = 1.0;
            A[2*i][7] = -p2.get(i).centroidX * p1.get(i).centroidX;
            A[2*i+1][7] = -p2.get(i).centroidY * p1.get(i).centroidY;
            A[2*i][8] = -p2.get(i).centroidY * p1.get(i).centroidX ;
            A[2*i+1][8] = -p2.get(i).centroidX * 1.0;
            A[2*i][9] = -p2.get(i).centroidX * p1.get(i).centroidY;
            A[2*i+1][9] = -p2.get(i).centroidY * 1.0;
        }

        // [evec,~] = eig(A'*A);
        Array2DRowRealMatrix matA = new Array2DRowRealMatrix(A);
        EigenDecomposition eig = new EigenDecomposition(matA.multiply(matA.transpose()));

        // H = reshape(evec(:,1),[3,3])';
        // H = H/H(end); % make H(3,3) = 1
        Array2DRowRealMatrix H = new Array2DRowRealMatrix(3, 3);
        RealVector eigVec = eig.getEigenvector(1);
        for (int r = 0, i = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++, i++) {
                H.setEntry(r, c, eigVec.getEntry(i) / eigVec.getEntry(9));
            }
        }

        return TransformationMatrix.createFrom(H);
    }

    private static double[][] zeros(int rows, int cols) {
        double[][] arr = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                arr[i][j] = 0.0;
            }
        }
        return arr;
    }

    // how much has the transformation changed?
    private static boolean validateH(double thDist, TransformationMatrix initial, TransformationMatrix result) {
        RealMatrix ones = new Array2DRowRealMatrix(new double[][] {{1.0}, {1.0}, {1.0}});   // 3x1 matrix
        return calcDist2(initial.transform(ones), result.transform(ones)) < (thDist * thDist);
    }

    private interface IRansacFunctions {
        TransformationMatrix findTransform(List<Position> p1, List<Position> p2);
        List<Pair<Double, Integer>> distance(TransformationMatrix t, List<Position> p1, List<Position> p2);   // returns <dist, idx>
        boolean isResultValid(TransformationMatrix t);
    }

    private static class RansacConfig {
        public int minPtNum;
        public int iterNum;
        public double thDist;
        public double thInlr;
        public boolean pairs;
        public double thPairDist;
        public double thAllowedTransformChange;
    }

    /**
     * Use RANdom SAmple Consensus to find a fit from X to Y.
     * X is M*n matrix including n points with dim M, Y is N*n;
     * The fit, f, and the indices of inliers, are returned.
     *
     * RANSACCOEF is a struct with following fields:
     * minPtNum,iterNum,thDist,thInlrRatio
     * MINPTNUM is the minimum number of points with whom can we
     * find a fit. For line fitting, it's 2. For homography, it's 4.
     * ITERNUM is the number of iteration, THDIST is the inlier
     * distance threshold and ROUND(THINLRRATIO*n) is the inlier number threshold.
     *
     * FUNCFINDF is a func handle, f1 = funcFindF(x1,y1)
     * x1 is M*n1 and y1 is N*n1, n1 >= ransacCoef.minPtNum
     * f1 can be of any type.
     * FUNCDIST is a func handle, d = funcDist(f,x1,y1)
     * It uses f returned by FUNCFINDF, and return the distance
     * between f and the points, d is 1*n1.
     * For line fitting, it should calculate the dist between the line and the
     * points [x1;y1]; for homography, it should project x1 to y2 then
     * calculate the dist between y1 and y2.
     */
    private static TransformationMatrix ransac(List<Position> p1, List<Position> p2, RansacConfig conf, IRansacFunctions functions) {
        int ptNum1 = p1.size();
        int ptNum2 = p2.size();
        if (conf.pairs && ptNum1 != ptNum2) {
            throw new IllegalArgumentException("ransac: `p1` and `p2` must have the same number of items!");
        }

        int[] inlrNum = new int[conf.iterNum];
        double[] inlrDist = new double[conf.iterNum];
        TransformationMatrix[] fLib = new TransformationMatrix[conf.iterNum];

        for (int p = 1; p <= conf.iterNum; p++) {
            // 1. fit using  random points
            int[] sampleIdx1 = randIndex(ptNum1, conf.minPtNum);
            int[] sampleIdx2 = conf.pairs ? sampleIdx1 : randIndex(ptNum2, conf.minPtNum);
            TransformationMatrix f1 = functions.findTransform(select(p1, sampleIdx1), select(p2, sampleIdx2));

            // 2. count the inliers, if more than thInlr, refit; else iterate
            List<Pair<Double, Integer>> distIdx = functions.distance(f1, p1, p2);
            List<Integer> inlier1 = findInliers(distIdx, conf.thDist);
            if (!functions.isResultValid(f1)) continue;
            if (inlier1.size() < conf.thInlr) continue;
            inlrNum[p] = inlier1.size();
            inlrDist[p] = VectorMath.sum(selectDistances(distIdx, inlier1));
            fLib[p] = functions.findTransform(select(p1, inlier1), select(p2, selectIndices(distIdx, inlier1)));
        }

        // 3. choose the coef with the most inliers
        return fLib[findBest(inlrNum, inlrDist)];
    }

    /**
     * Select the coef with the most inliers; if there is more of them
     * with the same number of inliers, then choose the one with the
     * minimum distance.
     */
    private static int findBest(int[] inlrNum, double[] inlrDist) {
        if (inlrNum.length != inlrDist.length) {
            throw new IllegalArgumentException("`inlrNum` and `inlrDist` must have the same number of elements!");
        }
        int maxInlrVal = VectorMath.max(inlrNum);
        int minDistIdx = 0;
        for (int i = 1; i < inlrNum.length; i++) {
            if (inlrNum[i] == maxInlrVal) {
                if (inlrDist[i] < inlrDist[minDistIdx]) {
                    minDistIdx = i;
                }
            }
        }
        return minDistIdx;
    }

    /**
     * Randomly, non-repeatedly select `len` integers from 1:`maxIndex`.
     */
    private static int[] randIndex(int maxIndex, int len) {
        if (len >= maxIndex) {
            throw new IllegalArgumentException("randIndex: `len` can't be greater than number of indices when selecting non-repeatedly!");
        }

        int[] index = new int[len];
        List<Integer> available = new LinkedList<Integer>();
        for (int i = 0; i <= maxIndex; i++) available.add(i);
        Random rnd = new Random();
        for (int i = 0; i < len; i++) {
            index[i] = available.remove(rnd.nextInt(available.size()));
        }
        return index;
    }

    private static List<Position> select(List<Position> p, int[] idx) {
        List<Position> ret = new ArrayList<Position>(idx.length);
        for (int i : idx) ret.add(p.get(i));
        return ret;
    }

    private static List<Position> select(List<Position> p, List<Integer> idx) {
        List<Position> ret = new ArrayList<Position>(idx.size());
        for (int i : idx) ret.add(p.get(i));
        return ret;
    }

    private static List<Integer> findInliers(List<Pair<Double, Integer>> distIdx, double thDist2) {
        List<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < distIdx.size(); i++) {
            if (distIdx.get(i).first < thDist2) {
                ret.add(i);
            }
        }
        return ret;
    }

    private static int[] selectIndices(List<Pair<Double, Integer>> distIdx, List<Integer> idx) {
        int[] ret = new int[idx.size()];
        for (int i : idx) {
            ret[i] = distIdx.get(i).second;
        }
        return ret;
    }

    private static double[] selectDistances(List<Pair<Double, Integer>> distIdx, List<Integer> idx) {
        double[] ret = new double[idx.size()];
        for (int i : idx) {
            ret[i] = distIdx.get(i).first;
        }
        return ret;
    }
}
