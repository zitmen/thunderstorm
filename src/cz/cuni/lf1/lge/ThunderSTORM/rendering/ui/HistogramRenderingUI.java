package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.HistogramRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import static cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.AbstractRenderingUI.MAGNIFICATION;
import static cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.AbstractRenderingUI.THREE_D;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterTracker;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.IntegerValidatorFactory;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class HistogramRenderingUI extends AbstractRenderingUI {

    private final String name = "Histograms";
    //default values
    private static final int DEFAULT_AVG = 0;
    private static final double DEFAULT_DX = 20;
    private static final boolean DEFAULT_FORCE_DX = false;
    private static final double DEFAULT_DZ = 100;
    //parameter names
    private static final ParameterName.Integer AVG = new ParameterName.Integer("avg");
    private static final ParameterName.Double DX = new ParameterName.Double("dx");
    private static final ParameterName.Boolean FORCE_DX = new ParameterName.Boolean("forcedx");
    private static final ParameterName.Double DZ = new ParameterName.Double("dz");

    public HistogramRenderingUI() {
        initPars();
    }

    public HistogramRenderingUI(int sizeX, int sizeY) {
        super(sizeX, sizeY);
        initPars();
    }

    private void initPars() {
        parameters.createIntField(AVG, IntegerValidatorFactory.positive(), DEFAULT_AVG);
        ParameterTracker.Condition avgGTZeroCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return parameters.getInt(AVG) > 0;
            }

            @Override
            public ParameterName[] dependsOn() {
                return new ParameterName[]{AVG};
            }
        };
        ParameterTracker.Condition threeDAndAvgGTZeroCondition = new ParameterTracker.Condition() {
            @Override
            public boolean isSatisfied() {
                return (parameters.getInt(AVG) > 0 && parameters.getBoolean(THREE_D));
            }

            @Override
            public ParameterName[] dependsOn() {
                return new ParameterName[]{AVG, THREE_D};
            }
        };
        parameters.createDoubleField(DX, DoubleValidatorFactory.positiveNonZero(), DEFAULT_DX, avgGTZeroCondition);
        parameters.createBooleanField(FORCE_DX, null, DEFAULT_FORCE_DX, avgGTZeroCondition);
        parameters.createDoubleField(DZ, DoubleValidatorFactory.positiveNonZero(), DEFAULT_DZ, threeDAndAvgGTZeroCondition);
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
        parameters.registerComponent(AVG, avgTextField);
        panel.add(new JLabel("Averages:"), GridBagHelper.leftCol());
        panel.add(avgTextField, GridBagHelper.rightCol());
        //dx
        JCheckBox forceDXCheckBox = new JCheckBox("Force", false);
        parameters.registerComponent(FORCE_DX, forceDXCheckBox);
        JPanel latUncertaintyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.add(new JLabel("Lateral uncertainty [nm]:"), GridBagHelper.leftCol());
        JTextField dxTextField = new JTextField("", 10);
        parameters.registerComponent(DX, dxTextField);
        latUncertaintyPanel.add(dxTextField);
        latUncertaintyPanel.add(forceDXCheckBox);
        panel.add(latUncertaintyPanel, GridBagHelper.rightCol());
        //dz
        final JLabel dzLabel = new JLabel("Axial uncertainty [nm]:");
        panel.add(dzLabel, GridBagHelper.leftCol());
        final JTextField dzTextField = new JTextField("", 20);
        parameters.registerComponent(DZ, dxTextField);
        panel.add(dzTextField, GridBagHelper.rightCol());
        final JCheckBox threeDCheckBox = (JCheckBox) parameters.getRegisteredComponent(THREE_D);
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
        if(parameters.getBoolean(THREE_D)) {
            Range zRange = Range.parseFromStepTo(parameters.getString(Z_RANGE));
            return new HistogramRendering.Builder()
                    .roi(0, sizeX, 0, sizeY)
                    .resolution(1 / parameters.getDouble(MAGNIFICATION))
                    .average(parameters.getInt(AVG))
                    .defaultDX(parameters.getDouble(DX) / CameraSetupPlugIn.pixelSize)
                    .forceDefaultDX(parameters.getBoolean(FORCE_DX))
                    .defaultDZ(parameters.getDouble(DZ))
                    .zRange(zRange.from, zRange.to, zRange.step).build();
        } else {
            return new HistogramRendering.Builder()
                    .roi(0, sizeX, 0, sizeY)
                    .resolution(1 / parameters.getDouble(MAGNIFICATION))
                    .average(parameters.getInt(AVG))
                    .defaultDX(parameters.getDouble(DX) / CameraSetupPlugIn.pixelSize)
                    .forceDefaultDX(parameters.getBoolean(FORCE_DX)).build();
        }
    }
}
