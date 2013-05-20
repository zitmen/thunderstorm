package cz.cuni.lf1.lge.ThunderSTORM;

import javax.swing.JPanel;

/**
 * The interface every module has to implement.
 * 
 * Module in this sense means a part of the ThunderSTORM which will
 * appear in the main window of this plugin.
 * For example different filters, detectors or estimators which are listed
 * in the combo boxes in the {@code AnalysisOptionsDialog}.
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
     * @return an instance of JPanel containing GUI controls necessary
     *         to recieve module settings from an user
     */
    public JPanel getOptionsPanel();
    
    /**
     * Read the parameters back from the GUI controls after used submited them.
     */
    public void readParameters();
    
    public T getImplementation();
}
