package ThunderSTORM.UI;

import ThunderSTORM.IModule;
import java.awt.CardLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
 
public class CardsPanel implements ItemListener {

    private JPanel cardsPanel, comboBoxPanel;
    
    public CardsPanel(Vector<IModule> items) {
        cardsPanel = createCardsPanel(items);
    }
     
    public final JPanel createCardsPanel(Vector<IModule> items) {
        comboBoxPanel = new JPanel();
        String comboBoxItems[] = new String[items.size()];
        for(int i = 0; i < items.size(); i++) {
            comboBoxItems[i] = items.elementAt(i).getName();
        }
        JComboBox cb = new JComboBox(comboBoxItems);
        cb.setEditable(false);
        cb.addItemListener(this);
        comboBoxPanel.add(cb);
        
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
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel(name), gbc);
        gbc.gridx = 1;
        panel.add(comboBoxPanel, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(cardsPanel, gbc);
        return panel;
    }
     
    @Override
    public void itemStateChanged(ItemEvent evt) {
        CardLayout cl = (CardLayout)(cardsPanel.getLayout());
        cl.show(cardsPanel, (String)evt.getItem());
    }
    
}