package cz.cuni.lf1.lge.ThunderSTORM.rendering.ui;

import cz.cuni.lf1.lge.ThunderSTORM.CameraSetupPlugIn;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.DensityRendering;
import cz.cuni.lf1.lge.ThunderSTORM.rendering.IncrementalRenderingMethod;
import static cz.cuni.lf1.lge.ThunderSTORM.rendering.ui.AbstractRenderingUI.THREE_D;
import cz.cuni.lf1.lge.ThunderSTORM.util.GridBagHelper;
import cz.cuni.lf1.lge.ThunderSTORM.util.Range;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.ParameterName;
import cz.cuni.lf1.lge.thunderstorm.util.macroui.validators.DoubleValidatorFactory;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class DensityRenderingUI extends AbstractRenderingUI {

    private final String name = "Normalized Gaussian";
    private static final double DEFAULT_DX = 20;
    private static final boolean DEFAULT_FORCE_DX = false;
    private static final double DEFAULT_DZ = 100;
    //param names
    private static final ParameterName.Double DX = new ParameterName.Double("dx");
    private static final ParameterName.Boolean FORCE_DX = new ParameterName.Boolean("dxforce");
    private static final ParameterName.Double DZ = new ParameterName.Double("dz");

    public DensityRenderingUI() {
        super();
        initPars();
    }

    public DensityRenderingUI(int sizeX, int sizeY) {
        super(sizeX, sizeY);
        initPars();
    }

    private void initPars() {
        parameters.createDoubleField(DX, DoubleValidatorFactory.positiveNonZero(), DEFAULT_DX);
        parameters.createBooleanField(FORCE_DX, null, DEFAULT_FORCE_DX);
        parameters.createDoubleField(DZ, DoubleValidatorFactory.positiveNonZero(), DEFAULT_DZ, threeDCondition);
    }

    @Override
    public JPanel getOptionsPanel() {
        JPanel panel = super.getOptionsPanel();
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
        parameters.registerComponent(DZ, dzTextField);
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
    public String getName() {
        return name;
    }

    @Override
    public IncrementalRenderingMethod getMethod() {
        if(parameters.getBoolean(THREE_D)) {
            Range r = Range.parseFromStepTo(parameters.getString(Z_RANGE));
            return new DensityRendering.Builder()
                    .roi(0, sizeX, 0, sizeY)
                    .resolution(1 / parameters.getDouble(MAGNIFICATION))
                    .defaultDX(parameters.getDouble(DX) / CameraSetupPlugIn.pixelSize)
                    .forceDefaultDX(parameters.getBoolean(FORCE_DX))
                    .zRange(r.from, r.to, r.step)
                    .defaultDZ(parameters.getDouble(DZ)).build();
        } else {
            return new DensityRendering.Builder()
                    .roi(0, sizeX, 0, sizeY)
                    .resolution(1 / parameters.getDouble(MAGNIFICATION))
                    .defaultDX(parameters.getDouble(DX) / CameraSetupPlugIn.pixelSize)
                    .forceDefaultDX(parameters.getBoolean(FORCE_DX)).build();
        }
    }
}
