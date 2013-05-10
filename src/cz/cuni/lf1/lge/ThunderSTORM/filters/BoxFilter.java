package cz.cuni.lf1.lge.ThunderSTORM.filters;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import ij.IJ;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 */
public final class BoxFilter extends UniformFilter implements IModule {
    
    private JTextField sizeTextField;
    
    /**
     *
     * @param size
     */
    public BoxFilter(int size) {
        super(size, 1.0f / (float) size);
    }
    
    /**
     *
     * @return
     */
    @Override
    public String getName() {
        return "Box (mean) filter";
        
    }
    
    /**
     *
     * @return
     */
    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = new JPanel();
        sizeTextField = new JTextField(Integer.toString(super.size), 20);
        //
        panel.add(new JLabel("Size: "));
        panel.add(sizeTextField);
        return panel;
    }
    
    /**
     *
     */
    @Override
    public void readParameters() {
        try {
            int s = Integer.parseInt(sizeTextField.getText());
            super.updateKernel(s, 1.0f / (float) s);
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }
    
}
