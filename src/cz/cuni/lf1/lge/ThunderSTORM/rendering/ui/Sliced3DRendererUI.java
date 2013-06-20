package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.ModuleLoader;
import cz.cuni.lf1.lge.ThunderSTORM.UI.CardsPanel;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.Macro;
import ij.plugin.frame.Recorder;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 */
public class Sliced3DRendererUI implements IRendererUI {

  double start = -50, stop = 50, step = 10;
  int nSlices = 10;
  List<IRendererUI> knownRenderers;
  CardsPanel<IRendererUI> knownRenderersPanel;
  JTextField rangeTextField;
  IRendererUI activeRenderer;

  @Override
  public void setSize(int sizeX, int sizeY) {
    activeRenderer.setSize(sizeX, sizeY);
  }

  @Override
  public String getName() {
    return "Sliced 3D renderer";
  }

  @Override
  public JPanel getOptionsPanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.add(new JLabel("Z range [from:step:to]:"), GridBagHelper.leftCol());
    rangeTextField = new JTextField("-50:10:50", 20);
    panel.add(rangeTextField, GridBagHelper.rightCol());

    knownRenderers = loadUIModules();
    knownRenderersPanel = new CardsPanel<IRendererUI>(knownRenderers, 0);
    panel.add(knownRenderersPanel.getPanel("Slice renderer"), GridBagHelper.twoCols());

    return panel;

  }

  @Override
  public void readParameters() {
    parseRange(rangeTextField.getText());
    activeRenderer = knownRenderersPanel.getActiveComboBoxItem();
    activeRenderer.readParameters();
  }

  @Override
  public void recordOptions() {
    Recorder.recordOption("range", start + ":" + step + ":" + stop);
    Recorder.recordOption("sliceRenderer", activeRenderer.getName());
    activeRenderer.recordOptions();
  }

  @Override
  public void readMacroOptions(String options) {
    knownRenderers = loadUIModules();
    parseRange(Macro.getValue(options, "range", start + ":" + step + ":" + stop));
    String rendererClass = Macro.getValue(options, "sliceRenderer", null);
    if (rendererClass == null) {
      activeRenderer = knownRenderers.get(0);
    } else {
      for (IRendererUI r : knownRenderers) {
        if (rendererClass.equals(r.getName())) {
          activeRenderer = r;
        }
      }
      if (activeRenderer == null) {
        throw new RuntimeException("Unknown renderer: " + rendererClass);
      }
    }
    activeRenderer.readMacroOptions(options);
  }

  @Override
  public IncrementalRenderingMethod getImplementation() {

    final List<IncrementalRenderingMethod> sliceRenderers = new ArrayList<IncrementalRenderingMethod>(nSlices);
    for (int i = 0; i < nSlices; i++) {
      sliceRenderers.add(activeRenderer.getImplementation());
    }
    IncrementalRenderingMethod rm = new IncrementalRenderingMethod() {
      @Override
      public void addToImage(double[] x, double[] y, double[] z, double[] dx) {
        if (z == null) {
          throw new RuntimeException("This renderer requires z coordinates.");
        }
        for (int i = 0; i < x.length; i++) {
          if (z[i] < start || z[i] > stop) {
            continue;
          }
          int slice = (int) ((z[i] - start) / step);
          sliceRenderers.get(slice).addToImage(new double[]{x[i]}, new double[]{y[i]}, new double[]{z[i] - slice * step}, dx == null ? null : new double[]{dx[i]});
        }
      }

      @Override
      public ImagePlus getRenderedImage() {
        ImageStack stack = new ImageStack(sliceRenderers.get(0).getRenderedImage().getWidth(), sliceRenderers.get(0).getRenderedImage().getHeight());
        for (int i = 0; i < sliceRenderers.size(); i++) {
          IncrementalRenderingMethod rm = sliceRenderers.get(i);
          stack.addSlice((i * step + start) + " to " + ((i + 1) * step + start), rm.getRenderedImage().getProcessor());
        }
        return new ImagePlus("Sliced 3D rendering - " + sliceRenderers.get(0).getClass().getSimpleName(), stack);
      }

      @Override
      public void reset() {
        for (IncrementalRenderingMethod i : sliceRenderers) {
          i.reset();
        }
      }
    };
    return rm;
  }

  @Override
  public int getRepaintFrequency() {
    return activeRenderer.getRepaintFrequency();
  }

  private List<IRendererUI> loadUIModules() {
    knownRenderers = ModuleLoader.getUIModules(IRendererUI.class);
    Iterator<IRendererUI> it = knownRenderers.iterator();
    //remove empty renderer and this renderer
    while (it.hasNext()) {
      IRendererUI renderer = it.next();
      if (renderer instanceof EmptyRendererUI || renderer instanceof Sliced3DRendererUI) {
        it.remove();
      }
    }
    return knownRenderers;
  }

  private void parseRange(String rangeText) throws RuntimeException {
    Matcher m = Pattern.compile("^([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?):([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?):([-+]?[0-9]*\\.?[0-9]+(?:[eE][-+]?[0-9]+)?)$").matcher(rangeText);
    if (m.lookingAt()) {
      start = Double.parseDouble(m.group(1));
      step = Double.parseDouble(m.group(2));
      stop = Double.parseDouble(m.group(3));
      nSlices = (int) ((stop - start) / step);
      if (start > stop) {
        throw new RuntimeException("Invalid range: Start must be smaller than range stop. Parsed values: " + start + ", " + stop);
      }
      if (nSlices < 1) {
        throw new RuntimeException("Invalid range: Must have at least one slice.");
      }
      stop = nSlices * step + start;
    } else {
      throw new RuntimeException("Wrong format of range field.");
    }
  }
}
