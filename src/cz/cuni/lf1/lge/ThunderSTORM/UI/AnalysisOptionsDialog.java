package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.detectors.IDetector;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.IEstimator;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSF;
import cz.cuni.lf1.lge.ThunderSTORM.filters.IFilter;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
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
    private FloatProcessor fp;
    private ImagePlus imp;
    
    // TODO: predavani IP je uplne dementni...tohle je tu jen pro jednoduchost, abych videl aspon nejakej vysledek!! REFACTOR!!
    public AnalysisOptionsDialog(ImagePlus imp, ImageProcessor ip, JFrame frame, Vector<IModule> filters, int default_filter, Vector<IModule> detectors, int default_detector, Vector<IModule> estimators, int default_estimator) {
        this.imp = imp;
        this.fp = (FloatProcessor)ip.convertToFloat();
        //
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
        this.preview = new JButton("Preview");
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
        } else if(e.getActionCommand().equals("Preview")) {
            IFilter filter = (IFilter)filters.getActiveComboBoxItem();
            IDetector detector = (IDetector)detectors.getActiveComboBoxItem();
            IEstimator estimator = (IEstimator)estimators.getActiveComboBoxItem();
            //
            ((IModule)filter).readParameters();
            ((IModule)detector).readParameters();
            ((IModule)estimator).readParameters();
            //
            Vector<PSF> results = estimator.estimateParameters(fp, detector.detectMoleculeCandidates(filter.filterImage(fp)));
            //Vector<Point> results = detector.detectMoleculeCandidates(filter.filterImage(fp));
            //
            double [] xCoord = new double[results.size()];
            double [] yCoord = new double[results.size()];
            for(int i = 0; i < results.size(); i++) {
                xCoord[i] = results.elementAt(i).xpos;
                yCoord[i] = results.elementAt(i).ypos;
                //xCoord[i] = results.elementAt(i).x.doubleValue() + 0.5;
                //yCoord[i] = results.elementAt(i).y.doubleValue() + 0.5;
            }    
            RenderingOverlay.showPointsInImage(imp, xCoord, yCoord, Color.red, RenderingOverlay.MARKER_CROSS);
        } else {
            throw new UnsupportedOperationException("Command '" + e.getActionCommand() + "' is not supported!");
        }
    }
    
}