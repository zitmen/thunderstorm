package cz.cuni.lf1.lge.ThunderSTORM.calibration;

public class CalibrationConfig {
    public double dist2thrZStackMatching;
    public int minimumFitsCount;
    public int polyFitMaxIters;
    public int finalPolyFitMaxIters;
    public int minFitsInZRange;
    public int movingAverageLag;
    public boolean checkIfDefocusIsInRange;
    public int inlierFittingMaxIters;
    public double inlierFittingInlierFraction;
    public boolean showResultsTable;
    public RansacConfig ransacTranslationAndFlip;
    public RansacConfig ransacHomography;

    // default configuration
    public CalibrationConfig() {
        dist2thrZStackMatching = 10.0;
        minimumFitsCount = 20;
        polyFitMaxIters = 750;
        finalPolyFitMaxIters = 2000;
        minFitsInZRange = 3;
        movingAverageLag = 5;
        checkIfDefocusIsInRange = false;
        inlierFittingMaxIters = 5;
        inlierFittingInlierFraction = 0.9;
        showResultsTable = false;
        ransacTranslationAndFlip = RansacConfig.createTranslationAndFlipConfig(1000, 15, 0.1);
        ransacHomography = RansacConfig.createHomographyConfig(1000, 1, 0.5, 3, 100.0);
    }

    public CalibrationConfig(double dist2thr, int minFits, int polyFitIters, int finalPolyFitIters,
                             int minFitsZRange, int movAvgLag, boolean checkDefocusInRange, int inlrFitIters,
                             double inlrFrac, boolean showResTable, RansacConfig transAndFlip, RansacConfig homography) {
        dist2thrZStackMatching = dist2thr;
        minimumFitsCount = minFits;
        polyFitMaxIters = polyFitIters;
        finalPolyFitMaxIters = finalPolyFitIters;
        minFitsInZRange = minFitsZRange;
        movingAverageLag = movAvgLag;
        checkIfDefocusIsInRange = checkDefocusInRange;
        inlierFittingMaxIters = inlrFitIters;
        inlierFittingInlierFraction = inlrFrac;
        showResultsTable = showResTable;
        ransacTranslationAndFlip = transAndFlip;
        ransacHomography = homography;
    }

    public static class RansacConfig {
        public int minPtNum;
        public int iterNum;
        public double thDist;         // precision [px]; usualy the precision is much higher (~1e-6 px)
        public double thInlr;         // [%]
        public boolean pairs;
        public double thPairDist;     // mutual distance of paired molecules [px] (should be same as `coef.thDist` set in `findTranslationAndFlip`)
        public double thAllowedTransformChange;   // point [1,1] after applying T can't move further than 100px,
                                                        // otherwise something is wrong, since this step should be just
                                                        // for fine-tuning! (this value is large on purpose, so the test
                                                        // does not reject a valid transformation)

        public static RansacConfig createTranslationAndFlipConfig(int iterNum, double thDist, double thInlr) {
            return new RansacConfig(2, iterNum, thDist, thInlr, false, 0.0, Double.POSITIVE_INFINITY);
        }

        public static RansacConfig createHomographyConfig(int iterNum, double thDist, double thInlr, double thPairDist, double thAllowedTransformChange) {
            return new RansacConfig(4, iterNum, thDist, thInlr, true, thPairDist, thAllowedTransformChange);
        }

        private RansacConfig(int minPtNum, int iterNum, double thDist, double thInlr, boolean pairs, double thPairDist, double thAllowedTransformChange) {
            this.minPtNum = minPtNum;
            this.iterNum = iterNum;
            this.thDist = thDist;
            this.thInlr = thInlr;
            this.pairs = pairs;
            this.thPairDist = thPairDist;
            this.thAllowedTransformChange = thAllowedTransformChange;
        }

        public int getInlierThreshold(int ptNum1, int ptNum2) {
            return (int) Math.max(minPtNum, Math.round(thInlr * Math.min(ptNum1, ptNum2)));
        }
    }
}
