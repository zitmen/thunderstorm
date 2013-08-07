package cz.cuni.lf1.lge.ThunderSTORM.UI;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class MacroException extends RuntimeException{

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

  public MacroException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
  
}
