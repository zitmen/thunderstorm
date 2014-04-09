
package cz.cuni.lf1.lge.ThunderSTORM.calibration;

public class NoMoleculesFittedException extends RuntimeException {

    public NoMoleculesFittedException() {
    }

    public NoMoleculesFittedException(String message) {
        super(message);
    }

    public NoMoleculesFittedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoMoleculesFittedException(Throwable cause) {
        super(cause);
    }

}
