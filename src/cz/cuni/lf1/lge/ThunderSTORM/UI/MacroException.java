package cz.cuni.lf1.lge.ThunderSTORM.UI;

public class MacroException extends RuntimeException {

    public MacroException() {
    }

    public MacroException(String message) {
        super(message);
    }

    public MacroException(String message, Throwable cause) {
        super(message, cause);
    }

    public MacroException(Throwable cause) {
        super(cause);
    }
}
