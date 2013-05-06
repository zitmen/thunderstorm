package cz.cuni.lf1.lge.ThunderSTORM;

import javax.swing.JPanel;

/**
 *
 * @author Martin Ovesny &lt;martin.ovesny[at]lf1.cuni.cz&gt;
 */
public interface IModule {
    
    /**
     *
     * @return
     */
    public String getName();
    /**
     *
     * @return
     */
    public JPanel getOptionsPanel();
    /**
     *
     */
    public void readParameters();
    
}
