package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.IModuleUI;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;

public abstract class IRendererUI extends IModuleUI<IncrementalRenderingMethod> {

    public abstract void setSize(int sizeX, int sizeY);
    public abstract void setSize(int left, int top, int sizeX, int sizeY);

    public abstract int getRepaintFrequency();

    @Override
    protected String getPreferencesPrefix() {
        return super.getPreferencesPrefix() + ".rendering";
    }
}
