package cz.cuni.lf1.lge.ThunderSTORM.UI;

import cz.cuni.lf1.lge.ThunderSTORM.ModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.util.MacroUI.handlers.ComponentHandler;

public class CardsPanelMacroUIHandler implements ComponentHandler<String> {

    @Override
    public String getValueFromComponent(Object comp) {
        CardsPanel panel = (CardsPanel) comp;
        return panel.getActiveComboBoxItem().getName();
    }

    @Override
    public void setValueToComponent(String value, Object comp) {
        CardsPanel<ModuleUI> panel = (CardsPanel) comp;
        ModuleUI[] modules = panel.getItems();
        for(int i = 0; i < modules.length; i++) {
            if(value.equals(modules[i].getName())) {
                panel.setSelectedItemIndex(i);
                return;
            }
        }
    }

}
