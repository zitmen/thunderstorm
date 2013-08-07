package cz.cuni.lf1.lge.ThunderSTORM.results;

import static cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn.LABEL_X_POS;
import static cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn.LABEL_Y_POS;
import static cz.cuni.lf1.lge.ThunderSTORM.AnalysisPlugIn.LABEL_Z_POS;
import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.RenderingPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.RenderingQueue;
import ij.IJ;
import ij.WindowManager;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

class JavaTableWindow {

  private JTable table;
  private JFrame frame;
  private JButton export;
  private JButton render;
  private JCheckBox preview;
  private JLabel status;
  private RenderingQueue previewRenderer;
  private boolean livePreview;
  private JButton showHist;
  private JButton resetButton;
  private JTabbedPane tabbedPane;
  private OperationsHistoryPanel operationsStackPanel;

  public JavaTableWindow() {
    frame = new JFrame("ThunderSTORM: Results");
    frame.setIconImage(IJ.getInstance().getIconImage());
    //
    JavaTableWindowListener windowListener = new JavaTableWindowListener(this);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.setPreferredSize(new Dimension(550, 550));
    frame.addWindowListener(windowListener);
    frame.addWindowStateListener(windowListener);
    //
    final TripleStateTableModel model = new TripleStateTableModel();
    table = new JTable(model);
    TableRowSorter<TripleStateTableModel> sorter = new TableRowSorter<TripleStateTableModel>(model);
    table.setRowSorter(sorter);
    //
    status = new JLabel(" ");
    status.setAlignmentX(Component.CENTER_ALIGNMENT);
    status.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
    //
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    showHist = new JButton("Plot histogram...");
    export = new JButton("Export...");
    render = new JButton("Render...");
    showHist.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new IJDistribution().run(null);
      }
    });
    export.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new ImportExportPlugIn().run("export");
      }
    });
    render.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        new RenderingPlugIn().run("");
      }
    });
    livePreview = true;
    preview = new JCheckBox("Preview", livePreview);
    preview.addItemListener(new ItemListener() {
      @Override
      public void itemStateChanged(ItemEvent e) {
        livePreview = (e.getStateChange() == ItemEvent.SELECTED);
        showPreview();
      }
    });
    buttons.add(preview);
    buttons.add(Box.createHorizontalGlue());
    buttons.add(showHist);
    buttons.add(Box.createHorizontalStrut(10));
    buttons.add(render);
    buttons.add(Box.createHorizontalStrut(10));
    buttons.add(export);
    //
    JPanel grouping = new ResultsGrouping(this, model).createUIPanel();
    JPanel filter = new ResultsFilter(this, model).createUIPanel();
    JPanel drift = new ResultsDriftCorrection().createUIPanel();

    //fill tabbed pane
    tabbedPane = new JTabbedPane();
    tabbedPane.addTab("filter", filter);
    tabbedPane.addTab("grouping", grouping);
    tabbedPane.addTab("drift correction", drift);
    
    //history pane
    JPanel historyPane = new JPanel(new GridBagLayout());
    operationsStackPanel = new OperationsHistoryPanel();
    resetButton = new JButton("Reset");
    resetButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        model.copyOriginalToActual();
        operationsStackPanel.removeAllOperations();
        setStatus("Results reset.");
        showPreview();
      }
    });
    historyPane.add(operationsStackPanel, new GridBagConstraints(0, 0, 1, 1, 1, 0, GridBagConstraints.BASELINE, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    historyPane.add(resetButton);
    
    Container contentPane = frame.getContentPane();
    JPanel controlsPane = new JPanel();
    controlsPane.setLayout(new BoxLayout(controlsPane, BoxLayout.PAGE_AXIS));
    contentPane.add(new JScrollPane(table), BorderLayout.CENTER);
    contentPane.add(controlsPane, BorderLayout.SOUTH);
    
    controlsPane.add(tabbedPane);
    controlsPane.add(historyPane);
    controlsPane.add(buttons);
    controlsPane.add(status);
    //
    frame.setContentPane(contentPane);
    frame.pack();
  }

  public void showPreview() {
    if (livePreview == false || previewRenderer == null) {
      return;
    }
    //
    IJResultsTable tableModel = IJResultsTable.getResultsTable();
    if (!tableModel.columnExists(LABEL_X_POS) || !tableModel.columnExists(LABEL_Y_POS)) {
      IJ.error(String.format("X and Y columns not found in Results table. Looking for: %s and %s. Found: %s.", LABEL_X_POS, LABEL_Y_POS, tableModel.getColumnNames()));
      return;
    }
    double[] xpos = tableModel.getColumnAsDoubles(LABEL_X_POS);
    double[] ypos = tableModel.getColumnAsDoubles(LABEL_Y_POS);
    double[] zpos = tableModel.columnExists(LABEL_Z_POS) ? tableModel.getColumnAsDoubles(LABEL_Z_POS) : null;
    if (xpos == null || ypos == null) {
      IJ.error("results were empty");
      return;
    }
    previewRenderer.resetLater();
    previewRenderer.renderLater(xpos, ypos, zpos, null);
    previewRenderer.repaintLater();
  }

  public TripleStateTableModel getModel() {
    return (TripleStateTableModel) table.getModel();
  }

  public JTable getView() {
    return table;
  }

  public void show(String title) {
    frame.setTitle(title);
    show();
  }

  public void show() {
    WindowManager.addWindow(frame); // ImageJ's own Window Manager
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        frame.setVisible(true);
      }
    });
    WindowManager.setWindow(frame); // ImageJ's own Window Manager
  }

  public void hide() {
    frame.setVisible(false);
    WindowManager.removeWindow(frame); // ImageJ's own Window Manager
  }

  public boolean isVisible() {
    return frame.isVisible();
  }

  public void setPreviewRenderer(RenderingQueue renderer) {
    previewRenderer = renderer;
  }
  
  public void setStatus(String text){
    this.status.setText(text);
  }
  
  public OperationsHistoryPanel getOperationHistoryPanel(){
    return operationsStackPanel;
  }

  private class JavaTableWindowListener extends WindowAdapter {

    private JavaTableWindow window;

    public JavaTableWindowListener(JavaTableWindow window) {
      this.window = window;
    }

    @Override
    public void windowClosing(WindowEvent e) {
      window.hide();
    }
  }
}
