package cz.cuni.lf1.lge.ThunderSTORM.calibration;

import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.Molecule;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.MoleculeDescriptor;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel;
import cz.cuni.lf1.lge.ThunderSTORM.util.*;
import org.apache.commons.math3.linear.*;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.SequenceNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import java.util.*;

import static cz.cuni.lf1.lge.ThunderSTORM.calibration.PSFSeparator.Position;

/**
 * This implementation is directly re-written from Matlab without any optimizations!
 */
public class Homography {

    private static final double dist2thr = 10.0;

    public static TransformationMatrix estimateTransform(int w1, int h1, List<Position> fits1, int w2, int h2, List<Position> fits2) {
        List<Position> pt1 = moveToCenterXY(fits1, w1, h1);
        List<Position> pt2 = moveToCenterXY(fits2, w2, h2);
        return findHomography(findTranslationAndFlip(pt1, pt2), pt1, pt2);
    }

    /**
     * 1) Based on the transformation, pair up the fits[1|2] together
     * 2) For each fit in matched subset of fits1 select only the subset of Molecules (z-slice) present both in fits[1|2]
     * 3) For each pair of Molecules assign sigma1 and sigma2 for the moleule from fits1
     * 4) Return the Map of Positions containing the Molecules with sigmas properly set
     *    --> these are used for estimation of calibration curves
     */
    public static Map<Position, Position> mergePositions(TransformationMatrix transform, IBinaryTransform<Position> mapping, int w1, int h1, List<Position> fits1, int w2, int h2, List<Position> fits2) {
        // 1)
        List<Position> p1 = copyInnerFits(applyH(transform, moveToCenterXY(fits1, w1, h1)), fits1);
        List<Position> p2 = copyInnerFits(moveToCenterXY(fits2, w2, h2), fits2);
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
            mapping.map(pos1, pos2);
            // -> [optional] recalculate the position as it has been transformed by the transform
            pos1.recalculateCentroid();
            pos2.recalculateCentroid();
        }
        // 4)
        return pairs;
    }

    /**
     * This method is simplier compared to the other one in the fact that fits[1|2]
     * are supposed to be in the same frame, but different planes. The only purpose
     * is to match the pairs.
     *
     * TODO: this is not very effective approach as the number of allocations is high!
     */
    public static List<Pair<Point, Point>> mergePositions(int width, int height, TransformationMatrix transform, List<Point> fits1, List<Point> fits2, double dist2px) {
        MoleculeDescriptor descriptor = new MoleculeDescriptor(
                new String[]{ MoleculeDescriptor.LABEL_ID, PSFModel.Params.LABEL_X, PSFModel.Params.LABEL_Y },
                new MoleculeDescriptor.Units[] { MoleculeDescriptor.Units.UNITLESS, MoleculeDescriptor.Units.PIXEL, MoleculeDescriptor.Units.PIXEL });
        // wrap to molecules
        List<Molecule> m1 = new ArrayList<Molecule>(fits1.size());
        List<Molecule> m2 = new ArrayList<Molecule>(fits2.size());
        for (int i = 0; i < fits1.size(); i++) {
            Point p = fits1.get(i);
            m1.add(new Molecule(descriptor, new double[] {
                    i, p.getX().doubleValue(), p.getY().doubleValue() }));
        }
        for (int i = 0; i < fits2.size(); i++) {
            Point p = fits2.get(i);
            m2.add(new Molecule(descriptor, new double[] {
                    i, p.getX().doubleValue(), p.getY().doubleValue() }));
        }
        // pair up the molecules
        m1 = applyH(transform, moveToCenterXY(m1, width, height));
        m2 = moveToCenterXY(m2, width, height);
        for (Molecule m : m1) {
            m.addNeighbors(m2, false, dist2px, MoleculeDescriptor.Units.PIXEL);
        }
        Map<Molecule, Molecule> map = StableMatching.match(m1);
        // unwrap
        List<Pair<Point, Point>> pairs = new ArrayList<Pair<Point, Point>>(map.size());
        int idIndex = descriptor.getParamIndex(MoleculeDescriptor.LABEL_ID);
        for (Map.Entry<Molecule, Molecule> pair : map.entrySet()) {
            pairs.add(new Pair<Point, Point>(fits1.get((int) pair.getKey().getParamAt(idIndex)),
                                             fits2.get((int) pair.getValue().getParamAt(idIndex))));
        }
        return pairs;
    }

    private static List<Position> copyInnerFits(List<Position> positions, List<Position> source) {
        if (positions.size() != source.size()) {
            throw new IllegalArgumentException("`positions` and `source` have to be of the same size!");
        }
        for (int i = 0; i < positions.size(); i++) {
            positions.get(i).fits = source.get(i).fits;
        }
        return positions;
    }

    public static List<PSFSeparator.Position> transformPositions(TransformationMatrix transformationMatrix, List<PSFSeparator.Position> positions, int width, int height) {
        return moveToBoundaryXY(applyH(transformationMatrix, moveToCenterXY(positions, width, height)), width, height);
    }

    public static class TransformationMatrix {
        private RealMatrix matrix;

        private TransformationMatrix() {
            matrix = null;
        }

        public <T extends IMatchable<T>> T transform(T p) {
            RealMatrix pMat = new Array2DRowRealMatrix(new double[][]{{p.getX()}, {p.getY()}, {1.0}});   // 3x1 matrix
            RealMatrix res = matrix.multiply(pMat);  // 1x3 matrix
            T pos = p.clone();
            pos.setX(res.getEntry(0, 0) / res.getEntry(2, 0));
            pos.setY(res.getEntry(1, 0) / res.getEntry(2, 0));
            return pos;
        }

        public static TransformationMatrix createIdentity() {
            TransformationMatrix tm = new TransformationMatrix();
            tm.matrix = new Array2DRowRealMatrix(new double[][] {{1,0,0},{0,1,0},{0,0,1}});    // 3x3 identity matrix
            return tm;
        }

        public TransformationMatrix inverse() {
            return TransformationMatrix.createFrom(new LUDecomposition(matrix).getSolver().getInverse());
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

        @Override
        public String toString() {
            if (matrix == null) return "null";
            String str = "{";
            for (int r = 0; r < matrix.getRowDimension(); r++) {
                str += "{";
                for (int c = 0; c < matrix.getColumnDimension(); c++) {
                    str += matrix.getEntry(r, c) + ",";
                }
                str = str.substring(0, str.length() - 1) + "},";
            }
            str = str.substring(0, str.length() - 1) + "}";
            return str;
        }

        public static TransformationMatrix getNormalization(List<Position> positions) {
            // calculate a translation
            Position centroid = new Position();
            for (Position p : positions) {
                centroid.centroidX += p.centroidX;
                centroid.centroidY += p.centroidY;
            }
            centroid.centroidX /= (double) positions.size();
            centroid.centroidY /= (double) positions.size();
            // calculate an isotropic scale
            double scale = 0;
            for (Position p : positions) {
                scale += Math.sqrt(calcDist2(centroid, p));
            }
            scale /= (double) positions.size() * Math.sqrt(2);
            // create a normalization matrix
            return TransformationMatrix.createFrom(new Array2DRowRealMatrix(new double[][]
                {{1.0/scale, 0.0      , -centroid.centroidX/scale},
                 {0.0      , 1.0/scale, -centroid.centroidY/scale},
                 {0.0      , 0.0      , 1.0}}));
        }

        public static TransformationMatrix getDenormalizedHomography(TransformationMatrix homography, TransformationMatrix norm1, TransformationMatrix norm2) {
            if (homography == null || norm1 == null || norm2 == null) return null;

            RealMatrix Hhat = homography.matrix;
            RealMatrix T1 = norm1.matrix;
            RealMatrix T2inv = new LUDecomposition(norm2.matrix).getSolver().getInverse();
            RealMatrix H = T2inv.multiply(Hhat).multiply(T1);
            double norm = H.getEntry(2, 2);
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    H.setEntry(r, c, H.getEntry(r, c) / norm);
                }
            }
            return TransformationMatrix.createFrom(H);
        }

        public static class YamlConstructor extends Constructor {
            public YamlConstructor() {
                this.yamlConstructors.put(new Tag("!homography"), new ConstructHomography());
            }

            private class ConstructHomography extends AbstractConstruct {
                public Object construct(Node node) {
                    @SuppressWarnings("unchecked") List<Double> sequence = (List<Double>) constructSequence((SequenceNode) node);
                    if (sequence == null || sequence.size() != 9) return null;
                    double[][] mat = new double[3][3];
                    for (int r = 0, i = 0; r < 3; r++) {
                        for (int c = 0; c < 3; c++, i++) {
                            mat[r][c] = sequence.get(i);
                        }
                    }
                    return TransformationMatrix.createFrom(new Array2DRowRealMatrix(mat));
                }
            }
        }

        public static class YamlRepresenter extends Representer {
            public YamlRepresenter() {
                this.representers.put(Homography.TransformationMatrix.class, new RepresentHomography());
            }

            private class RepresentHomography implements Represent {
                public Node representData(Object data) {
                    List<Double> sequence = new ArrayList<Double>(9);
                    TransformationMatrix mat = (TransformationMatrix) data;
                    for (int r = 0; r < 3; r++) {
                        for (int c = 0; c < 3; c++) {
                            sequence.add(mat.matrix.getEntry(r, c));
                        }
                    }
                    return representSequence(new Tag("!homography"), sequence, null);
                }
            }
        }
    }

    private static <T extends IMatchable<T>> List<T> moveToCenterXY(Iterable<T> posIn, int width, int height) {
        double shiftX = ((double) width) / 2.0;
        double shiftY = ((double) height) / 2.0;
        List<T> posOut = new ArrayList<T>();
        for (T p : posIn) {
            T pos = p.clone();
            pos.setX(p.getX() - shiftX);
            pos.setY(p.getY() - shiftY);
            posOut.add(pos);
        }
        return posOut;
    }

    private static List<Position> moveToBoundaryXY(List<Position> posIn, int width, int height) {
        double shiftX = ((double) width) / 2.0;
        double shiftY = ((double) height) / 2.0;
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
        if (p1 == null || p2 == null) return null;

        RansacConfig config = new RansacConfig();
        config.minPtNum = 2;
        config.iterNum = 1000;
        config.thDist = 15;
        config.thInlr = Math.max(config.minPtNum, Math.round(0.1 * Math.min(p1.size(), p2.size())));    // 10%
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
        if (initialGuess == null || p1 == null || p2 == null) return null;

        RansacConfig config = new RansacConfig();
        config.minPtNum = 4;
        config.iterNum = 1000;
        config.thDist = 1;  // precision [px]; usualy the precision is much higher (~1e-6 px)
        config.thInlr = Math.max(config.minPtNum, Math.round(0.5 * Math.min(p1.size(), p2.size())));    // 50%
        config.pairs = true;
        config.thPairDist = 3;   // mutual distance of paired molecules [px] (should be same as `coef.thDist` set in `findTranslationAndFlip`)
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
        TransformationMatrix norm1 = TransformationMatrix.getNormalization(pairs.first);
        TransformationMatrix norm2 = TransformationMatrix.getNormalization(pairs.second);
        config.thDist *= norm1.matrix.getEntry(0, 0);   // isotropic scale
        TransformationMatrix homography = ransac(applyH(norm1, pairs.first), applyH(norm2, pairs.second), config, functions);
        return TransformationMatrix.getDenormalizedHomography(homography, norm1, norm2);
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
            Pair<Double, Integer> pair = distIdx1.get(distIdx2.get(i).second);
            bi[i] = pair.second == i;
            if (!bi[i]) {
                pair.first = Double.POSITIVE_INFINITY;
            }
        }

        return distIdx1;
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
            for (int j = 0; j < pts2.size(); j++) {
                distMap[i][j] = Math.sqrt(calcDist2(pts1.get(i), pts2.get(j)));
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

    private static <T extends IMatchable<T>> List<T> applyH(TransformationMatrix t, Iterable<T> pIn) {
        List<T> pOut = new ArrayList<T>();
        for (T p : pIn) {
            pOut.add(t.transform(p));
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
        List<Position> X = new ArrayList<Position>();
        List<Position> Y = new ArrayList<Position>();
        for (int i = 0; i < DI.first.length; i++){
            for (int j = 0; j < DI.first[i].length; j++){
                if (DI.first[i][j] < thPairDist) {
                    X.add(pts1.get(j));
                    Y.add(pts2.get(i));
                }
            }
        }
        return new Pair<List<Position>, List<Position>>(X, Y);
    }

    // H is 3x3, H*[pts1(:,i);1] ~ [pts2(:,i);1], H(3,3) = 1
    private static TransformationMatrix solveHomography(List<Position> p1, List<Position> p2) {
        if (p1.size() != p2.size()) {
            throw new IllegalArgumentException("`p1` and `p2` must have the same number of elements!");
        }
        if (p1.size() < 4) {
            throw new IllegalArgumentException("Homography can't be solved for less than 4 points of interest!");
        }

        double[][] A = zeros(2*p1.size(), 9);
        for (int i = 0; i < p1.size(); i++) {
            A[2*i][0] = p1.get(i).centroidX;
            A[2*i][1] = p1.get(i).centroidY;
            A[2*i][2] = 1.0;
            A[2*i+1][3] = p1.get(i).centroidX;
            A[2*i+1][4] = p1.get(i).centroidY;
            A[2*i+1][5] = 1.0;
            A[2*i][6] = -p2.get(i).centroidX * p1.get(i).centroidX;
            A[2*i+1][6] = -p2.get(i).centroidY * p1.get(i).centroidX;
            A[2*i][7] = -p2.get(i).centroidX * p1.get(i).centroidY;
            A[2*i+1][7] = -p2.get(i).centroidY * p1.get(i).centroidY;
            A[2*i][8] = -p2.get(i).centroidX;
            A[2*i+1][8] = -p2.get(i).centroidY;
        }

        // [evec,~] = eig(A'*A);
        Array2DRowRealMatrix matA = new Array2DRowRealMatrix(A);
        EigenDecomposition eig = new EigenDecomposition(matA.transpose().multiply(matA));

        // H = reshape(evec(:,1),[3,3])';
        // H = H/H(end); % make H(3,3) = 1
        Array2DRowRealMatrix H = new Array2DRowRealMatrix(3, 3);
        RealVector eigVec = eig.getEigenvector(8);
        for (int r = 0, i = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++, i++) {
                H.setEntry(r, c, eigVec.getEntry(i) / eigVec.getEntry(8));
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
        Position ones = new Position(1.0, 1.0);
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

        List<Integer> inlrNum = new ArrayList<Integer>(conf.iterNum);
        List<Double> inlrDist = new ArrayList<Double>(conf.iterNum);
        List<TransformationMatrix> fLib = new ArrayList<TransformationMatrix>(conf.iterNum);

        for (int p = 0; p < conf.iterNum; p++) {
            // 1. fit using  random points
            int[] sampleIdx1 = randIndex(ptNum1 - 1, conf.minPtNum);
            int[] sampleIdx2 = conf.pairs ? sampleIdx1 : randIndex(ptNum2 - 1, conf.minPtNum);
            TransformationMatrix f1 = functions.findTransform(select(p1, sampleIdx1), select(p2, sampleIdx2));
            if (!functions.isResultValid(f1)) continue;

            // 2. count the inliers, if more than thInlr, refit; else iterate
            List<Pair<Double, Integer>> distIdx = functions.distance(f1, p1, p2);
            List<Integer> inlier1 = findInliers(distIdx, conf.thDist);
            if (inlier1.size() < conf.thInlr) continue;

            // 3. store the transform made of the inliers
            inlrNum.add(inlier1.size());
            inlrDist.add(VectorMath.sum(selectDistances(distIdx, inlier1)));
            fLib.add(functions.findTransform(select(p1, inlier1), select(p2, selectIndices(distIdx, inlier1))));
        }

        // 4. choose the coef with the most inliers
        if (inlrNum.isEmpty()) return null;
        return fLib.get(findBest(inlrNum, inlrDist));
    }

    /**
     * Select the coef with the most inliers; if there is more of them
     * with the same number of inliers, then choose the one with the
     * minimum distance.
     */
    private static int findBest(List<Integer> inlrNum, List<Double> inlrDist) {
        if (inlrNum.size() != inlrDist.size()) {
            throw new IllegalArgumentException("`inlrNum` and `inlrDist` must have the same number of elements!");
        }
        int minDistIdx = 0, maxInlrVal = inlrNum.get(0);
        for (int i = 1; i < inlrNum.size(); i++) {
            int inlr = inlrNum.get(i);
            if (inlr > maxInlrVal) {
                maxInlrVal = inlr;
                minDistIdx = i;
            } else if (inlr == maxInlrVal) {
                if (inlrDist.get(i) < inlrDist.get(minDistIdx)) {
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
        if (len > maxIndex + 1) {
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

    private static List<Integer> findInliers(List<Pair<Double, Integer>> distIdx, double thDist) {
        List<Integer> ret = new ArrayList<Integer>();
        for (int i = 0; i < distIdx.size(); i++) {
            if (distIdx.get(i).first < thDist) {
                ret.add(i);
            }
        }
        return ret;
    }

    private static int[] selectIndices(List<Pair<Double, Integer>> distIdx, List<Integer> idx) {
        int[] ret = new int[idx.size()];
        for (int i = 0; i < idx.size(); i++) {
            ret[i] = distIdx.get(idx.get(i)).second;
        }
        return ret;
    }

    private static double[] selectDistances(List<Pair<Double, Integer>> distIdx, List<Integer> idx) {
        double[] ret = new double[idx.size()];
        for (int i = 0; i < idx.size(); i++) {
            ret[i] = distIdx.get(idx.get(i)).first;
        }
        return ret;
    }
}
