package cz.cuni.lf1.lge.ThunderSTORM;

import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_X;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Y;
import static cz.cuni.lf1.lge.ThunderSTORM.estimators.PSF.PSFModel.Params.LABEL_Z;
import cz.cuni.lf1.lge.ThunderSTORM.UI.CardsPanel;
import cz.cuni.lf1.lge.ThunderSTORM.results.IJResultsTable;
import cz.cuni.lf1.lge.ThunderSTORM.UI.GUI;
import cz.cuni.lf1.lge.ThunderSTORM.UI.MacroParser;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.EmptyRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.IRendererUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import ij.plugin.PlugIn;
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

  public static final String[] METHODS = new String[]{"Density", "ASH", "Histogram", "Scatter"};

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
    double[] z = rt.columnExists(LABEL_Z)? rt.getColumnAsDoubles(LABEL_Z): null;
    double[] dx = rt.columnExists("dx")? rt.getColumnAsDoubles("dx"): null;

    List<IRendererUI> knownRenderers = ModuleLoader.getUIModules(IRendererUI.class);
    for (Iterator<IRendererUI> it = knownRenderers.iterator(); it.hasNext();) {
      if (it.next() instanceof EmptyRendererUI) {
        it.remove();
      }
    }
    IRendererUI selectedRendererUI;
    int sizeX, sizeY;
    
    if (MacroParser.isRanFromMacro()) {
      MacroParser parser = new MacroParser(null, null, null, knownRenderers);
      selectedRendererUI = parser.getRendererUI();
      
      //TODO: parse size
      sizeX = 0;
      sizeY = 0;
    } else {
      RenderingDialog dialog = new RenderingDialog(knownRenderers, (int)Math.ceil(max(xpos)) + 1, (int)Math.ceil(max(ypos)) + 1);
      dialog.setVisible(true);
      if(!dialog.okPressed){
        return;
      }
      selectedRendererUI = dialog.getSelectedRendererUI();
      sizeX = dialog.sizeX;
      sizeY = dialog.sizeY;
    }

    selectedRendererUI.setSize(sizeX, sizeY);
    IncrementalRenderingMethod method = selectedRendererUI.getImplementation();
    
    method.reset();
    method.addToImage(xpos, ypos, z, dx);
    method.getRenderedImage().show();
    //TODO: adjust brightness

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
  JButton okButton;
  JButton cancelButton;
  boolean okPressed = false;
  JTextField sizeXTextField;
  JTextField sizeYTextField;
  int sizeX, sizeY;

  public RenderingDialog(List<IRendererUI> knownRenderers, int sizeX, int sizeY) {
    super(IJ.getInstance(), "Rendering options", true);
    this.cardsPanel = new CardsPanel<IRendererUI>(knownRenderers, 0);
    this.sizeX = sizeX;
    this.sizeY = sizeY;
    layoutComponents();
  }

  @Override
  public  void setVisible(boolean b) {
    super.setVisible(b);
  }

  public IRendererUI getSelectedRendererUI(){
    return cardsPanel.getActiveComboBoxItem();
  }
  
  private void layoutComponents() {
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    getContentPane().setLayout(new GridBagLayout());
    JPanel sizePanel = new JPanel(new GridBagLayout());
    sizeXTextField = new JTextField(Integer.toString(sizeX),20);
    sizeYTextField = new JTextField(Integer.toString(sizeY),20);
    sizePanel.add(new JLabel("Image size X [px]:"), GridBagHelper.leftCol());
    sizePanel.add(sizeXTextField, GridBagHelper.rightCol());
    sizePanel.add(new JLabel("Image size Y [px]:"), GridBagHelper.leftCol());
    sizePanel.add(sizeYTextField, GridBagHelper.rightCol());

    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    okButton = new JButton("OK");
    okButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        try {
          validateFields();
          okPressed = true;
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
        okPressed = false;
        dispose();
      }
    });
    buttonsPanel.add(okButton);
    buttonsPanel.add(cancelButton);

    add(sizePanel, GridBagHelper.leftCol());
    add(cardsPanel.getPanel("Renderer:"),GridBagHelper.leftCol());
    add(buttonsPanel,GridBagHelper.leftCol());

    pack();
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
