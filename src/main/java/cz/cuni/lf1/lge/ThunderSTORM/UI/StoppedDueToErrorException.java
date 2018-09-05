
package cz.cuni.lf1.lge.ThunderSTORM.UI;

public class StoppedDueToErrorException extends RuntimeException {

	public StoppedDueToErrorException() {}

	public StoppedDueToErrorException(String message) {
		super(message);
	}

	public StoppedDueToErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	public StoppedDueToErrorException(Throwable cause) {
		super(cause);
	}

}
