package cz.cuni.lf1.lge.ThunderSTORM.results;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.JLabel;
import javax.swing.JTextField;

class GroupingListener implements ActionListener, KeyListener {
    
    private JavaTableWindow table;
    private JLabel status;
    private JTextField distance;
    
    public GroupingListener(JavaTableWindow table, JLabel status, JTextField distance) {
        this.table = table;
        this.status = status;
        this.distance = distance;
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

    protected void runGrouping() {
        distance.setBackground(Color.WHITE);
        GUI.closeBalloonTip();
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
            FrameSequence frames = new FrameSequence();
            for (int i = 0, im = rtv.getRowCount(); i < im; i++)
                frames.InsertMolecule(new Molecule((int)id[i], x[i], y[i], I[i], b[i], s[i], (int)fno[i]));
            frames.matchMolecules(dist2);
            //
            int merged = 0, into = 0;
            IJResultsTable rt = IJResultsTable.getResultsTable();
            ArrayList<Integer> deleteLater = new ArrayList<Integer>();  // it has to be deleted carefuly so the indices are not messed up!
            for(Molecule mol : frames.getAllMolecules()) {
                if(!mol.isSingle()) {
                    merged += mol.detections.size();
                    into += 1;
                    //
                    for(int di = 1, dim = mol.detections.size(); di < dim; di++) {
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
        } catch(Exception ex) {
            ex.printStackTrace();
            distance.setBackground(new Color(255, 200, 200));
            GUI.showBalloonTip(distance, ex.toString());
        }
    }
    
    //
    // ====================================================================
    //
    
    // TODO: in future this should be replaced by a PSF which implements the method for merging
    // + speed-up or put it into a separate thread!!
    class Molecule {

        public ArrayList<Molecule> detections;
        public double x, y, I, b, s; // xpos, ypos, Intensity, background, sigma
        public int id, frame;

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
            detections = new ArrayList<Molecule>();
            this.id = id;
            this.x = x;
            this.y = y;
            this.I = I;
            this.b = b;
            this.s = sigma;
            this.frame = frame;
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
        }
        
        public boolean isSingle() {
            return (detections.size() == 1);
        }
        
    }
    
    //
    // ===================================================================
    //
    
    class FrameSequence {

        // <Frame #, List of Molecules>
        private HashMap<Integer, ArrayList<Molecule>> detections;
        private ArrayList<Molecule> molecules;
        private SortedSet frames;
        
        public FrameSequence() {
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
        public void matchMolecules(double dist2_thr) {
            molecules.clear();
            Integer[] frno = new Integer[frames.size()];
            frames.toArray(frno);
            for (int fi = 1; fi < frno.length; fi++) {
                ArrayList<Molecule> fr1mol = detections.get(frno[fi-1]);
                ArrayList<Molecule> fr2mol = detections.get(frno[fi]);
                boolean[] selected = new boolean[fr1mol.size()];
                //
                for (Molecule mol2 : fr2mol) {
                    // init flag array
                    for (int si = 0; si < selected.length; si++) {
                        selected[si] = false;
                    }
                    // match
                    {
                        int si = 0;
                        for (Molecule mol1 : fr1mol) {
                            if (selected[si] == false) {
                                if (mol2.dist2xy(mol1) <= dist2_thr) {
                                    mol2.addDetection(mol1);
                                    selected[si] = true;
                                }
                            }
                            si++;
                        }
                    }
                }
                // store the not-selected molecules as real ones
                for (int si = 0; si < selected.length; si++) {
                    if (selected[si] == false) {
                        molecules.add(fr1mol.get(si));
                    }
                }
            }
            // finally, store all the molecules from the last frame
            for (Molecule mol : detections.get(frno[frno.length-1])) {
                molecules.add(mol);
            }
        }
    }


}
