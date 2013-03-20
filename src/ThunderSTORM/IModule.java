package ThunderSTORM;

import javax.swing.JPanel;

public interface IModule {
    
    public String getName();
    public JPanel getOptionsPanel();
    public void readParameters();
    
}
