package ThunderSTORM.filters;

import ThunderSTORM.IModule;
import ij.IJ;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BoxFilter extends UniformFilter implements IModule {
    
    private boolean params_changed;
    
    private void initialize() {
        // TODO
        params_changed = false;
    }
    
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
    
    @Override
    public void readParameters() {
        try {
            size = Integer.parseInt(sizeTextField.getText());
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }
    
}
