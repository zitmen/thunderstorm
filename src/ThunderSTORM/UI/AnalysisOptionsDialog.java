package ThunderSTORM.UI;

import ThunderSTORM.IModule;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
 
public class AnalysisOptionsDialog {

    private CardsPanel filters, detectors, estimators;
    
    public AnalysisOptionsDialog(Vector<IModule> filters, int default_filter, Vector<IModule> detectors, int default_detector, Vector<IModule> estimators, int default_estimator) {
        this.filters = new CardsPanel(filters);
        this.detectors = new CardsPanel(detectors);
        this.estimators = new CardsPanel(estimators);
        //
        this.filters.setDefaultComboBoxItem(default_filter);
        this.detectors.setDefaultComboBoxItem(default_detector);
        this.estimators.setDefaultComboBoxItem(default_estimator);
    }
     
    public void addComponentsToPane(Container pane) {
        pane.setLayout(new GridLayout(7,1));
        pane.add(filters.getPanel("Filters: "));
        pane.add(new JSeparator(JSeparator.HORIZONTAL));
        pane.add(detectors.getPanel("Detectors: "));
        pane.add(new JSeparator(JSeparator.HORIZONTAL));
        pane.add(estimators.getPanel("Estimators: "));
        pane.add(new JSeparator(JSeparator.HORIZONTAL));
        //
        JPanel buttons = new JPanel();
        buttons.add(new JButton("Preview..."));
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(new JButton("OK"));
        buttons.add(new JButton("Cancel"));
        pane.add(buttons);
    }
    
}