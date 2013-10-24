package cz.cuni.lf1.lge.ThunderSTORM.UI;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class StoppedByUserException extends RuntimeException{

    public StoppedByUserException() {
    }

    public StoppedByUserException(String message) {
        super(message);
    }

    public StoppedByUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public StoppedByUserException(Throwable cause) {
        super(cause);
    }

    public StoppedByUserException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
    
}
