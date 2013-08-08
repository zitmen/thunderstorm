package cz.cuni.lf1.lge.ThunderSTORM.results;

import ags.utils.dataStructures.MaxHeap;
import ags.utils.dataStructures.trees.thirdGenKD.KdTree;
import ags.utils.dataStructures.trees.thirdGenKD.SquareEuclideanDistanceFunction;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFInstance;
import static cz.cuni.lf1.lge.ThunderSTORM.util.Math.sqr;
import ij.IJ;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

class ResultsGrouping {

  private JavaTableWindow table;
  private TripleStateTableModel model;
  private JPanel grouping;
  private JTextField distanceTextField;
  private JButton applyButton;

  public ResultsGrouping(JavaTableWindow table, TripleStateTableModel model) {
    this.table = table;
    this.model = model;
  }

  public JPanel createUIPanel() {
    grouping = new JPanel(new GridBagLayout());
    InputListener listener = new InputListener();
    distanceTextField = new JTextField();
    distanceTextField.addKeyListener(listener);
    JLabel groupThrLabel = new JLabel("Merge molecules in subsequent frames with mutual lateral distance equal or less than: ", SwingConstants.TRAILING);
    groupThrLabel.setLabelFor(distanceTextField);
    applyButton = new JButton("Merge");
    applyButton.addActionListener(listener);
    grouping.add(groupThrLabel);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.weightx = 1;
    grouping.add(distanceTextField, gbc);
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
        applyButton.doClick();
      }
    }
  }

  // TODO: due to the wrong design of cooperation with results table the grouping
  //       cannot be called more than once, because it will throw an exception
  //       (index out of bounds), because some rows have been deleted
  protected void runGrouping(final double dist) {
    if (dist == 0) {
      return;
    }
    distanceTextField.setBackground(Color.WHITE);
    GUI.closeBalloonTip();
    try {
      applyButton.setEnabled(false);
      final OperationsHistoryPanel opHistory = table.getOperationHistoryPanel();
      if (opHistory.getLastOperation() instanceof ResultsGrouping.MergingOperation) {
        model.copyUndoToActual();
        opHistory.removeLastOperation();
      } else {
        model.copyActualToUndo();
      }
      model.setSelectedState(TripleStateTableModel.StateName.ACTUAL);
      final int merged = model.getRowCount();
      new SwingWorker() {
        @Override
        protected Object doInBackground() throws Exception {
          applyToModel(model, dist);
          return null;
        }

        @Override
        protected void done() {
          try {
            get();
            int into = model.getRowCount();
            opHistory.addOperation(new MergingOperation(dist));

            table.setStatus(Integer.toString(merged) + " molecules were merged into " + Integer.toString(into) + " molecules");
            table.showPreview();
          } catch (InterruptedException ex) {
          } catch (ExecutionException ex) {
            IJ.handleException(ex);
            distanceTextField.setBackground(new Color(255, 200, 200));
            GUI.showBalloonTip(distanceTextField, ex.getCause().toString());
          } finally {
            applyButton.setEnabled(true);
          }
        }
      }.execute();

    } catch (Exception ex) {
      IJ.handleException(ex);
      distanceTextField.setBackground(new Color(255, 200, 200));
      GUI.showBalloonTip(distanceTextField, ex.toString());
    }
  }

  private class MergingOperation extends OperationsHistoryPanel.Operation {

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

  public static void applyToModel(ResultsTableModel model, double dist) {
    if (!model.columnExists(PSFInstance.X) || !model.columnExists(PSFInstance.Y)) {
      throw new RuntimeException(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", PSFInstance.X, PSFInstance.Y, model.getColumnNames()));
    }
    //
    double dist2 = sqr(dist);
    double[] x = model.getColumnAsDoubles(PSFInstance.X);
    double[] y = model.getColumnAsDoubles(PSFInstance.Y);
    double[] I = model.getColumnAsDoubles(PSFInstance.INTENSITY);
    double[] b = model.getColumnAsDoubles(PSFInstance.BACKGROUND);
    double[] s = model.getColumnAsDoubles(PSFInstance.SIGMA);
    double[] fno = model.getColumnAsDoubles("frame");
    double[] id = model.getColumnAsDoubles(IJResultsTable.COLUMN_ID);
    FrameSequence frames = new FrameSequence();
    for (int i = 0, im = model.getRowCount(); i < im; i++) {
      frames.InsertMolecule(new Molecule((int) id[i], x[i], y[i], I[i], b[i], s[i], (int) fno[i]));
    }
    frames.matchMolecules(dist2);
    //

    model.reset();

    for (Molecule mol : frames.getAllMolecules()) {
      //
      model.addRow();
      model.addValue((double) mol.frame, "frame");
      model.addValue(mol.x, PSFInstance.X);
      model.addValue(mol.y, PSFInstance.Y);
      model.addValue(mol.I, PSFInstance.INTENSITY);
      model.addValue(mol.b, PSFInstance.BACKGROUND);
      model.addValue(mol.s, PSFInstance.SIGMA);
      model.addValue((double) mol.detections.size(), "detections");
    }
  }

  //
  // ====================================================================
  //
  // TODO: in future this should be replaced by a PSF which implements the method for merging
  // + put it into a separate thread!!
  static class Molecule implements Comparable<Molecule> {

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
      this.s = sigma / (double) detections.size();
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
      if (sorted == false) {
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
      if (frame == mol.frame) {
        return (id - mol.id);
      } else {
        return (frame - mol.frame);
      }
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
      SquareEuclideanDistanceFunction dist_fn = new SquareEuclideanDistanceFunction();
      MaxHeap<Molecule> nn_mol;
      for (int fi = 1; fi < frno.length; fi++) {
        ArrayList<Molecule> fr1mol = detections.get(frno[fi - 1]);
        ArrayList<Molecule> fr2mol = detections.get(frno[fi]);
        //
        boolean[] selected = new boolean[fr1mol.size()];
        Arrays.fill(selected, false);
        //
        KdTree<Molecule> tree = new KdTree<Molecule>(2);
        for (Molecule mol : fr2mol) {
          tree.addPoint(new double[]{mol.x, mol.y}, mol);
        }
        for (int si = 0, sim = fr1mol.size(); si < sim; si++) {
          Molecule mol = fr1mol.get(si);
          nn_mol = tree.findNearestNeighbors(new double[]{mol.x, mol.y}, 1, dist_fn);
          if (nn_mol.getMaxKey() < dist2_thr) {
            nn_mol.getMax().addDetection(mol);
            selected[si] = true;
          }
        }
        // store the not-selected molecules as real ones
        for (int si = 0; si < selected.length; si++) {
          if (selected[si] == false) {
            molecules.add(fr1mol.get(si));
          }
        }
      }
      // at the end store all the molecules from the last frame
      for (Molecule mol : detections.get(frno[frno.length - 1])) {
        molecules.add(mol);
      }
    }
  }
}
