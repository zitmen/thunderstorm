package ThunderSTORM.UI;

import ThunderSTORM.IModule;
import ThunderSTORM.detectors.IDetector;
import ThunderSTORM.estimators.IEstimator;
import ThunderSTORM.estimators.PSF.GaussianPSF;
import ThunderSTORM.estimators.PSF.PSF;
import ThunderSTORM.filters.IFilter;
import ij.process.FloatProcessor;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
 
public class AnalysisOptionsDialog implements ActionListener {

    private CardsPanel filters, detectors, estimators;
    private JButton preview, ok, cancel;
    private JFrame frame;
    
    public AnalysisOptionsDialog(JFrame frame, Vector<IModule> filters, int default_filter, Vector<IModule> detectors, int default_detector, Vector<IModule> estimators, int default_estimator) {
        this.frame = frame;
        //
        this.filters = new CardsPanel(filters);
        this.detectors = new CardsPanel(detectors);
        this.estimators = new CardsPanel(estimators);
        //
        this.filters.setDefaultComboBoxItem(default_filter);
        this.detectors.setDefaultComboBoxItem(default_detector);
        this.estimators.setDefaultComboBoxItem(default_estimator);
        //
        this.preview = new JButton("Preview...");
        this.ok = new JButton("Ok");
        this.cancel = new JButton("Cancel");
    }
     
    public void addComponentsToPane() {
        Container pane = frame.getContentPane();
        //
        pane.setLayout(new GridLayout(7,1));
        pane.add(filters.getPanel("Filters: "));
        pane.add(new JSeparator(JSeparator.HORIZONTAL));
        pane.add(detectors.getPanel("Detectors: "));
        pane.add(new JSeparator(JSeparator.HORIZONTAL));
        pane.add(estimators.getPanel("Estimators: "));
        pane.add(new JSeparator(JSeparator.HORIZONTAL));
        //
        preview.addActionListener(this);
        ok.addActionListener(this);
        cancel.addActionListener(this);
        //
        JPanel buttons = new JPanel();
        buttons.add(preview);
        buttons.add(Box.createHorizontalStrut(30));
        buttons.add(ok);
        buttons.add(cancel);
        pane.add(buttons);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals("Cancel")) {
            frame.dispose();
        } else if(e.getActionCommand().equals("Ok")) {
            throw new UnsupportedOperationException("Run the analysis!");
        } else if(e.getActionCommand().equals("Preview...")) {
            IFilter filter = (IFilter)filters.getActiveComboBoxItem();
            IDetector detector = (IDetector)detectors.getActiveComboBoxItem();
            IEstimator estimator = (IEstimator)estimators.getActiveComboBoxItem();
            //
            ((IModule)filter).readParameters();
            ((IModule)detector).readParameters();
            ((IModule)estimator).readParameters();
            //
            FloatProcessor image = null;    // TODO!
            Vector<PSF> results = estimator.estimateParameters(image, detector.detectMoleculeCandidates(filter.filterImage(image)));
            //
            // TODO: overlay!
            //
            throw new UnsupportedOperationException("Run the analysis just on the active frame, show the overlay and render the points!");
        } else {
            throw new UnsupportedOperationException("Command '" + e.getActionCommand() + "' is not supported!");
        }
    }
    
}