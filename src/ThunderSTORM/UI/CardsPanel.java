package ThunderSTORM.UI;

import ThunderSTORM.IModule;
import ThunderSTORM.utils.GridBagHelper;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
 
public class CardsPanel implements ItemListener {

    private JPanel cardsPanel;
    private JComboBox cb;
    
    public CardsPanel(Vector<IModule> items) {
        cardsPanel = createCardsPanel(items);
    }
    
    public void setDefaultComboBoxItem(int index) {
        cb.setSelectedIndex(index);
    }
     
    public final JPanel createCardsPanel(Vector<IModule> items) {
        String comboBoxItems[] = new String[items.size()];
        for(int i = 0; i < items.size(); i++) {
            comboBoxItems[i] = items.elementAt(i).getName();
        }
        cb = new JComboBox(comboBoxItems);
        cb.setEditable(false);
        cb.addItemListener(this);
        
        // Create the cards
        JPanel[] cards = new JPanel[items.size()];
        for(int i = 0; i < items.size(); i++) {
            cards[i] = items.elementAt(i).getOptionsPanel();
            if(cards[i] == null) {
                cards[i] = new JPanel();
            }
        }
        
        // Create the panel that contains the cards
        JPanel panel = new JPanel(new CardLayout());
        for(int i = 0; i < items.size(); i++) {
            panel.add(cards[i], comboBoxItems[i]);
        }
        
        return panel;
    }
    
    public JPanel getPanel(String name) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(new JLabel(name), GridBagHelper.pos(0, 0));
        panel.add(cb, GridBagHelper.pos(1, 0));
        panel.add(cardsPanel, GridBagHelper.pos_width(0, 1, 2, 1));
        return panel;
    }
     
    @Override
    public void itemStateChanged(ItemEvent evt) {
        CardLayout cl = (CardLayout)(cardsPanel.getLayout());
        cl.show(cardsPanel, (String)evt.getItem());
    }
    
}