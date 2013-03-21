package ThunderSTORM.detectors;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.Graph;
import ThunderSTORM.utils.GridBagHelper;
import ThunderSTORM.utils.Point;
import ij.IJ;
import ij.process.FloatProcessor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class LocalMaximaDetector implements IDetector, IModule {

    private int connectivity;
    private double threshold;
    private FloatProcessor image;
    
    private JTextField thrTextField;
    private JRadioButton conn4RadioButton, conn8RadioButton;
    
    public LocalMaximaDetector(int connectivity, double threshold) {
        assert((connectivity == Graph.CONNECTIVITY_4) || (connectivity == Graph.CONNECTIVITY_8));
        
        this.connectivity = connectivity;
        this.threshold = threshold;
    }
    
    // values local, (w)est, (e)ast, (n)orth, (s)outh are precomputed too speed things up speed
    private boolean isMax4Thr(int x, int y, float local, boolean w, boolean e, boolean n, boolean s) {
        if(local < threshold) return false;
        
        if(w) if(image.getPixelValue(x-1,y  ) > local) return false;
        if(e) if(image.getPixelValue(x+1,y  ) > local) return false;
        if(n) if(image.getPixelValue(x  ,y-1) > local) return false;
        if(s) if(image.getPixelValue(x  ,y+1) > local) return false;
        return true;
    }
    
    private boolean isMax8Thr(int x, int y, float local, boolean w, boolean e, boolean n, boolean s) {
        if(isMax4Thr(x, y, local, w, e, n, s) == false) return false;
        
        if(w && n) if(image.getPixelValue(x-1,y-1) > local) return false;
        if(w && s) if(image.getPixelValue(x-1,y+1) > local) return false;
        if(e && n) if(image.getPixelValue(x+1,y-1) > local) return false;
        if(e && s) if(image.getPixelValue(x+1,y+1) > local) return false;
        return false;
    }
    
    // the following two methods are duplicates, because of speed...this way I dont need to check every iteration if it is 4 or 8 neighbourhood version
    private Vector<Point> getMax4Candidates(FloatProcessor image) {
        Vector<Point> detections = new Vector<Point>();
        int cx = image.getWidth(), cy = image.getHeight();
        float value;
        
        // inner part of the image
        for(int x = 1, xm = cx-1; x < xm; x++) {
            for(int y = 1, ym = cy-1; y < ym; y++) {
                value = image.getPixelValue(x,y);
                isMax4Thr(x, y, value, true, true, true, true);
            }
        }
        // left border of the image
        for(int x = 0, y = 1, ym = cy-1; y < ym; y++) {
            value = image.getPixelValue(x,y);
            isMax4Thr(x, y, value, false, true, true, true);
        }
        // right border of the image
        for(int x = cx-1, y = 1, ym = cy-1; y < ym; y++) {
            value = image.getPixelValue(x,y);
            isMax4Thr(x, y, value, true, false, true, true);
        }
        // top border of the image
        for(int x = 1, xm = cx-1, y = 0; x < xm; x++) {
            value = image.getPixelValue(x,y);
            isMax4Thr(x, y, value, true, true, false, true);
        }
        // bottom border of the image
        for(int x = 1, xm = cx-1, y = cy-1; x < xm; x++) {
            value = image.getPixelValue(x,y);
            isMax4Thr(x, y, value, true, true, true, false);
        }
        // corners
        isMax4Thr(0 , 0, image.getPixelValue(0 , 0), false, true , false, true );
        isMax4Thr(cx, 0, image.getPixelValue(cx, 0), true , false, false, true );
        isMax4Thr(0 ,cy, image.getPixelValue(0 ,cy), false, true , true , false);
        isMax4Thr(cx,cy, image.getPixelValue(cx,cy), true , false, true , false);
        
        return detections;
    }
    
    private Vector<Point> getMax8Candidates(FloatProcessor image) {
        Vector<Point> detections = new Vector<Point>();
        int cx = image.getWidth(), cy = image.getHeight();
        
        // inner part of the image
        for(int x = 1, xm = cx-1; x < xm; x++) {
            for(int y = 1, ym = cy-1; y < ym; y++) {
                if(isMax8Thr(x, y, image.getPixelValue(x,y), true, true, true, true)) {
                    detections.add(new Point(x, y));
                }
            }
        }
        // left border of the image
        for(int x = 0, y = 1, ym = cy-1; y < ym; y++) {
            if(isMax8Thr(x, y, image.getPixelValue(x,y), false, true, true, true)) {
                detections.add(new Point(x, y));
            }
        }
        // right border of the image
        for(int x = cx-1, y = 1, ym = cy-1; y < ym; y++) {
            if(isMax8Thr(x, y, image.getPixelValue(x,y), true, false, true, true)) {
                detections.add(new Point(x, y));
            }
        }
        // top border of the image
        for(int x = 1, xm = cx-1, y = 0; x < xm; x++) {
            if(isMax8Thr(x, y, image.getPixelValue(x,y), true, true, false, true)) {
                detections.add(new Point(x, y));
            }
        }
        // bottom border of the image
        for(int x = 1, xm = cx-1, y = cy-1; x < xm; x++) {
            if(isMax8Thr(x, y, image.getPixelValue(x,y), true, true, true, false)) {
                detections.add(new Point(x, y));
            }
        }
        // corners
        if(isMax8Thr(0,0, image.getPixelValue(0,0), false, true, false, true)) {
            detections.add(new Point(0,0));
        }
        if(isMax8Thr(cx,0, image.getPixelValue(cx,0), true, false, false, true)) {
            detections.add(new Point(cx,0));
        }
        if(isMax8Thr(0,cy, image.getPixelValue(0,cy), false, true, true, false)) {
            detections.add(new Point(0,cy));
        }
        if(isMax8Thr(cx,cy, image.getPixelValue(cx,cy), true, false, true, false)) {
            detections.add(new Point(cx,cy));
        }
        
        return detections;
    }
    
    @Override
    public Vector<Point> detectMoleculeCandidates(FloatProcessor image) {
        return ((connectivity == Graph.CONNECTIVITY_4) ? getMax4Candidates(image) : getMax8Candidates(image));
    }

    @Override
    public String getName() {
        return "Search for local maxima";
    }

    @Override
    public JPanel getOptionsPanel() {
        thrTextField = new JTextField("Threshold", 20);
        conn4RadioButton = new JRadioButton("4-neighbourhood");
        conn8RadioButton = new JRadioButton("8-neighbourhood");
        //
        conn4RadioButton.setSelected(connectivity == Graph.CONNECTIVITY_4);
        conn8RadioButton.setSelected(connectivity == Graph.CONNECTIVITY_8);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel("Threshold: "), GridBagHelper.pos(0, 0));
        panel.add(thrTextField, GridBagHelper.pos(1, 0));
        panel.add(new JLabel("Connectivity: "), GridBagHelper.pos(0, 1));
        panel.add(conn8RadioButton, GridBagHelper.pos(1, 1));
        panel.add(conn4RadioButton, GridBagHelper.pos(1, 2));
        return panel;
    }

    @Override
    public void readParameters() {
        try {
            threshold = Double.parseDouble(thrTextField.getText());
            if(conn4RadioButton.isSelected()) connectivity = Graph.CONNECTIVITY_4;
            if(conn8RadioButton.isSelected()) connectivity = Graph.CONNECTIVITY_8;
        } catch(NumberFormatException ex) {
            IJ.showMessage("Error!", ex.getMessage());
        }
    }

}
