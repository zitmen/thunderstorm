package cz.cuni.lf1.lge.ThunderSTORM;

import javax.swing.JPanel;

/**
 * The interface every module has to implement.
 *
 * Module in this sense means a part of the ThunderSTORM which will appear in
 * the main window of this plugin. For example different filters, detectors or
 * estimators which are listed in the combo boxes in the
 * {@code AnalysisOptionsDialog}.
 */
public interface IModuleUI<T extends IModule> {

  /**
   * Return name of a module.
   *
   * @return name of a module
   */
  public String getName();

  /**
   * Panel with possible settings of a module.
   *
   * @return an instance of JPanel containing GUI controls necessary to recieve
   * module settings from an user
   */
  public JPanel getOptionsPanel();

  /**
   * Read the parameters back from the GUI controls after used submited them.
   */
  public void readParameters();
  
  public void resetToDefaults();
  
  /**
   * Record the module parameters to the imagej macro recorder. Use
   * {@code Recorder.recordOption(name, value)}. The parameter should not
   * conflict with other modules.
   */
  public void recordOptions();

  /**
   * Read the parameters from macro options string. Use
   * {@code Macro.getValue(options, name, defaultValue)} to get individual
   * parameter values.
   *
   * @param options String with options passed by
   * {@code IJ.run(command, options)}.
   */
  public void readMacroOptions(String options);

  /**
   * Returns the object that does the actual calculation. The object returned
   * <b>must be thread safe or a new Object must be returned</b> for each
   * invocation of this method.
   */
  public T getImplementation();
}
