package cz.cuni.lf1.lge.ThunderSTORM.results;

import static cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn.LABEL_X_POS;
import static cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn.LABEL_Y_POS;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import ij.IJ;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

class GroupingListener {

  private JavaTableWindow table;
  private TripleStateTableModel model;
  private JPanel grouping;
  private JTextField distanceTextField;
  private JButton applyButton;

  public GroupingListener(JavaTableWindow table, TripleStateTableModel model) {
    this.table = table;
    this.model = model;
  }

  public JPanel createUIPanel() {
    grouping = new JPanel();
    grouping.setLayout(new BoxLayout(grouping, BoxLayout.X_AXIS));
    InputListener listener = new InputListener();
    distanceTextField = new JTextField();
    distanceTextField.addKeyListener(listener);
    JLabel groupThrLabel = new JLabel("Merge molecules in subsequent frames with mutual lateral distance equal or less than: ", SwingConstants.TRAILING);
    groupThrLabel.setLabelFor(distanceTextField);
    applyButton = new JButton("Merge");
    applyButton.addActionListener(listener);
    grouping.add(groupThrLabel);
    grouping.add(distanceTextField);
    grouping.add(applyButton);
    return grouping;
  }

  private class InputListener extends KeyAdapter implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
      runGrouping(distanceTextField.getText().isEmpty() ? 0.0 : Double.parseDouble(distanceTextField.getText()));
    }

    @Override
    public void keyPressed(KeyEvent e) {
      if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        runGrouping(distanceTextField.getText().isEmpty() ? 0.0 : Double.parseDouble(distanceTextField.getText()));
      }
    }
  }

  protected void runGrouping(double dist) {
    if (dist == 0) {
      return;
    }
    distanceTextField.setBackground(Color.WHITE);
    GUI.closeBalloonTip();
    try {
      OperationsStackPanel opHistory = table.getOperationHistoryPanel();
      if (opHistory.getLastOperation() instanceof ResultsFilter.FilteringOperation) {
        model.copyUndoToActual();
        opHistory.removeLastOperation();
      } else {
        model.copyActualToUndo();
      }
      model.setSelectedState(TripleStateTableModel.State.ACTUAL);
      int merged = model.getRowCount();
      applyToModel(model, dist);
      int into = model.getRowCount();
      opHistory.addOperation(new MergingOperation(dist));

      table.setStatus(Integer.toString(merged) + " molecules were merged into " + Integer.toString(into) + " molecules");
      table.showPreview();
    } catch (Exception ex) {
      IJ.handleException(ex);
      distanceTextField.setBackground(new Color(255, 200, 200));
      GUI.showBalloonTip(distanceTextField, ex.toString());
    }
  }

  private class MergingOperation extends OperationsStackPanel.Operation {

    double threshold;

    public MergingOperation(double threshold) {
      this.threshold = threshold;
    }

    @Override
    protected String getName() {
      return "Merging";
    }

    @Override
    protected boolean isUndoAble() {
      return true;
    }

    @Override
    protected void clicked() {
      if (grouping.getParent() instanceof JTabbedPane) {
        JTabbedPane tabbedPane = (JTabbedPane) grouping.getParent();
        tabbedPane.setSelectedComponent(grouping);
      }
      distanceTextField.setText(Double.toString(threshold));
    }

    @Override
    protected void undo() {
      model.swapUndoAndActual();
      table.setStatus("Merging: Undo.");
      table.showPreview();
    }

    @Override
    protected void redo() {
      model.swapUndoAndActual();
      table.setStatus("Merging: Redo.");
      table.showPreview();
    }
  }

  public static void applyToModel(TripleStateTableModel model, double dist) {
    if (!model.columnExists(LABEL_X_POS) || !model.columnExists(LABEL_Y_POS)) {
      throw new RuntimeException(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X_POS, LABEL_Y_POS, model.getColumnNames()));
    }
    //
    double dist2 = sqr(dist);
    double[] x = model.getColumnAsDoubles(LABEL_X_POS);
    double[] y = model.getColumnAsDoubles(LABEL_Y_POS);
    double[] I = model.getColumnAsDoubles("intensity");
    double[] b = model.getColumnAsDoubles("background");
    double[] s = model.getColumnAsDoubles("sigma");
    double[] fno = model.getColumnAsDoubles("frame");
    double[] id = model.getColumnAsDoubles("#");
    FrameSequence frames = new FrameSequence();
    for (int i = 0, im = model.getRowCount(); i < im; i++) {
      frames.InsertMolecule(new Molecule((int) id[i], x[i], y[i], I[i], b[i], s[i], (int) fno[i]));
    }
    frames.matchMolecules(dist2);
    //

    model.resetSelected();

    for (Molecule mol : frames.getAllMolecules()) {
      if (!mol.isSingle()) {
        //
        model.addRow();
        model.addValue(mol.x, LABEL_X_POS);
        model.addValue(mol.y, LABEL_Y_POS);
        model.addValue(mol.I, "intensity");
        model.addValue(mol.b, "background");
        model.addValue(mol.s, "sigma");
        model.addValue((double) mol.detections.size(), "detections");
      }
    }
  }

  //
  // ====================================================================
  //
  // TODO: in future this should be replaced by a PSF which implements the method for merging
  // + speed-up or put it into a separate thread!!
  static class Molecule {

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
      this.s = sigma / (double) detections.size();
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
  static class FrameSequence {

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
        ArrayList<Molecule> fr1mol = detections.get(frno[fi - 1]);
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
      for (Molecule mol : detections.get(frno[frno.length - 1])) {
        molecules.add(mol);
      }
    }
  }
}
