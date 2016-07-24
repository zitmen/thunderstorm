package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.filters.ui.IFilterUI;
import cz.cuni.lf1.lge.ThunderSTORM.thresholding.Thresholder;
import ij.process.FloatProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import javax.swing.JPanel;

/**
 * No filtering.
 * 
 * This is useful in case of detectors of estimators that work better with raw images.
 * The {@code filterImage} method returns the {@code image} that it got on its input.
 */
public final class EmptyFilter extends IFilterUI implements IFilter {

  private final String name = "No filter";
  transient private FloatProcessor input = null;
  transient private HashMap<String, FloatProcessor> export_variables = null;

  @NotNull
  @Override
  public FloatProcessor filterImage(@NotNull FloatProcessor image) {
      GUI.checkIJEscapePressed();
      input = image;
      return image;
  }

  @Override
  public String getFilterVarName() {
      return "empty";
  }

  @NotNull
  @Override
  public HashMap<String, FloatProcessor> exportVariables(boolean reevaluate) {
      if(export_variables == null) export_variables = new HashMap<String, FloatProcessor>();
      //
      if(reevaluate) {
        filterImage(Thresholder.getCurrentImage());
      }
      //
      export_variables.put("I", input);
      export_variables.put("F", input);
      return export_variables;
  }
  
  @Override
  public String getName() {
    return name;
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
  
  @NotNull
  @Override
  public IFilter clone() {
    return new EmptyFilter();
  }

  @Override
  public void resetToDefaults() {
  }
}
