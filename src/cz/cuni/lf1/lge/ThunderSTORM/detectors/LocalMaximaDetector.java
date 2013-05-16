package cz.cuni.lf1.lge.ThunderSTORM.detectors;

import cz.cuni.lf1.lge.ThunderSTORM.util.Graph;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Point;
import ij.process.FloatProcessor;
import java.awt.GridBagLayout;
import java.util.Vector;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Detection of local maxima points.
 */
public class LocalMaximaDetector implements IDetector {

    private int connectivity;
    private double threshold;
    
    private JTextField thrTextField;
    private JRadioButton conn4RadioButton, conn8RadioButton;
    
    private boolean isMax4Thr(FloatProcessor image, int x, int y, float local, boolean w, boolean e, boolean n, boolean s) {
        if(local < threshold) return false;
        
        if(w) if(image.getf(x-1,y  ) > local) return false;
        if(e) if(image.getf(x+1,y  ) > local) return false;
        if(n) if(image.getf(x  ,y-1) > local) return false;
        if(s) if(image.getf(x  ,y+1) > local) return false;
        return true;
    }
    
    private boolean isMax8Thr(FloatProcessor image, int x, int y, float local, boolean w, boolean e, boolean n, boolean s) {
        if(isMax4Thr(image, x, y, local, w, e, n, s) == false) return false;
        
        if(w && n) if(image.getf(x-1,y-1) > local) return false;
        if(w && s) if(image.getf(x-1,y+1) > local) return false;
        if(e && n) if(image.getf(x+1,y-1) > local) return false;
        if(e && s) if(image.getf(x+1,y+1) > local) return false;
        return true;
    }
    
    // the following two methods are duplicates, because of speed...this way I dont need to check every iteration if it is 4 or 8 neighbourhood version
    private Vector<Point> getMax4Candidates(FloatProcessor image) {
        Vector<Point> detections = new Vector<Point>();
        int cx = image.getWidth(), cy = image.getHeight();
        
        // inner part of the image
        for(int x = 1, xm = cx-1; x < xm; x++) {
            for(int y = 1, ym = cy-1; y < ym; y++) {
                if(isMax4Thr(image, x, y, image.getf(x,y), true, true, true, true))
                    detections.add(new Point(x, y, image.getf(x,y)));
            }
        }
        // left border of the image
        for(int x = 0, y = 1, ym = cy-1; y < ym; y++) {
            if(isMax4Thr(image, x, y, image.getf(x,y), false, true, true, true))
                detections.add(new Point(x, y, image.getf(x,y)));
        }
        // right border of the image
        for(int x = cx-1, y = 1, ym = cy-1; y < ym; y++) {
            if(isMax4Thr(image, x, y, image.getf(x,y), true, false, true, true))
                detections.add(new Point(x, y, image.getf(x,y)));
        }
        // top border of the image
        for(int x = 1, xm = cx-1, y = 0; x < xm; x++) {
            if(isMax4Thr(image, x, y, image.getf(x,y), true, true, false, true))
                detections.add(new Point(x, y, image.getf(x,y)));
        }
        // bottom border of the image
        for(int x = 1, xm = cx-1, y = cy-1; x < xm; x++) {
            if(isMax4Thr(image, x, y, image.getf(x,y), true, true, true, false))
                detections.add(new Point(x, y, image.getf(x,y)));
        }
        // corners
        cx -= 1; cy -= 1;
        if(isMax4Thr(image, 0 , 0, image.getf(0 , 0), false, true , false, true )) detections.add(new Point( 0, 0, image.getf( 0, 0)));
        if(isMax4Thr(image, cx, 0, image.getf(cx, 0), true , false, false, true )) detections.add(new Point(cx, 0, image.getf(cx, 0)));
        if(isMax4Thr(image, 0 ,cy, image.getf(0 ,cy), false, true , true , false)) detections.add(new Point( 0,cy, image.getf( 0,cy)));
        if(isMax4Thr(image, cx,cy, image.getf(cx,cy), true , false, true , false)) detections.add(new Point(cx,cy, image.getf(cx,cy)));
        
        return detections;
    }
    
    private Vector<Point> getMax8Candidates(FloatProcessor image) {
        Vector<Point> detections = new Vector<Point>();
        int cx = image.getWidth(), cy = image.getHeight();
        
        // inner part of the image
        for(int x = 1, xm = cx-1; x < xm; x++) {
            for(int y = 1, ym = cy-1; y < ym; y++) {
                if(isMax8Thr(image, x, y, image.getf(x,y), true, true, true, true))
                    detections.add(new Point(x, y, image.getf(x,y)));
            }
        }
        // left border of the image
        for(int x = 0, y = 1, ym = cy-1; y < ym; y++) {
            if(isMax8Thr(image, x, y, image.getf(x,y), false, true, true, true))
                detections.add(new Point(x, y, image.getf(x,y)));
        }
        // right border of the image
        for(int x = cx-1, y = 1, ym = cy-1; y < ym; y++) {
            if(isMax8Thr(image, x, y, image.getf(x,y), true, false, true, true))
                detections.add(new Point(x, y, image.getf(x,y)));
        }
        // top border of the image
        for(int x = 1, xm = cx-1, y = 0; x < xm; x++) {
            if(isMax8Thr(image, x, y, image.getf(x,y), true, true, false, true))
                detections.add(new Point(x, y, image.getf(x,y)));
        }
        // bottom border of the image
        for(int x = 1, xm = cx-1, y = cy-1; x < xm; x++) {
            if(isMax8Thr(image, x, y, image.getf(x,y), true, true, true, false))
                detections.add(new Point(x, y, image.getf(x,y)));
        }
        // corners
        cx -= 1; cy -= 1;
        if(isMax8Thr(image,  0, 0, image.getf( 0, 0), false, true , false, true )) detections.add(new Point( 0, 0, image.getf( 0, 0)));
        if(isMax8Thr(image, cx, 0, image.getf(cx, 0), true , false, false, true )) detections.add(new Point(cx, 0, image.getf(cx, 0)));
        if(isMax8Thr(image,  0,cy, image.getf( 0,cy), false, true , true , false)) detections.add(new Point( 0,cy, image.getf( 0,cy)));
        if(isMax8Thr(image, cx,cy, image.getf(cx,cy), true , false, true , false)) detections.add(new Point(cx,cy, image.getf(cx,cy)));
        
        return detections;
    }
    
    /**
     * Constructor.
     * 
     * @param connectivity determines in whar neighbourhood will be maxima looked for
     * @param threshold points with their intensities lower than the threshold will not be included in a list of molecule candidates
     */
    public LocalMaximaDetector(int connectivity, double threshold) {
        assert((connectivity == Graph.CONNECTIVITY_4) || (connectivity == Graph.CONNECTIVITY_8));
        
        this.connectivity = connectivity;
        this.threshold = threshold;
    }
    
    /**
     * Detection of candidates using the local maxima method.
     * 
     * Examples:
     * <pre>
     * {@code
     * 5487
     * 1934
     * 4467}
     * </pre>
     * Points [x=1,y=1]=9 and [x=3,y=2]=7 were detected as local maxima.
     * 
     * <pre>
     * {@code
     * 5487
     * 1994
     * 4467}
     * </pre>
     * Points [x=1,y=1]=9 and [x=2,y=1]=9 were detected as local maxima.
     *
     * @param image an input image
     * @return {@code Vector} of detected {@code Points} {x,y,I}
     */
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
          threshold = Double.parseDouble(thrTextField.getText());
          if(conn4RadioButton.isSelected()) connectivity = Graph.CONNECTIVITY_4;
          if(conn8RadioButton.isSelected()) connectivity = Graph.CONNECTIVITY_8;
    }

}
