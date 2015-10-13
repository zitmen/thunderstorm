package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.handlers.ComponentHandler;
import java.util.List;

public class CardsPanelMacroUIHandler implements ComponentHandler<String> {

    @Override
    public String getValueFromComponent(Object comp) {
        CardsPanel panel = (CardsPanel) comp;
        return panel.getActiveComboBoxItem().getName();
    }

    @Override
    public void setValueToComponent(String value, Object comp) {
        CardsPanel<IModuleUI> panel = (CardsPanel) comp;
        List<IModuleUI> modules = panel.getItems();
        for(int i = 0; i < modules.size(); i++) {
            if(value.equals(modules.get(i).getName())) {
                panel.setSelectedItemIndex(i);
                return;
            }
        }
    }

}
