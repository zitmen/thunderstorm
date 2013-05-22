package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import ij.process.FloatProcessor;
import java.util.HashMap;
import javax.swing.JPanel;

/**
 * No filtering.
 * 
 * This is useful in case of detectors of estimators that work better with raw images.
 * The {@code filterImage} method returns the {@code image} that it got on its input.
 */
public final class EmptyFilter implements IFilter, IFilterUI {
    
    private FloatProcessor input = null;
    private HashMap<String, FloatProcessor> export_variables = null;

    @Override
    public FloatProcessor filterImage(FloatProcessor image) {
        input = image;
        return image;
    }

    @Override
    public String getFilterVarName() {
        return "empty";
    }

    @Override
    public HashMap<String, FloatProcessor> exportVariables() {
        if(export_variables == null) export_variables = new HashMap<String, FloatProcessor>();
        //
        export_variables.put("I", input);
        export_variables.put("F", input);
        return export_variables;
    }
    @Override
  public String getName() {
    return "No filter";
  }

  @Override
  public JPanel getOptionsPanel() {
    return null;
  }

  @Override
  public void readParameters() {
    // nothing to do here
  }

  @Override
  public IFilter getImplementation() {
    return new EmptyFilter();
  }

  @Override
  public void recordOptions() {
  }

  @Override
  public void readMacroOptions(String options) {
  }
}
