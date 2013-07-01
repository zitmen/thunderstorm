package cz.cuni.lf1.lge.ThunderSTORM.results;

import ij.IJ;
import ij.WindowManager;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

class JavaTableWindow {
  
  private JTable table;
  private JFrame frame;

  public JavaTableWindow() {
    frame = new JFrame("ThunderSTORM: Results");
    frame.setIconImage(IJ.getInstance().getIconImage());
    //
    JavaTableWindowListener windowListener = new JavaTableWindowListener(this);
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.addWindowListener(windowListener);
    frame.addWindowStateListener(windowListener);
    //
    table = new JTable(new ResultsTableModel());
    table.setAutoCreateRowSorter(true);
    //
    frame.setContentPane(new JScrollPane(table));
    frame.pack();
    //
    // TODO#1: razeni (kliknutim na hlavicku, jak je zvykem + zakazat prepisovani cisel ve sloupci "#")
    // TODO#2: doplnit dalsi ovladaci prvky - filtrovani, renderovani, import/export
    // TODO#3: @Override: public void tableChanged(TableModelEvent e); --> moznost aktualizovat napriklad renderer po vyfiltrovani vysledku
  }
  
  public ResultsTableModel getModel() {
    return (ResultsTableModel)table.getModel();
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
