package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.IModule;
import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * UI panel which contains a combo box with selection different modules (they
 * must implement IModule) and their options panels.
 *
 * When a module is selected from the combo box, its options panel gets shown
 * underneath the combo box.
 *
 * @see IModule
 */
public class CardsPanel<T extends IModuleUI> implements ItemListener {

  private JPanel cardsPanel;
  private JComboBox cb;
  private List<T> items;

  /**
   * Initialize the panel.
   *
   * Insert names of modules into the combo box, initialize the options panels
   * of the modules, and show the options panel of {@code items[index_default]}
   * module.
   *
   * @param items Vector of modules you want to insert into the combo box
   * @param index_default index of an item you want to be selected
   */
  public CardsPanel(List<T> items, int index_default) {
    this.items = items;
    cardsPanel = createCardsPanel();
    cb.setSelectedIndex(index_default);
  }

  /**
   * Return the module selected in the combo box.
   *
   * @return the module selected in the combo box
   */
  public T getActiveComboBoxItem() {
    return items.get(cb.getSelectedIndex());
  }

  /**
   * Return the index of module selected in the combo box.
   *
   * @return the index of module selected in the combo box
   */
  public int getActiveComboBoxItemIndex() {
    return cb.getSelectedIndex();
  }

  private JPanel createCardsPanel() {
    String comboBoxItems[] = new String[items.size()];
    for (int i = 0; i < items.size(); i++) {
      comboBoxItems[i] = items.get(i).getName();
    }
    cb = new JComboBox(comboBoxItems);
    cb.setEditable(false);
    cb.addItemListener(this);

    // Create the cards
    JPanel[] cards = new JPanel[items.size()];
    for (int i = 0; i < items.size(); i++) {
      cards[i] = new JPanel(new BorderLayout());
      JPanel modulePanel = items.get(i).getOptionsPanel();
      if (modulePanel != null) {
        cards[i].add(modulePanel, BorderLayout.NORTH);
      }
    }

    // Create the panel that contains the cards
    JPanel panel = new JPanel(new CardLayout());
    for (int i = 0; i < items.size(); i++) {
      panel.add(cards[i], comboBoxItems[i]);
    }

    return panel;
  }

  /**
   * Return a JPanel containing all the options of the inserted modules.
   *
   * @param title title of the panel shown next to the combo box
   * @return a <strong>new instance</strong> of JPanel which contains the label
   * with title of panel, the combo box with all the modules, and the options
   * panels with the options of selected module on top (the other options are
   * hidden and they will get shown after a specific module gets selected from
   * the combo box)
   */
  public JPanel getPanel(String title) {
    GridBagLayout gbl = new GridBagLayout();
    gbl.columnWidths = new int[]{50, 200};
    JPanel panel = new JPanel(gbl);
    panel.add(new JLabel(title), GridBagHelper.leftCol());
    panel.add(cb, GridBagHelper.rightCol());
    GridBagConstraints gbc = (GridBagConstraints) GridBagHelper.pos_size(0, 1, 2, 1).clone();
    gbc.fill = GridBagConstraints.BOTH;
    panel.add(cardsPanel, gbc);
    return panel;
  }

  /**
   * Select a options panel of a selected module in the combo box.
   *
   * @param evt event object (not required)
   */
  @Override
  public void itemStateChanged(ItemEvent evt) {
    CardLayout cl = (CardLayout) (cardsPanel.getLayout());
    cl.show(cardsPanel, (String) evt.getItem());
  }
}