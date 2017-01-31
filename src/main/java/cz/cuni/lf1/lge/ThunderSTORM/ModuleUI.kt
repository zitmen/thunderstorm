package cz.cuni.lf1.lge.ThunderSTORM

import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.ParameterTracker

import javax.swing.*

public abstract class ModuleUI<T> {

    @Transient protected val parameters = ParameterTracker(getPreferencesPrefix())

    init {
        parameters.setNoGuiParametersAllowed(true)
    }

    /**
     * Return name of a module.
     *
     * @return name of a module
     */
    public abstract fun getName(): String

    /**
     * Panel with possible settings of a module.
     *
     * @return an instance of JPanel containing GUI controls necessary to
     * recieve module settings from an user
     */
    public abstract fun getOptionsPanel(): JPanel

    /**
     * Read the parameters back from the GUI controls after used submited them.
     */
    public open fun readParameters() {
        parameters.readDialogOptions()
        parameters.savePrefs()
    }

    protected open fun getPreferencesPrefix()
            = "thunderstorm"

    public open fun resetToDefaults() {
        parameters.resetToDefaults(true)
    }

    /**
     * Record the module parameters to the imagej macro recorder. Use
     * {@code Recorder.recordOption(name, value)}. The parameter should not
     * conflict with other modules.
     */
    public open fun recordOptions() {
        parameters.recordMacroOptions()
    }

    /**
     * Read the parameters from macro options string. Use
     * {@code Macro.getValue(options, name, defaultValue)} to get individual
     * parameter values.
     *
     * @param options String with options passed by
     * {@code IJ.run(command, options)}.
     */
    public open fun readMacroOptions(options: String) {
        parameters.readMacroOptions()
    }

    protected fun getParameterTracker()
            = parameters

    /**
     * Returns the object that does the actual calculation. The object returned
     * <b>must be thread safe or a new Object must be returned</b> for each
     * invocation of this method.
     */
    public abstract fun getImplementation(): T
}
