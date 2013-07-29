package cz.cuni.lf1.lge.ThunderSTORM.results;

import javax.swing.JFrame;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Josef Borkovec <josef.borkovec[at]lf1.cuni.cz>
 */
public class OperationsStackPanelTest {

  @Test
  public void testAddOperation() throws InterruptedException {

    final OperationsStackPanel panel = new OperationsStackPanel();
    panel.addOperation(new OperationsStackPanel.Operation() {
      @Override
      public String getName() {
        return "operation1";
      }

      @Override
      public boolean isUndoAble() {
        return true;
      }

      @Override
      public void clicked() {
        panel.addOperation(new OperationsStackPanel.Operation() {
          @Override
          public String getName() {
            return "nextOperation";
          }

          @Override
          public boolean isUndoAble() {
            return true;
          }

          @Override
          public void clicked() {
          }

          @Override
          public void undo() {
            panel.removeAllOperations();
          }

          @Override
          public void redo() {
          }
        });
      }

      @Override
      public void undo() {
      }

      @Override
      public void redo() {
      }
    });

    JFrame f = new JFrame();
    f.add(panel);
    f.pack();
    f.setVisible(true);
    //Thread.sleep(10000);

  }
}