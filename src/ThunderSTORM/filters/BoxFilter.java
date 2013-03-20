package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BoxFilter extends UniformFilter implements IModule {
    
    public BoxFilter(int size) {
        super(size, 1.0f / (float) size);
    }
    
    @Override
    public String getName() {
        return "Box (mean) filter";
        
    }
    
    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel();
        panel.add(new JLabel("Size: "));
        panel.add(new JTextField("Size", 20));
        return panel;
    }
    
}
