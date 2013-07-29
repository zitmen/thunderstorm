package cz.cuni.lf1.lge.ThunderSTORM.results;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import static cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn.LABEL_X_POS;
import static cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn.LABEL_Y_POS;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

class GroupingListener implements ActionListener, KeyListener {
    
    private JLabel status;
    private JTextField distance;
    private JButton button;
    
    public GroupingListener(JavaTableWindow table, JLabel status, JTextField distance, JButton button) {
        this.status = status;
        this.distance = distance;
        this.button = button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        runGrouping();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // nothing to do
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_ENTER) {
            runGrouping();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // nothing to do
    }
    
    //
    // =====================================================================
    //

    // TODO: due to the wrong design of cooperation with results table the grouping
    //       cannot be called more than once, because it will throw an exception
    //       (index out of bounds), because some rows have been deleted
    protected void runGrouping() {
        if(!distance.isEditable()) {
            return; // there is already one grouping thread in progress
        }
        distance.setBackground(Color.WHITE);
        GUI.closeBalloonTip();
        distance.setEnabled(false);
        button.setEnabled(false);
        status.setText("Grouping the molecules.");
        try {
            IJResultsTable.View rtv = IJResultsTable.getResultsTable().view;
            if (!rtv.columnExists(LABEL_X_POS) || !rtv.columnExists(LABEL_Y_POS)) {
                throw new Exception(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X_POS, LABEL_Y_POS, rtv.getColumnHeadings()));
            }
            //
            double dist2 = distance.getText().isEmpty() ? 0.0 : sqr(Double.parseDouble(distance.getText()));
            double [] x = rtv.getColumnAsDoubles(rtv.getColumnIndex(LABEL_X_POS));
            double [] y = rtv.getColumnAsDoubles(rtv.getColumnIndex(LABEL_Y_POS));
            double [] I = rtv.getColumnAsDoubles(rtv.getColumnIndex("intensity"));
            double [] b = rtv.getColumnAsDoubles(rtv.getColumnIndex("background"));
            double [] s = rtv.getColumnAsDoubles(rtv.getColumnIndex("sigma"));
            double [] fno = rtv.getColumnAsDoubles(rtv.getColumnIndex("frame"));
            double [] id = rtv.getColumnAsDoubles(rtv.getColumnIndex("#"));
            FrameSequence frames = new FrameSequence(dist2);
            for (int i = 0, im = rtv.getRowCount(); i < im; i++)
                frames.InsertMolecule(new Molecule((int)id[i], x[i], y[i], I[i], b[i], s[i], (int)fno[i]));
            Thread worker = new Thread(frames);
            worker.start();
        } catch(Exception ex) {
            distance.setEnabled(true);
            button.setEnabled(true);
            status.setText("Grouping failed!");
            distance.setBackground(new Color(255, 200, 200));
            GUI.showBalloonTip(distance, ex.toString());
        }
    }
    
    //
    // ===================================================================
    //
    
    class FrameSequence implements Runnable {

        // <Frame #, List of Molecules>
        private HashMap<Integer, ArrayList<Molecule>> detections;
        private ArrayList<Molecule> molecules;
        private SortedSet frames;
        private double dist2_thr;
        
        public FrameSequence(double dist2_thr) {
            this.dist2_thr = dist2_thr;
            detections = new HashMap<Integer, ArrayList<Molecule>>();
            molecules = new ArrayList<Molecule>();
            frames = new TreeSet();
        }

        public void InsertMolecule(Molecule mol) {
            if (!detections.containsKey(mol.frame)) {
                detections.put(mol.frame, new ArrayList<Molecule>());
            }
            detections.get(mol.frame).add(mol);
            frames.add(mol.frame);
        }

        public ArrayList<Molecule> getAllMolecules() {
            return molecules;
        }

        /*
         * Note: this method makes changes into `detections`!
         * 
         * The method matches molecules at the same positions
         * lasting for more than just 1 frame.
         * 
         * In the description of the competition
         * (http://bigwww.epfl.ch/smlm/challenge/images/lm.003.png)
         * they generate molecules only with their energy going down,
         * but in real-life data I have whitnessed meny sitiations
         * where the molecules were pulsing, i.e., their intensity
         * went up and down again. Also a molecule could be photoactivated
         * later in a frame acquisition Therefore this constraint is not
         * included in this method.
         */
        @Override
        public void run() {
            molecules.clear();
            Integer[] frno = new Integer[frames.size()];
            frames.toArray(frno);
            SquareEuclideanDistanceFunction dist_fn = new SquareEuclideanDistanceFunction();
            MaxHeap<Molecule> nn_mol;
            for (int fi = 1; fi < frno.length; fi++) {
                ArrayList<Molecule> fr1mol = detections.get(frno[fi-1]);
                ArrayList<Molecule> fr2mol = detections.get(frno[fi]);
                //
                boolean[] selected = new boolean[fr1mol.size()];
                Arrays.fill(selected, false);
                //
                KdTree<Molecule> tree = new KdTree<Molecule>(2);
                for(Molecule mol : fr2mol) {
                    tree.addPoint(new double[]{mol.x, mol.y}, mol);
                }
                for(int si = 0, sim = fr1mol.size(); si < sim; si++) {
                    Molecule mol = fr1mol.get(si);
                    nn_mol = tree.findNearestNeighbors(new double[]{mol.x, mol.y}, 1, dist_fn);
                    if(nn_mol.getMaxKey() < dist2_thr) {
                        nn_mol.getMax().addDetection(mol);
                        selected[si] = true;
                    }
                }
                // store the not-selected molecules as real ones
                for(int si = 0; si < selected.length; si++) {
                    if (selected[si] == false) {
                        molecules.add(fr1mol.get(si));
                    }
                }
            }
            // at the end store all the molecules from the last frame
            for(Molecule mol : detections.get(frno[frno.length-1])) {
                molecules.add(mol);
            }
            //
            updateResults();
        }
        
        public synchronized void updateResults() {
            try {
                int merged = 0, into = 0;
                IJResultsTable rt = IJResultsTable.getResultsTable();
                ArrayList<Integer> deleteLater = new ArrayList<Integer>();  // it has to be deleted carefuly so the indices are not messed up!
                for(Molecule mol : getAllMolecules()) {
                    if(!mol.isSingle()) {
                        merged += mol.detections.size();
                        into += 1;
                        //
                        for(int di = 1, dim = mol.detections.size(); di < dim; di++) {  // slip the first one, because it will represent the group of molecules
                            deleteLater.add(new Integer(mol.detections.get(di).id));
                        }
                        rt.setValue(LABEL_X_POS, mol.id, mol.x);
                        rt.setValue(LABEL_Y_POS, mol.id, mol.y);
                        rt.setValue("intensity", mol.id, mol.I);
                        rt.setValue("background", mol.id, mol.b);
                        rt.setValue("sigma", mol.id, mol.s);
                    }
                }
                Collections.sort(deleteLater, Collections.reverseOrder());
                for(Integer row_id : deleteLater) {
                    rt.deleteRow(row_id.intValue());
                }
                //
                status.setText(Integer.toString(merged) + " molecules were merged into " + Integer.toString(into) + " molecules");
                distance.setEnabled(true);
                button.setEnabled(true);
            } catch(Exception ex) {
                distance.setEnabled(true);
                button.setEnabled(true);
                status.setText("Grouping failed!");
                distance.setBackground(new Color(255, 200, 200));
                GUI.showBalloonTip(distance, ex.toString());
            }
        }
    }
    
    //
    // ====================================================================
    //
    
    // TODO: in future this should be replaced by a PSF which implements the method for merging
    // + put it into a separate thread!!
    class Molecule implements Comparable<Molecule> {

        public double x, y, I, b, s; // xpos, ypos, Intensity, background, sigma
        public int id, frame;
        public double abs_pos;
        private boolean sorted;
        private ArrayList<Molecule> detections;

        /*
         * [xpos,ypos] = center of gravity of individual detections
         * Intensity = sum of intensities of all contributing detections
         * sigma = mean value
         * background = sum of all background intensities to keep the SNR of single detections
         * frame = first frame where the molecule appeared
         */
        private void updateParameters() {
            double xpos = 0.0, ypos = 0.0, Intensity = 0.0, background = 0.0, sigma = 0.0;
            for (int i = 0; i < detections.size(); i++) {
                Molecule mol = detections.get(i);
                id = Math.min(id, mol.id);
                frame = Math.min(frame, mol.frame);
                Intensity += mol.I;
                background += mol.b;
                sigma += mol.s;
                xpos += mol.x * mol.I;
                ypos += mol.y * mol.I;
            }
            this.x = xpos / Intensity;
            this.y = ypos / Intensity;
            this.I = Intensity;
            this.b = background;
            this.s = sigma / (double)detections.size();
        }

        public Molecule(int id, double x, double y, double I, double b, double sigma, int frame) {
            this.id = id;
            this.x = x;
            this.y = y;
            this.I = I;
            this.b = b;
            this.s = sigma;
            this.frame = frame;
            //
            detections = new ArrayList<Molecule>();
            this.sorted = true;
            //
            addDetection(this);
        }

        public double dist2xy(Molecule mol) {
            return (sqr(mol.x - x) + sqr(mol.y - y));
        }

        public void addDetection(Molecule mol) {
            if (mol.detections.isEmpty()) {
                detections.add(mol);
            } else {    // if it is not empty, it already contains, at least, itself
                for (Molecule m : mol.detections) {
                    detections.add(m);
                }
            }
            updateParameters();
            this.sorted = false;
        }
        
        public ArrayList<Molecule> getDetections() {
            if(sorted == false) {
                Collections.sort(detections);
                sorted = true;
            }
            return detections;
        }
        
        public boolean isSingle() {
            return (detections.size() == 1);
        }

        @Override
        public int compareTo(Molecule mol) {
            // first by frame, then by id, but it should never happen,
            // since two molecules cannot be merged if they are in the same frame
            if(frame == mol.frame) {
                return (id - mol.id);
            } else {
                return (frame - mol.frame);
            }
        }
        
    }

}
