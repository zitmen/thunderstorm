package cz.cuni.lf1.lge.ThunderSTORM;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Z;
import cz.cuni.lf1.lge.ThunderSTORM.UI.CardsPanel;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.EmptyRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import ij.Macro;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RenderingPlugIn implements PlugIn {

  @Override
  public void run(String string) {
    GUI.setLookAndFeel();
    //
    IJResultsTable rt = IJResultsTable.getResultsTable();
    if (!IJResultsTable.isResultsWindow()) {
      IJ.error("Requires Results window open");
      return;
    }
    if (!rt.columnExists(LABEL_X) || !rt.columnExists(LABEL_Y)) {
      IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X, LABEL_Y, rt.getColumnNames()));
      return;
    }

    double[] xpos = rt.getColumnAsDoubles(LABEL_X);
    double[] ypos = rt.getColumnAsDoubles(LABEL_Y);
    if (xpos == null || ypos == null) {
      IJ.error("results were null");
      return;
    }
    double[] z = rt.columnExists(LABEL_Z) ? rt.getColumnAsDoubles(LABEL_Z) : null;
    double[] dx = rt.columnExists("dx") ? rt.getColumnAsDoubles("dx") : null;

    List<IRendererUI> knownRenderers = ModuleLoader.getUIModules(IRendererUI.class);
    //do not show EmptyRenderer
    for (Iterator<IRendererUI> it = knownRenderers.iterator(); it.hasNext();) {
      if (it.next() instanceof EmptyRendererUI) {
        it.remove();
      }
    }
    IRendererUI selectedRendererUI;
    int sizeX, sizeY;
    boolean setAsPreview = false;

    if (MacroParser.isRanFromMacro()) {
      MacroParser parser = new MacroParser(null, null, null, knownRenderers);
      selectedRendererUI = parser.getRendererUI();

      sizeX = Integer.parseInt(Macro.getValue(Macro.getOptions(), "imwidth", "0"));
      sizeY = Integer.parseInt(Macro.getValue(Macro.getOptions(), "imheight", "0"));
    } else {
      RenderingDialog dialog = new RenderingDialog(knownRenderers, (int) Math.ceil(max(xpos)) + 1, (int) Math.ceil(max(ypos)) + 1);
      dialog.setVisible(true);
      if (dialog.result == RenderingDialog.DialogResult.CANCELLED) {
        return;
      }
      if (dialog.result == RenderingDialog.DialogResult.PREVIEW) {
        setAsPreview = true;
      }
      selectedRendererUI = dialog.getSelectedRendererUI();
      sizeX = dialog.sizeX;
      sizeY = dialog.sizeY;
    }

    selectedRendererUI.setSize(sizeX, sizeY);
    IncrementalRenderingMethod method = selectedRendererUI.getImplementation();

    if (setAsPreview) {
      RenderingQueue queue = new RenderingQueue(method, new RenderingQueue.DefaultRepaintTask(method.getRenderedImage()), selectedRendererUI.getRepaintFrequency());
      rt.setPreviewRenderer(queue);
      rt.showPreview();
    } else {
      if (Recorder.record) {
        Recorder.recordOption("imwidth", Integer.toString(sizeX));
        Recorder.recordOption("imheight", Integer.toString(sizeY));
        MacroParser.recordRendererUI(selectedRendererUI);
      }

      method.reset();
      method.addToImage(xpos, ypos, z, dx);
      new RenderingQueue.DefaultRepaintTask(method.getRenderedImage()).run();
    }
  }

  private double max(double[] arr) {
    double max = arr[0];
    for (int i = 0; i < arr.length; i++) {
      if (arr[i] > max) {
        max = arr[i];
      }
    }
    return max;
  }
}

class RenderingDialog extends JDialog {

  CardsPanel<IRendererUI> cardsPanel;
  JButton previewButton;
  JButton okButton;
  JButton cancelButton;
  DialogResult result = DialogResult.CANCELLED;
  JTextField sizeXTextField;
  JTextField sizeYTextField;
  int sizeX, sizeY;

  enum DialogResult {

    CANCELLED, OK, PREVIEW;
  }

  public RenderingDialog(List<IRendererUI> knownRenderers, int sizeX, int sizeY) {
    super(IJ.getInstance(), "Rendering options", true);
    this.cardsPanel = new CardsPanel<IRendererUI>(knownRenderers, 0);
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    layoutComponents();
  }

  @Override
  public void setVisible(boolean b) {
    super.setVisible(b);
  }

  public IRendererUI getSelectedRendererUI() {
    return cardsPanel.getActiveComboBoxItem();
  }

  private void layoutComponents() {
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new GridBagLayout());
    JPanel sizePanel = new JPanel(new GridBagLayout());
    sizeXTextField = new JTextField(Integer.toString(sizeX), 20);
    sizeYTextField = new JTextField(Integer.toString(sizeY), 20);
    sizePanel.add(new JLabel("Image size X [px]:"), GridBagHelper.leftCol());
    sizePanel.add(sizeXTextField, GridBagHelper.rightCol());
    sizePanel.add(new JLabel("Image size Y [px]:"), GridBagHelper.leftCol());
    sizePanel.add(sizeYTextField, GridBagHelper.rightCol());

    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    previewButton = new JButton("Use for preview");
    previewButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          validateFields();
          result = DialogResult.PREVIEW;
          dispose();
        } catch (Exception ex) {
          IJ.showMessage(ex.toString());
        }
      }
    });
    okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          validateFields();
          result = DialogResult.OK;
          dispose();
        } catch (Exception ex) {
          IJ.showMessage(ex.toString());
        }
      }
    });
    getRootPane().setDefaultButton(okButton);
    cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        result = DialogResult.CANCELLED;
        dispose();
      }
    });
    buttonsPanel.add(previewButton);
    buttonsPanel.add(okButton);
    buttonsPanel.add(cancelButton);

    add(sizePanel, GridBagHelper.leftCol());
    add(cardsPanel.getPanel("Renderer:"), GridBagHelper.leftCol());
    add(buttonsPanel, GridBagHelper.leftCol());

    pack();
    setLocationRelativeTo(null);
    setResizable(false);
  }

  private void validateFields() {
    sizeX = Integer.parseInt(sizeXTextField.getText());
    if (sizeX < 1) {
      throw new IllegalArgumentException("Image width must be positive.");
    }
    sizeY = Integer.parseInt(sizeYTextField.getText());
    if (sizeY < 1) {
      throw new IllegalArgumentException("Image height must be positive.");
    }
    cardsPanel.getActiveComboBoxItem().readParameters();
  }
}
