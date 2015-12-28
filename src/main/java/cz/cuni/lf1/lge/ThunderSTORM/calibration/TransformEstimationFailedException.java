
package cz.cuni.lf1.lge.ThunderSTORM.calibration;

public class TransformEstimationFailedException extends RuntimeException {

    public TransformEstimationFailedException() {
    }

    public TransformEstimationFailedException(String message) {
        super(message);
    }

    public TransformEstimationFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformEstimationFailedException(Throwable cause) {
        super(cause);
    }

}
