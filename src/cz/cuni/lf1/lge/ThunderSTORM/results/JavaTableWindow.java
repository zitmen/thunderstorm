package cz.cuni.lf1.lge.ThunderSTORM.results;

import cz.cuni.lf1.lge.ThunderSTORM.ImportExportPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.RenderingPlugIn;
import ij.IJ;
import ij.WindowManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.TableRowSorter;

class JavaTableWindow {
  
  private JTable table;
  private JFrame frame;
  private JLabel filterLabel;
  private JTextField filterText;
  private TableRowSorter<ResultsTableModel> sorter;
  private JButton export;
  private JButton render;
  private JButton filterButton;
  private JCheckBox preview;
  private JLabel status;
  
  public JavaTableWindow() {
    frame = new JFrame("ThunderSTORM: Results");
    frame.setIconImage(IJ.getInstance().getIconImage());
    //
    JavaTableWindowListener windowListener = new JavaTableWindowListener(this);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(windowListener);
    frame.addWindowStateListener(windowListener);
    //
    ResultsTableModel model = new ResultsTableModel();
    table = new JTable(model);
    sorter = new TableRowSorter<ResultsTableModel>(model);
    table.setRowSorter(sorter);
    //
    // TODO: why is there a margin on the left ??!!
    status = new JLabel(" ", JLabel.CENTER);
    JPanel statusBar = new JPanel();
    statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.Y_AXIS));
    statusBar.add(Box.createVerticalStrut(10));
    statusBar.add(new JSeparator(JSeparator.HORIZONTAL));
    statusBar.add(status);
    //
    JPanel filter = new JPanel();
    filter.setLayout(new BoxLayout(filter, BoxLayout.X_AXIS));
    filterText = new JTextField();
    FilterListener filterListener = new FilterListener(model, sorter, filterText, status);
    filterText.addKeyListener(filterListener);
    filterLabel = new JLabel("Filter: ", SwingConstants.TRAILING);
    filterLabel.setLabelFor(filterText);
    filterButton = new JButton("Apply");
    filterButton.addActionListener(filterListener);
    filter.add(filterLabel);
    filter.add(filterText);
    filter.add(filterButton);
    //
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    export = new JButton("Export...");
    render = new JButton("Render...");
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
    preview = new JCheckBox("Preview");
    render.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        // TODO: activate preview
      }
    });
    buttons.add(preview);
    buttons.add(Box.createHorizontalGlue());
    buttons.add(render);
    buttons.add(Box.createHorizontalStrut(10));
    buttons.add(export);
    //
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    pane.add(new JScrollPane(table));
    pane.add(filter);
    pane.add(buttons);
    pane.add(statusBar);
    //
    frame.setContentPane(pane);
    frame.pack();
  }
  
  public ResultsTableModel getModel() {
    return (ResultsTableModel)table.getModel();
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
    frame.setVisible(true);
    WindowManager.setWindow(frame); // ImageJ's own Window Manager
  }
  
  public void hide() {
    frame.setVisible(false);
    WindowManager.removeWindow(frame); // ImageJ's own Window Manager
  }
  
  public boolean isVisible() {
    return frame.isVisible();
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
