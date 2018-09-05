
package cz.cuni.lf1.lge.ThunderSTORM.results;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

public class IJGroundTruthTable extends GenericTable<GroundTruthTableWindow> {

	public static final String TITLE = "ThunderSTORM: ground-truth";
	public static final String IDENTIFIER = "ground-truth";

	private static IJGroundTruthTable gtTable = null;

	public synchronized static IJGroundTruthTable getGroundTruthTable() {
		if (gtTable == null) {
			if (SwingUtilities.isEventDispatchThread()) {
				setGroundTruthTable(new IJGroundTruthTable());
			}
			else {
				try {
					SwingUtilities.invokeAndWait(new Runnable() {

						@Override
						public void run() {
							setGroundTruthTable(new IJGroundTruthTable());
						}
					});
				}
				catch (InterruptedException ex) {
					throw new RuntimeException(ex);
				}
				catch (InvocationTargetException ex) {
					throw new RuntimeException(ex.getCause());
				}
			}
		}
		return gtTable;
	}

	public static void setGroundTruthTable(IJGroundTruthTable gt) {
		gtTable = gt;
	}

	public static boolean isGroundTruthWindow() {
		if (gtTable == null) {
			return false;
		}
		return gtTable.tableWindow.isVisible();
	}

	/**
	 * Constructs an empty GroundTruthTable.
	 */
	public IJGroundTruthTable() {
		super(new GroundTruthTableWindow(IJGroundTruthTable.TITLE));
	}

	@Override
	public String getFrameTitle() {
		return IJGroundTruthTable.TITLE;
	}

	@Override
	public String getTableIdentifier() {
		return IJGroundTruthTable.IDENTIFIER;
	}

}
