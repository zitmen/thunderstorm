package cz.cuni.lf1.lge.ThunderSTORM.util;

import ij.IJ;

public class IJProgressTracker implements ProgressTracker {

    double offset;
    double allAmmount;

    public IJProgressTracker() {
        this(0, 1);
    }

    public IJProgressTracker(double offset, double allAmmount) {
        if(offset + allAmmount > 1) {
            throw new RuntimeException("Maximum progress exceeds 1.");
        }
        this.offset = offset;
        this.allAmmount = allAmmount;
    }

    @Override
    public void progress(double progress) {
        IJ.showProgress(offset + progress * allAmmount);
    }

}
