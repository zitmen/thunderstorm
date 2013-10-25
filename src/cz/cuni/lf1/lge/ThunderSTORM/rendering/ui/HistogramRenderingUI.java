package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.HistogramRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class HistogramRenderingUI extends AbstractRenderingUI {

    public static final String name = "Histograms";
    //parameter names
    private ParameterName.Integer avg;
    private ParameterName.Double dx;
    private ParameterName.Boolean forceDx;
    private ParameterName.Double dz;

    public HistogramRenderingUI() {
        initPars();
    }

    public HistogramRenderingUI(int sizeX, int sizeY) {
        super(sizeX, sizeY);
        initPars();
    }

    private void initPars() {
        avg = parameters.createIntField("avg", IntegerValidatorFactory.positive(), 0);
        ParameterTracker.Condition avgGTZeroCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return avg.getValue() > 0;
            }

            @Override
            public ParameterName[] dependsOn() {
                return new ParameterName[]{avg};
            }
        };
        ParameterTracker.Condition threeDAndAvgGTZeroCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return (avg.getValue() > 0 && threeD.getValue());
            }

            @Override
            public ParameterName[] dependsOn() {
                return new ParameterName[]{avg, threeD};
            }
        };
        dx = parameters.createDoubleField("dx", DoubleValidatorFactory.positiveNonZero(), 20, avgGTZeroCondition);
        forceDx = parameters.createBooleanField("dxforce", null, false, avgGTZeroCondition);
        dz = parameters.createDoubleField("dz", DoubleValidatorFactory.positiveNonZero(), 100, threeDAndAvgGTZeroCondition);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = super.getOptionsPanel();
        //avg
        JTextField avgTextField = new JTextField("", 20);
        parameters.registerComponent(avg, avgTextField);
        panel.add(new JLabel("Averages:"), GridBagHelper.leftCol());
        panel.add(avgTextField, GridBagHelper.rightCol());
        //dx
        JCheckBox forceDXCheckBox = new JCheckBox("Force", false);
        forceDXCheckBox.setBorder(BorderFactory.createEmptyBorder());
        parameters.registerComponent(forceDx, forceDXCheckBox);
        JPanel latUncertaintyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel("Lateral uncertainty [nm]:"), GridBagHelper.leftCol());
        JTextField dxTextField = new JTextField("", 10);
        parameters.registerComponent(dx, dxTextField);
        latUncertaintyPanel.add(dxTextField);
        latUncertaintyPanel.add(Box.createHorizontalStrut(5));
        latUncertaintyPanel.add(forceDXCheckBox);
        panel.add(latUncertaintyPanel, GridBagHelper.rightCol());
        //dz
        final JLabel dzLabel = new JLabel("Axial uncertainty [nm]:");
        panel.add(dzLabel, GridBagHelper.leftCol());
        final JTextField dzTextField = new JTextField("", 20);
        parameters.registerComponent(dz, dzTextField);
        panel.add(dzTextField, GridBagHelper.rightCol());
        final JCheckBox threeDCheckBox = (JCheckBox) parameters.getRegisteredComponent(threeD);
        threeDCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dzLabel.setEnabled(threeDCheckBox.isSelected());
                dzTextField.setEnabled(threeDCheckBox.isSelected());
            }
        });
        parameters.loadPrefs();
        return panel;
    }

    @Override
    public IncrementalRenderingMethod getMethod() {
        if(threeD.getValue()) {
            Range zRange = Range.parseFromStepTo(this.zRange.getValue());
            return new HistogramRendering.Builder()
                    .roi(0, sizeX, 0, sizeY)
                    .resolution(1 / magnification.getValue())
                    .average(avg.getValue())
                    .defaultDX(dx.getValue() / CameraSetupPlugIn.getPixelSize())
                    .forceDefaultDX(forceDx.getValue())
                    .defaultDZ(dz.getValue())
                    .zRange(zRange.from, zRange.to, zRange.step).build();
        } else {
            return new HistogramRendering.Builder()
                    .roi(0, sizeX, 0, sizeY)
                    .resolution(1 / magnification.getValue())
                    .average(avg.getValue())
                    .defaultDX(dx.getValue() / CameraSetupPlugIn.getPixelSize())
                    .forceDefaultDX(forceDx.getValue()).build();
        }
    }
}
