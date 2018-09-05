
package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import javax.swing.JPanel;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;

public class EmptyRendererUI extends AbstractRenderingUI {

	public EmptyRendererUI() {
		super();
		parameters.loadPrefs();
	}

	@Override
	public String getName() {
		return "No Renderer";
	}

	@Override
	public JPanel getOptionsPanel() {
		return null;
	}

	@Override
	public void readParameters() {}

	@Override
	public void setSize(double width, double height) {}

	@Override
	public void recordOptions() {}

	@Override
	public void readMacroOptions(String options) {}

	@Override
	public int getRepaintFrequency() {
		return 0;
	}

	@Override
	public IncrementalRenderingMethod getImplementation() {
		return null;
	}

	@Override
	public void resetToDefaults() {}

	@Override
	protected IncrementalRenderingMethod getMethod() {
		return null;
	}
}
