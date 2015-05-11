package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.ScatterRendering;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import javax.swing.JPanel;

public class ScatterRenderingUI extends AbstractRenderingUI {

    public static final String name = "Scatter plot";

    public ScatterRenderingUI() {
    }

    public ScatterRenderingUI(double sizeX, double sizeY) {
        super(sizeX, sizeY);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel p = super.getOptionsPanel();
        parameters.loadPrefs();
        return p;
    }

    @Override
    public IncrementalRenderingMethod getMethod() {
        if(threeD.getValue()) {
            Range zrange = Range.parseFromStepTo(zRange.getValue());
            return new ScatterRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification.getValue())
                    .colorize(colorize.getValue())
                    .colorizationLUT(lutPicker.getLut(colorizationLut.getValue()))
                    .zRange(zrange.from, zrange.to, zrange.step)
                    .build();
        } else {
            return new ScatterRendering.Builder()
                    .roi(left, left+sizeX, top, top+sizeY)
                    .resolution(1 / magnification.getValue())
                    .build();
        }
    }
}
